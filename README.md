Cece's Candi Corner Inventory System
Hey there! This is the inventory management system I built for Cece's Candi Corner. It's a simple desktop app designed to help keep track of all those lovely bracelets without needing physical order forms anymore!

This application provides a user-friendly interface to manage inventory, with all data persistently stored in a local SQLite database.

What it Does
Displays All Bracelets in the System: View all the bracelets currently in the inventory in a clear, organized list.

Add New Bracelets: Easily add new bracelet designs to the inventory.

Remove Bracelets: Take out items you no longer carry.

Update Stock: Change quantities, prices, and other details for existing bracelets.

Low Stock Reports: Get a quick report of any bracelets running low so you know when to make more.

Dark Mode: Because who doesn't love a dark theme?

How I Built It
This app is made with:

Java (JDK 17): The main programming language.

JavaFX: The framework used to build the graphical user interface.

SQLite: For robust, file-based data persistence. The database is created automatically, so no separate database server is needed.

JUnit 5: To ensure the application's logic is reliable and correct through a comprehensive test suite.


Getting It Running on Your Computer
This application requires you to create the database from the provided SQL script before the first launch.

Prerequisites
Java 17 (or newer) installed on your system.

The CeceCandiCornerApp.jar application file.

The schema.sql script file.

An SQLite database tool. I recommend DB Browser for SQLite, which is free and easy to use.

Step 1: Create the Database File
Open DB Browser for SQLite.

Click the New Database button. Choose a location on your computer and save the file as inventory.db.

A "New Table" dialog will appear; you can just click Cancel since we will use the script.

Go to the Execute SQL tab.

Open the schema.sql file on your computer, copy its entire content, and paste it into the SQL editor window in DB Browser.

Click the blue Execute SQL "play" button to run the script. It should create and populate your inventory table.

Important: Click the Write Changes button to save the data to your inventory.db file. You can now close DB Browser.

Step 2: Run the Application
Open your terminal or command prompt, navigate to the folder containing the CeceCandiCornerApp.jar file, and run this command:

Bash
java -jar CeceCandiCornerApp.jar

How to Use It
On the first launch, the application will prompt you to locate and select the inventory.db file you just created. After you select it, the application will load the data and function normally.

All your changes are saved automatically to that database file.
