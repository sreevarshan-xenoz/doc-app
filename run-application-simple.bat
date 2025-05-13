@echo off
echo Doctor Appointment System Runner (Simple Version)
echo ============================================
echo.

REM Set JavaFX SDK path - use older JavaFX version compatible with Java 8
REM Download JavaFX 8 from https://gluonhq.com/products/javafx/ if needed
set JAVAFX_PATH=libs

echo Using JavaFX and libraries from: %JAVAFX_PATH%
echo.

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin
if not exist bin\resources mkdir bin\resources

REM Copy resources
echo Copying FXML resources...
copy src\main\resources\*.fxml bin\resources\

REM Compile the Java files with proper classpath
echo Compiling Java classes...
javac -d bin -cp "libs\*" src\main\java\DoctorAppointmentSystem\*.java

REM Run the application with proper classpath
echo Running application...
java -cp "bin;bin\resources;libs\*" DoctorAppointmentSystem.Main

echo.
echo Application closed. 