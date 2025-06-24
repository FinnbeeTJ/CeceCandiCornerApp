import java.util.Scanner; // Required for reading user input from the console

/**
 * CeceCandiCornerApp.java
 * This is the main application class for Cece's Candi Corner Inventory Management System.
 * It provides a console-based menu interface for the user to interact with the
 * InventoryManager. This class orchestrates the application flow, displaying options,
 * reading user input, and calling the appropriate methods in the InventoryManager.
 * This version focuses solely on Phase 1 requirements: in-memory data management
 * and command-line interaction, without a GUI or persistent database.
 */
public class CeceCandiCornerApp {

    /**
     * Private helper method to display the main menu options to the user.
     */
    private static void displayMainMenu() {
        System.out.println("\n--- Cece's Candi Corner Inventory DMS ---");
        System.out.println("1. Load Data from File");     // Test 2 & 3 (Read Data & Display)
        System.out.println("2. Display All Bracelets");   // Test 3 (Display Data)
        System.out.println("3. Add New Bracelet");        // Test 4 (Create Data)
        System.out.println("4. Remove Bracelet");         // Test 5 (Remove Data)
        System.out.println("5. Update Bracelet");         // Test 6 (Update Data)
        System.out.println("6. Generate Low Stock Report");// Test 7 (Custom Feature)
        System.out.println("7. Exit");                    // Test 1 (Program exit)
        System.out.println("------------------------------------------");
    }

    /**
     * The main entry point of the application.
     * Initializes the InventoryManager and handles the main application loop,
     * processing user menu choices.
     * @param args Command line arguments (not used in this application).
     */
    public static void main(String[] args) {
        // Create an instance of the InventoryManager to handle all inventory operations
        InventoryManager manager = new InventoryManager();
        // Create a Scanner object to read user input from the console
        Scanner scanner = new Scanner(System.in);

        // Main application loop: runs continuously until the user chooses to exit
        while (true) {
            displayMainMenu(); // Show the menu options
            System.out.print("Enter your choice (1-7): ");
            String choice = scanner.nextLine().trim(); // Read user's choice and trim whitespace

            // Use a switch statement to perform actions based on user's choice
            switch (choice) {
                case "1":
                    // Handle "Load Data from File" - Test 2 & 3
                    System.out.print("Enter the path to the text file (e.g., data.txt): ");
                    String filePath = scanner.nextLine().trim(); // Get file path from user
                    manager.readDataFromFile(filePath); // Call the manager to read data
                    break;
                case "2":
                    // Handle "Display All Bracelets" - Test 3
                    manager.displayAllBracelets(); // Call the manager to display current inventory
                    break;
                case "3":
                    // Handle "Add New Bracelet" - Test 4
                    manager.addBracelet(scanner); // Call the manager to add a new bracelet, passing scanner for input
                    break;
                case "4":
                    // Handle "Remove Bracelet" - Test 5
                    manager.removeBracelet(scanner); // Call the manager to remove a bracelet
                    break;
                case "5":
                    // Handle "Update Bracelet" - Test 6
                    manager.updateBracelet(scanner); // Call the manager to update a bracelet
                    break;
                case "6":
                    // Handle "Generate Low Stock Report" - Test 7 (Custom Feature)
                    manager.generateLowStockReport(scanner); // Call the manager to generate the report
                    break;
                case "7":
                    // Handle "Exit" - Test 1 (Program termination)
                    System.out.println("Exiting Cece's Candi Corner Inventory DMS. Goodbye!");
                    scanner.close(); // Close the scanner to release resources
                    return; // Terminate the program
                default:
                    // Handle invalid menu choices - Test 1
                    System.out.println("Invalid option. Please enter a number between 1 and 7.");
                    break;
            }
            // Pause the execution to allow the user to read the output before displaying the menu again
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine(); // Consume the newline character or wait for user input
        }
    }
}