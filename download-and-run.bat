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

echo Downloading Supabase dependencies...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/io/github/supabase-community/supabase-java/0.0.2/supabase-java-0.0.2.jar' -OutFile '%LIBS_DIR%\supabase-java-0.0.2.jar'}"
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/json/json/20230618/json-20230618.jar' -OutFile '%LIBS_DIR%\json-20230618.jar'}"

set JAVAFX_PATH=%CD%\%TEMP_DIR%\javafx-sdk-21.0.2\lib
set LIBS_PATH=%CD%\%LIBS_DIR%\*.jar

echo JavaFX extracted to: %JAVAFX_PATH%
echo Dependencies downloaded to: %LIBS_PATH%
echo.

echo Compiling Java files...
if not exist bin mkdir bin
javac -d bin -cp ".;%LIBS_PATH%" --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml src\main\java\DoctorAppointmentSystem\*.java

echo Copying resources...
if not exist bin\resources mkdir bin\resources
copy src\main\resources\*.fxml bin\resources\

echo Running application...
java -cp "bin;bin\resources;%LIBS_PATH%" --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml DoctorAppointmentSystem.Main

echo.
echo Application closed. 