#!/bin/bash
export WFLY_VERSION=15.0.0.Alpha1
rm -rf wildfly-$WFLY_VERSION-SNAPSHOT*
cp ../../../narayana/jboss-as/build/target/wildfly-$WFLY_VERSION-SNAPSHOT.zip .
unzip wildfly-$WFLY_VERSION-SNAPSHOT.zip
cp wildfly-$WFLY_VERSION-SNAPSHOT/docs/examples/configs/standalone-rts.xml wildfly-$WFLY_VERSION-SNAPSHOT/standalone/configuration/
cd wildfly-$WFLY_VERSION-SNAPSHOT; git init .; git add .; git commit -m "initial"; cd .