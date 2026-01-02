package com.group18.greengrocer.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
/**
 * Utility class responsible for applying dynamic schema updates to the database.
 * <p>
 * This class serves as a migration tool to ensure the database structure stays in sync
 * with the Java application logic. It is particularly useful for updating ENUM definitions
 * in MySQL without needing to manually drop and recreate tables.
 *
 * @version 1.0
 */
public class SchemaPatcher {
    /**
     * Checks and updates the database schema to support new Order Status values.
     * <p>
     * This method executes an {@code ALTER TABLE} SQL command to modify the 'status' column
     * in the 'OrderInfo' table. It expands the allowed ENUM values to include all states
     * used in the application (e.g., 'ON_THE_WAY', 'PREPARING').
     * <p>
     * <b>Note:</b> SQL Exceptions are caught and logged rather than thrown, ensuring that
     * a schema update failure (e.g., if already updated) does not crash the application startup.
     */
    public static void updateSchema() {
        System.out.println("Checking and updating database schema...");
        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
                Statement stmt = conn.createStatement()) {

            // Update enum to include new values while keeping old ones to prevent data loss
            // We use IGNORE or try-catch in case it's already done, but ALTER Table is
            // usually safe to repeat if it just changes definition to superset
            String sql = "ALTER TABLE OrderInfo MODIFY COLUMN status " +
                    "ENUM('AVAILABLE', 'SELECTED', 'COMPLETED', 'CANCELLED', " +
                    "'RECEIVED', 'PREPARING', 'ON_THE_WAY', 'DELIVERED', 'WAITING') " +
                    "DEFAULT 'WAITING'";

            stmt.executeUpdate(sql);
            System.out.println("Schema update: OrderInfo status enum updated.");

        } catch (SQLException e) {
            System.err.println("Schema update failed (might be already updated or other error): " + e.getMessage());
        }
    }
}
