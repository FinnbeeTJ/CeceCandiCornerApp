import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

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
 * It uses a temporary directory for file tests and captures system output
 * to verify print statements for validation errors and success messages.
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

    // --- Helper Methods for simulating user input ---
    /**
     * Creates a Scanner that reads from a given string, simulating user input.
     * @param input The string to simulate as input.
     * @return A Scanner instance.
     */
    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
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
        manager.readDataFromFile(testFilePath.toString());

        // Assertions
        assertEquals(2, manager.getInventory().size(), "Inventory should contain 2 bracelets after reading valid file.");
        assertTrue(outContent.toString().contains("Successfully loaded 2 bracelets from"), "Success message should be printed.");
    }

    @Test
    @DisplayName("Test 1: File can be opened - Negative (Non-existent File)")
    void testReadFile_Negative_NonExistentFile() {
        // Try to read from a non-existent file path
        String nonExistentFilePath = tempDir.resolve("non_existent_file.txt").toString();
        manager.readDataFromFile(nonExistentFilePath);

        // Assertions
        assertTrue(manager.getInventory().isEmpty(), "Inventory should remain empty for non-existent file.");
        assertTrue(outContent.toString().contains("Error: File not found at"), "Error message for file not found should be printed.");
    }

    @Test
    @DisplayName("Test 1: File can be opened - Negative (Malformed Data in File)")
    void testReadFile_Negative_MalformedData() throws IOException {
        // Create a temporary file with malformed data (e.g., missing parts, invalid quantity)
        String fileContent = "001,Malformed Bracelet,abc,10.00,In Stock\n003,Another Bracelet,10"; // Invalid quantity, missing parts
        Path testFilePath = tempDir.resolve("malformed_data.txt");
        Files.writeString(testFilePath, fileContent);

        manager.readDataFromFile(testFilePath.toString());

        // Assertions
        assertTrue(manager.getInventory().isEmpty(), "Inventory should be empty if all lines are malformed or invalid.");
        assertTrue(outContent.toString().contains("Warning: Invalid quantity"), "Warning for invalid quantity should be printed.");
        assertTrue(outContent.toString().contains("Warning: Skipping malformed line"), "Warning for malformed line should be printed.");
    }

    @Test
    @DisplayName("Test 2: Objects can be added - Affirmative (Valid New Bracelet)")
    void testAddBracelet_Positive() {
        String input = "001\nNew Bracelet\n10\n25.50\n";
        Scanner scanner = createScanner(input);
        manager.addBracelet(scanner);

        assertEquals(1, manager.getInventory().size(), "Inventory should contain one bracelet after adding.");
        Bracelet addedBracelet = manager.getInventory().get(0);
        assertAll("New Bracelet Attributes",
                () -> assertEquals("001", addedBracelet.getId()),
                () -> assertEquals("New Bracelet", addedBracelet.getDescription()),
                () -> assertEquals(10, addedBracelet.getQuantity()),
                () -> assertEquals(25.50, addedBracelet.getPrice()),
                () -> assertEquals("In Stock", addedBracelet.getStatus())
        );
        assertTrue(outContent.toString().contains("Successfully added:"), "Success message should be printed.");
    }

    @Test
    @DisplayName("Test 2: Objects can be added - Negative (Duplicate ID)")
    void testAddBracelet_Negative_DuplicateID() {
        // First add a bracelet
        manager.getInventory().add(new Bracelet("001", "Existing Bracelet", 5, 10.00, "In Stock"));

        // Try to add another with the same ID, then provide valid details for a NEW ID
        // The first ID will be rejected, and the method will re-prompt for ID, then other details.
        String input = "001\n002\nDuplicate Bracelet\n2\n5.00\n"; // Duplicate ID, then new ID and valid details
        Scanner scanner = createScanner(input);
        manager.addBracelet(scanner);

        assertEquals(2, manager.getInventory().size(), "Inventory size should increase by 1 after adding a new valid bracelet.");
        assertTrue(manager.getInventory().stream().anyMatch(b -> b.getId().equals("002")), "Bracelet with new ID '002' should be added.");
        assertTrue(outContent.toString().contains("Error: A bracelet with this ID already exists."), "Error message for duplicate ID should be printed.");
        assertTrue(outContent.toString().contains("Successfully added:"), "Success message for the newly added bracelet should be printed.");
    }

    @Test
    @DisplayName("Test 2: Objects can be added - Negative (Invalid Quantity)")
    void testAddBracelet_Negative_InvalidQuantity() {
        // Simulate input with an invalid quantity, followed by a valid one.
        // The method will loop and re-prompt for quantity.
        String input = "001\nInvalid Qty Bracelet\n-5\n10\n20.00\n"; // Invalid -5, then valid 10
        Scanner scanner = createScanner(input);
        manager.addBracelet(scanner);

        // Assertions: Should try to add with -5, fail, prompt again, then add with 10.
        assertEquals(1, manager.getInventory().size(), "Inventory should add valid bracelet after retry.");
        assertTrue(outContent.toString().contains("Error: Quantity cannot be negative."), "Error for negative quantity should be printed.");
        assertTrue(outContent.toString().contains("Successfully added:"), "Success message should eventually be printed.");
        assertEquals(10, manager.getInventory().get(0).getQuantity(), "Quantity should be the valid one after retry.");

        // New scenario for invalid string quantity (resets manager and output for isolated test)
        manager = new InventoryManager();
        outContent.reset(); // Clear previous output
        input = "002\nInvalid Str Qty Bracelet\nabc\n1\n30.00\n"; // Invalid 'abc', then valid 1
        scanner = createScanner(input);
        manager.addBracelet(scanner);

        assertEquals(1, manager.getInventory().size(), "Inventory should add valid bracelet after retry with string.");
        assertTrue(outContent.toString().contains("Error: Quantity must be a valid integer."), "Error for non-integer quantity should be printed.");
        assertEquals(1, manager.getInventory().get(0).getQuantity(), "Quantity should be the valid one after retry with string.");
    }

    @Test
    @DisplayName("Test 3: Object can be removed - Affirmative (Valid ID)")
    void testRemoveBracelet_Positive() {
        Bracelet b1 = new Bracelet("001", "Test Remove", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);
        manager.getInventory().add(new Bracelet("002", "Another", 2, 5.00, "In Stock"));

        String input = "001\n";
        Scanner scanner = createScanner(input);
        manager.removeBracelet(scanner);

        assertEquals(1, manager.getInventory().size(), "Inventory size should decrease by 1.");
        assertFalse(manager.getInventory().contains(b1), "Bracelet 001 should be removed.");
        assertTrue(outContent.toString().contains("Successfully removed:"), "Success message should be printed.");
    }

    @Test
    @DisplayName("Test 3: Object can be removed - Negative (Non-existent ID)")
    void testRemoveBracelet_Negative_NonExistentID() {
        manager.getInventory().add(new Bracelet("002", "Existing", 2, 5.00, "In Stock"));

        String input = "999\n"; // Non-existent ID
        Scanner scanner = createScanner(input);
        manager.removeBracelet(scanner);

        assertEquals(1, manager.getInventory().size(), "Inventory size should not change for non-existent ID.");
        assertTrue(outContent.toString().contains("Error: Bracelet with ID '999' not found in inventory."), "Error message for non-existent ID should be printed.");
    }

    @Test
    @DisplayName("Test 3: Object can be removed - Negative (Empty ID Input)")
    void testRemoveBracelet_Negative_EmptyID() {
        manager.getInventory().add(new Bracelet("002", "Existing", 2, 5.00, "In Stock"));

        String input = "\n"; // Empty ID input, followed by Enter
        Scanner scanner = createScanner(input);

        // Ensure that trying to call nextLine on a closed or exhausted scanner doesn't happen
        // Mocking System.in and Scanner behavior is tricky for multiple prompts.
        // For this specific case, validateId handles it before Scanner tries to read more.
        manager.removeBracelet(scanner);

        assertEquals(1, manager.getInventory().size(), "Inventory size should not change for empty ID.");
        assertTrue(outContent.toString().contains("Error: ID cannot be empty."), "Error message for empty ID should be printed.");
    }


    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Quantity)")
    void testUpdateBracelet_Positive_Quantity() {
        Bracelet b1 = new Bracelet("001", "Update Qty", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Simulate input: ID, then choose 1 (Quantity), then new quantity
        String input = "001\n1\n15\n";
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals(15, b1.getQuantity(), "Quantity should be updated to 15.");
        assertEquals("In Stock", b1.getStatus(), "Status should remain 'In Stock'.");
        assertTrue(outContent.toString().contains("Quantity updated."), "Success message should be printed.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Quantity to 0, status auto change)")
    void testUpdateBracelet_Positive_QuantityToZeroStatusChange() {
        Bracelet b1 = new Bracelet("001", "Update Qty", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        String input = "001\n1\n0\n"; // ID, update quantity, new quantity 0
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals(0, b1.getQuantity(), "Quantity should be updated to 0.");
        assertEquals("Out of Stock", b1.getStatus(), "Status should change to 'Out of Stock'.");
        assertTrue(outContent.toString().contains("Status automatically updated to 'Out of Stock'"), "Auto status update message should be printed.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Quantity from 0, status auto change)")
    void testUpdateBracelet_Positive_QuantityFromZeroStatusChange() {
        Bracelet b1 = new Bracelet("001", "Update Qty", 0, 10.00, "Out of Stock");
        manager.getInventory().add(b1);

        String input = "001\n1\n5\n"; // ID, update quantity, new quantity 5
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals(5, b1.getQuantity(), "Quantity should be updated to 5.");
        assertEquals("In Stock", b1.getStatus(), "Status should change to 'In Stock'.");
        assertTrue(outContent.toString().contains("Status automatically updated to 'In Stock'"), "Auto status update message should be printed.");
    }


    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Price)")
    void testUpdateBracelet_Positive_Price() {
        Bracelet b1 = new Bracelet("001", "Update Price", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Simulate input: ID, then choose 2 (Price), then new price
        String input = "001\n2\n12.75\n";
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals(12.75, b1.getPrice(), 0.001, "Price should be updated to 12.75.");
        assertTrue(outContent.toString().contains("Price updated."), "Success message should be printed.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Affirmative (Update Status)")
    void testUpdateBracelet_Positive_Status() {
        Bracelet b1 = new Bracelet("001", "Update Status", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Simulate input: ID, then choose 3 (Status), then new status
        String input = "001\n3\nOut of Stock\n";
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals("Out of Stock", b1.getStatus(), "Status should be updated to 'Out of Stock'.");
        assertTrue(outContent.toString().contains("Status updated."), "Success message should be printed.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Non-existent ID)")
    void testUpdateBracelet_Negative_NonExistentID() {
        manager.getInventory().add(new Bracelet("001", "Existing", 5, 10.00, "In Stock"));

        String input = "999\n"; // Non-existent ID
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertTrue(outContent.toString().contains("Error: Bracelet with ID '999' not found in inventory."), "Error message for non-existent ID should be printed.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Invalid Quantity Input)")
    void testUpdateBracelet_Negative_InvalidQuantityInput() {
        Bracelet b1 = new Bracelet("001", "Update Bad Qty", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Simulate input: ID, then choose 1 (Quantity), then bad quantity, then valid quantity
        String input = "001\n1\nabc\n-5\n12\n"; // Try "abc", then "-5", then valid "12"
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals(12, b1.getQuantity(), "Quantity should be updated to the final valid input.");
        assertTrue(outContent.toString().contains("Error: Quantity must be a valid integer."), "Error for non-integer should be printed.");
        assertTrue(outContent.toString().contains("Error: Quantity cannot be negative."), "Error for negative quantity should be printed.");
        assertTrue(outContent.toString().contains("Quantity updated."), "Success message should be printed eventually.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Invalid Price Input)")
    void testUpdateBracelet_Negative_InvalidPriceInput() {
        Bracelet b1 = new Bracelet("001", "Update Bad Price", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Simulate input: ID, then choose 2 (Price), then bad price, then valid price
        String input = "001\n2\nxyz\n-10.00\n15.50\n"; // Try "xyz", then "-10.00", then valid "15.50"
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals(15.50, b1.getPrice(), 0.001, "Price should be updated to the final valid input.");
        assertTrue(outContent.toString().contains("Error: Price must be a valid number."), "Error for non-numeric should be printed.");
        assertTrue(outContent.toString().contains("Error: Price cannot be negative."), "Error for negative price should be printed.");
        assertTrue(outContent.toString().contains("Price updated."), "Success message should be printed eventually.");
    }

    @Test
    @DisplayName("Test 4: Object can be updated - Negative (Invalid Status Input)")
    void testUpdateBracelet_Negative_InvalidStatusInput() {
        Bracelet b1 = new Bracelet("001", "Update Bad Status", 5, 10.00, "In Stock");
        manager.getInventory().add(b1);

        // Simulate input: ID, then choose 3 (Status), then bad status, then valid status
        String input = "001\n3\nWrongStatus\nAvailable\nIn Stock\n"; // Try "WrongStatus", "Available", then valid "In Stock"
        Scanner scanner = createScanner(input);
        manager.updateBracelet(scanner);

        assertEquals("In Stock", b1.getStatus(), "Status should be updated to the final valid input.");
        assertTrue(outContent.toString().contains("Error: Status must be 'In Stock' or 'Out of Stock'."), "Error for invalid status should be printed.");
        assertTrue(outContent.toString().contains("Status updated."), "Success message should be printed eventually.");
    }

    @Test
    @DisplayName("Test 5: Custom Action (Low Stock Report) - Affirmative (Items Below Threshold)")
    void testGenerateLowStockReport_Positive() {
        manager.getInventory().add(new Bracelet("001", "Low Stock A", 2, 10.00, "In Stock"));
        manager.getInventory().add(new Bracelet("002", "Normal Stock B", 10, 15.00, "In Stock"));
        manager.getInventory().add(new Bracelet("003", "Low Stock C", 1, 5.00, "Out of Stock"));
        manager.getInventory().add(new Bracelet("004", "Normal Stock D", 7, 20.00, "In Stock"));

        String input = "5\n"; // Threshold of 5
        Scanner scanner = createScanner(input);
        manager.generateLowStockReport(scanner);

        String output = outContent.toString();
        assertTrue(output.contains("--- Bracelets Below Stock Threshold (5) ---"), "Report header should be present.");
        assertTrue(output.contains("ID: 003, Description: Low Stock C, Current Quantity: 1"), "Bracelet 003 should be in report.");
        assertTrue(output.contains("ID: 001, Description: Low Stock A, Current Quantity: 2"), "Bracelet 001 should be in report.");
        assertFalse(output.contains("ID: 002"), "Bracelet 002 should NOT be in report.");
        assertFalse(output.contains("ID: 004"), "Bracelet 004 should NOT be in report.");

        // Check order (sorted by quantity)
        int index003 = output.indexOf("ID: 003");
        int index001 = output.indexOf("ID: 001");
        assertTrue(index003 < index001, "Low Stock C (qty 1) should appear before Low Stock A (qty 2).");
    }

    @Test
    @DisplayName("Test 5: Custom Action (Low Stock Report) - Negative (No Items Below Threshold)")
    void testGenerateLowStockReport_Negative_NoItems() {
        manager.getInventory().add(new Bracelet("001", "High Stock", 10, 10.00, "In Stock"));
        manager.getInventory().add(new Bracelet("002", "Medium Stock", 8, 15.00, "In Stock"));

        String input = "5\n"; // Threshold of 5
        Scanner scanner = createScanner(input);
        manager.generateLowStockReport(scanner);

        assertTrue(outContent.toString().contains("No bracelets currently below the specified stock threshold of 5."), "Message for no low stock items should be printed.");
    }

    @Test
    @DisplayName("Test 5: Custom Action (Low Stock Report) - Negative (Invalid Threshold Input)")
    void testGenerateLowStockReport_Negative_InvalidThreshold() {
        // Corrected: Add a bracelet that *will* be below the threshold of 3
        manager.getInventory().add(new Bracelet("001", "Low Stock Item", 2, 10.00, "In Stock"));

        // Simulate input: Invalid string, then negative, then valid 3
        // This input needs to match the re-prompting behavior of validateQuantity
        String input = "abc\n-2\n3\n";
        Scanner scanner = createScanner(input);
        manager.generateLowStockReport(scanner);

        // The assertions should now reflect that a valid report *was* generated after retries
        String output = outContent.toString();
        assertTrue(output.contains("Error: Quantity must be a valid integer."), "Error for non-integer threshold should be printed.");
        assertTrue(output.toString().contains("Error: Quantity cannot be negative."), "Error for negative threshold should be printed.");
        assertTrue(output.toString().contains("--- Bracelets Below Stock Threshold (3) ---"), "Report header should be generated with valid threshold after retries.");
        // Additionally, check that the specific low stock item is in the report
        assertTrue(output.toString().contains("ID: 001, Description: Low Stock Item, Current Quantity: 2"), "The low stock item should be listed in the report.");
        assertEquals(1, manager.getInventory().size(), "Inventory size should remain unchanged.");
    }
}