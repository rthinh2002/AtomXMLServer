#!/bin/bash

pkill -f java
javac *.java
java -cp .:sqlite-jdbc-3.36.0.3.jar ResetDatabase
java -cp .:sqlite-jdbc-3.36.0.3.jar AggregationServer &
