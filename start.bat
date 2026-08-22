@echo off
setlocal
REM Build and run the application as one command.
REM Usage: start.bat

REM Always run from repository root (directory of this script)
cd /d "%~dp0"

REM Attempt to stop any running instance of this JAR (avoid file lock on Windows)
powershell -NoProfile -Command "Try { Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -match 'PSPLProject-0.0.1-SNAPSHOT.jar' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force } } Catch { }" 2>nul

call mvn -DskipTests clean package
IF %ERRORLEVEL% NEQ 0 (
  echo Maven build failed
  exit /b %ERRORLEVEL%
)

set "JAR="
for %%f in ("target\*.jar") do (
  if not defined JAR set "JAR=%%~ff"
)

if "%JAR%"=="" (
  echo No jar found in target\
  exit /b 1
)

if "%PORT%"=="" set "PORT=8080"
echo Starting application on http://localhost:%PORT%
java -Dserver.port=%PORT% -jar "%JAR%"
