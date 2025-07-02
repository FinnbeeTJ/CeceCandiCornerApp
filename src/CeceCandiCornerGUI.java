/**
 * CeceCandiCornerGUI.java
 * This version has been updated to include JavaFX GUI capatibility (FKA CeceCandiCornerApp.java)
 * This is the main JavaFX application class for Cece's Candi Corner Inventory Management System.
 * It provides a graphical user interface for the user to interact with the InventoryManager.
 * This class handles all UI elements, user input, and displays results,
 * interacting with the InventoryManager for business logic.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Base64; // Import for Base64 encoding

public class CeceCandiCornerGUI extends Application {

    private InventoryManager inventoryManager;
    private TableView<Bracelet> inventoryTable;
    private TextArea messageArea; // For displaying general messages and reports
    private ObservableList<Bracelet> braceletData; // The ObservableList backing the TableView

    @Override
    public void start(Stage primaryStage) {
        inventoryManager = new InventoryManager();
        // Initialize the ObservableList that will hold the TableView's data
        braceletData = FXCollections.observableArrayList();

        primaryStage.setTitle("Cece's Candi Corner Inventory Management System");

        // --- UI Elements ---
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setPrefHeight(100); // Set a preferred height for the message area

        // Inventory Table
        inventoryTable = new TableView<>();
        inventoryTable.setPlaceholder(new Label("No bracelets to display. Load data or add new items."));
        inventoryTable.setItems(braceletData); // Link the TableView to the ObservableList

        // --- IMPORTANT: Using property() methods for PropertyValueFactory ---
        // This ensures the TableView observes the observable properties in Bracelet.java
        TableColumn<Bracelet, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id")); // Looks for idProperty()
        idCol.setPrefWidth(80);

        TableColumn<Bracelet, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description")); // Looks for descriptionProperty()
        descCol.setPrefWidth(180);

        TableColumn<Bracelet, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity")); // Looks for quantityProperty()
        quantityCol.setPrefWidth(90);

        TableColumn<Bracelet, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price")); // Looks for priceProperty()
        priceCol.setPrefWidth(90);

        TableColumn<Bracelet, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status")); // Looks for statusProperty()
        statusCol.setPrefWidth(100);
        // --- END IMPORTANT CHANGE ---

        inventoryTable.getColumns().addAll(idCol, descCol, quantityCol, priceCol, statusCol);
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Make columns fill width

        // --- Buttons for Main Operations ---
        Button loadDataButton = new Button("Load Data from File");
        loadDataButton.setMaxWidth(Double.MAX_VALUE); // Make button fill width
        loadDataButton.setOnAction(e -> handleLoadData());

        Button displayAllButton = new Button("Display All Bracelets");
        displayAllButton.setMaxWidth(Double.MAX_VALUE);
        displayAllButton.setOnAction(e -> handleDisplayAll());

        Button addBraceletButton = new Button("Add New Bracelet");
        addBraceletButton.setMaxWidth(Double.MAX_VALUE);
        addBraceletButton.setOnAction(e -> showAddBraceletDialog());

        Button removeBraceletButton = new Button("Remove Bracelet");
        removeBraceletButton.setMaxWidth(Double.MAX_VALUE);
        removeBraceletButton.setOnAction(e -> showRemoveBraceletDialog());

        Button updateBraceletButton = new Button("Update Bracelet");
        updateBraceletButton.setMaxWidth(Double.MAX_VALUE);
        updateBraceletButton.setOnAction(e -> showUpdateBraceletDialog());

        Button lowStockReportButton = new Button("Generate Low Stock Report");
        lowStockReportButton.setMaxWidth(Double.MAX_VALUE);
        lowStockReportButton.setOnAction(e -> showLowStockReportDialog());

        Button exitButton = new Button("Exit");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setOnAction(e -> Platform.exit()); // Exit the application

        VBox buttonLayout = new VBox(10, loadDataButton, displayAllButton, addBraceletButton,
                removeBraceletButton, updateBraceletButton, lowStockReportButton, exitButton);
        buttonLayout.setPadding(new Insets(10));
        buttonLayout.setAlignment(Pos.TOP_CENTER);

        // --- Main Layout ---
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setLeft(buttonLayout); // Buttons on the left
        root.setCenter(inventoryTable); // Table in the center
        root.setBottom(messageArea); // Message area at the bottom

        // Initial display of inventory (empty)
        updateInventoryTable();

        Scene scene = new Scene(root, 900, 600); // Increased width for better layout

        // --- Apply Dark Mode CSS ---
        String darkModeCss =
                ".root {" +
                        "    -fx-background-color: #2b2b2b;" + /* Dark background */
                        "}" +
                        ".button {" +
                        "    -fx-background-color: #4a4a4a;" +
                        "    -fx-text-fill: #e0e0e0;" +
                        "    -fx-background-radius: 5;" +
                        "    -fx-border-radius: 5;" +
                        "    -fx-border-color: #6a6a6a;" +
                        "}" +
                        ".button:hover {" +
                        "    -fx-background-color: #5a5a5a;" +
                        "}" +
                        ".label {" +
                        "    -fx-text-fill: #e0e0e0;" + /* Light text for labels */
                        "}" +
                        ".text-area {" +
                        "    -fx-control-inner-background: #3c3c3c;" + /* Darker background for text area */
                        "    -fx-text-fill: #e0e0e0;" +
                        "    -fx-highlight-fill: #007bff;" +
                        "    -fx-highlight-text-fill: #ffffff;" +
                        "}" +
                        ".table-view {" +
                        "    -fx-base: #3c3c3c;" + /* Base color for table */
                        "    -fx-control-inner-background: #3c3c3c;" +
                        "    -fx-background-color: #3c3c3c;" +
                        "    -fx-table-cell-border-color: #5a5a5a;" +
                        "    -fx-text-fill: #e0e0e0;" +
                        "}" +
                        ".table-view .column-header-background {" +
                        "    -fx-background-color: #4a4a4a;" +
                        "}" +
                        ".table-view .column-header .label {" +
                        "    -fx-text-fill: #ffffff;" +
                        "}" +
                        ".table-row-cell {" +
                        "    -fx-background-color: -fx-table-cell-border-color, #3c3c3c;" +
                        "    -fx-background-insets: 0, 0 0 1 0;" +
                        "    -fx-padding: 0;" +
                        "}" +
                        ".table-row-cell:odd {" +
                        "    -fx-background-color: -fx-table-cell-border-color, #424242;" +
                        "}" +
                        ".table-row-cell:selected {" +
                        "    -fx-background-color: #007bff;" +
                        "    -fx-text-fill: #ffffff;" +
                        "}" +
                        ".text-field {" +
                        "    -fx-control-inner-background: #3c3c3c;" +
                        "    -fx-text-fill: #e0e0e0;" +
                        "    -fx-prompt-text-fill: #a0a0a0;" +
                        "    -fx-background-radius: 5;" +
                        "    -fx-border-radius: 5;" +
                        "    -fx-border-color: #6a6a6a;" +
                        "}" +
                        ".combo-box {" +
                        "    -fx-background-color: #3c3c3c;" +
                        "    -fx-text-fill: #e0e0e0;" +
                        "    -fx-background-radius: 5;" +
                        "    -fx-border-radius: 5;" +
                        "    -fx-border-color: #6a6a6a;" +
                        "}" +
                        ".combo-box .list-cell {" +
                        "    -fx-background-color: #3c3c3c;" +
                        "    -fx-text-fill: #e0e0e0;" +
                        "}" +
                        ".combo-box .list-cell:filled:selected, .combo-box .list-cell:filled:selected:hover {" +
                        "    -fx-background-color: #007bff;" +
                        "    -fx-text-fill: #ffffff;" +
                        "}" +
                        ".combo-box .list-cell:filled:hover {" +
                        "    -fx-background-color: #5a5a5a;" +
                        "}";

        scene.getStylesheets().add("data:text/css;base64," + Base64.getEncoder().encodeToString(darkModeCss.getBytes()));
        // --- End Apply Dark Mode CSS ---

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Handles loading data from a file chosen by the user.
     */
    private void handleLoadData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Data File");
        File file = fileChooser.showOpenDialog(inventoryTable.getScene().getWindow());

        if (file != null) {
            String result = inventoryManager.readDataFromFile(file.getAbsolutePath());
            showMessage(result);
            updateInventoryTable(); // Refresh table after loading data
        } else {
            showMessage("File selection cancelled.");
        }
    }

    /**
     * Handles displaying all bracelets currently in the inventory.
     */
    private void handleDisplayAll() {
        updateInventoryTable();
        if (inventoryManager.getInventory().isEmpty()) {
            showMessage("Inventory is empty. No data to display.");
        } else {
            showMessage("Displaying all bracelets in inventory.");
        }
    }

    /**
     * Updates the TableView with the current inventory data from InventoryManager.
     * This method now uses setAll() to replace all elements in the ObservableList,
     * which can be more efficient and reliable for triggering UI updates.
     */
    private void updateInventoryTable() {
        braceletData.setAll(inventoryManager.getInventory()); // Use setAll to replace all elements
        inventoryTable.refresh(); // Explicitly tell the table to refresh its view
    }

    /**
     * Displays a message to the user in the message area.
     * @param message The message to display.
     */
    private void showMessage(String message) {
        messageArea.setText(message);
    }

    /**
     * Shows a dialog for adding a new bracelet.
     */
    private void showAddBraceletDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Bracelet");
        dialog.setHeaderText("Enter details for the new bracelet:");

        // Set the button types.
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the ID field by default.
        Platform.runLater(() -> idField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String id = idField.getText().trim();
                String description = descriptionField.getText().trim();
                String quantityStr = quantityField.getText().trim();
                String priceStr = priceField.getText().trim();

                String result = inventoryManager.addBracelet(id, description, quantityStr, priceStr);
                showMessage(result);
                updateInventoryTable(); // Refresh table
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Shows a dialog for removing an existing bracelet.
     */
    private void showRemoveBraceletDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Bracelet");
        dialog.setHeaderText("Enter the ID of the bracelet to remove:");
        dialog.setContentText("Bracelet ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(itemId -> {
            String message = inventoryManager.removeBracelet(itemId.trim());
            showMessage(message);
            updateInventoryTable(); // Refresh table
        });
    }

    /**
     * Shows a dialog for updating an existing bracelet.
     */
    private void showUpdateBraceletDialog() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Update Bracelet");
        idDialog.setHeaderText("Enter the ID of the bracelet to update:");
        idDialog.setContentText("Bracelet ID:");

        Optional<String> idResult = idDialog.showAndWait();
        idResult.ifPresent(itemId -> {
            String id = itemId.trim();
            Bracelet braceletToUpdate = inventoryManager.getBraceletById(id);

            if (braceletToUpdate == null) {
                showMessage(String.format("Error: Bracelet with ID '%s' not found.", id));
                return;
            }

            // If bracelet found, show update options
            Dialog<ButtonType> updateDialog = new Dialog<>();
            updateDialog.setTitle("Update Bracelet Details");
            updateDialog.setHeaderText(String.format("Updating Bracelet: %s\nCurrent Details: %s\nSelect field(s) to update:", braceletToUpdate.getDescription(), braceletToUpdate.toString()));

            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            updateDialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            Label currentQuantityLabel = new Label("Current Quantity: " + braceletToUpdate.getQuantity());
            TextField newQuantityField = new TextField();
            newQuantityField.setPromptText("New Quantity");

            Label currentPriceLabel = new Label("Current Price: $" + String.format("%.2f", braceletToUpdate.getPrice()));
            TextField newPriceField = new TextField();
            newPriceField.setPromptText("New Price");

            Label currentStatusLabel = new Label("Current Status: " + braceletToUpdate.getStatus());
            ComboBox<String> newStatusComboBox = new ComboBox<>(FXCollections.observableArrayList("In Stock", "Out of Stock"));
            newStatusComboBox.setValue(braceletToUpdate.getStatus()); // Set current status as default

            grid.add(new Label("Quantity:"), 0, 0);
            grid.add(currentQuantityLabel, 1, 0);
            grid.add(newQuantityField, 2, 0);

            grid.add(new Label("Price:"), 0, 1);
            grid.add(currentPriceLabel, 1, 1);
            grid.add(newPriceField, 2, 1);

            grid.add(new Label("Status:"), 0, 2);
            grid.add(currentStatusLabel, 1, 2);
            grid.add(newStatusComboBox, 2, 2);

            updateDialog.getDialogPane().setContent(grid);

            updateDialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    String message = "";
                    boolean quantityUpdated = false; // Flag to track if quantity was updated
                    boolean statusUpdatedManually = false; // Flag to track if status was updated manually

                    // 1. Handle Quantity Update FIRST
                    String newQuantityStr = newQuantityField.getText().trim();
                    if (!newQuantityStr.isEmpty()) {
                        message = inventoryManager.updateBracelet(id, "quantity", newQuantityStr);
                        showMessage(message);
                        quantityUpdated = true;
                    }

                    // 2. Handle Price Update
                    String newPriceStr = newPriceField.getText().trim();
                    if (!newPriceStr.isEmpty()) {
                        // If quantity was updated, the message might be combined.
                        // For simplicity, we'll show the latest message from the last update.
                        message = inventoryManager.updateBracelet(id, "price", newPriceStr);
                        showMessage(message);
                    }

                    // 3. Handle Status Update (ONLY if quantity didn't dictate it)
                    String selectedStatus = newStatusComboBox.getValue();
                    // Check if a status was explicitly selected AND it's different from the bracelet's *current* status
                    // after potential quantity update.
                    // IMPORTANT: We only allow manual status update if quantity didn't already force it.
                    if (selectedStatus != null && !selectedStatus.equalsIgnoreCase(braceletToUpdate.getStatus())) {
                        // Check if the status selected manually contradicts the quantity rule
                        int currentQuantity = braceletToUpdate.getQuantity(); // Get the quantity AFTER potential update
                        boolean quantityDemandsOutOfStock = (currentQuantity == 0 && selectedStatus.equalsIgnoreCase("In Stock"));
                        boolean quantityDemandsInStock = (currentQuantity > 0 && selectedStatus.equalsIgnoreCase("Out of Stock"));

                        if (quantityDemandsOutOfStock || quantityDemandsInStock) {
                            showMessage("Warning: Status cannot be manually set to contradict current quantity. Status remains: " + braceletToUpdate.getStatus());
                            // Do NOT update status if it contradicts the quantity rule
                        } else {
                            message = inventoryManager.updateBracelet(id, "status", selectedStatus);
                            showMessage(message);
                            statusUpdatedManually = true;
                        }
                    }

                    if (!quantityUpdated && !statusUpdatedManually && newPriceStr.isEmpty()) {
                        showMessage("No changes made to bracelet " + id + ".");
                    }
                    updateInventoryTable(); // Refresh table after all potential updates
                }
                return null;
            });
            updateDialog.showAndWait();
        });
    }

    /**
     * Shows a dialog for generating a low stock report.
     */
    private void showLowStockReportDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Low Stock Report");
        dialog.setHeaderText("Enter the low stock threshold quantity:");
        dialog.setContentText("Threshold:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(thresholdStr -> {
            List<Bracelet> lowStockItems = inventoryManager.generateLowStockReport(thresholdStr.trim());

            if (lowStockItems == null) {
                showMessage("Error: Threshold must be a valid non-negative integer.");
            } else if (lowStockItems.isEmpty()) {
                showMessage(String.format("No bracelets currently below the specified stock threshold of %s.", thresholdStr));
            } else {
                StringBuilder report = new StringBuilder();
                report.append(String.format("--- Bracelets Below Stock Threshold (%s) ---\n", thresholdStr));
                for (Bracelet bracelet : lowStockItems) {
                    report.append(String.format("ID: %s, Description: %s, Current Quantity: %d\n",
                            bracelet.getId(), bracelet.getDescription(), bracelet.getQuantity()));
                }
                report.append("-------------------------------------------------------\n");
                showMessage(report.toString());
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}