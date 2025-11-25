@echo off

setlocal

REM Base directory = where this script is located
set BASEDIR=%~dp0

REM Clean and compile all code for maven
echo Cleaning and compiling Maven project...
call mvn clean compile -e
echo Finished compiling Maven project

REM Use Maven to copy dependencies into dev\lib folder
echo Copying Maven dependencies.
call mvn dependency:copy-dependencies -DoutputDirectory=%BASEDIR%\lib
echo Finished maven dependencies commands.
pause

REM All compilation handled by Maven above
echo Maven compilation complete - all classes ready in target/classes
pause

REM Get instance IDs from user input or command line arguments
echo.
echo ===== Instance Configuration =====
echo You can specify instance IDs for the server and node components.
echo Leave blank to use default configurations.
echo.

REM Use command line arguments if provided, otherwise prompt for input
if "%1"=="" (
    set /p SERVER_INSTANCE="Enter Server Instance ID (e.g., server1, server2, or leave blank for default): "
) else (
    set SERVER_INSTANCE=%1
    echo Using Server Instance ID from command line: %SERVER_INSTANCE%
)

if "%2"=="" (
    if "%1"=="" (
        set /p NODE_INSTANCE="Enter Node Instance ID (e.g., node1, node2, or leave blank for default): "
    ) else (
        set /p NODE_INSTANCE="Enter Node Instance ID (e.g., node1, node2, or leave blank for default): "
    )
) else (
    set NODE_INSTANCE=%2
    echo Using Node Instance ID from command line: %NODE_INSTANCE%
)

echo.
if "%SERVER_INSTANCE%"=="" (
    echo Starting EdgeServer with default configuration
) else (
    echo Starting EdgeServer with instance ID: %SERVER_INSTANCE%
)

if "%NODE_INSTANCE%"=="" (
    echo Starting EdgeNode with default configuration
) else (
    echo Starting EdgeNode with instance ID: %NODE_INSTANCE%
)

echo Starting EdgeCoordinator with default configuration
echo.
pause

cd /d %BASEDIR%\

start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* coordinator.edge_coordinator.EdgeCoordinator"

if "%SERVER_INSTANCE%"=="" (
    start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* server.edge_server.EdgeServer"
) else (
    start cmd /k "cd /d %BASEDIR% && java -cp target/classes;%BASEDIR%\lib\* server.edge_server.EdgeServer %SERVER_INSTANCE%"
)

endlocal