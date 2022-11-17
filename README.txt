Xuan Thinh Le - a1807507
Assignment 2 Report

Database:
This program used SQLite database to perform connection. Database file and sqlite driver will be included in the repository

TESTING:

GENERAL COMPILE COMMAND WITH BASIC TESTING:
Navigate to assignment2 folder: cd assignment2
Run: 
1/ javac *.java
2/ pkill -f java
3/ java -cp .:sqlite-jdbc-3.36.0.3.jar ResetDatabase
4/ java -cp .:sqlite-jdbc-3.36.0.3.jar AggregationServer 
5/ On another terminal, run: java ContentServer localhost:4567 basic.txt
6/ On another terminal, run: java GETClient localhost:4567

MANUAL TESTING INSTRUCTION:
1. Open at least 3 terminals (1 for Aggregation Server, multiple for Clients, multiple for Content Server)
2. In the first terminal, to start the aggregation server, enter the command: ./manualAS.sh 4567. To end the Aggregation Server, press Ctrl + C (Command + C on Mac)
3. In the second terminal, to start the content server enter the command: ./manualCS.sh localhost:4567 basic.txt. It will require the user to press enter to send a request. To end connection press Ctrl + C (Command + C for Macs).
4. In the third terminal, to start the GETClient, enter the command: ./manualGETClient.sh localhost:4567. To end the Client's connection press Ctrl + C (Command + C on Macs).

AUTOMATED TESTING INSTRUCTION:
Below is the instruction:
1. Open the terminal at assignment2 repository, direct to AutomatedTesting folder by command: cd AutomatedTesting
2. Run the command: ./terminateProcess.sh
3. Run the command: ./run_all_test.sh
4. The result will be log in /testingResult folder, expected result is in /expectedResult folder

List of testing result in testingResult folder:
 - basic_response
	- The basic responses when Content Server send a request to the Aggregation Server (valid, no_content, no_link, no_title, no_id, duplicated, unimplemented request)
	- The responses when the Client send a request to the Aggregation Server (no content in database and having content)
 - lamport_clock
	- Ordering the request by lamport clock testing to see if request is being execute in order
 - expired_feed
	- The feed got expired after 12 seconds of insertion
 - server_crash
	- Insert the feed into the server and make the server crash, restart and the Client immediately send a GET request to prove that server is fault tolerance and the feed survive through crashing

IMPORTANCE NOTE:
 - This program/assignment has been tested on University of Adelaide RedHat platform
 - If you got error about sqlite appear, it maybe because your computer don't have sqlite3 running. Since when I tested on RedHat on University 
 computer, sqlite3 is already installed. So incase this kind of error appear, follow below instruction:
	- First, cd to the folder of assignment2
	- Use the command: sudo apt update
	- Use the command: sudo apt install sqlite3
	- After it is install successfully, type: sqlite3, you will get in the sqlite3 shell
	- Then type, attach "database.db" as "db1"
	- Check the database by using "SELECT * FROM db1.feeds;"
	- If information is show, then it is all set, use .exit to exit the shell and continue with the program.
 - The main working program is located in the assignment2 folder, everything in java file has been modified to fit the purpose of automated testing.
 - References:
	- https://www.ibm.com/docs/en/cics-ts/5.3?topic=client-making-get-requests-atom-feeds-collections
