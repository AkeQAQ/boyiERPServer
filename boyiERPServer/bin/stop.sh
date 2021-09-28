#!/bin/bash
JAR_NAME=`ls | grep jar`

pid=`(ps -ef | grep $JAR_NAME | grep -v "grep") | awk '{print $2}'`
if [ "$pid" = "" ]
then 
      echo "$JAR_NAME is not run!"
else
      echo "stop $JAR_NAME"
      kill $pid
fi
