#!/bin/bash

pkill -f java
java -cp .:sqlite-jdbc-3.36.0.3.jar ResetDatabase
echo -e "Running Complicated Testing\n" > "testingResult/server_crash.txt"
echo "Test 2 Complicated: Server Crash - PUT CRASH GET" >> "testingResult/server_crash.txt"
java -cp .:sqlite-jdbc-3.36.0.3.jar AggregationServer &
pid=$!
sleep 0.5
java ContentServerCrash localhost:4567 another_basic.txt & 
sleep 0.5
kill $pid
sleep 0.5
java -cp .:sqlite-jdbc-3.36.0.3.jar AggregationServer &
sleep 0.5
java GETClientTesting localhost:4567 1 >> "testingResult/server_crash.txt"
