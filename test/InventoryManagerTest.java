import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File; // Keep for FileChooser usage in GUI, though not directly used here
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException; // Keep if other tests use it, not directly needed here

import static org.junit.jupiter.api.Assertions.*;

/**
 * InventoryManagerTest.java
 *
 * Unit tests for the InventoryManager class using JUnit 5.
 * These tests verify the core functionalities including:
 * - File loading (positive and negative cases)
 * - Adding bracelets (positive and negative cases, including uniqueness)
 * - Removing bracelets (positive and negative cases)
 * - Updating bracelet attributes (quantity, price, status) with validation
 * - Generating low stock reports
 *
 * This version is updated to reflect the InventoryManager's methods
 * now accepting direct string inputs instead of Scanner,
 * and returning messages for GUI integration.
 */
class InventoryManagerTest {

    private InventoryManager manager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // JUnit 5 @TempDir annotation provides a temporary directory for tests
    @TempDir
    Path tempDir;

    /**
     * Sets up the test environment before each test method runs.
     * Initializes a new InventoryManager and redirects System.out to capture console output.
     */
    @BeforeEach
    void setUp() {
        manager = new InventoryManager();
        // Redirect System.out to capture console output for validation messages
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Restores original System.out after each test.
     * This ensures that console output behaves normally for other parts of the application
     * or for subsequent tests that might not redirect System.out.
     */
    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    // --- Test Cases ---

    @Test
    @DisplayName("Test 1: File can be opened - Affirmative (Valid File)")
    void testReadFile_Positive() throws IOException {
        // Create a temporary file with valid data
        String fileContent = "001,Test Bracelet 1,10,19.99,In Stock\n002,Test Bracelet 2,5,9.50,Out of Stock";
        Path testFilePath = tempDir.resolve("valid_data.txt");
        Files.writeString(testFilePath, fileContent);

        // Call the method under test
        String result = manager.readDataFromFile(testFilePath.toString());

        // Assertions
        assertEquals(2, manager.getInventory().size(), "Inventory should contain 2 bracelets after reading valid file.");
        assertTrue(result.contains("Successfully loaded 2 bracelets from"), "Success message should be returned.");
        // outContent is still useful for checking warnings from readDataFromFile, if any
        assertFalse(outContent.toString().contains("Warning"), "No warnings expected for valid file.");
    }

    @Test
    @DisplayName("Test 1: File can be opened - Negative (Non-existent File)")
    void testReadFile_Negative_NonExistentFile() {
        // Try to read from a non-existent file path
        String nonExistentFilePath = tempDir.resolve("non_existent_file.txt").toString();
        String result = manager.readDataFromFile(nonExistentFilePath);

        // Assertions
        assertTrue(manager.getInventory().isEmpty(), "Inventory should remain empty for non-existent file.");
        assertTrue(result.contains("Error: File not found at"), "Error message for file not found should be returned.");
    }

    @Test
    @DisplayName("Test 1: File can be opened - Negative (Malformed Data in File)")
    void testReadFile_Negative_MalformedData() throws IOException {
        // Create a temporary file with malformed data (e.g., missing parts, invalid quantity)
        String fileContent = "001,Malformed Bracelet,abc,10.00,In Stock\n003,Another Bracelet,10"; // Invalid quantity, missing parts
        Path testFilePath = tempDir.resolve("malformed_data.txt");
        Files.writeString(testFilePath, fileContent);

        String result = manager.readDataFromFile(testFilePath.toString());

        // Assertions
        assertTrue(manager.getInventory().isEmpty(), "Inventory should be empty if all lines are malformed or invalid.");
        assertTrue(result.contains("No valid bracelets found or loaded from"), "No valid bracelets message should be returned.");
        assertTrue(result.contains("Warning: Invalid quantity"), "Warning for invalid quantity should be returned in message.");
        assertTrue(result.contains("Warning: Skipping malformed line"), "Warning for malformed line should be returned in message.");
    }

    @Test
    @DisplayName("Test 2: Objects can be added - Affirmative (Valid New Bracelet)")
    void testAddBracelet_Positive() {
        String id = "001";
        String description = "New Bracelet";
        String quantityStr = "10";
        String priceStr = "25.50";

        String result = manager.addBracelet(id, description, quantityStr, priceStr);

        assertEquals(1, manager.getInventory().size(), "Inventory should contain one bracelet after adding.");
        Bracelet addedBracelet = manager.getInventory().get(0);
        assertAll("New Bracelet Attributes",
                () -> assertEquals(id, addedBracelet.getId()),
                () -> assertEquals(description, addedBracelet.getDescription()),
                () -> assertEquals(Integer.parseInt(quantityStr), addedBracelet.getQuantity()),
                () -> assertEquals(Double.parseDouble(priceStr), addedBracelet.getPrice(), 0.001),
                () -> assertEquals("In Stock", addedBracelet.getStatus())
        );
        assertTrue(result.contains("Successfully added:"), "Success message should be returned.");
    }

    @Test
    @DisplayName("Test 2: Objects can be added - Negative (Duplicate ID)")
    void testAddBracelet_Negative_DuplicateID() {
        // First add a bracelet successfully
        manager.addBracelet("001", "Existing Bracelet", "5", "10.00");

        // Try to add another with the same ID
        String result = manager.addBracelet("001", "Duplicate Bracelet", "2", "5.00");

        assertEquals(1, manager.getInventory().size(), "Inventory size should not change for duplicate ID.");
        assertTrue(result.contains("Error: A bracelet with this ID already exists."), "Error message for duplicate ID should be returned.");
    }

    @Test
    @DisplayName("Test 2: Objects can be added - Negative (Invalid Quantity)")
    void testAddBracelet_Negative_InvalidQuantity() {
        // Test with negative quantity
        String resultNegative = manager.addBracelet("001", "Invalid Qty Bracelet", "-5", "20.00");
        assertTrue(resultNegative.contains("Error: Quantity must be a valid non-negative integer."), "Error for negative quantity should be returned.");
        assertTrue(manager.getInventory().isEmpty(), "Inventory should be empty after invalid add attempt.");

        // Test with non-integer quantity
        String resultNonInteger = manager.addBracelet("002", "Invalid Str Qty Bracelet", "abc", "30.00");
        assertTrue(resultNonInteger.contains("Error: Quantity must be a valid non-negative integer."), "Error for non-integer quantity should be returned.");
        assertTrue(manager.getInventory().isEmpty(), "Inventory should be empty after invalid add attempt.");
    }

    @Test
    @DisplayName("Test 3: Object can be removed - Affirmative (Valid ID)")
    void testRemoveBracelet_Positive() {
        Bracelet b1 = new Bracelet("001", "Test Remove", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);
        manager.getInventory().add(new Bracelet("002", "Another", 2, 5.00, "In Stock"));

        String result = manager.removeBracelet("001");

        assertEquals(1, manager.getInventory().size(), "Inventory size should decrease by 1.");
        assertFalse(manager.getInventory().contains(b1), "Bracelet 001 should be removed.");
        assertTrue(result.contains("Successfully removed:"), "Success message should be returned.");
    }

    @Test
    @DisplayName("Test 3: Object can be removed - Negative (Non-existent ID)")
    void testRemoveBracelet_Negative_NonExistentID() {
        manager.getInventory().add(new Bracelet("002", "Existing", 2, 5.00, "In Stock"));

        String result = manager.removeBracelet("999"); // Non-existent ID

        assertEquals(1, manager.getInventory().size(), "Inventory size should not change for non-existent ID.");
        assertTrue(result.contains("Error: Bracelet with ID '999' not found in inventory."), "Error message for non-existent ID should be returned.");
    }

    @Test
    @DisplayName("Test 3: Object can be removed - Negative (Empty ID Input)")
    void testRemoveBracelet_Negative_EmptyID() {
        manager.getInventory().add(new Bracelet("002", "Existing", 2, 5.00, "In Stock"));

        String result = manager.removeBracelet(""); // Empty ID input

        assertEquals(1, manager.getInventory().size(), "Inventory size should not change for empty ID.");
        assertTrue(result.contains("Error: Bracelet ID cannot be empty."), "Error message for empty ID should be returned.");
    }


    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Quantity)")
    void testUpdateBracelet_Positive_Quantity() {
        Bracelet b1 = new Bracelet("001", "Update Qty", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        String result = manager.updateBracelet("001", "quantity", "15");

        assertEquals(15, b1.getQuantity(), "Quantity should be updated to 15.");
        assertEquals("In Stock", b1.getStatus(), "Status should remain 'In Stock'.");
        assertTrue(result.contains("Quantity updated."), "Success message should be returned.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Quantity to 0, status auto change)")
    void testUpdateBracelet_Positive_QuantityToZeroStatusChange() {
        Bracelet b1 = new Bracelet("001", "Update Qty", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        String result = manager.updateBracelet("001", "quantity", "0"); // New quantity 0

        assertEquals(0, b1.getQuantity(), "Quantity should be updated to 0.");
        assertEquals("Out of Stock", b1.getStatus(), "Status should change to 'Out of Stock'.");
        assertTrue(result.contains("Status automatically updated to 'Out of Stock'"), "Auto status update message should be returned.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Quantity from 0, status auto change)")
    void testUpdateBracelet_Positive_QuantityFromZeroStatusChange() {
        Bracelet b1 = new Bracelet("001", "Update Qty", 0, 10.00, "Out of Stock");
        manager.getInventory().add(b1);

        String result = manager.updateBracelet("001", "quantity", "5"); // New quantity 5

        assertEquals(5, b1.getQuantity(), "Quantity should be updated to 5.");
        assertEquals("In Stock", b1.getStatus(), "Status should change to 'In Stock'.");
        assertTrue(result.contains("Status automatically updated to 'In Stock'"), "Auto status update message should be returned.");
    }


    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Price)")
    void testUpdateBracelet_Positive_Price() {
        Bracelet b1 = new Bracelet("001", "Update Price", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        String result = manager.updateBracelet("001", "price", "12.75");

        assertEquals(12.75, b1.getPrice(), 0.001, "Price should be updated to 12.75.");
        assertTrue(result.contains("Price updated."), "Success message should be returned.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Status)")
    void testUpdateBracelet_Positive_Status() {
        Bracelet b1 = new Bracelet("001", "Update Status", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        String result = manager.updateBracelet("001", "status", "Out of Stock");

        assertEquals("Out of Stock", b1.getStatus(), "Status should be updated to 'Out of Stock'.");
        assertTrue(result.contains("Status updated."), "Success message should be returned.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Non-existent ID)")
    void testUpdateBracelet_Negative_NonExistentID() {
        manager.getInventory().add(new Bracelet("001", "Existing", 5, 10.00, "In Stock"));

        String result = manager.updateBracelet("999", "quantity", "10"); // Non-existent ID

        assertTrue(result.contains("Error: Bracelet with ID '999' not found in inventory."), "Error message for non-existent ID should be returned.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Invalid Quantity Input)")
    void testUpdateBracelet_Negative_InvalidQuantityInput() {
        Bracelet b1 = new Bracelet("001", "Update Bad Qty", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Test with non-integer quantity
        String resultNonInteger = manager.updateBracelet("001", "quantity", "abc");
        assertTrue(resultNonInteger.contains("Error: New quantity must be a valid non-negative integer."), "Error for non-integer should be returned.");
        assertEquals(5, b1.getQuantity(), "Quantity should not change after invalid input.");

        // Test with negative quantity
        String resultNegative = manager.updateBracelet("001", "quantity", "-5");
        assertTrue(resultNegative.contains("Error: New quantity must be a valid non-negative integer."), "Error for negative quantity should be returned.");
        assertEquals(5, b1.getQuantity(), "Quantity should not change after invalid input.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Invalid Price Input)")
    void testUpdateBracelet_Negative_InvalidPriceInput() {
        Bracelet b1 = new Bracelet("001", "Update Bad Price", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Test with non-numeric price
        String resultNonNumeric = manager.updateBracelet("001", "price", "xyz");
        assertTrue(resultNonNumeric.contains("Error: New price must be a valid non-negative number."), "Error for non-numeric should be returned.");
        assertEquals(10.00, b1.getPrice(), 0.001, "Price should not change after invalid input.");

        // Test with negative price
        String resultNegative = manager.updateBracelet("001", "price", "-10.00");
        assertTrue(resultNegative.contains("Error: New price must be a valid non-negative number."), "Error for negative price should be returned.");
        assertEquals(10.00, b1.getPrice(), 0.001, "Price should not change after invalid input.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Invalid Status Input)")
    void testUpdateBracelet_Negative_InvalidStatusInput() {
        Bracelet b1 = new Bracelet("001", "Update Bad Status", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        String resultInvalid = manager.updateBracelet("001", "status", "WrongStatus");
        assertTrue(resultInvalid.contains("Error: New status must be 'In Stock' or 'Out of Stock'."), "Error for invalid status should be returned.");
        assertEquals("In Stock", b1.getStatus(), "Status should not change after invalid input.");
    }

    @Test
    @DisplayName("Test 5: Custom Action (Low Stock Report) - Affirmative (Items Below Threshold)")
    void testGenerateLowStockReport_Positive() {
        manager.getInventory().add(new Bracelet("001", "Low Stock A", 2, 10.00, "In Stock"));
        manager.getInventory().add(new Bracelet("002", "Normal Stock B", 10, 15.00, "In Stock"));
        manager.getInventory().add(new Bracelet("003", "Low Stock C", 1, 5.00, "Out of Stock"));
        manager.getInventory().add(new Bracelet("004", "Normal Stock D", 7, 20.00, "In Stock"));

        List<Bracelet> lowStockItems = manager.generateLowStockReport("5"); // Threshold of 5

        assertNotNull(lowStockItems, "Low stock items list should not be null.");
        assertEquals(2, lowStockItems.size(), "Should find 2 items below threshold.");
        assertEquals("003", lowStockItems.get(0).getId(), "Bracelet 003 (qty 1) should be first.");
        assertEquals("001", lowStockItems.get(1).getId(), "Bracelet 001 (qty 2) should be second.");
    }

    @Test
    @DisplayName("Test 5: Custom Action (Low Stock Report) - Negative (No Items Below Threshold)")
    void testGenerateLowStockReport_Negative_NoItems() {
        manager.getInventory().add(new Bracelet("001", "High Stock", 10, 10.00, "In Stock"));
        manager.getInventory().add(new Bracelet("002", "Medium Stock", 8, 15.00, "In Stock"));

        List<Bracelet> lowStockItems = manager.generateLowStockReport("5"); // Threshold of 5

        assertNotNull(lowStockItems, "Low stock items list should not be null.");
        assertTrue(lowStockItems.isEmpty(), "No items should be below threshold.");
    }

    @Test
    @DisplayName("Test 5: Custom Action (Low Stock Report) - Negative (Invalid Threshold Input)")
    void testGenerateLowStockReport_Negative_InvalidThreshold() {
        manager.getInventory().add(new Bracelet("001", "Low Stock Item", 2, 10.00, "In Stock"));

        // Test with non-integer threshold
        List<Bracelet> resultInvalidString = manager.generateLowStockReport("abc");
        assertNull(resultInvalidString, "Should return null for invalid string threshold.");

        // Test with negative threshold
        List<Bracelet> resultNegative = manager.generateLowStockReport("-2");
        assertNull(resultNegative, "Should return null for negative threshold.");
    }
}
