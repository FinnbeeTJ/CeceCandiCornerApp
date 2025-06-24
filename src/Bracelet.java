/**
 * Bracelet.java
 * Represents a single bracelet item in the inventory for Cece's Candi Corner.
 * This class encapsulates the properties of a bracelet and provides methods
 * to access and modify these properties.
 */
class Bracelet {
    // Private fields to encapsulate the bracelet's data
    private String id;
    private String description;
    private int quantity;
    private double price;
    private String status;

    /**
     * Constructor to initialize a new Bracelet object.
     * id The unique identifier for the bracelet.
     * description A brief description or name of the bracelet.
     * quantity The current stock quantity of the bracelet.
     * price The selling price of the bracelet.
     * status The stock status (e.g., "In Stock", "Out of Stock").
     * Defaults to "In Stock" if not specified in the constructor call.
     */
    public Bracelet(String id, String description, int quantity, double price, String status) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
    }

    // --- Getter Methods ---
    /**
     * Retrieves the unique ID of the bracelet.
     * @return The bracelet's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the description of the bracelet.
     * @return The bracelet's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the current stock quantity of the bracelet.
     * @return The bracelet's quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Retrieves the selling price of the bracelet.
     * @return The bracelet's price.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Retrieves the stock status of the bracelet.
     * @return The bracelet's status.
     */
    public String getStatus() {
        return status;
    }

    // --- Setter Methods ---
    /**
     * Sets a new quantity for the bracelet.
     * @param newQuantity The new quantity to set.
     */
    public void setQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    /**
     * Sets a new price for the bracelet.
     * @param newPrice The new price to set.
     */
    public void setPrice(double newPrice) {
        this.price = newPrice;
    }

    /**
     * Sets a new stock status for the bracelet.
     * @param newStatus The new status to set (e.g., "In Stock", "Out of Stock").
     */
    public void setStatus(String newStatus) {
        this.status = newStatus;
    }

    /**
     * Provides a string representation of the Bracelet object,
     * useful for printing details to the console or log.
     * @return A formatted string containing all bracelet details.
     */
    @Override
    public String toString() {
        return String.format("ID: %s, Description: %s, Quantity: %d, Price: $%.2f, Status: %s",
                id, description, quantity, price, status);
    }
}
