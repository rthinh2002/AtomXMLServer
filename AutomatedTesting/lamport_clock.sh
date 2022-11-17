#!/bin/bash

pkill -f java
javac *.java
java -cp .:sqlite-jdbc-3.36.0.3.jar AggregationServer &

java -cp .:sqlite-jdbc-3.36.0.3.jar ResetDatabase
# First complicated test, request in order: GET PUT GET PUT GET
echo -e "Running Complicated Testing\n" > "testingResult/lamport_clock.txt"
echo "Test 1 Complicated: Lamport Clock - Request Order GET PUT GET PUT GET" >> "testingResult/lamport_clock.txt"
java GETClientTesting localhost:4567 1 >> "testingResult/lamport_clock.txt"
java ContentServerTestingGETResponse localhost:4567 another_basic.txt &
java GETClientTesting localhost:4567 1 >> "testingResult/lamport_clock.txt"
java ContentServerTestingGETResponse localhost:4567 another_basic_entry1.txt &
java GETClientTesting localhost:4567 1 >> "testingResult/lamport_clock.txt"
