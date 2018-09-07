/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.spring.service;

import org.jboss.narayana.quickstart.spring.xa.DummyXAResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("/")
@Service
public class ExampleService {
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DummyXAResource xaResource;

    @Autowired
    private TransactionManager tm;

    @PostConstruct
    public void setupTable() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS example ( id varchar(10), name varchar(32) )");
    }

    @GET
    @Produces({"application/json"})
    @javax.transaction.Transactional
    @Path("/insert/{trigger}")
    public Response testTransaction(@PathParam("trigger") int trigger) {
//        try {
            jdbc.execute("delete from example");
            int count = testCommits(trigger);
            return (Response.status(Response.Status.OK).entity("first get " + count).build());
//        } catch (Exception ex) {
//            Integer count = jdbc.queryForObject("select count(*) from example", Integer.class);
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity(ex.getMessage() + " count in db in first get" + count).build();
//        } finally {
//            System.out.println("shutdownDatabase();");
////            TxControl.disable(true);
////            TransactionReaper.terminate(true);
//        }
    }


    public int testCommits(int trigger) throws BadRequestException {
        try {
            Transaction transaction = tm.getTransaction();
            xaResource.setFault(DummyXAResource.faultType.NONE);//HALT
            transaction.enlistResource(xaResource);
            jdbc.execute("insert into example values (1, 'test2')");
            if (trigger == 1) {
                jdbc.execute("insert into example values (1, 'test3')");
            } else {
                jdbc.execute("insert into examples values (1, 'test3')");
            }
            return jdbc.queryForObject("select count(*) from example", Integer.class);
        } catch (Exception e) {
            Integer count = jdbc.queryForObject("select count(*) from example", Integer.class);
            throw new BadRequestException("insereted entries " + count + "exception" + e.getMessage());
        }
    }

    @Transactional
    public void testCommit() throws Exception {
        try {
            System.out.println("testCommit");
            Transaction transaction = tm.getTransaction();
            transaction.enlistResource(xaResource);
            jdbc.execute("insert into example values (1, 'test1')");
            System.out.println("testCommit OK");
        } catch (Exception e) {
            System.out.println("testCommit FAIL with " + e);
            throw e;
        }
    }

    @Transactional
    public void testRecovery() {
        try {
            System.out.println("testRecovery");
            Transaction transaction = tm.getTransaction();
            xaResource.setFault(DummyXAResource.faultType.HALT);
            transaction.enlistResource(xaResource);
            jdbc.execute("insert into example values (1, 'test1')");
        } catch (Exception e) {
            System.out.println("testRecovery FAIL with " + e);
        }
    }

    @GET
    @Produces({"application/json"})
    @javax.transaction.Transactional
    @Path("/check")
    public Response check() {
        Integer count = jdbc.queryForObject("select count(*) from example", Integer.class);
        System.out.println("check in the database (count = " + count + ")");
        return (Response.status(Response.Status.OK).entity(""+count).build());
    }


    public int checkRecord() {
        Integer count = jdbc.queryForObject("select count(*) from example", Integer.class);
        System.out.println("check in the database (count = " + count + ")");
        return count;
    }

    public void shutdownDatabase() {
        jdbc.execute("SHUTDOWN");
    }
}
