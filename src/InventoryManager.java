import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Collections; // For sorting low stock items

/**
 * InventoryManager.java
 *
 * Manages the inventory of Bracelet objects for Cece's Candi Corner.
 * This class handles all core CRUD (Create, Read, Update, Delete) operations,
 * as well as reading data from files and generating reports.
 * It includes robust input validation to ensure data integrity.
 */
class InventoryManager {
    // List to store Bracelet objects in memory (in-memory inventory)
    private List<Bracelet> inventory;

    /**
     * Constructor to initialize the InventoryManager.
     * Creates an empty list to hold Bracelet objects.
     */
    public InventoryManager() {
        this.inventory = new ArrayList<>();
    }

    /**
     * Getter for the inventory list. This is added primarily for unit testing purposes.
     * @return The internal list of Bracelet objects.
     */
    public List<Bracelet> getInventory() {
        return inventory;
    }

    // --- Input Validation Methods ---
    /**
     * Validates if the provided ID is not null or empty after trimming whitespace.
     * @param itemId The ID string to validate.
     * @return true if the ID is valid, false otherwise.
     */
    private boolean validateId(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            System.out.println("Error: ID cannot be empty.");
            return false;
        }
        return true;
    }

    /**
     * Validates if the provided description is not null or empty after trimming whitespace.
     * @param description The description string to validate.
     * @return true if the description is valid, false otherwise.
     */
    private boolean validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            System.out.println("Error: Description cannot be empty.");
            return false;
        }
        return true;
    }

    /**
     * Validates if a string can be parsed into a non-negative integer for quantity.
     * @param quantityStr The string representation of the quantity.
     * @return The parsed integer quantity if valid, -1 to indicate an error.
     */
    private int validateQuantity(String quantityStr) {
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                System.out.println("Error: Quantity cannot be negative.");
                return -1; // Indicate error
            }
            return quantity;
        } catch (NumberFormatException e) {
            System.out.println("Error: Quantity must be a valid integer.");
            return -1; // Indicate error
        }
    }

    /**
     * Validates if a string can be parsed into a non-negative double for price.
     * @param priceStr The string representation of the price.
     * @return The parsed double price if valid, -1.0 to indicate an error.
     */
    private double validatePrice(String priceStr) {
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                System.out.println("Error: Price cannot be negative.");
                return -1.0; // Indicate error
            }
            return price;
        } catch (NumberFormatException e) {
            System.out.println("Error: Price must be a valid number.");
            return -1.0; // Indicate error
        }
    }

    /**
     * Validates if the provided status string is either "In Stock" or "Out of Stock" (case-insensitive).
     * @param status The status string to validate.
     * @return true if the status is valid, false otherwise.
     */
    private boolean validateStatus(String status) {
        if (status == null || (!status.equalsIgnoreCase("In Stock") && !status.equalsIgnoreCase("Out of Stock"))) {
            System.out.println("Error: Status must be 'In Stock' or 'Out of Stock'.");
            return false;
        }
        return true;
    }

    /**
     * Checks if a given bracelet ID already exists in the current inventory.
     * @param itemId The ID to check for uniqueness.
     * @return true if the ID is unique (does not exist in inventory), false otherwise.
     */
    private boolean isIdUnique(String itemId) {
        for (Bracelet bracelet : inventory) {
            if (bracelet.getId().equalsIgnoreCase(itemId)) {
                return false; // ID already exists
            }
        }
        return true; // ID is unique
    }

    // --- Core Functional Methods ---

    /**
     * Reads bracelet data from a specified text file.
     * Each line in the file is expected to be comma-separated values:
     * ID, Description, Quantity, Price, Status.
     * Handles file existence checks, IO errors, and validates each line of data
     * before adding it to the in-memory inventory.
     *
     * @param filePath The full path to the text file containing bracelet data.
     */
    public void readDataFromFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("File path cannot be blank. Aborting file read.");
            return;
        }

        // Check if the file exists before attempting to open
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            System.out.println(String.format("Error: File not found at '%s'. Please check the path and try again.", filePath));
            return;
        }

        // Use try-with-resources to ensure the BufferedReader is closed automatically
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNum = 0;
            List<Bracelet> newBracelets = new ArrayList<>(); // Temporarily store valid bracelets from file

            while ((line = reader.readLine()) != null) {
                lineNum++; // Increment line number for error reporting
                line = line.trim(); // Remove leading/trailing whitespace

                if (line.isEmpty()) { // Skip empty lines in the file
                    continue;
                }

                String[] parts = line.split(","); // Split the line by comma
                if (parts.length != 5) { // Check for expected number of parts
                    System.out.println(String.format("Warning: Skipping malformed line %d in file '%s': " +
                                    "Expected 5 comma-separated values, got %d. Line: '%s'",
                            lineNum, filePath, parts.length, line));
                    continue;
                }

                // Extract and validate each part of the line
                String idStr = parts[0].trim();
                String description = parts[1].trim();
                String quantityStr = parts[2].trim();
                String priceStr = parts[3].trim();
                String status = parts[4].trim();

                // Perform individual validations for each field
                if (!validateId(idStr)) {
                    System.out.println(String.format("Warning: Invalid ID '%s' on line %d. Skipping bracelet.", idStr, lineNum));
                    continue;
                }
                // When reading from file, we don't *strictly* need to check uniqueness here if we intend to overwrite/handle duplicates later.
                // For unit tests, we'll primarily test adding individual unique objects.
                // If the file could contain duplicates that should be rejected, you'd enable this:
                /*
                if (!isIdUnique(idStr)) {
                    System.out.println(String.format("Warning: Duplicate ID '%s' on line %d. Skipping bracelet.", idStr, lineNum));
                    continue;
                }
                */
                if (!validateDescription(description)) {
                    System.out.println(String.format("Warning: Invalid description on line %d. Skipping bracelet.", lineNum));
                    continue;
                }

                int quantity = validateQuantity(quantityStr);
                if (quantity == -1) { // -1 indicates validation failure from validateQuantity
                    System.out.println(String.format("Warning: Invalid quantity '%s' on line %d. Skipping bracelet.", quantityStr, lineNum));
                    continue;
                }

                double price = validatePrice(priceStr);
                if (price == -1.0) { // -1.0 indicates validation failure from validatePrice
                    System.out.println(String.format("Warning: Invalid price '%s' on line %d. Skipping bracelet.", priceStr, lineNum));
                    continue;
                }

                if (!validateStatus(status)) {
                    System.out.println(String.format("Warning: Invalid status '%s' on line %d. Skipping bracelet.", status, lineNum));
                    continue;
                }

                // If all validations pass, create a Bracelet object and add to temporary list
                try {
                    Bracelet newBracelet = new Bracelet(idStr, description, quantity, price, status);
                    newBracelets.add(newBracelet);
                } catch (Exception e) {
                    System.out.println(String.format("Error creating Bracelet object from line %d: %s. Line: '%s'", lineNum, e.getMessage(), line));
                    // Continue processing next lines even if one fails object creation
                    continue;
                }
            }

            // Add all successfully parsed new bracelets from the file to the main inventory
            if (!newBracelets.isEmpty()) {
                this.inventory.addAll(newBracelets);
                System.out.println(String.format("Successfully loaded %d bracelets from '%s'.", newBracelets.size(), filePath));
            } else {
                System.out.println(String.format("No valid bracelets found or loaded from '%s'.", filePath));
            }

        } catch (IOException e) {
            // Catch specific IOException for file reading issues
            System.out.println(String.format("Error reading file '%s': %s", filePath, e.getMessage()));
        } catch (Exception e) {
            // Catch any other unexpected exceptions during file processing
            System.out.println(String.format("An unexpected error occurred during file processing: %s", e.getMessage()));
        }
    }

    /**
     * Displays all bracelets currently stored in the in-memory inventory.
     * If the inventory is empty, a message is displayed.
     */
    public void displayAllBracelets() {
        if (inventory.isEmpty()) {
            System.out.println("\nInventory is empty. No data to display.");
            return;
        }

        System.out.println("\n--- Current Bracelet Inventory ---");
        for (Bracelet bracelet : inventory) {
            System.out.println(bracelet);
        }
        System.out.println("----------------------------------");
    }

    /**
     * Prompts the user to manually enter details for a new bracelet.
     * All inputs are validated thoroughly, and the new bracelet is added
     * to the inventory if all validations pass.
     * @param scanner The Scanner object used for reading user input.
     */
    public void addBracelet(Scanner scanner) {
        System.out.println("\n--- Add New Bracelet ---");
        String itemId;
        String description;
        int quantity;
        double price;

        // Loop until a valid and unique ID is entered
        while (true) {
            System.out.print("Enter unique ID: ");
            itemId = scanner.nextLine().trim();
            if (!validateId(itemId)) { // Validate for non-empty ID
                continue;
            }
            if (!isIdUnique(itemId)) { // Validate for uniqueness
                System.out.println("Error: A bracelet with this ID already exists. Please enter a unique ID.");
                continue;
            }
            break; // Exit loop if ID is valid and unique
        }

        // Loop until a valid description is entered
        while (true) {
            System.out.print("Enter description: ");
            description = scanner.nextLine().trim();
            if (!validateDescription(description)) {
                continue;
            }
            break;
        }

        // Loop until a valid quantity is entered
        while (true) {
            System.out.print("Enter quantity: ");
            String quantityStr = scanner.nextLine().trim();
            quantity = validateQuantity(quantityStr);
            if (quantity == -1) { // -1 indicates validation failure
                continue;
            }
            break;
        }

        // Loop until a valid price is entered
        while (true) {
            System.out.print("Enter price: ");
            String priceStr = scanner.nextLine().trim();
            price = validatePrice(priceStr);
            if (price == -1.0) { // -1.0 indicates validation failure
                continue;
            }
            break;
        }

        // Default status to "In Stock" as per project requirements
        String status = "In Stock";

        try {
            // Create the new Bracelet object and add it to the inventory
            Bracelet newBracelet = new Bracelet(itemId, description, quantity, price, status);
            this.inventory.add(newBracelet);
            System.out.println(String.format("\nSuccessfully added: %s", newBracelet));
        } /* No longer catching general Exception here as specific checks are done by validation methods
           catch (Exception e) {
            // Catch any unexpected errors during object creation/addition
            System.out.println(String.format("An error occurred while adding the bracelet: %s", e.getMessage()));
           }
         */
        finally {
            // This block will always execute, good for cleanup if needed in a real app
        }
    }

    /**
     * Removes a bracelet from the inventory based on its unique ID.
     * If the ID is not found, an appropriate error message is displayed.
     * @param scanner The Scanner object used for reading user input.
     */
    public void removeBracelet(Scanner scanner) {
        System.out.println("\n--- Remove Bracelet ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. Nothing to remove.");
            return;
        }

        System.out.print("Enter the ID of the bracelet to remove: ");
        String itemId = scanner.nextLine().trim();
        if (!validateId(itemId)) { // Validate ID format
            return;
        }

        Bracelet braceletToRemove = null;
        // Search for the bracelet by ID
        for (Bracelet bracelet : inventory) {
            if (bracelet.getId().equalsIgnoreCase(itemId)) {
                braceletToRemove = bracelet;
                break;
            }
        }

        if (braceletToRemove != null) {
            // If found, remove it from the list
            inventory.remove(braceletToRemove);
            System.out.println(String.format("Successfully removed: %s", braceletToRemove));
        } else {
            // If not found, inform the user
            System.out.println(String.format("Error: Bracelet with ID '%s' not found in inventory.", itemId));
        }
    }

    /**
     * Updates fields (quantity, price, or status) of an existing bracelet.
     * The user is prompted for the bracelet's ID, and then given options
     * to choose which field to update, with full input validation.
     * @param scanner The Scanner object used for reading user input.
     */
    public void updateBracelet(Scanner scanner) {
        System.out.println("\n--- Update Bracelet ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. Nothing to update.");
            return;
        }

        System.out.print("Enter the ID of the bracelet to update: ");
        String itemId = scanner.nextLine().trim();
        if (!validateId(itemId)) { // Validate ID format
            return;
        }

        Bracelet braceletToUpdate = null;
        // Find the bracelet by ID
        for (Bracelet bracelet : inventory) {
            if (bracelet.getId().equalsIgnoreCase(itemId)) {
                braceletToUpdate = bracelet;
                break;
            }
        }

        if (braceletToUpdate == null) {
            System.out.println(String.format("Error: Bracelet with ID '%s' not found in inventory.", itemId));
            return;
        }

        // Display current details of the found bracelet
        System.out.println(String.format("Found bracelet:\n%s", braceletToUpdate));
        System.out.println("\nWhich field do you want to update?");
        System.out.println("1. Quantity");
        System.out.println("2. Price");
        System.out.println("3. Status");
        System.out.println("4. Cancel");

        // Loop until a valid choice is made or cancelled
        while (true) {
            System.out.print("Enter your choice (1-4): ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    while (true) { // Loop for quantity input until valid
                        System.out.print("Enter new quantity: ");
                        String newQuantityStr = scanner.nextLine().trim();
                        int newQuantity = validateQuantity(newQuantityStr);
                        if (newQuantity != -1) {
                            braceletToUpdate.setQuantity(newQuantity);
                            // Auto-update status based on new quantity
                            if (newQuantity == 0 && !braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                                braceletToUpdate.setStatus("Out of Stock");
                                System.out.println("Status automatically updated to 'Out of Stock' due to zero quantity.");
                            } else if (newQuantity > 0 && braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                                braceletToUpdate.setStatus("In Stock");
                                System.out.println("Status automatically updated to 'In Stock' due to positive quantity.");
                            }
                            System.out.println(String.format("Quantity updated. Updated bracelet:\n%s", braceletToUpdate));
                            return; // Exit update method
                        }
                    }
                case "2":
                    while (true) { // Loop for price input until valid
                        System.out.print("Enter new price: ");
                        String newPriceStr = scanner.nextLine().trim();
                        double newPrice = validatePrice(newPriceStr);
                        if (newPrice != -1.0) {
                            braceletToUpdate.setPrice(newPrice);
                            System.out.println(String.format("Price updated. Updated bracelet:\n%s", braceletToUpdate));
                            return; // Exit update method
                        }
                    }
                case "3":
                    while (true) { // Loop for status input until valid
                        System.out.print("Enter new status ('In Stock' or 'Out of Stock'): ");
                        String newStatus = scanner.nextLine().trim();
                        if (validateStatus(newStatus)) {
                            braceletToUpdate.setStatus(newStatus);
                            System.out.println(String.format("Status updated. Updated bracelet:\n%s", braceletToUpdate));
                            return; // Exit update method
                        }
                    }
                case "4":
                    System.out.println("Update cancelled.");
                    return; // Exit update method
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }

    /**
     * Generates a report listing all bracelets whose quantity falls below
     * a user-specified threshold. Includes input validation for the threshold.
     * @param scanner The Scanner object used for reading user input.
     */
    public void generateLowStockReport(Scanner scanner) {
        System.out.println("\n--- Generate Low Stock Report ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. No low stock items to report.");
            return;
        }

        int threshold;
        while (true) { // Loop until a valid threshold quantity is entered
            System.out.print("Enter the low stock threshold quantity: ");
            String thresholdStr = scanner.nextLine().trim();
            threshold = validateQuantity(thresholdStr); // Reusing quantity validation logic
            if (threshold != -1) {
                break; // Exit loop if threshold is valid
            }
        }

        List<Bracelet> lowStockItems = new ArrayList<>();
        // Iterate through inventory to find items below threshold
        for (Bracelet bracelet : inventory) {
            if (bracelet.getQuantity() < threshold) {
                lowStockItems.add(bracelet);
            }
        }

        if (lowStockItems.isEmpty()) {
            System.out.println(String.format("\nNo bracelets currently below the specified stock threshold of %d.", threshold));
        } else {
            System.out.println(String.format("\n--- Bracelets Below Stock Threshold (%d) ---", threshold));
            // Sort low stock items by quantity for better readability
            Collections.sort(lowStockItems, (b1, b2) -> Integer.compare(b1.getQuantity(), b2.getQuantity()));
            for (Bracelet bracelet : lowStockItems) {
                System.out.println(String.format("ID: %s, Description: %s, Current Quantity: %d",
                        bracelet.getId(), bracelet.getDescription(), bracelet.getQuantity()));
            }
            System.out.println("-------------------------------------------------------");
        }
    }
}
