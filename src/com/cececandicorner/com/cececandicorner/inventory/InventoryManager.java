package com.cececandicorner.inventory; // IMPORTANT: Ensure this matches your package name

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** InventoryManager.java
* Manages the inventory of Bracelet objects for Cece's Candi Corner.
* This class now interfaces with a DatabaseManager for persistent storage
* instead of an in-memory list or text files.
* It handles all core CRUD (Create, Read, Update, Delete) operations,
* and generates reports by delegating to the DatabaseManager.
* It includes robust input validation to ensure data integrity.
*/

public class InventoryManager {
    // No longer storing an in-memory list; operations delegate to database
    private DatabaseManager dbManager;

    /**
     * Constructor to initialize the InventoryManager with a DatabaseManager.
     * @param dbManager The DatabaseManager instance to use for database operations.
     */
    public InventoryManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        // Ensure the table exists when manager is initialized
        if (!dbManager.createTable()) {
            System.err.println("Failed to ensure database table exists on startup.");
        }
    }

    /**
     * Getter for the inventory list. This now fetches all bracelets from the database.
     * @return A list of Bracelet objects from the database.
     */
    public List<Bracelet> getInventory() {
        return dbManager.selectAllBracelets();
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
     * Checks if a given bracelet ID already exists in the database.
     * @param itemId The ID to check for uniqueness.
     * @return true if the ID is unique (does not exist in database), false otherwise.
     */
    public boolean isIdUnique(String itemId) {
        return !dbManager.doesIdExist(itemId);
    }

    /**
     * Finds a bracelet by its ID from the database.
     * @param itemId The ID of the bracelet to find.
     * @return The Bracelet object if found, null otherwise.
     */
    public Bracelet getBraceletById(String itemId) {
        return dbManager.selectBraceletById(itemId);
    }

    // --- Core Functional Methods (adapted for GUI and Database) ---

    /**
     * Adds a new bracelet to the database.
     * @param id The unique ID for the bracelet.
     * @param description A brief description or name of the bracelet.
     * @param quantityStr The current stock quantity of the bracelet.
     * @param priceStr The selling price of the bracelet.
     * @return A string message indicating the outcome (success or error).
     */
    public String addBracelet(String id, String description, String quantityStr, String priceStr) {
        if (!validateId(id)) {
            return "Error: ID cannot be empty.";
        }
        if (!isIdUnique(id)) { // Check uniqueness against database
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
            if (dbManager.insertBracelet(newBracelet)) { // Insert into database
                return String.format("Successfully added: %s", newBracelet);
            } else {
                return "Error: Failed to add bracelet to database.";
            }
        } catch (Exception e) {
            return String.format("An unexpected error occurred while adding the bracelet: %s", e.getMessage());
        }
    }

    /**
     * Removes a bracelet from the database based on its unique ID.
     * @param itemId The ID of the bracelet to remove.
     * @return A string message indicating the outcome (success or error).
     */
    public String removeBracelet(String itemId) {
        if (!validateId(itemId)) {
            return "Error: Bracelet ID cannot be empty.";
        }

        if (!dbManager.doesIdExist(itemId)) { // Check existence in database
            return String.format("Error: Bracelet with ID '%s' not found in inventory.", itemId);
        }

        if (dbManager.deleteBracelet(itemId)) { // Delete from database
            return String.format("Successfully removed bracelet with ID: %s", itemId);
        } else {
            return String.format("Error: Failed to remove bracelet with ID: %s from database.", itemId);
        }
    }

    /**
     * Updates a specific field of an existing bracelet in the database.
     * @param itemId The ID of the bracelet to update.
     * @param fieldToUpdate The name of the field to update ("quantity", "price", or "status").
     * @param newValue The new value for the specified field.
     * @return A string message indicating the outcome (success or error).
     */
    public String updateBracelet(String itemId, String fieldToUpdate, String newValue) {
        if (!validateId(itemId)) {
            return "Error: Bracelet ID cannot be empty.";
        }

        Bracelet braceletToUpdate = dbManager.selectBraceletById(itemId); // Get from database
        if (braceletToUpdate == null) {
            return String.format("Error: Bracelet with ID '%s' not found in inventory.", itemId);
        }

        String message = "";
        boolean changed = false; // Flag to track if any value actually changed

        switch (fieldToUpdate.toLowerCase()) {
            case "quantity":
                int newQuantity = validateQuantity(newValue);
                if (newQuantity != -1) {
                    if (braceletToUpdate.getQuantity() != newQuantity) { // Only update if value changed
                        braceletToUpdate.setQuantity(newQuantity);
                        // Auto-update status based on new quantity
                        if (newQuantity == 0 && !braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                            braceletToUpdate.setStatus("Out of Stock");
                            // FIX: Ensure message matches test expectation exactly
                            message = "Quantity updated.\nStatus automatically updated to 'Out of Stock'";
                        } else if (newQuantity > 0 && braceletToUpdate.getStatus().equalsIgnoreCase("Out of Stock")) {
                            braceletToUpdate.setStatus("In Stock");
                            // FIX: Ensure message matches test expectation exactly
                            message = "Quantity updated.\nStatus automatically updated to 'In Stock'";
                        } else {
                            message = "Quantity updated.";
                        }
                        changed = true;
                    } else {
                        return "No change: Quantity is already " + newQuantity + ".";
                    }
                } else {
                    return "Error: New quantity must be a valid non-negative integer.";
                }
                break;
            case "price":
                double newPrice = validatePrice(newValue);
                if (newPrice != -1.0) {
                    if (braceletToUpdate.getPrice() != newPrice) { // Only update if value changed
                        braceletToUpdate.setPrice(newPrice);
                        message = "Price updated.";
                        changed = true;
                    } else {
                        return "No change: Price is already " + newPrice + ".";
                    }
                } else {
                    return "Error: New price must be a valid non-negative number.";
                }
                break;
            case "status":
                if (validateStatus(newValue)) {
                    if (!braceletToUpdate.getStatus().equalsIgnoreCase(newValue)) { // Only update if value changed
                        braceletToUpdate.setStatus(newValue);
                        message = "Status updated.";
                        changed = true;
                    } else {
                        return "No change: Status is already '" + newValue + "'.";
                    }
                } else {
                    return "Error: New status must be 'In Stock' or 'Out of Stock'.";
                }
                break;
            default:
                return "Error: Invalid field to update. Choose 'quantity', 'price', or 'status'.";
        }

        if (changed) {
            if (dbManager.updateBracelet(braceletToUpdate)) { // Update in database
                return String.format("%s Updated bracelet: %s", message, braceletToUpdate);
            } else {
                return String.format("Error: Failed to update bracelet %s in database.", itemId);
            }
        } else {
            return message; // Return "No change" message if no update occurred
        }
    }

    /**
     * Generates a report listing all bracelets whose quantity falls below
     * a user-specified threshold, fetching data from the database.
     * @param thresholdStr The string representation of the threshold quantity.
     * @return A list of Bracelet objects that are below the threshold, or an empty list if none.
     * If the threshold input is invalid, returns null.
     */
    public List<Bracelet> generateLowStockReport(String thresholdStr) {
        int threshold = validateQuantity(thresholdStr);
        if (threshold == -1) {
            return null; // Indicate invalid threshold
        }

        List<Bracelet> allBracelets = dbManager.selectAllBracelets(); // Get all from database
        List<Bracelet> lowStockItems = new ArrayList<>();
        for (Bracelet bracelet : allBracelets) {
            if (bracelet.getQuantity() < threshold) {
                lowStockItems.add(bracelet);
            }
        }
        // Sort low stock items by quantity for better readability
        Collections.sort(lowStockItems, (b1, b2) -> Integer.compare(b1.getQuantity(), b2.getQuantity()));
        return lowStockItems;
    }
}
