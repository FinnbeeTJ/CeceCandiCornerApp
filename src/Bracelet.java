/**
 * Bracelet.java
 * Represents a single bracelet item in the inventory for Cece's Candi Corner.
 * This class encapsulates the properties of a bracelet and provides methods
 * to access and modify these properties.
 *
 * This version has been updated to include JavaFX GUI capatibility
 */

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class Bracelet {
    // Private fields using JavaFX Properties for observability
    private final SimpleStringProperty id;
    private final SimpleStringProperty description;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty price;
    private final SimpleStringProperty status;

    /**
     * Constructor to initialize a new Bracelet object.
     * @param id The unique identifier for the bracelet.
     * @param description A brief description or name of the bracelet.
     * @param quantity The current stock quantity of the bracelet.
     * @param price The selling price of the bracelet.
     * @param status The stock status (e.g., "In Stock", "Out of Stock").
     */
    public Bracelet(String id, String description, int quantity, double price, String status) {
        this.id = new SimpleStringProperty(id);
        this.description = new SimpleStringProperty(description);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.status = new SimpleStringProperty(status);
    }

    // --- Getter Methods (returning raw values) ---
    /**
     * Retrieves the unique ID of the bracelet.
     * @return The bracelet's ID.
     */
    public String getId() {
        return id.get();
    }

    /**
     * Retrieves the description of the bracelet.
     * @return The bracelet's description.
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * Retrieves the current stock quantity of the bracelet.
     * @return The bracelet's quantity.
     */
    public int getQuantity() {
        return quantity.get();
    }

    /**
     * Retrieves the selling price of the bracelet.
     * @return The bracelet's price.
     */
    public double getPrice() {
        return price.get();
    }

    /**
     * Retrieves the stock status of the bracelet.
     * @return The bracelet's status.
     */
    public String getStatus() {
        return status.get();
    }

    // --- Property Getter Methods (for TableView PropertyValueFactory) ---
    /**
     * Returns the SimpleStringProperty for the ID.
     * @return The ID property.
     */
    public SimpleStringProperty idProperty() {
        return id;
    }

    /**
     * Returns the SimpleStringProperty for the description.
     * @return The description property.
     */
    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    /**
     * Returns the SimpleIntegerProperty for the quantity.
     * @return The quantity property.
     */
    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    /**
     * Returns the SimpleDoubleProperty for the price.
     * @return The price property.
     */
    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    /**
     * Returns the SimpleStringProperty for the status.
     * @return The status property.
     */
    public SimpleStringProperty statusProperty() {
        return status;
    }


    // --- Setter Methods (setting raw values, which updates the property) ---
    /**
     * Sets a new quantity for the bracelet.
     * @param newQuantity The new quantity to set.
     */
    public void setQuantity(int newQuantity) {
        this.quantity.set(newQuantity);
    }

    /**
     * Sets a new price for the bracelet.
     * @param newPrice The new price to set.
     */
    public void setPrice(double newPrice) {
        this.price.set(newPrice);
    }

    /**
     * Sets a new stock status for the bracelet.
     * @param newStatus The new status to set (e.g., "In Stock", "Out of Stock").
     */
    public void setStatus(String newStatus) {
        this.status.set(newStatus);
    }

    /**
     * Provides a string representation of the Bracelet object,
     * useful for printing details to the console or log.
     * @return A formatted string containing all bracelet details.
     */
    @Override
    public String toString() {
        return String.format("ID: %s, Description: %s, Quantity: %d, Price: $%.2f, Status: %s",
                getId(), getDescription(), getQuantity(), getPrice(), getStatus());
    }
}
