@echo off

rem jar平级目录
set AppName=console-admin.jar

rem JVM参数
set JVM_OPTS="-Dname=%AppName%  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"
rem 命令执行参数默认为空。重新初始化 -i； 内置数据库模式下重新载入备份数据：--restore export-202211150130.zip
set CMD_OPTS=""

rem 得到当前工作目录
cd ..
set "APP_HOME=%cd%"
cd "%APP_HOME%"
ECHO.  APP_HOME:%APP_HOME%

ECHO.
	ECHO.  [1] 启动%AppName%
	ECHO.  [2] 关闭%AppName%
	ECHO.  [3] 启动状态 %AppName%
	ECHO.  [4] 启动并重设管理员密码 %AppName%
	ECHO.  [5] 退 出
ECHO.

ECHO.请输入选择项目的序号:
set /p ID=
	IF "%id%"=="1" GOTO start
	IF "%id%"=="2" GOTO stop
	IF "%id%"=="3" GOTO status
	IF "%id%"=="4" GOTO start_reset
	IF "%id%"=="5" EXIT
PAUSE
:start
    for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if  defined pid (
		echo %%is running
		PAUSE
	)

start javaw %JVM_OPTS% -jar lib/%AppName% %CMD_OPTS%

echo  starting……
echo  Start %AppName% success...
goto:eof

rem 函数stop通过jps命令查找pid并结束进程
:stop
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo process %AppName% does not exists) else (
		echo prepare to kill %image_name%
		echo start kill %pid% ...
		rem 根据进程ID，kill进程
		taskkill /f /pid %pid%
	)
goto:eof
:status
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo process %AppName% is dead ) else (
		echo %image_name% is running
	)
goto:eof
:start_reset
set CMD_OPTS="-i"
goto:start