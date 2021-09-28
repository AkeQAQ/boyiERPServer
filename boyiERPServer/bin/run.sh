#!/bin/sh
# -Dfastjson.parser.autoTypeAccept=cn.com.bsfit. 这个是fastjson接受请求时数据类型的白名单，不在这个列表里的数据fastjson拒绝处理。
# -Duser.language=fr和-Duser.country=FR为了让每周的第一天可以是周一，这样更适合中国的情况，否则脚本里expirePattern使用周（w/W）时第一天会是周日

JAR_NAME=`ls | grep jar`
echo "start up $JAR_NAME"
path=$(cd "$(dirname "$0")"; pwd)
cd $path
# rm -rf logs
nohup  java -Duser.language=fr -Duser.country=FR -jar $JAR_NAME > /dev/null 2>error.log &
