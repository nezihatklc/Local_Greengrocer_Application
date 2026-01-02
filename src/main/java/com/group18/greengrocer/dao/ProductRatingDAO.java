package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.ProductRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRatingDAO {

    private DatabaseAdapter dbAdapter;

    public ProductRatingDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
        // Schema is handled by database_schema.sql usually, but we keep this for safety/legacy
        // Note: The schema.sql definition includes order_id, so we must respect that.
    }

    public boolean addRating(ProductRating rating) {
        // Updated to include order_id
        String sql = "INSERT INTO ProductRatings (order_id, customer_id, product_id, rating, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating.getOrderId());
            stmt.setInt(2, rating.getCustomerId());
            stmt.setInt(3, rating.getProductId());
            stmt.setInt(4, rating.getRating());
            stmt.setTimestamp(5, rating.getCreatedAt());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ProductRating> getRatingsByProduct(int productId) {
        List<ProductRating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ProductRatings WHERE product_id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProductRating pr = new ProductRating();
                    pr.setId(rs.getInt("id"));
                    pr.setOrderId(rs.getInt("order_id"));
                    pr.setCustomerId(rs.getInt("customer_id"));
                    pr.setProductId(rs.getInt("product_id"));
                    pr.setRating(rs.getInt("rating"));
                    pr.setCreatedAt(rs.getTimestamp("created_at"));
                    ratings.add(pr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }

    /**
     * Checks if any product in the given order has been rated.
     * @param orderId The ID of the order.
     * @return true if at least one rating exists for this order.
     */
    public boolean hasRatedOrder(int orderId) {
        String sql = "SELECT 1 FROM ProductRatings WHERE order_id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
