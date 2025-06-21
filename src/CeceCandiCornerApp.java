import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Collections; // For sorting

/**
 * Represents a single bracelet item in the inventory.
 */
class Bracelet {
    private String id;
    private String description;
    private int quantity;
    private double price;
    private String status;

    /**
     * Initializes a new Bracelet object.
     *
     * @param id The unique ID of the bracelet.
     * @param description The description of the bracelet.
     * @param quantity The quantity of the bracelet.
     * @param price The price of the bracelet.
     * @param status The stock status of the bracelet (defaults to "In Stock").
     */
    public Bracelet(String id, String description, int quantity, double price, String status) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    // --- Setters ---
    public void setQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public void setPrice(double newPrice) {
        this.price = newPrice;
    }

    public void setStatus(String newStatus) {
        this.status = newStatus;
    }

    /**
     * Returns a string representation of the Bracelet object.
     * @return A formatted string with bracelet details.
     */
    @Override
    public String toString() {
        return String.format("ID: %s, Description: %s, Quantity: %d, Price: $%.2f, Status: %s",
                id, description, quantity, price, status);
    }
}

/**
 * Manages the inventory of Bracelet objects.
 * Handles CRUD operations, file loading, and reporting.
 */
class InventoryManager {
    private List<Bracelet> inventory;

    /**
     * Initializes the InventoryManager with an empty inventory list.
     */
    public InventoryManager() {
        this.inventory = new ArrayList<>();
    }

    // --- Input Validation Methods ---

