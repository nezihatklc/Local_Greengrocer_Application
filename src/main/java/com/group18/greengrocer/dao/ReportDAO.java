package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.ReportData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    private static ReportDAO instance;

    private ReportDAO() { }

    public static synchronized ReportDAO getInstance() {
        if (instance == null) {
            instance = new ReportDAO();
        }
        return instance;
    }

    // Example 1: Total Revenue per Product
    public List<ReportData> getRevenueByProduct() {
        List<ReportData> data = new ArrayList<>();
        String query = "SELECT p.name, SUM(oi.quantity * oi.price_at_purchase) as revenue " +
                       "FROM OrderItems oi " +
                       "JOIN ProductInfo p ON oi.product_id = p.id " +
                       "GROUP BY p.name " +
                       "ORDER BY revenue DESC " +
                       "LIMIT 10"; // Top 10 products

        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String label = rs.getString("name");
                double value = rs.getDouble("revenue");
                data.add(new ReportData(label, value));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Example 2: Count of Orders by Status
    public List<ReportData> getOrdersByStatus() {
        List<ReportData> data = new ArrayList<>();
        String query = "SELECT status, COUNT(*) as count FROM OrderInfo GROUP BY status";

        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String label = rs.getString("status");
                double value = rs.getDouble("count");
                data.add(new ReportData(label, value));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    // Example 3: Revenue by Month (Simple version)
    public List<ReportData> getMonthlyRevenue() {
        List<ReportData> data = new ArrayList<>();
        // Note: Formatting date might be DB specific. Using generic approach or MySQL specific.
        String query = "SELECT DATE_FORMAT(ordertime, '%Y-%m') as month, SUM(totalcost) as revenue " +
                       "FROM OrderInfo " +
                       "WHERE status = 'COMPLETED' " + 
                       "GROUP BY month " +
                       "ORDER BY month DESC " +
                       "LIMIT 12";

        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String label = rs.getString("month");
                double value = rs.getDouble("revenue");
                data.add(new ReportData(label, value));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public List<ReportData> getDailySales(int days) {
        List<ReportData> data = new ArrayList<>();
        String sql =
            "SELECT DATE(ordertime) AS day, SUM(totalcost) AS total " +
            "FROM OrderInfo " +
            "WHERE ordertime >= (NOW() - INTERVAL ? DAY) " +
            "AND status = 'COMPLETED' " +
            "GROUP BY DATE(ordertime) " +
            "ORDER BY day";

        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date day = rs.getDate("day");
                    double total = rs.getDouble("total");
                    // Assuming ReportData label is String, convert Date to String
                    data.add(new ReportData(String.valueOf(day), total));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}
