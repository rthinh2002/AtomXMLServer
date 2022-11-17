#!/bin/bash

# Test for PUT request
echo -e "PUT Request Testing\n" > "testingResult/basic_response_PUT.txt"
echo "Testing 1: Basic Valid Test" > "testingResult/basic_response_PUT.txt"
java ContentServerTestingPUTResponse localhost:4567 basic.txt 2 >> "testingResult/basic_response_PUT.txt"
echo "Testing 2: Duplicated Atom Field Test" >> "testingResult/basic_response_PUT.txt"
java ContentServerTestingPUTResponse localhost:4567 duplicated.txt >> "testingResult/basic_response_PUT.txt"
echo "Testing 3: No Content In The File Test" >> "testingResult/basic_response_PUT.txt"
java ContentServerTestingPUTResponse localhost:4567 no_content.txt >> "testingResult/basic_response_PUT.txt"
echo "Testing 4: Atom Feed No ID Test" >> "testingResult/basic_response_PUT.txt"
java ContentServerTestingPUTResponse localhost:4567 no_id.txt >> "testingResult/basic_response_PUT.txt"
echo "Testing 5: Atom Feed No Link Test" >> "testingResult/basic_response_PUT.txt"
java ContentServerTestingPUTResponse localhost:4567 no_link.txt >> "testingResult/basic_response_PUT.txt"
echo "Testing 6: Atom Feed No Title Test" >> "testingResult/basic_response_PUT.txt"
java ContentServerTestingPUTResponse localhost:4567 no_title.txt >> "testingResult/basic_response_PUT.txt"
echo "Testing 7: Invalid Request" >> "testingResult/basic_response_PUT.txt"
java ContentServerInvalidRequest localhost:4567 no_title.txt >> "testingResult/basic_response_PUT.txt"
echo -e "PUT Response Testing DONE!" >> "testingResult/basic_response_PUT.txt"

# Test for GET request
echo -e "GET Request Testing\n" > "testingResult/basic_response_GET.txt"
echo "Testing 1: Client Send Request before any PUT request" >> "testingResult/basic_response_GET.txt"
java GETClientTesting localhost:4567 1 >> "testingResult/basic_response_GET.txt"
echo -e "\n\nTesting 2: Client Send Request after a valid PUT request" >> "testingResult/basic_response_GET.txt"
java ContentServerTestingGETResponse localhost:4567 basic.txt &
java GETClientTesting localhost:4567 1 >> "testingResult/basic_response_GET.txt"

