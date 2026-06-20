@echo off

@REM ============================================================
@REM  Usage:
@REM    java25.bat <action> [extra]
@REM
@REM  Actions:
@REM    run            -> mvn clean install && mvn spring-boot:run
@REM    run_with_tests -> mvn clean install -Dmaven.test.skip=true && mvn spring-boot:run
@REM    test           -> mvn clean compile test
@REM    test <Class>   -> mvn clean compile && mvn -Dtest=<Class> test
@REM    jacoco         -> mvn clean compile test jacoco:report
@REM    doc            -> mvn clean compile && mvn javadoc:javadoc
@REM    uml            -> mvn clean compile process-classes
@REM ============================================================

@REM  Adjust this path to your JDK 25 installation directory
set JAVA_HOME=D:\Java\jdk-25.0.3
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"

@REM Check for .env file
if "%~1"=="" (
	echo Usage: %~nx0 ^<run^|test^|jacoco^|doc^|uml^> [extra]
	exit /b 1
)

@REM Load environment variables from .env file
if not exist "%~dp0.env" (
	echo Missing .env file next to %~nx0
	exit /b 1
)

@REM Parse .env file and set environment variables
for /f "usebackq eol=# tokens=1* delims==" %%A in ("%~dp0.env") do (
	set "%%A=%%B"
)

@REM Ensure ssl directory exists
if not exist ssl mkdir ssl

set "KEYSTORE_PATH=%SERVER_SSL_KEY_STORE%"
if not exist "%KEYSTORE_PATH%" (
	keytool -genkeypair ^
		-alias %SERVER_SSL_KEY_ALIAS% ^
		-keyalg RSA ^
		-keysize 2048 ^
		-storetype %SERVER_SSL_KEY_STORE_TYPE% ^
		-keystore "%KEYSTORE_PATH%" ^
		-storepass %SERVER_SSL_KEY_STORE_PASSWORD% ^
		-keypass %SERVER_SSL_KEY_STORE_PASSWORD% ^
		-validity 3650 ^
		-dname "CN=localhost,OU=DataShare,O=OpenClassrooms,L=Paris,S=IDF,C=FR" ^
		-ext SAN=dns:localhost,ip:127.0.0.1
)

REM Configure JVM memory for large file uploads
REM Reduced to 2G max to avoid Windows pagefile exhaustion on 800MB+ uploads
set "MAVEN_OPTS=-Xmx2G -Xms1G"

@REM Retrieve action and extra parameters
set "ACTION=%~1"
set "EXTRA=%~2"

if /i "%ACTION%"=="run" (
	mvn clean install -Dmaven.test.skip=true && mvn spring-boot:run
	goto :eof
)

if /i "%ACTION%"=="run_with_tests" (
	mvn clean install && mvn spring-boot:run
	goto :eof
)

if /i "%ACTION%"=="test" (
	if not "%EXTRA%"=="" (
		mvn clean compile && mvn -Dtest=%EXTRA% test
	) else (
		mvn clean compile test
	)
	goto :eof
)

if /i "%ACTION%"=="jacoco" (
	mvn clean compile test jacoco:report
	goto :eof
)

if /i "%ACTION%"=="doc" (
	mvn clean compile && mvn javadoc:javadoc
	goto :eof
)

if /i "%ACTION%"=="uml" (
	mvn clean compile process-classes
	goto :eof
)

echo Unknown action: %ACTION%
echo Valid actions: run, test, jacoco, doc, uml
exit /b 1