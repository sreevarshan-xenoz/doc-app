@echo off
echo Doctor Appointment System Runner
echo ==============================
echo.

REM Set JavaFX SDK path
set JAVAFX_PATH="C:\Users\theja\Downloads\openjfx-24.0.1_windows-x64_bin-sdk\javafx-sdk-24.0.1\lib"

echo Running with JavaFX from: %JAVAFX_PATH%
echo.

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin
if not exist bin\resources mkdir bin\resources

REM Copy resources
echo Copying FXML resources...
copy src\main\resources\*.fxml bin\resources\

REM Compile the Java files with proper classpath
echo Compiling Java classes...
javac -d bin --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml src\main\java\DoctorAppointmentSystem\*.java

REM Run the application with proper classpath
echo Running application...
java --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml ^
     --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     -cp "bin;bin\resources" DoctorAppointmentSystem.Main

echo.
echo Application closed. 