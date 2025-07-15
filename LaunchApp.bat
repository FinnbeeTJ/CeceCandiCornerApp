@echo off
REM Launch script for Cece's Candi Corner Inventory Management System (Self-Contained - External JavaFX)

REM --- Configuration ---
REM Path to the bundled Java executable within this runtime folder
SET "JAVA_EXE=.\bin\java.exe"

REM Path to your JavaFX SDK 17.0.15 lib directory (EXTERNAL)
REM IMPORTANT: Verify this path matches your JavaFX SDK installation
SET "JAVAFX_LIB=C:\Users\denni\Desktop\Software Development I\javafx-sdk-17.0.15\lib"

REM Name of your application JAR (contains your code + SQLite JDBC, in the same directory)
SET "APP_JAR=CeceCandiCornerApp.jar"

REM Fully qualified main class
SET "MAIN_CLASS=com.cececandicorner.inventory.CeceCandiCornerGUI"

REM --- Execution ---
REM This command launches the application using the bundled JVM.
REM JavaFX modules are provided via --module-path from the external SDK.
REM Your application JAR (with SQLite JDBC) is on the classpath.
"%JAVA_EXE%" ^
  --module-path "%JAVAFX_LIB%" ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
  -classpath "%APP_JAR%" ^
  %MAIN_CLASS%

REM --- Pause to see output if there's an error ---
pause