#!/bin/sh
# ./console.sh run|start|stop|status
AppName=console-admin.jar

# Check if Java exists in the system
JAVA=$(command -v java || which java)

if [ -z "$JAVA" ]; then
    echo -e "\033[0;31m ERROR: Java was not found \033[0m"
    exit 1
fi

# JVM参数
JVM_OPTS="-Dname=$AppName  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"
# 命令执行参数默认为空。重新初始化 -i； 内置数据库模式下重新载入备份数据：--restore export-202211150130.zip
CMD_OPTS=""

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Set APP_HOME as current working directory
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
LOG_PATH=$APP_HOME/logs/$AppName.log
cd $APP_HOME

INSTANCE_MARKER="-Dconsole.app.home=$APP_HOME"
PID_FILE="$APP_HOME/logs/$AppName.pid"
JVM_OPTS="$JVM_OPTS $INSTANCE_MARKER"

echo "APP_HOME: $APP_HOME"

find_instance_pid()
{
    ps -ef | grep '[j]ava' | grep -- "$AppName" | grep -- "$INSTANCE_MARKER" | awk '{print $2}'
}


if [ "$1" = "" ];
then
    echo -e "\033[0;31m Please enter argument: \033[0m  \033[0;34m {start|run|stop|status} \033[0m"
    exit 1
fi

if [ "$AppName" = "" ];
then
    echo -e "\033[0;31m Application name not entered \033[0m"
    exit 1
fi

start()
{
    PID=`find_instance_pid`

	if [ x"$PID" != x"" ]; then
	    echo "$AppName is running..."
	else
		nohup java $JVM_OPTS -jar lib/$AppName $CMD_OPTS > /dev/null 2>&1 &
    echo $! > "$PID_FILE"
		echo "Start $AppName success..."
	fi
}

run()
{
    PID=`find_instance_pid`

    if [ x"$PID" != x"" ]; then
        echo "$AppName is running..."
    else
      if [ ! -d "data/db" ]; then
          echo "data/ do not contained data files! try to initial it!"
          if [ ! -d "data" ]; then
              mkdir data
          fi
          cp -a data-init/* data/
      fi

      java $JVM_OPTS -jar lib/$AppName $CMD_OPTS
    fi
}

stop()
{
    echo "Stop $AppName"

	PID=""
	query(){
    PID=`find_instance_pid`
	}

	query
	if [ x"$PID" != x"" ]; then
		kill -TERM $PID
		echo "$AppName (pid:$PID) exiting..."
		while [ x"$PID" != x"" ]
		do
			sleep 1
			query
		done
 		echo "$AppName exited."
	else
		echo "$AppName already stopped."
	fi
}


status()
{
    PID=`find_instance_pid`
    if [ x"$PID" != x"" ];then
        echo "$AppName is running..."
    else
        echo "$AppName is not running..."
    fi
}

start_reset()
{
  CMD_OPTS="-i"
  start
}

run_reset()
{
  CMD_OPTS="-i"
  run
}

case $1 in
    start)
    start;;
    start_reset)
    start_reset;;
    run)
    run;;
    run_reset)
    run_reset;;
    stop)
    stop;;
    status)
    status;;
    *)

esac
