package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.CarrierRating;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing carrier ratings.
 * Handles database operations for the CarrierRatings table.
 */
public class CarrierRatingDAO {

    private DatabaseAdapter dbAdapter;

    public CarrierRatingDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Adds a new rating for a carrier.
     * 
     * @param rating The CarrierRating object to add.
     * @return true if successful, false otherwise.
     */
    public boolean addRating(CarrierRating rating) {
        String sql = "INSERT INTO CarrierRatings (order_id, customer_id, carrier_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, rating.getOrderId());
            stmt.setInt(2, rating.getCustomerId());
            stmt.setInt(3, rating.getCarrierId());
            stmt.setInt(4, rating.getRating());
            stmt.setString(5, rating.getComment());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rating.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Calculates the average rating for a specific carrier.
     * 
     * @param carrierId The ID of the carrier.
     * @return The average rating calculated from CarrierRatings table, or 0.0 if no ratings exist.
     */
    public double getAverageRatingForCarrier(int carrierId) {
        String sql = "SELECT AVG(rating) FROM CarrierRatings WHERE carrier_id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    if (rs.wasNull()) {
                        return 0.0;
                    }
                    return avg;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    /**
     * Retrieves all ratings for a specific carrier.
     * Useful for the Owner to view feedback.
     * 
     * @param carrierId The ID of the carrier.
     * @return List of CarrierRating objects.
     */
    public List<CarrierRating> getRatingsForCarrier(int carrierId) {
        List<CarrierRating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM CarrierRatings WHERE carrier_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ratings.add(mapRating(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }
    
    /**
     * Checks if a customer has already rated a specific order.
     * 
     * @param orderId The ID of the order.
     * @return true if a rating exists for this order.
     */
    public boolean hasRated(int orderId) {
        String sql = "SELECT 1 FROM CarrierRatings WHERE order_id = ?";
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
    
    private CarrierRating mapRating(ResultSet rs) throws SQLException {
        CarrierRating r = new CarrierRating();
        r.setId(rs.getInt("id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setCarrierId(rs.getInt("carrier_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
