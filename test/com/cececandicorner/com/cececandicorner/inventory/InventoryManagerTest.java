package com.cececandicorner.inventory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Import all static assertion methods from JUnit
import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager manager;
    private DatabaseManager dbManager;

    @TempDir
    Path tempDir; // JUnit provides a temporary directory for test files

    private Path tempDbFile;

    @BeforeEach // This method runs BEFORE each test
    void setUp() throws IOException {
        // Create a unique temporary database file for each test
        tempDbFile = tempDir.resolve("test_inventory_" + System.nanoTime() + ".db");
        dbManager = new DatabaseManager(tempDbFile.toString());
        manager = new InventoryManager(dbManager);

        // Ensure the table is created
        dbManager.createTable();
    }

    @AfterEach // This method runs AFTER each test
    void tearDown() throws IOException {
        // Clean up the temporary database file
        Files.deleteIfExists(tempDbFile);
    }
    @Test // Marks this as a test method
    @DisplayName("Test: New inventory should be empty on initialization") // Describes the test's purpose
    void inventory_shouldBeEmpty_onInit() {
        // Arrange - The setUp() method has already created a new, empty manager.

        // Act - We get the list of bracelets from the inventory.
        var inventory = manager.getInventory();

        // Assert - We check that the list is not null and is empty.
        assertNotNull(inventory, "Inventory should not be null.");
        assertTrue(inventory.isEmpty(), "Inventory should be empty on initialization.");
    }

    @Test
    @DisplayName("Test: Add a valid new bracelet")
    void shouldAddBraceletSuccessfully() {
        // Arrange: Define the new bracelet's properties.
        String id = "001";
        String description = "New Bracelet";

        // Act: Call the method to add the bracelet.
        manager.addBracelet(id, description, "10", "25.50");

        // Assert: Verify the bracelet was added correctly.
        Bracelet addedBracelet = manager.getBraceletById(id);

        assertEquals(1, manager.getInventory().size(), "Inventory size should be 1.");
        assertNotNull(addedBracelet, "Added bracelet should not be null.");
        assertEquals(description, addedBracelet.getDescription(), "Bracelet description should match.");
    }
    @Test
    @DisplayName("Test: Prevent adding a bracelet with a duplicate ID")
    void shouldNotAddBraceletWithDuplicateId() {
        // Arrange: Add an initial bracelet.
        manager.addBracelet("001", "First Bracelet", "5", "15.00");

        // Act: Try to add another bracelet with the same ID.
        String result = manager.addBracelet("001", "Duplicate Bracelet", "2", "5.00");

        // Assert: Verify that the second bracelet was not added.
        assertEquals(1, manager.getInventory().size(), "Inventory size should remain 1.");
        assertTrue(result.contains("already exists"), "Error message for duplicate ID should be returned.");
    }
    @Test
    @DisplayName("Test: Remove an existing bracelet")
    void shouldRemoveBraceletSuccessfully() {
        // Arrange: Add a bracelet to the inventory.
        String id = "001";
        manager.addBracelet(id, "Bracelet to Remove", "5", "10.00");

        // Act: Call the method to remove the bracelet.
        manager.removeBracelet(id);

        // Assert: Verify the bracelet is no longer in the inventory.
        assertNull(manager.getBraceletById(id), "Bracelet should be null after removal.");
        assertTrue(manager.getInventory().isEmpty(), "Inventory should be empty.");
    }

    @Test
    @DisplayName("Test: Prevent removing a non-existent bracelet")
    void shouldNotRemoveNonExistentBracelet() {
        // Arrange: Ensure inventory has an item, but not the one we'll try to remove.
        manager.addBracelet("001", "Existing Bracelet", "5", "15.00");

        // Act: Try to remove a bracelet with an ID that doesn't exist.
        String result = manager.removeBracelet("999");

        // Assert: Verify the inventory was not changed and an error message was returned.
        assertEquals(1, manager.getInventory().size(), "Inventory size should not change.");
        assertTrue(result.contains("not found"), "Error message for non-existent ID should be returned.");
    }
    @Test
    @DisplayName("Test: Update an existing bracelet's quantity")
    void shouldUpdateBraceletQuantity() {
        // Arrange: Add a bracelet with an initial quantity.
        String id = "001";
        manager.addBracelet(id, "Bracelet to Update", "10", "20.00");

        // Act: Call the method to update the quantity.
        manager.updateBracelet(id, "quantity", "25");

        // Assert: Verify the quantity was updated correctly.
        Bracelet updatedBracelet = manager.getBraceletById(id);
        assertNotNull(updatedBracelet);
        assertEquals(25, updatedBracelet.getQuantity(), "Quantity should be updated to 25.");
    }

    @Test
    @DisplayName("Test: Prevent updating a non-existent bracelet")
    void shouldNotUpdateNonExistentBracelet() {
        // Arrange: The inventory can be empty or have other items.

        // Act: Try to update a bracelet with an ID that doesn't exist.
        String result = manager.updateBracelet("999", "quantity", "10");

        // Assert: Verify an error message was returned.
        assertTrue(result.contains("not found"), "Error message for non-existent ID should be returned.");
    }
    @Test
    @DisplayName("Test: Prevent update with an invalid quantity")
    void shouldNotUpdateWithInvalidQuantity() {
        // Arrange: Add a bracelet.
        String id = "001";
        manager.addBracelet(id, "Test Bracelet", "10", "20.00");

        // Act: Try to update the quantity with a non-numeric value.
        String result = manager.updateBracelet(id, "quantity", "abc");

        // Assert: Verify an error message was returned and the data did not change.
        Bracelet originalBracelet = manager.getBraceletById(id);

        assertTrue(result.contains("must be a valid non-negative integer"), "Error message for invalid quantity should be returned.");
        assertEquals(10, originalBracelet.getQuantity(), "Quantity should not have changed.");
    }
    @Test
    @DisplayName("Test: Generate a low stock report correctly")
    void shouldGenerateLowStockReport() {
        // Arrange: Add several bracelets with various stock levels.
        manager.addBracelet("001", "Low Stock A", "2", "10.00"); // Below threshold
        manager.addBracelet("002", "High Stock B", "20", "15.00");
        manager.addBracelet("003", "Low Stock C", "4", "5.00"); // Below threshold

        // Act: Generate a report for items with quantity less than 5.
        var lowStockItems = manager.generateLowStockReport("5");

        // Assert: Verify the report contains only the correct items.
        assertNotNull(lowStockItems);
        assertEquals(2, lowStockItems.size(), "Report should contain 2 items.");
        assertTrue(lowStockItems.stream().anyMatch(b -> b.getId().equals("001")), "Report should include item 001.");
        assertTrue(lowStockItems.stream().anyMatch(b -> b.getId().equals("003")), "Report should include item 003.");
    }
    @Test
    @DisplayName("Test: Update Bracelet - Affirmative (Update Quantity to 0, status auto change)")
    void shouldUpdateStatusToOutOfStockWhenQuantityIsZero() {
        // Arrange: Add a bracelet that is in stock.
        String id = "001";
        manager.addBracelet(id, "Test Bracelet", "10", "20.00");

        // Act: Update the quantity to 0.
        manager.updateBracelet(id, "quantity", "0");

        // Assert: Verify the quantity and status were automatically updated.
        Bracelet updatedBracelet = manager.getBraceletById(id);
        assertNotNull(updatedBracelet);
        assertEquals(0, updatedBracelet.getQuantity(), "Quantity should be updated to 0.");
        assertEquals("Out of Stock", updatedBracelet.getStatus(), "Status should be 'Out of Stock'.");
    }
    @Test
    @DisplayName("Test: Update Bracelet - Affirmative (Update Quantity from 0, status auto change)")
    void shouldUpdateStatusToInStockWhenQuantityIsPositive() {
        // Arrange: Add a bracelet that is out of stock (quantity 0).
        String id = "001";
        manager.addBracelet(id, "Out of Stock Bracelet", "0", "20.00");

        // Act: Update the quantity to a positive number.
        manager.updateBracelet(id, "quantity", "15");

        // Assert: Verify the quantity and status were automatically updated.
        Bracelet updatedBracelet = manager.getBraceletById(id);
        assertNotNull(updatedBracelet);
        assertEquals(15, updatedBracelet.getQuantity(), "Quantity should be updated to 15.");
        assertEquals("In Stock", updatedBracelet.getStatus(), "Status should be 'In Stock'.");
    }

}