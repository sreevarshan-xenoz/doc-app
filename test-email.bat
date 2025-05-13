@echo off
echo Doctor Appointment System - Email Test Runner
echo =========================================
echo.

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile just the necessary classes for email testing
echo Compiling email test classes...
javac -d bin -cp "libs\*" src\main\java\DoctorAppointmentSystem\Config.java src\main\java\DoctorAppointmentSystem\User.java src\main\java\DoctorAppointmentSystem\EmailService.java src\main\java\DoctorAppointmentSystem\EmailTest.java

REM Run the email test
echo Running email test...
java -cp "bin;libs\*" DoctorAppointmentSystem.EmailTest

echo.
echo Test completed. 