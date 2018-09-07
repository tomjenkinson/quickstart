Integration with Narayana and Spring
==================================================================================================
Author: Amos Feng;
Level: Intermediate;
Technologies: JTA, JTS, Spring 

What is it?
-----------

The jta example shows how to config the narayana transaction manager with spring annotation.



System requirements
-------------------

All you need to build this project is Java 7.0 (Java SDK 1.7) or better and Maven 3.0 or better.


Build and Run the Spring JTA Quickstart
-------------------------------

Simple steps:
  unzip ./wildfly-15.0.0.Alpha1-SNAPSHOT.zip to <WFLY_LOCATION>
  cp <WFLY_LOCATION>/docs/examples/configs/standalone-rts.xml <WFLY_LOCATION>/standalone/configuration/
  mvn clean install -DskipTests
  cp target/spring-jta.war <WFLY_LOCATION>/standalone/deployments
  ./<WFLY_LOCATION>/bin/standalone.sh -c standalone-rts.xml

Examples (replace URLs):
  curl -X POST -i -d data="timeout=360000" http://localhost:8080/rest-at-coordinator/tx/transaction-manager/
  curl -X GET -i http://localhost:8080/spring-jta/insert/1 -H 'Link: <http://localhost:8080/rest-at-coordinator/tx/transaction-manager/0_ffffc0a86301_4baee559_5b918a18_95>; rel="durable-participant"; title="durable-participant"'
  curl -X PUT -i -d "txstatus=TransactionCommitted" http://localhost:8080/rest-at-coordinator/tx/transaction-manager/0_ffffc0a86301_4baee559_5b918a18_95/terminator

1. Open a command line and navigate to the root directory of this quickstart.
* Build and run the tests:

        mvn clean install
* Run the commit example

        java -jar jta/target/spring-jta.jar
* Run the recovery example

        java -jar jta/target/spring-jta.jar -f
        java -jar jta/target/spring-jta.jar -r
* Check the database after recovery

        java -jar jta/target/spring-jta.jar -c
