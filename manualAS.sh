#!/bin/bash

javac *.java
java -cp .:sqlite-jdbc-3.36.0.3.jar AggregationServer $1