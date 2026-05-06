@echo off
set JAVA_HOME=D:\Java\jdk-26
set PATH=%JAVA_HOME%\bin;%PATH%
mvn clean install && mvn spring-boot:run
