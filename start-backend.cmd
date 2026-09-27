@echo off
setlocal

set "PROJECT_ROOT=%~dp0"
if exist "%PROJECT_ROOT%backend\.env.local" (
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%PROJECT_ROOT%backend\.env.local") do set "%%A=%%B"
)

if not exist "%JAVA_HOME%\bin\javac.exe" (
    set "JAVA_HOME="
    for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-17*") do if exist "%%~fD\bin\javac.exe" set "JAVA_HOME=%%~fD"
)
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo JDK 17 is required. Set JAVA_HOME to a JDK installation.
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

where mvn.cmd >nul 2>&1
if errorlevel 1 (
    echo Maven was not found on PATH.
    exit /b 1
)

cd /d "%PROJECT_ROOT%backend"
mvn.cmd spring-boot:run -Dspring-boot.run.arguments=--debug=false
exit /b %ERRORLEVEL%
