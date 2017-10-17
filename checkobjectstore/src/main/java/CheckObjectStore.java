/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteTransaction;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
@Path("/")
public class CheckObjectStore {
    private static Logger log = Logger.getLogger(CheckObjectStore.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isStoreEmpty() throws ObjectStoreException, IOException {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        List<String> splitObjectTypes = Arrays.asList(new AtomicAction().type().substring(1),
                ArjunaTransactionImple.typeName().substring(1), XAResourceRecord.typeName().substring(1),
                SubordinateAtomicAction.getType().substring(1), ServerTransaction.typeName().substring(1),
                com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction.getType().substring(1),
                new AtomicAction().type().substring(1) + "Connectable",
                AssumedCompleteServerTransaction.typeName().substring(1),
                AssumedCompleteHeuristicTransaction.typeName().substring(1),
                AssumedCompleteHeuristicServerTransaction.typeName().substring(1), AssumedCompleteTransaction.typeName().substring(1));

        Collection<Uid> ids = new ArrayList<Uid>();
        InputObjectState types = new InputObjectState();
        if (recoveryStore.allTypes(types)) {
            String theName;
            boolean endOfList = false;

            while (!endOfList) {
                theName = types.unpackString();
                if (theName.compareTo("") == 0)
                    endOfList = true;
                else {
                    if (splitObjectTypes != null && !splitObjectTypes.contains(theName)) {
                        log.warn("Ignored object store entry: " + theName);
                        continue;
                    }

                    InputObjectState uids = new InputObjectState();
                    if (recoveryStore.allObjUids(theName, uids)) {
                        Uid theUid;
                        boolean endOfUids = false;

                        while (!endOfUids) {
                            theUid = UidHelper.unpackFrom(uids);

                            if (theUid.equals(Uid.nullUid()))
                                endOfUids = true;
                            else
                                ids.add(theUid);
                        }
                    }
                }
            }
        }
        return ids.size() == 0;
    }
}