    /**
     * Checks if an ID is valid (non-empty).
     * @param itemId The ID to validate.
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
     * Checks if a description is valid (non-empty).
     * @param description The description to validate.
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
     * Validates if a quantity string is a positive integer.
     * @param quantityStr The quantity string to validate.
     * @return The parsed integer quantity if valid, -1 otherwise.
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
     * Validates if a price string is a positive double.
     * @param priceStr The price string to validate.
     * @return The parsed double price if valid, -1.0 otherwise.
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
     * Checks if a status is valid ("In Stock" or "Out of Stock").
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
     * Checks if a given ID already exists in the inventory.
     * @param itemId The ID to check for uniqueness.
     * @return true if the ID is unique, false otherwise.
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
     * Handles file errors and validates each line of data.
     * @param filePath The path to the text file.
     */
    public void readDataFromFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("File path cannot be blank. Aborting file read.");
            return;
        }

        // Using java.io.File to check existence, similar to os.path.exists in Python
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            System.out.println(String.format("Error: File not found at '%s'. Please check the path and try again.", filePath));
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNum = 0;
            List<Bracelet> newBracelets = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) { // Skip empty lines
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 5) {
                    System.out.println(String.format("Warning: Skipping malformed line %d in file '%s': " +
                                    "Expected 5 comma-separated values, got %d. Line: '%s'",
                            lineNum, filePath, parts.length, line));
                    continue;
                }

                // Extract and validate each part
                String idStr = parts[0].trim();
                String description = parts[1].trim();
                String quantityStr = parts[2].trim();
                String priceStr = parts[3].trim();
                String status = parts[4].trim();

                if (!validateId(idStr)) {
                    System.out.println(String.format("Warning: Invalid ID '%s' on line %d. Skipping bracelet.", idStr, lineNum));
                    continue;
                }
                if (!isIdUnique(idStr)) {
                    System.out.println(String.format("Warning: Duplicate ID '%s' on line %d. Skipping bracelet.", idStr, lineNum));
                    continue;
                }
                if (!validateDescription(description)) {
                    System.out.println(String.format("Warning: Invalid description on line %d. Skipping bracelet.", lineNum));
                    continue;
                }

                int quantity = validateQuantity(quantityStr);
                if (quantity == -1) { // -1 indicates validation failure
                    System.out.println(String.format("Warning: Invalid quantity '%s' on line %d. Skipping bracelet.", quantityStr, lineNum));
                    continue;
                }

                double price = validatePrice(priceStr);
                if (price == -1.0) { // -1.0 indicates validation failure
                    System.out.println(String.format("Warning: Invalid price '%s' on line %d. Skipping bracelet.", priceStr, lineNum));
                    continue;
                }

                if (!validateStatus(status)) {
                    System.out.println(String.format("Warning: Invalid status '%s' on line %d. Skipping bracelet.", status, lineNum));
                    continue;
                }

                // If all validations pass, create a Bracelet object
                try {
                    Bracelet newBracelet = new Bracelet(idStr, description, quantity, price, status);
                    newBracelets.add(newBracelet);
                } catch (Exception e) {
                    System.out.println(String.format("Error creating Bracelet object from line %d: %s. Line: '%s'", lineNum, e.getMessage(), line));
                    continue;
                }
            }

            // Add all successfully parsed new bracelets to the inventory
            if (!newBracelets.isEmpty()) {
                this.inventory.addAll(newBracelets);
                System.out.println(String.format("Successfully loaded %d bracelets from '%s'.", newBracelets.size(), filePath));
            } else {
                System.out.println(String.format("No valid bracelets found or loaded from '%s'.", filePath));
            }

        } catch (IOException e) {
            System.out.println(String.format("Error reading file '%s': %s", filePath, e.getMessage()));
        } catch (Exception e) {
            System.out.println(String.format("An unexpected error occurred during file processing: %s", e.getMessage()));
        }
    }

    /**
     * Displays all bracelets currently in the inventory.
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
     * Prompts the user for new bracelet details and adds it to the inventory
     * after rigorous input validation.
     * @param scanner Scanner object for user input.
     */
    public void addBracelet(Scanner scanner) {
        System.out.println("\n--- Add New Bracelet ---");
        String itemId;
        String description;
        int quantity;
        double price;

        while (true) {
            System.out.print("Enter unique ID: ");
            itemId = scanner.nextLine().trim();
            if (!validateId(itemId)) {
                continue;
            }
            if (!isIdUnique(itemId)) {
                System.out.println("Error: A bracelet with this ID already exists. Please enter a unique ID.");
                continue;
            }
            break;
        }

        while (true) {
            System.out.print("Enter description: ");
            description = scanner.nextLine().trim();
            if (!validateDescription(description)) {
                continue;
            }
            break;
        }

        while (true) {
            System.out.print("Enter quantity: ");
            String quantityStr = scanner.nextLine().trim();
            quantity = validateQuantity(quantityStr);
            if (quantity == -1) {
                continue;
            }
            break;
        }

        while (true) {
            System.out.print("Enter price: ");
            String priceStr = scanner.nextLine().trim();
            price = validatePrice(priceStr);
            if (price == -1.0) {
                continue;
            }
            break;
        }

        // Default status to "In Stock" as per requirements
        String status = "In Stock";

        try {
            Bracelet newBracelet = new Bracelet(itemId, description, quantity, price, status);
            this.inventory.add(newBracelet);
            System.out.println(String.format("\nSuccessfully added: %s", newBracelet));
        } catch (Exception e) {
            System.out.println(String.format("An error occurred while adding the bracelet: %s", e.getMessage()));
        }
    }

    /**
     * Removes a bracelet from the inventory based on its unique ID.
     * Handles cases where the ID does not exist.
     * @param scanner Scanner object for user input.
     */
    public void removeBracelet(Scanner scanner) {
        System.out.println("\n--- Remove Bracelet ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. Nothing to remove.");
            return;
        }

        System.out.print("Enter the ID of the bracelet to remove: ");
        String itemId = scanner.nextLine().trim();
        if (!validateId(itemId)) {
            return;
        }

        Bracelet braceletToRemove = null;
        for (Bracelet bracelet : inventory) {
            if (bracelet.getId().equalsIgnoreCase(itemId)) {
                braceletToRemove = bracelet;
                break;
            }
        }

        if (braceletToRemove != null) {
            inventory.remove(braceletToRemove);
            System.out.println(String.format("Successfully removed: %s", braceletToRemove));
        } else {
            System.out.println(String.format("Error: Bracelet with ID '%s' not found in inventory.", itemId));
        }
    }

    /**
     * Updates fields of an existing bracelet based on its ID.
     * Provides options to update quantity, price, or status with validation.
     * @param scanner Scanner object for user input.
     */
    public void updateBracelet(Scanner scanner) {
        System.out.println("\n--- Update Bracelet ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. Nothing to update.");
            return;
        }

        System.out.print("Enter the ID of the bracelet to update: ");
        String itemId = scanner.nextLine().trim();
        if (!validateId(itemId)) {
            return;
        }

        Bracelet braceletToUpdate = null;
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

        System.out.println(String.format("Found bracelet:\n%s", braceletToUpdate));
        System.out.println("\nWhich field do you want to update?");
        System.out.println("1. Quantity");
        System.out.println("2. Price");
        System.out.println("3. Status");
        System.out.println("4. Cancel");

        while (true) {
            System.out.print("Enter your choice (1-4): ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    while (true) {
                        System.out.print("Enter new quantity: ");
                        String newQuantityStr = scanner.nextLine().trim();
                        int newQuantity = validateQuantity(newQuantityStr);
                        if (newQuantity != -1) {
                            braceletToUpdate.setQuantity(newQuantity);
                            // Automatic status update based on quantity
                            if (newQuantity == 0 && !braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                                braceletToUpdate.setStatus("Out of Stock");
                                System.out.println("Status automatically updated to 'Out of Stock' due to zero quantity.");
                            } else if (newQuantity > 0 && braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                                braceletToUpdate.setStatus("In Stock");
                                System.out.println("Status automatically updated to 'In Stock' due to positive quantity.");
                            }
                            System.out.println(String.format("Quantity updated. Updated bracelet:\n%s", braceletToUpdate));
                            return;
                        }
                    }
                case "2":
                    while (true) {
                        System.out.print("Enter new price: ");
                        String newPriceStr = scanner.nextLine().trim();
                        double newPrice = validatePrice(newPriceStr);
                        if (newPrice != -1.0) {
                            braceletToUpdate.setPrice(newPrice);
                            System.out.println(String.format("Price updated. Updated bracelet:\n%s", braceletToUpdate));
                            return;
                        }
                    }
                case "3":
                    while (true) {
                        System.out.print("Enter new status ('In Stock' or 'Out of Stock'): ");
                        String newStatus = scanner.nextLine().trim();
                        if (validateStatus(newStatus)) {
                            braceletToUpdate.setStatus(newStatus);
                            System.out.println(String.format("Status updated. Updated bracelet:\n%s", braceletToUpdate));
                            return;
                        }
                    }
                case "4":
                    System.out.println("Update cancelled.");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }

    /**
     * Generates a report of bracelets with quantity below a user-specified threshold.
     * @param scanner Scanner object for user input.
     */
    public void generateLowStockReport(Scanner scanner) {
        System.out.println("\n--- Generate Low Stock Report ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. No low stock items to report.");
            return;
        }

        int threshold;
        while (true) {
            System.out.print("Enter the low stock threshold quantity: ");
            String thresholdStr = scanner.nextLine().trim();
            threshold = validateQuantity(thresholdStr);
            if (threshold != -1) {
                break;
            }
        }

        List<Bracelet> lowStockItems = new ArrayList<>();
        for (Bracelet bracelet : inventory) {
            if (bracelet.getQuantity() < threshold) {
                lowStockItems.add(bracelet);
            }
        }

        if (lowStockItems.isEmpty()) {
            System.out.println(String.format("\nNo bracelets currently below the specified stock threshold of %d.", threshold));
        } else {
            System.out.println(String.format("\n--- Bracelets Below Stock Threshold (%d) ---", threshold));
            // Sort items by quantity for better readability
            Collections.sort(lowStockItems, (b1, b2) -> Integer.compare(b1.getQuantity(), b2.getQuantity()));
            for (Bracelet bracelet : lowStockItems) {
                System.out.println(String.format("ID: %s, Description: %s, Current Quantity: %d",
                        bracelet.getId(), bracelet.getDescription(), bracelet.getQuantity()));
            }
            System.out.println("-------------------------------------------------------");
        }
    }
}

