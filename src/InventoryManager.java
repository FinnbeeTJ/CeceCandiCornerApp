/**
 * InventoryManager.java
 * Manages the inventory of Bracelet objects for Cece's Candi Corner.
 * This class handles all core CRUD (Create, Read, Update, Delete) operations,
 * as well as reading data from files and generating reports.
 * It includes robust input validation to ensure data integrity.
 *
 * This version has been updated to include JavaFX GUI capatibility
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class InventoryManager { // Changed to public class
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
     * Getter for the inventory list. This is added primarily for unit testing purposes
     * and for the GUI to display the current state.
     * @return The internal list of Bracelet objects.
     */
    public List<Bracelet> getInventory() {
        return inventory;
    }

    // --- Input Validation Methods (private helpers) ---
    /**
     * Validates if the provided ID is not null or empty after trimming whitespace.
     * @param itemId The ID string to validate.
     * @return true if the ID is valid, false otherwise.
     */
    private boolean validateId(String itemId) {
        return itemId != null && !itemId.trim().isEmpty();
    }

    /**
     * Validates if the provided description is not null or empty after trimming whitespace.
     * @param description The description string to validate.
     * @return true if the description is valid, false otherwise.
     */
    private boolean validateDescription(String description) {
        return description != null && !description.trim().isEmpty();
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
                return -1; // Indicate error: negative quantity
            }
            return quantity;
        } catch (NumberFormatException e) {
            return -1; // Indicate error: not a valid integer
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
                return -1.0; // Indicate error: negative price
            }
            return price;
        } catch (NumberFormatException e) {
            return -1.0; // Indicate error: not a valid number
        }
    }

    /**
     * Validates if the provided status string is either "In Stock" or "Out of Stock" (case-insensitive).
     * @param status The status string to validate.
     * @return true if the status is valid, false otherwise.
     */
    private boolean validateStatus(String status) {
        return status != null && (status.equalsIgnoreCase("In Stock") || status.equalsIgnoreCase("Out of Stock"));
    }

    /**
     * Checks if a given bracelet ID already exists in the current inventory.
     * @param itemId The ID to check for uniqueness.
     * @return true if the ID is unique (does not exist in inventory), false otherwise.
     */
    public boolean isIdUnique(String itemId) { // Changed to public for potential external use/testing
        for (Bracelet bracelet : inventory) {
            if (bracelet.getId().equalsIgnoreCase(itemId)) {
                return false; // ID already exists
            }
        }
        return true; // ID is unique
    }

    /**
     * Finds a bracelet by its ID.
     * @param itemId The ID of the bracelet to find.
     * @return The Bracelet object if found, null otherwise.
     */
    public Bracelet getBraceletById(String itemId) {
        for (Bracelet bracelet : inventory) {
            if (bracelet.getId().equalsIgnoreCase(itemId)) {
                return bracelet;
            }
        }
        return null;
    }

    // --- Core Functional Methods (adapted for GUI) ---

    /**
     * Reads bracelet data from a specified text file.
     * Each line in the file is expected to be comma-separated values:
     * ID, Description, Quantity, Price, Status.
     * Handles file existence checks, IO errors, and validates each line of data
     * before adding it to the in-memory inventory.
     *
     * @param filePath The full path to the text file containing bracelet data.
     * @return A string message indicating the outcome (success or error).
     */
    public String readDataFromFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "Error: File path cannot be blank. Aborting file read.";
        }

        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            return String.format("Error: File not found at '%s'. Please check the path and try again.", filePath);
        }

        List<Bracelet> newBracelets = new ArrayList<>();
        StringBuilder warnings = new StringBuilder();
        int lineNum = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 5) {
                    warnings.append(String.format("Warning: Skipping malformed line %d: Expected 5 comma-separated values, got %d. Line: '%s'\n",
                            lineNum, parts.length, line));
                    continue;
                }

                String idStr = parts[0].trim();
                String description = parts[1].trim();
                String quantityStr = parts[2].trim();
                String priceStr = parts[3].trim();
                String status = parts[4].trim();

                // Validation for file loading - note: isIdUnique is called here to prevent duplicates from file
                if (!validateId(idStr)) {
                    warnings.append(String.format("Warning: Invalid ID '%s' on line %d. Skipping bracelet.\n", idStr, lineNum));
                    continue;
                }
                if (!isIdUnique(idStr)) {
                    warnings.append(String.format("Warning: Duplicate ID '%s' on line %d. Skipping bracelet.\n", idStr, lineNum));
                    continue;
                }
                if (!validateDescription(description)) {
                    warnings.append(String.format("Warning: Invalid description on line %d. Skipping bracelet.\n", lineNum));
                    continue;
                }

                int quantity = validateQuantity(quantityStr);
                if (quantity == -1) {
                    warnings.append(String.format("Warning: Invalid quantity '%s' on line %d. Skipping bracelet.\n", quantityStr, lineNum));
                    continue;
                }

                double price = validatePrice(priceStr);
                if (price == -1.0) {
                    warnings.append(String.format("Warning: Invalid price '%s' on line %d. Skipping bracelet.\n", priceStr, lineNum));
                    continue;
                }

                if (!validateStatus(status)) {
                    warnings.append(String.format("Warning: Invalid status '%s' on line %d. Skipping bracelet.\n", status, lineNum));
                    continue;
                }

                try {
                    Bracelet newBracelet = new Bracelet(idStr, description, quantity, price, status);
                    newBracelets.add(newBracelet);
                } catch (Exception e) {
                    warnings.append(String.format("Error creating Bracelet object from line %d: %s. Line: '%s'\n", lineNum, e.getMessage(), line));
                }
            }

            if (!newBracelets.isEmpty()) {
                this.inventory.addAll(newBracelets);
                return String.format("Successfully loaded %d bracelets from '%s'.\n%s", newBracelets.size(), filePath, warnings.toString());
            } else {
                return String.format("No valid bracelets found or loaded from '%s'.\n%s", filePath, warnings.toString());
            }

        } catch (IOException e) {
            return String.format("Error reading file '%s': %s", filePath, e.getMessage());
        } catch (Exception e) {
            return String.format("An unexpected error occurred during file processing: %s", e.getMessage());
        }
    }

    /**
     * Adds a new bracelet to the inventory.
     *
     * @param id The unique ID for the bracelet.
     * @param description A brief description or name of the bracelet.
     * @param quantity The current stock quantity of the bracelet.
     * @param price The selling price of the bracelet.
     * @return A string message indicating the outcome (success or error).
     */
    public String addBracelet(String id, String description, String quantityStr, String priceStr) {
        if (!validateId(id)) {
            return "Error: ID cannot be empty.";
        }
        if (!isIdUnique(id)) {
            return "Error: A bracelet with this ID already exists. Please enter a unique ID.";
        }
        if (!validateDescription(description)) {
            return "Error: Description cannot be empty.";
        }

        int quantity = validateQuantity(quantityStr);
        if (quantity == -1) {
            return "Error: Quantity must be a valid non-negative integer.";
        }

        double price = validatePrice(priceStr);
        if (price == -1.0) {
            return "Error: Price must be a valid non-negative number.";
        }

        // Default status to "In Stock"
        String status = "In Stock";

        try {
            Bracelet newBracelet = new Bracelet(id, description, quantity, price, status);
            this.inventory.add(newBracelet);
            return String.format("Successfully added: %s", newBracelet);
        } catch (Exception e) {
            return String.format("An unexpected error occurred while adding the bracelet: %s", e.getMessage());
        }
    }

    /**
     * Removes a bracelet from the inventory based on its unique ID.
     * @param itemId The ID of the bracelet to remove.
     * @return A string message indicating the outcome (success or error).
     */
    public String removeBracelet(String itemId) {
        if (!validateId(itemId)) {
            return "Error: Bracelet ID cannot be empty.";
        }

        Bracelet braceletToRemove = getBraceletById(itemId);

        if (braceletToRemove != null) {
            inventory.remove(braceletToRemove);
            return String.format("Successfully removed: %s", braceletToRemove);
        } else {
            return String.format("Error: Bracelet with ID '%s' not found in inventory.", itemId);
        }
    }

    /**
     * Updates a specific field of an existing bracelet.
     * @param itemId The ID of the bracelet to update.
     * @param fieldToUpdate The name of the field to update ("quantity", "price", or "status").
     * @param newValue The new value for the specified field.
     * @return A string message indicating the outcome (success or error).
     */
    public String updateBracelet(String itemId, String fieldToUpdate, String newValue) {
        if (!validateId(itemId)) {
            return "Error: Bracelet ID cannot be empty.";
        }

        Bracelet braceletToUpdate = getBraceletById(itemId);
        if (braceletToUpdate == null) {
            return String.format("Error: Bracelet with ID '%s' not found in inventory.", itemId);
        }

        String message = "";
        switch (fieldToUpdate.toLowerCase()) {
            case "quantity":
                int newQuantity = validateQuantity(newValue);
                if (newQuantity != -1) {
                    braceletToUpdate.setQuantity(newQuantity);
                    // Auto-update status based on new quantity
                    // If quantity becomes 0 and status is not already "Out of Stock"
                    if (newQuantity == 0 && !braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                        braceletToUpdate.setStatus("Out of Stock");
                        message = "Quantity updated. Status automatically updated to 'Out of Stock' due to zero quantity.";
                    }
                    // If quantity becomes positive and status is "Out of Stock"
                    else if (newQuantity > 0 && braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                        braceletToUpdate.setStatus("In Stock");
                        message = "Quantity updated. Status automatically updated to 'In Stock' due to positive quantity.";
                    }
                    // Otherwise, just update quantity message
                    else {
                        message = "Quantity updated.";
                    }
                } else {
                    return "Error: New quantity must be a valid non-negative integer.";
                }
                break;
            case "price":
                double newPrice = validatePrice(newValue);
                if (newPrice != -1.0) {
                    braceletToUpdate.setPrice(newPrice);
                    message = "Price updated.";
                } else {
                    return "Error: New price must be a valid non-negative number.";
                }
                break;
            case "status":
                if (validateStatus(newValue)) {
                    braceletToUpdate.setStatus(newValue);
                    message = "Status updated.";
                } else {
                    return "Error: New status must be 'In Stock' or 'Out of Stock'.";
                }
                break;
            default:
                return "Error: Invalid field to update. Choose 'quantity', 'price', or 'status'.";
        }
        return String.format("%s Updated bracelet: %s", message, braceletToUpdate);
    }

    /**
     * Generates a report listing all bracelets whose quantity falls below
     * a user-specified threshold.
     * @param thresholdStr The string representation of the threshold quantity.
     * @return A list of Bracelet objects that are below the threshold, or an empty list if none.
     * If the threshold input is invalid, returns null.
     */
    public List<Bracelet> generateLowStockReport(String thresholdStr) {
        int threshold = validateQuantity(thresholdStr);
        if (threshold == -1) {
            return null; // Indicate invalid threshold
        }

        List<Bracelet> lowStockItems = new ArrayList<>();
        for (Bracelet bracelet : inventory) {
            if (bracelet.getQuantity() < threshold) {
                lowStockItems.add(bracelet);
            }
        }
        // Sort low stock items by quantity for better readability
        Collections.sort(lowStockItems, (b1, b2) -> Integer.compare(b1.getQuantity(), b2.getQuantity()));
        return lowStockItems;
    }
}
