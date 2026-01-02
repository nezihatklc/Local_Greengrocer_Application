package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.ProductRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRatingDAO {

    private DatabaseAdapter dbAdapter;

    public ProductRatingDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS ProductRatings (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "customer_id INT NOT NULL, " +
                "product_id INT NOT NULL, " +
                "rating INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (customer_id) REFERENCES UserInfo(id), " +
                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id))";
        try (Connection conn = dbAdapter.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addRating(ProductRating rating) {
        String sql = "INSERT INTO ProductRatings (customer_id, product_id, rating, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating.getCustomerId());
            stmt.setInt(2, rating.getProductId());
            stmt.setInt(3, rating.getRating());
            stmt.setTimestamp(4, rating.getCreatedAt());
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
}
