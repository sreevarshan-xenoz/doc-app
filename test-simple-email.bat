@echo off
echo Simple Email Test
echo ================
echo.

REM Compile the standalone test
echo Compiling...
javac -cp "libs\*" simpleEmailTest.java

REM Run the test
echo Running...
java -cp ".;libs\*" simpleEmailTest

echo.
echo Test completed. 