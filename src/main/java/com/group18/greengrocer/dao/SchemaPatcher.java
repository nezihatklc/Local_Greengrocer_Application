package com.group18.greengrocer.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SchemaPatcher {
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
