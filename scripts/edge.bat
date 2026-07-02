@echo off
REM Utility script
REM Usage: scripts\edge.bat [build|test|clean|package|deps|verify]

cd /d "%~dp0.."

if "%1"=="" goto :help

if "%1"=="build" (
    echo === Building...
    call mvn compile
    goto :end
)
if "%1"=="test" (
    echo === Testing...
    call mvn test
    goto :end
)
if "%1"=="clean" (
    echo === Cleaning...
    call mvn clean
    if exist target rmdir /s /q target
    goto :end
)
if "%1"=="package" (
    echo === Packaging...
    call mvn package -DskipTests
    goto :end
)
if "%1"=="deps" (
    echo === Dependencies...
    call mvn dependency:tree
    goto :end
)
if "%1"=="verify" (
    echo === Verifying...
    call mvn verify
    goto :end
)

:help
echo Usage: scripts\edge.bat ^<command^>
echo.
echo Commands:
echo   build     Compile
echo   test      Run tests
echo   clean     Clean output
echo   package   Build JAR
echo   deps      Show dependency tree
echo   verify    Compile + test
echo.

:end
