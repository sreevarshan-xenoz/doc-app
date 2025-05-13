@echo off
echo Doctor Appointment System Runner (Java 24)
echo =====================================
echo.

REM Set paths - update these to your actual installed locations
set JAVA_HOME=C:\Program Files\Java\jdk-24
set JAVAFX_PATH="C:\Users\theja\Downloads\openjfx-24.0.1_windows-x64_bin-sdk\javafx-sdk-24.0.1\lib"

echo Using Java from: %JAVA_HOME%
echo Using JavaFX from: %JAVAFX_PATH%
echo.

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin
if not exist bin\resources mkdir bin\resources

REM Copy resources
echo Copying FXML resources...
copy src\main\resources\*.fxml bin\resources\

REM Enable delayed expansion for variables inside loops
setlocal enabledelayedexpansion

REM Build JARs classpath
set JARS_CLASSPATH=
for %%f in (libs\*.jar) do set JARS_CLASSPATH=!JARS_CLASSPATH!;%%f

echo Using additional JAR files from libs directory

REM Compile the Java files with proper classpath
echo Compiling Java classes...
"%JAVA_HOME%\bin\javac" -d bin --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml src\main\java\DoctorAppointmentSystem\*.java

REM Run the application with proper classpath
echo Running application...
"%JAVA_HOME%\bin\java" --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml ^
     --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     --add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED ^
     --add-reads javafx.base=ALL-UNNAMED ^
     --add-reads javafx.graphics=ALL-UNNAMED ^
     -cp "bin;bin\resources;libs\*" DoctorAppointmentSystem.Main

endlocal

echo.
echo Application closed. 