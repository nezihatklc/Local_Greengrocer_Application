package com.group18.greengrocer.main;

import com.group18.greengrocer.dao.DatabaseAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTest {
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");
        
        try {
            Connection conn = DatabaseAdapter.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: Connection established to " + conn.getCatalog());
                
                // Optional: Test a simple query
                try (PreparedStatement stmt = conn.prepareStatement("SELECT 1");
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Test Query (SELECT 1) Executed Successfully.");
                    }
                }
                
            } else {
                System.out.println("FAILURE: Connection is null or closed.");
            }
        } catch (SQLException e) {
            System.err.println("FAILURE: Exception occurred.");
            e.printStackTrace();
        } finally {
            DatabaseAdapter.getInstance().closeConnection();
        }
    }
}
