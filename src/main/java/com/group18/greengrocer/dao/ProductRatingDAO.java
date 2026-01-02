package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.ProductRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Data Access Object (DAO) for managing Product Ratings.
 * <p>
 * This class handles all database interactions related to the 'ProductRatings' table.
 * It provides methods to:
 * <ul>
 * <li>Insert new ratings for products purchased in an order.</li>
 * <li>Retrieve all ratings associated with a specific product.</li>
 * <li>Check if an order has already been rated to prevent duplicate entries.</li>
 * </ul>
 *
 * @author Group18
 * @version 1.0
 */
public class ProductRatingDAO {
/**
     * Initializes the DAO and obtains the database adapter instance.
     */
    private DatabaseAdapter dbAdapter;

    public ProductRatingDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
        // Schema is handled by database_schema.sql usually, but we keep this for safety/legacy
        // Note: The schema.sql definition includes order_id, so we must respect that.
    }
/**
     * Adds a new product rating to the database.
     * <p>
     * Inserts a record containing the order ID, customer ID, product ID, rating score,
     * and the timestamp.
     *
     * @param rating The {@link ProductRating} object containing the details to be saved.
     * @return {@code true} if the insertion was successful (1 row affected), {@code false} otherwise.
     */
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
/**
     * Retrieves a list of all ratings associated with a specific product.
     * <p>
     * This method is typically used to display reviews on the product detail page
     * or to calculate the average rating of a product.
     *
     * @param productId The unique identifier of the product.
     * @return A {@link List} of {@link ProductRating} objects. Returns an empty list if no ratings are found or if an error occurs.
     */
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
     * Checks if any product in the given order has already been rated.
     * <p>
     * This is used to enforce the business rule that an order (or items within it)
     * cannot be rated multiple times.
     *
     * @param orderId The unique identifier of the order.
     * @return {@code true} if at least one rating exists linked to this order ID, {@code false} otherwise.
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
