@echo off
echo Doctor Appointment System with Supabase - Download and Run
echo ===================================================
echo.

echo Setting up environment...
set TEMP_DIR=temp
set LIBS_DIR=libs
if not exist %TEMP_DIR% mkdir %TEMP_DIR%
if not exist %LIBS_DIR% mkdir %LIBS_DIR%

REM Check if .env file exists, create a default one if not
if not exist .env (
    echo Creating default .env file...
    echo # Supabase Configuration> .env
    echo # Replace these values with your actual Supabase project credentials>> .env
    echo SUPABASE_URL=https://your-project-id.supabase.co>> .env
    echo SUPABASE_API_KEY=your-supabase-api-key>> .env
    echo.
    echo NOTE: Please update the .env file with your actual Supabase credentials before using the application.
    echo.
)

echo Downloading JavaFX 21.0.2 (compatible with Java 21)...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-sdk.zip' -OutFile '%TEMP_DIR%\javafx.zip'}"

echo Extracting JavaFX...
powershell -Command "& {Expand-Archive -Force '%TEMP_DIR%\javafx.zip' -DestinationPath '%TEMP_DIR%'}"

set JAVAFX_PATH=%CD%\%TEMP_DIR%\javafx-sdk-21.0.2\lib

echo JavaFX extracted to: %JAVAFX_PATH%
echo.

echo Cleaning previous build...
if exist bin rmdir /s /q bin

echo Compiling Java files...
mkdir bin
javac -d bin --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml src\main\java\DoctorAppointmentSystem\*.java

echo Copying resources...
mkdir bin\resources
copy src\main\resources\*.fxml bin\resources\

echo Running application...
java -cp "bin;bin\resources" --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml DoctorAppointmentSystem.Main

echo.
echo Application closed. 