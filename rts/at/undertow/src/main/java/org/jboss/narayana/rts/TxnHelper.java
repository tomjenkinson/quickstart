/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */
package org.jboss.narayana.rts;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatusMediaType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Set;

public class TxnHelper {
    public static final int NO_OF_SVC_CALLS = 2;

    public static Link getLink(Set<Link> links, String relation) {
        for (Link link : links)
            if (link.getRel().equals(relation))
                return link;

        return null;
    }

    public static Set<Link> beginTxn(Client client, String txurl) throws IOException {
        Response response = null;

        try {
            response = client.target(txurl).request().post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));
            Set<Link> links = response.getLinks();

            if (response.getStatus() != HttpURLConnection.HTTP_CREATED)
                throw new RuntimeException("beginTxn returned " + response.getStatus());

            return links;
        } finally {
            if (response != null)
                response.close();
        }
    }

    public static int endTxn(Client client, Set<Link> links) throws IOException {
        Response response = null;

        try {
            response = client.target(getLink(links, TxLinkNames.TERMINATOR).getUri())
                    .request().put(Entity.entity(TxStatusMediaType.TX_COMMITTED, TxMediaType.TX_STATUS_MEDIA_TYPE));

            int sc = response.getStatus();

            if (sc != HttpURLConnection.HTTP_OK)
                throw new RuntimeException("endTxn returned " + sc);

            return sc;
        } finally {
            if (response != null)
                response.close();
        }
    }
}
