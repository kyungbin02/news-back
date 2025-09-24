@echo off
cd /d "%~dp0"
echo Starting Spring Boot Backend...
gradlew.bat bootRun
pause