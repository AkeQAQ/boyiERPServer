#!/bin/sh
# -Dfastjson.parser.autoTypeAccept=cn.com.bsfit. 这个是fastjson接受请求时数据类型的白名单，不在这个列表里的数据fastjson拒绝处理。
# -Duser.language=fr和-Duser.country=FR为了让每周的第一天可以是周一，这样更适合中国的情况，否则脚本里expirePattern使用周（w/W）时第一天会是周日
cd /sdb1/boyi/ERPServer
JAR_NAME=`ls | grep jar`
PCOUNT=` ps -ef|grep $JAR_NAME|grep -v grep|wc -l`
echo "jar: $JAR_NAME, process:$PCOUNT"

# 判断是否启动，不存在则启动
if [ ${PCOUNT} -ne 0 ]; then
    echo "Process is running!" >> check.log
else
    echo "Starting process..." >> check.log
    sh ./run.sh
fi

