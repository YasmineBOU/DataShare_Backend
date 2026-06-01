@echo off
@REM  Adjust this path to your JDK 25 installation directory
set JAVA_HOME=D:\Java\jdk-25.0.3
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"

if not exist "%~dp0.env" (
	echo Missing .env file next to test-java25.bat
	exit /b 1
)

for /f "usebackq eol=# tokens=1* delims==" %%A in ("%~dp0.env") do (
	set "%%A=%%B"
)

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

@REM mvn clean compile && mvn install
@REM mvn -Dtest=FileServiceTest test
@REM mvn clean && mvn compile 
mvn clean compile && mvn -Dtest=UserControllerTest test
