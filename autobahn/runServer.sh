#!/bin/sh

mvn clean compile
mvn exec:java -Dexec.mainClass="io.rocketeer.autobahn.TestServer"


