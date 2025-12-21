package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    
    private DatabaseAdapter dbAdapter;

    public UserDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM UserInfo WHERE username = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    // map other fields
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean createUser(User user) {
        // Implementation for INSERT
        return false;
    }
}
