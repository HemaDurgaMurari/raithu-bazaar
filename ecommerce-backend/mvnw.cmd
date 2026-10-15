@echo off
setlocal
set "MVN_VERSION=3.9.11"
set "MVN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VERSION%"
if exist "%MVN_HOME%\bin\mvn.cmd" goto run
set "MVN_ZIP=%TEMP%\apache-maven-%MVN_VERSION%-bin.zip"
set "MVN_EXTRACT=%TEMP%\apache-maven-%MVN_VERSION%"
echo Downloading Apache Maven %MVN_VERSION% for this user...
curl.exe -fL --retry 8 --retry-all-errors --retry-delay 2 -C - "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip" -o "%MVN_ZIP%"
if errorlevel 1 exit /b %errorlevel%
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%MVN_ZIP%' -DestinationPath '%TEMP%' -Force"
if errorlevel 1 exit /b %errorlevel%
if not exist "%USERPROFILE%\.m2\wrapper\dists" mkdir "%USERPROFILE%\.m2\wrapper\dists"
move "%MVN_EXTRACT%" "%MVN_HOME%" >nul
:run
call "%MVN_HOME%\bin\mvn.cmd" %*
exit /b %errorlevel%
