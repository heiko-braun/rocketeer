#!/bin/sh

java -Xms64m -Xmx512m -XX:MaxPermSize=256m -server -d64 -Xverify:none -XX:+UseFastAccessorMethods -XX:+DisableExplicitGC -jar target/rocketeer-dist-1.0.0-SNAPSHOT.jar
