Cece's Candi Corner Inventory System

Hey there! This is the inventory management system I built for Cece's Candi Corner. It's a simple desktop app designed to help keep track of all those lovely bracelets without needing physical order forms anymore!

What it Does
Basically, this app lets you:

Load Data: Pull in all your existing bracelet info from a text file.

Displays All Bracelets in the System: Display all the bracelets currently in stock in a clear list.

Add New Bracelets: Easily add new bracelet designs to the inventory.

Remove Bracelets: Take out items you no longer carry.

Update Stock: Change quantities or prices for existing bracelets.

Low Stock Reports: Get a quick report of any bracelets running low so you know when to reorder.

Dark Mode: Because who doesn't love a dark theme?

How I Built It
This app is made with:

Java (JDK 17): The main programming language.
JavaFX (SDK 17.0.15): This is what I used to make the actual window and buttons you see.

Getting It Running on Your Computer
You'll need Java 17 and JavaFX 17.0.15 installed.

Quick Setup (If you have IntelliJ IDEA)
Grab the code: git clone <your-repository-url>

Open in IntelliJ: Open the project folder in IntelliJ IDEA.

Link JavaFX: You'll need to tell IntelliJ where your JavaFX SDK 17.0.15 is. Go to File > Project Structure > Libraries and add the lib folder from your JavaFX SDK. Then, in Modules > Dependencies, make sure it's linked there too.

module-info.java: There's a module-info.java file in the src folder that helps JavaFX run smoothly.

Run it! Find CeceCandiCornerGUI.java and hit the green "Run" button.

(If you're running from the command line, it's a bit more involved with --module-path arguments, but IntelliJ handles that for you.)

How to Use It
Start by loading data: Click "Load Data from File" and pick the data.txt file.

Play around: Try adding, removing, or updating bracelets. See how the "Status" changes automatically when you adjust the quantity!

Check reports: Generate a low stock report to see what needs attention.

Exit: When you're done, hit the "Exit" button.

A Couple of Things to Note
It forgets stuff: Right now, if you close the app, all your changes disappear. It's just working in memory.

Future Plan: I am purposely did not add database functionality per my teachers requests, but it is coming.

Data from file: The app loads the status exactly as it is in data.txt. So, if the file says "Quantity 5, Out of Stock," it'll show that until you update it.

Future Plan: Maybe make it smart enough to fix those inconsistencies when it first loads?
