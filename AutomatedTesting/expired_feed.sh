#!/bin/bash

java -cp .:sqlite-jdbc-3.36.0.3.jar ResetDatabase
echo -e "Running Complicated Testing\n" > "testingResult/expired_feed.txt"
echo "Test 3 Complicated: Feed Expired After 12 Seconds - Request Order: PUT GET -12s- GET" >> "testingResult/expired_feed.txt"
java ContentServerCrash localhost:4567 another_basic.txt &
java GETClientTesting localhost:4567 1 >> "testingResult/expired_feed.txt"
sleep 12
java GETClientTesting localhost:4567 1 >> "testingResult/expired_feed.txt"
