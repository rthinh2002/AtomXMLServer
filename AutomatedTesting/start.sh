#!/bin/bash

cd java
java -cp .:sqlite-jdbc-3.36.0.3.jar AggregationServer &
pid=$!
echo $pid > "javaProgram.pid"
