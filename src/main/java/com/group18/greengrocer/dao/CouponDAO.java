package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.Coupon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing Coupons.
 * Handles database operations for the Coupons table.
 */
public class CouponDAO {

    private DatabaseAdapter dbAdapter;

    public CouponDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Creates a new coupon.
     * 
     * @param coupon The coupon to create.
     * @return true if successful.
     */
    public boolean addCoupon(Coupon coupon) {
        String sql = "INSERT INTO Coupons (code, discount_amount, expiry_date, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, coupon.getCode());
            stmt.setDouble(2, coupon.getDiscountAmount());
            stmt.setDate(3, coupon.getExpiryDate());
            stmt.setBoolean(4, coupon.isActive());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        coupon.setId(generatedKeys.getInt(1));
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
     * Finds a coupon by its code.
     * Important for validation during checkout.
     * 
     * @param code The coupon code (e.g., "SAVE10").
     * @return The Coupon object if found, otherwise null.
     */
    public Coupon findCouponByCode(String code) {
        String sql = "SELECT * FROM Coupons WHERE code = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCoupon(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves all coupons (for Owner management).
     * 
     * @return List of all coupons.
     */
    public List<Coupon> findAllCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM Coupons ORDER BY expiry_date DESC";
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                coupons.add(mapCoupon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coupons;
    }
    
    /**
     * Deactivates a coupon (soft delete).
     * 
     * @param id The ID of the coupon.
     * @return true if successful.
     */
    public boolean deactivateCoupon(int id) {
        String sql = "UPDATE Coupons SET is_active = FALSE WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Deletes a coupon permanently from the database.
     * Not recommended if the coupon has been used in orders (foreign key constraint).
     * 
     * @param id The ID of the coupon.
     * @return true if successful.
     */
    public boolean deleteCoupon(int id) {
        String sql = "DELETE FROM Coupons WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Likely foreign key constraint violation if invalid
            e.printStackTrace();
        }
        return false;
    }

    private Coupon mapCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDiscountAmount(rs.getDouble("discount_amount"));
        c.setExpiryDate(rs.getDate("expiry_date"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}