/**
 * Main class to run the console-based inventory management system.
 * This simulates the user interaction that would typically be handled by a JavaFX GUI.
 */
public class CeceCandiCornerApp {

    /**
     * Displays the main menu options to the user.
     */
    private static void displayMainMenu() {
        System.out.println("\n--- Cece's Candi Corner Inventory DMS ---");
        System.out.println("1. Load Data from File");
        System.out.println("2. Display All Bracelets");
        System.out.println("3. Add New Bracelet");
        System.out.println("4. Remove Bracelet");
        System.out.println("5. Update Bracelet");
        System.out.println("6. Generate Low Stock Report");
        System.out.println("7. Exit");
        System.out.println("------------------------------------------");
    }

    /**
     * Main function to run the inventory management system.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMainMenu();
            System.out.print("Enter your choice (1-7): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter the path to the text file (e.g., data.txt): ");
                    String filePath = scanner.nextLine().trim();
                    manager.readDataFromFile(filePath);
                    break;
                case "2":
                    manager.displayAllBracelets();
                    break;
                case "3":
                    manager.addBracelet(scanner);
                    break;
                case "4":
                    manager.removeBracelet(scanner);
                    break;
                case "5":
                    manager.updateBracelet(scanner);
                    break;
                case "6":
                    manager.generateLowStockReport(scanner);
                    break;
                case "7":
                    System.out.println("Exiting Cece's Candi Corner Inventory DMS. Goodbye!");
                    scanner.close();
                    return; // Exit the program
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 7.");
                    break;
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine(); // Consume the newline character after operations
        }
    }
}
