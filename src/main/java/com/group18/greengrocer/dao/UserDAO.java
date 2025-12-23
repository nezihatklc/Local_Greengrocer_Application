package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing User entities.
 * handles database operations such as CRUD and role fetching.
 */
public class UserDAO {
    
    private DatabaseAdapter dbAdapter;

    public UserDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Finds a user by their username.
     * Useful for authentication and finding specific user details.
     * 
     * @param username The username to search for.
     * @return The User object if found, otherwise null.
     */
    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM UserInfo WHERE username = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds a user by their unique ID.
     * 
     * @param id The unique ID of the user.
     * @return The User object if found, otherwise null.
     */
    public User findUserById(int id) {
        String sql = "SELECT * FROM UserInfo WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all users from the database.
     * 
     * @return A list of all registered Users.
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM UserInfo";
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * Retrieves users filtered by their role (e.g., CUSTOMER, CARRIER, OWNER).
     * 
     * @param role The Role enum to filter by.
     * @return A list of Users with the specified role.
     */
    public List<User> findUsersByRole(Role role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM UserInfo WHERE role = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Creates a new user in the database.
     * Also retrieves and sets the auto-generated ID for the user object.
     * 
     * @param user The User object containing details to be saved.
     * @return true if the user was successfully created, false otherwise.
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO UserInfo (username, password, role, address, phone_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, user.getAddress());
            stmt.setString(5, user.getPhoneNumber());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
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
     * Updates an existing user's details (profile info).
     * Note: Use updatePassword for password changes to be safer.
     * 
     * @param user The User object with updated details.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE UserInfo SET username = ?, password = ?, role = ?, address = ?, phone_number = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, user.getAddress());
            stmt.setString(5, user.getPhoneNumber());
            stmt.setInt(6, user.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Updates only the password for a specific user.
     * 
     * @param userId The ID of the user.
     * @param newPassword The new password.
     * @return true if successful.
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE UserInfo SET password = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
             e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a user by their ID.
     * 
     * @param id The ID of the user to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM UserInfo WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        // Convert DB string role to Enum
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try {
                user.setRole(Role.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                // Handle unknown role or set to null/default
                user.setRole(null);
            }
        }
        user.setAddress(rs.getString("address"));
        user.setPhoneNumber(rs.getString("phone_number"));
        return user;
    }
}
