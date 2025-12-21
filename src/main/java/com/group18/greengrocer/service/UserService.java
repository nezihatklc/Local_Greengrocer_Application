package com.group18.greengrocer.service;

import com.group18.greengrocer.model.User;
import java.util.List;

public class UserService {

    /**
     * Retrieves all registered users (for admin purposes).
     * 
     * @return List of all users.
     */
    public List<User> getAllUsers() {
        return null;
    }
    
    /**
     * Retrieves a specific user by ID.
     * 
     * @param userId The ID of the user.
     * @return The User object or null.
     */
    public User getUserById(int userId) {
        return null;
    }

    /**
     * Updates user profile information (address, contact, etc.).
     * 
     * @param user The user object with updated info.
     */
    public void updateUser(User user) {
    }

    /**
     * Changes a user's password.
     * Must validate the old password before updating.
     * 
     * @param userId The ID of the user.
     * @param oldPass The current password.
     * @param newPass The new password.
     */
    public void changePassword(int userId, String oldPass, String newPass) {
    }

    /**
     * Hires a new carrier (creates a CARRIER user).
     * 
     * @param carrier The User object representing the carrier.
     */
    public void addCarrier(User carrier) {
    }

    /**
     * Fires a carrier (removes from system or deactivates).
     * 
     * @param carrierId The ID of the carrier to remove.
     */
    public void removeCarrier(int carrierId) {
    }

    /**
     * Retrieves all users with the CARRIER role.
     * 
     * @return List of carriers.
     */
    public List<User> getAllCarriers() {
        return null;
    }

    /**
     * Calculates the average rating for a carrier based on customer feedback.
     * 
     * @param carrierId The ID of the carrier.
     * @return The average rating (1-5).
     */
    public double getCarrierRating(int carrierId) {
        return 0.0;
    }
}
