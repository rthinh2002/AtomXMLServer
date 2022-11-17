#!/bin/bash

echo "Start to run all test cases"
echo "Basic Test"
./basic_test.sh
echo "Lamport Clock Test"
./lamport_clock.sh
echo "Expired Feed Test"
./expired_feed.sh
sleep 15
echo "Server Crash"
./server_crash.sh

pkill -f java
echo -e "\nRunning Comparision...\n"
java Comparison basic_response_GET.txt expected_basic_response_GET.txt
java Comparison basic_response_PUT.txt expected_basic_response_PUT.txt
java Comparison expired_feed.txt expected_expired_feed.txt
java Comparison lamport_clock.txt expected_lamport_clock.txt
java Comparison server_crash.txt expected_server_crash.txt
echo -e "\nAll Comparison Done!"

echo "Testing Done!"


