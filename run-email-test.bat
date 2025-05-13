@echo off
echo Doctor Appointment System - Email Test
echo =====================================
echo.

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin
if not exist bin\resources mkdir bin\resources

REM Copy resources
echo Copying FXML resources...
copy src\main\resources\*.fxml bin\resources\

REM Temporarily rename module-info.java
echo Temporarily disabling module system...
ren src\main\java\DoctorAppointmentSystem\module-info.java module-info.java.bak

REM Compile the Java files with proper classpath
echo Compiling Java classes...
javac -d bin -cp "libs\*" src\main\java\DoctorAppointmentSystem\*.java

REM Run the application with proper classpath
echo Running application...
java -cp "bin;bin\resources;libs\*" DoctorAppointmentSystem.Main

REM Restore module-info.java
echo Restoring module system...
ren src\main\java\DoctorAppointmentSystem\module-info.java.bak module-info.java

echo.
echo Application closed. 