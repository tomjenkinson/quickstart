package org.jboss.narayana.quickstart.spring;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.quickstart.spring.config.DatabaseConfig;
import org.jboss.narayana.quickstart.spring.service.ExampleService;
import org.jboss.narayana.quickstart.spring.xa.DummyXAResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;

import static junit.framework.TestCase.assertTrue;

@RunWith(Arquillian.class)
public class TestRESTATBridge {

    protected static final String MANIFEST = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.jts,org.jboss.jboss-transaction-spi,org.jboss.jts.integration,org.jboss.narayana.rts,com.h2database.h2\n";

    private static final String SPRING = "org.springframework:spring-web:" + System.getProperty("version.org.springframwork");
    private static final String SPRING2 = "org.springframework:spring-jdbc:" + System.getProperty("version.org.springframwork");
    private static final String DBCP = "org.apache.commons:commons-dbcp2:" + System.getProperty("version.commons-dbcp2");


    @Deployment
    public static WebArchive getDeployment() {
        final File[] libraries = Maven.resolver().resolve(SPRING, SPRING2, DBCP).withTransitivity().asFile();
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(ExampleService.class)
                .addPackage(DatabaseConfig.class.getPackage().getName())
                .addPackage(DummyXAResource.class.getPackage().getName())
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"), "web.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"), "beans.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/applicationContext.xml"), "applicationContext.xml")
                .addAsLibraries(libraries)
                .setManifest(new StringAsset(MANIFEST));
        return archive;
    }

    @Test
    public void testRollback() {
        Client client = ClientBuilder.newClient(); // get a JAX-RS Client

        Response response = client.target("http://localhost:8080").path("/test/check").request().get();
        int initial = Integer.parseInt(response.readEntity(String.class));

        TxSupport txn = new TxSupport("http://localhost:8080/rest-at-coordinator/tx/transaction-manager/");// get a helper for using REST Atomic Transactions
        txn.startTx(); // start a REST Atomic transaction
        {
            // invoke the Service A
            Response response2 = client.target("http://localhost:8080").path("/test/insert/2").request().get();
            System.out.println(response2.getStatus());
            System.out.println(response2.getEntity());
        }
        String s = txn.commitTx();
        System.out.println(s);

        Response response3 = client.target("http://localhost:8080").path("/test/check").request().get();
        int afterTx = Integer.parseInt(response3.readEntity(String.class));
        assertTrue(afterTx == initial);
    }


    @Test
    public void testCommit() {
        Client client = ClientBuilder.newClient(); // get a JAX-RS Client

        Response response = client.target("http://localhost:8080").path("/test/check").request().get();
        int initial = Integer.parseInt(response.readEntity(String.class));

        TxSupport txn = new TxSupport("http://localhost:8080/rest-at-coordinator/tx/transaction-manager/");// get a helper for using REST Atomic Transactions
        txn.startTx(); // start a REST Atomic transaction
        {
            // invoke the Service A
            Response response2 = client.target("http://localhost:8080").path("/test/insert/1").request().get();
            System.out.println(response2.getStatus());
            System.out.println(response2.getEntity());
        }
        String s = txn.commitTx();
        System.out.println(s);

        Response response3 = client.target("http://localhost:8080").path("/test/check").request().get();
        int afterTx = Integer.parseInt(response3.readEntity(String.class));
        assertTrue(afterTx == initial + 2);
    }
}
