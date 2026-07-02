@echo off
REM Launch map coloring app
REM Usage: scripts\run.bat

cd /d "%~dp0.."

echo === Building...
call mvn compile -q
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo === Launching...
call mvn javafx:run
