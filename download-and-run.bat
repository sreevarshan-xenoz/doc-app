@echo off
echo Doctor Appointment System - Download and Run
echo ==========================================
echo.

echo Setting up environment...
set TEMP_DIR=temp
if not exist %TEMP_DIR% mkdir %TEMP_DIR%

echo Downloading JavaFX 21.0.2 (compatible with Java 21)...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-sdk.zip' -OutFile '%TEMP_DIR%\javafx.zip'}"

echo Extracting JavaFX...
powershell -Command "& {Expand-Archive -Force '%TEMP_DIR%\javafx.zip' -DestinationPath '%TEMP_DIR%'}"

set JAVAFX_PATH=%CD%\%TEMP_DIR%\javafx-sdk-21.0.2\lib

echo JavaFX extracted to: %JAVAFX_PATH%
echo.

echo Compiling Java files...
if not exist bin mkdir bin
javac -d bin --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml src\main\java\DoctorAppointmentSystem\*.java

echo Copying resources...
if not exist bin\resources mkdir bin\resources
copy src\main\resources\*.fxml bin\resources\

echo Running application...
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "bin;bin\resources" DoctorAppointmentSystem.Main

echo.
echo Application closed. 