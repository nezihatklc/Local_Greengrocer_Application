package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.CarrierRatingDAO;
import com.group18.greengrocer.dao.UserDAO;
import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.util.ValidatorUtil;

import java.util.List;

/**
 * UserService
 * Business rules + validation for user operations.
 * Controllers must call this service, not DAO directly.
 */
public class UserService {

    private final UserDAO userDAO;
    private final CarrierRatingDAO carrierRatingDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.carrierRatingDAO = new CarrierRatingDAO();
    }

    // -------------------------
    // Queries
    // -------------------------

    /** Owner/admin: list all users */
    // ASSIGNED TO: Team Leader
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /** Get user by id */
    // ASSIGNED TO: Team Leader
    public User getUserById(int userId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid user id.");
        return userDAO.findUserById(userId);
    }

    /** Owner: list carriers */
    // ASSIGNED TO: Owner
    public List<User> getAllCarriers() {
        return userDAO.findUsersByRole(Role.CARRIER);
    }

    // -------------------------
    // Profile update
    // -------------------------

    /**
     * Updates user profile info (username/address/phone).
     *
     * CRITICAL:
     * UserDAO.updateUser() updates password too.
     * Profile screen usually does NOT send password -> keep old password.
     *
     * Role may be null in the UI object -> keep existing role.
     */
    // ASSIGNED TO: Team Leader
    public void updateUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");
        if (user.getId() <= 0) throw new IllegalArgumentException("Invalid user id.");

        // Fetch existing user first (existence + keep password/role safely)
        User existing = userDAO.findUserById(user.getId());
        if (existing == null) throw new IllegalArgumentException("User not found.");

        // Normalize inputs
        String username = safeTrim(user.getUsername());
        String address  = safeTrim(user.getAddress());
        String phone    = safeTrim(user.getPhoneNumber());

        if (username.isEmpty()) throw new IllegalArgumentException("Username cannot be empty.");
        if (address.isEmpty())  throw new IllegalArgumentException("Address cannot be empty.");
        if (phone.isEmpty())    throw new IllegalArgumentException("Phone number cannot be empty.");
        if (!ValidatorUtil.isValidPhoneNumber(phone)) throw new IllegalArgumentException("Invalid phone number format (10-13 digits).");

        // CRITICAL: Profile update must NEVER change the role.
        // We force the existing role, ignoring whatever came from the UI.
        user.setRole(existing.getRole());

        // Username unique check ONLY if changed
        if (!existing.getUsername().equals(username)) {
            User check = userDAO.findUserByUsername(username);
            if (check != null) throw new IllegalArgumentException("Username already taken.");
        }

        // Password safety:
        // If UI doesn't provide password, keep old one.
        String incomingPass = safeTrim(user.getPassword());
        if (incomingPass.isEmpty()) {
            user.setPassword(existing.getPassword());
        } else {
            // If password is provided (optional), enforce your ValidatorUtil rule
            if (!ValidatorUtil.isStrongPassword(incomingPass)) {
                throw new IllegalArgumentException("Weak password (min 8, upper+lower+digit).");
            }
        }

        // Apply normalized values
        user.setUsername(username);
        user.setAddress(address);
        user.setPhoneNumber(phone);

        boolean ok = userDAO.updateUser(user);
        if (!ok) throw new IllegalArgumentException("Profile update failed.");
    }

    // -------------------------
    // Password change
    // -------------------------

    /**
     * Changes password after validating old password.
     * Rules: strong password required (ValidatorUtil).
     */
    // ASSIGNED TO: Team Leader
    public void changePassword(int userId, String oldPass, String newPass) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid user id.");

        oldPass = safeTrim(oldPass);
        newPass = safeTrim(newPass);

        if (oldPass.isEmpty() || newPass.isEmpty())
            throw new IllegalArgumentException("Passwords cannot be empty.");

        if (oldPass.equals(newPass))
            throw new IllegalArgumentException("New password must be different.");

        User existing = userDAO.findUserById(userId);
        if (existing == null) throw new IllegalArgumentException("User not found.");

        if (existing.getPassword() == null || !existing.getPassword().equals(oldPass))
            throw new IllegalArgumentException("Old password is incorrect.");

        if (!ValidatorUtil.isStrongPassword(newPass))
            throw new IllegalArgumentException("Weak password (min 8, upper+lower+digit).");

        boolean ok = userDAO.updatePassword(userId, newPass);
        if (!ok) throw new IllegalArgumentException("Password update failed.");
    }

    // -------------------------
    // Owner: carrier management
    // -------------------------

    /**
     * Employ carrier.
     * Rules: username unique, strong password (ValidatorUtil), role is CARRIER.
     */
    // ASSIGNED TO: Owner
    public void addCarrier(User carrier) {
        if (carrier == null) throw new IllegalArgumentException("Carrier cannot be null.");

        String username = safeTrim(carrier.getUsername());
        String password = safeTrim(carrier.getPassword());
        String address  = safeTrim(carrier.getAddress());
        String phone    = safeTrim(carrier.getPhoneNumber());

        if (username.isEmpty()) throw new IllegalArgumentException("Username cannot be empty.");
        if (password.isEmpty()) throw new IllegalArgumentException("Password cannot be empty.");
        if (address.isEmpty())  throw new IllegalArgumentException("Address cannot be empty.");
        if (phone.isEmpty())    throw new IllegalArgumentException("Phone number cannot be empty.");
        if (!ValidatorUtil.isValidPhoneNumber(phone)) throw new IllegalArgumentException("Invalid phone number format (10-13 digits).");

        if (userDAO.findUserByUsername(username) != null)
            throw new IllegalArgumentException("Username already taken.");

        if (!ValidatorUtil.isStrongPassword(password))
            throw new IllegalArgumentException("Weak password (min 8, upper+lower+digit).");

        carrier.setRole(Role.CARRIER);
        carrier.setUsername(username);
        carrier.setPassword(password);
        carrier.setAddress(address);
        carrier.setPhoneNumber(phone);

        boolean ok = userDAO.createUser(carrier);
        if (!ok) throw new IllegalArgumentException("Carrier could not be created.");
    }

    /**
     * Fire carrier (delete).
     * If you later add an 'active' column, switch to deactivate instead.
     */
    // ASSIGNED TO: Owner
    public void removeCarrier(int carrierId) {
        if (carrierId <= 0) throw new IllegalArgumentException("Invalid carrier id.");

        User existing = userDAO.findUserById(carrierId);
        if (existing == null) throw new IllegalArgumentException("Carrier not found.");
        if (existing.getRole() != Role.CARRIER) throw new IllegalArgumentException("User is not a carrier.");

        boolean ok = userDAO.deleteUser(carrierId);
        if (!ok) throw new IllegalArgumentException("Carrier could not be removed.");
    }

    // -------------------------
    // Ratings
    // -------------------------

    /** Returns average rating for a carrier from CarrierRatings table. */
    // ASSIGNED TO: Carrier
    public double getCarrierRating(int carrierId) {
        if (carrierId <= 0) throw new IllegalArgumentException("Invalid carrier id.");

        // Optional: verify it is a carrier
        User u = userDAO.findUserById(carrierId);
        if (u == null) throw new IllegalArgumentException("Carrier not found.");
        if (u.getRole() != Role.CARRIER) throw new IllegalArgumentException("User is not a carrier.");

        return carrierRatingDAO.getAverageRatingForCarrier(carrierId);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
