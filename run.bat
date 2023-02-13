@echo off

cd ./src/JavaBot/

start mvn package
timeout /t 3

:: Game Runner
cd ../../runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
cd ../reference-bot-publish/

timeout /t 1
start "" dotnet ReferenceBot.dll
timeout /t 1
start "" dotnet ReferenceBot.dll
timeout /t 1
start "" dotnet ReferenceBot.dll
timeout /t 1

cd ../src/JavaBot/target/
start java -jar JavaBot.jar
timeout /t 10
cd ../../../
@REM start  .\visualiser\Galaxio.exe
pause