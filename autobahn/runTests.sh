#/bin/sh

rm -rf target/reports
wstest -m fuzzingclient -w ws://localhost:9002/echo

