package com.group18.greengrocer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarrierRatingDAO {

    private DatabaseAdapter dbAdapter;

    public CarrierRatingDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Calculates the average rating for a specific carrier.
     * 
     * @param carrierId The ID of the carrier.
     * @return The average rating calculated from CarrierRatings table.
     */
    public double getAverageRatingForCarrier(int carrierId) {
        String sql = "SELECT AVG(rating) FROM CarrierRatings WHERE carrier_id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
