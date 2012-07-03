#/bin/sh

mvn clean
wstest -m fuzzingclient -w ws://localhost:9002/echo

