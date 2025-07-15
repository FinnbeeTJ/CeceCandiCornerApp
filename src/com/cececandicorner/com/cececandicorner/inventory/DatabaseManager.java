package com.cececandicorner.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager.java
 * Manages all interactions with the SQLite database for Cece's Candi Corner Inventory System.
 * This class handles connecting to the database, creating the table, and performing
 * CRUD (Create, Read, Update, Delete) operations on the 'bracelets' table.
 * It encapsulates JDBC logic and handles SQL exceptions.
 */
public class DatabaseManager {

    // Database URL prefix for SQLite
    private String DB_URL_PREFIX = "jdbc:sqlite:";
    private String dbPath; // Stores the user-provided database file path

    /**
     * Constructor for DatabaseManager.
     * @param dbPath The file path to the SQLite database file (e.g., "inventory.db").
     */
    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
        // Load the SQLite JDBC driver. This is typically not strictly necessary for modern JDBC,
        // but good practice for clarity.
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: SQLite JDBC driver not found. Make sure sqlite-jdbc.jar is in your classpath.");
            // You might want to throw a runtime exception here or handle more gracefully
        }
    }

    /**
     * Establishes a connection to the SQLite database.
     * @return A Connection object if successful, null otherwise.
     * Displays an error message if connection fails.
     */
    private Connection connect() {
        Connection conn = null;
        try {
            // dbPath will be something like "C:/path/to/inventory.db"
            conn = DriverManager.getConnection(DB_URL_PREFIX + dbPath);
            // System.out.println("Connection to SQLite database established successfully."); // For debugging
        } catch (SQLException e) {
            System.err.println("Error connecting to database at " + dbPath + ": " + e.getMessage());
        }
        return conn;
    }

    /**
     * Creates the 'bracelets' table if it does not already exist.
     * Assumes the table schema: id (TEXT PRIMARY KEY), description (TEXT),
     * quantity (INTEGER), price (REAL), status (TEXT).
     * @return true if table creation was successful or table already exists, false otherwise.
     */
    public boolean createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS bracelets (" +
                "id TEXT PRIMARY KEY," +
                "description TEXT NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "price REAL NOT NULL," +
                "status TEXT NOT NULL" +
                ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sql);
                // System.out.println("Table 'bracelets' checked/created successfully."); // For debugging
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
        return false;
    }

    /**
     * Inserts a new bracelet into the database.
     * @param bracelet The Bracelet object to insert.
     * @return true if insertion is successful, false otherwise.
     */
    public boolean insertBracelet(Bracelet bracelet) {
        String sql = "INSERT INTO bracelets(id, description, quantity, price, status) VALUES(?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bracelet.getId());
            pstmt.setString(2, bracelet.getDescription());
            pstmt.setInt(3, bracelet.getQuantity());
            pstmt.setDouble(4, bracelet.getPrice());
            pstmt.setString(5, bracelet.getStatus());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting bracelet " + bracelet.getId() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Selects all bracelets from the database.
     * @return A List of Bracelet objects, or an empty list if no bracelets found or an error occurs.
     */
    public List<Bracelet> selectAllBracelets() {
        List<Bracelet> bracelets = new ArrayList<>();
        String sql = "SELECT id, description, quantity, price, status FROM bracelets";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                bracelets.add(new Bracelet(
                        rs.getString("id"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error selecting all bracelets: " + e.getMessage());
        }
        return bracelets;
    }

    /**
     * Selects a single bracelet by its ID.
     * @param id The ID of the bracelet to retrieve.
     * @return The Bracelet object if found, null otherwise.
     */
    public Bracelet selectBraceletById(String id) {
        String sql = "SELECT id, description, quantity, price, status FROM bracelets WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Bracelet(
                        rs.getString("id"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error selecting bracelet by ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing bracelet in the database.
     * @param bracelet The Bracelet object with updated values.
     * @return true if update is successful, false otherwise.
     */
    public boolean updateBracelet(Bracelet bracelet) {
        String sql = "UPDATE bracelets SET description = ?, quantity = ?, price = ?, status = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bracelet.getDescription());
            pstmt.setInt(2, bracelet.getQuantity());
            pstmt.setDouble(3, bracelet.getPrice());
            pstmt.setString(4, bracelet.getStatus());
            pstmt.setString(5, bracelet.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating bracelet " + bracelet.getId() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a bracelet from the database by its ID.
     * @param id The ID of the bracelet to delete.
     * @return true if deletion is successful, false otherwise.
     */
    public boolean deleteBracelet(String id) {
        String sql = "DELETE FROM bracelets WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting bracelet " + id + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if a bracelet with the given ID already exists in the database.
     * @param id The ID to check.
     * @return true if the ID exists, false otherwise.
     */
    public boolean doesIdExist(String id) {
        String sql = "SELECT COUNT(*) FROM bracelets WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking ID existence for " + id + ": " + e.getMessage());
        }
        return false;
    }
}
