@echo off
REM Launch script for Cece's Candi Corner Inventory Management System

REM --- Configuration ---
REM Path to your JDK 17 bin directory (where java.exe is located)
REM IMPORTANT: Verify this path matches your JDK 17 installation
SET "JAVA_HOME_BIN=C:\Program Files\Java\jdk-17\bin"

REM Path to your JavaFX SDK 17.0.15 lib directory
REM IMPORTANT: Verify this path matches your JavaFX SDK installation
SET "JAVAFX_LIB=C:\Users\denni\Desktop\Software Development I\javafx-sdk-17.0.15\lib"

REM Name of your runnable JAR file (should be in the same directory as this .bat file)
SET "APP_JAR=CeceCandiCornerApp.jar"

REM JavaFX modules required by your application
SET "JAVAFX_MODULES=javafx.controls,javafx.fxml"

REM --- Execution ---
REM This command launches the JavaFX application
"%JAVA_HOME_BIN%\java.exe" --module-path "%JAVAFX_LIB%" --add-modules %JAVAFX_MODULES% -jar "%APP_JAR%"

REM --- Pause to see output if there's an error ---
REM This line keeps the command window open after execution, useful for debugging
pause