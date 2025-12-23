package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.UserDAO;
import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.util.ValidatorUtil;

public class AuthenticationService {

    private UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates user with username and password.
     * 
     * - MUST return full User object including Role on success
     * - MUST return null if authentication fails
     * - NO UI logic here (alerts handled in controller)
     */
    // ASSIGNED TO: Team Leader
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User user = userDAO.findUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Registers a new user.
     *
     * IMPORTANT RULES:
     * - ONLY users with role CUSTOMER can be registered
     * - Username MUST be unique
     * - Password MUST pass validatePasswordStrength(...)
     * - Throws ValidationException on any rule violation
     *
     * Business logic only, NO UI code.
     */
    // ASSIGNED TO: Team Leader
    public void register(User user) {
        if (user == null) {
            throw new ValidationException("User information cannot be empty.");
        }

        // Rule: Only CUSTOMER can register via this service
        if (user.getRole() != Role.CUSTOMER) {
            throw new ValidationException("Only customers can register.");
        }

        // Rule: Username cannot be empty
        if (ValidatorUtil.isEmpty(user.getUsername())) {
            throw new ValidationException("Username cannot be empty.");
        }

        // Rule: Username must be unique
        if (!isUsernameUnique(user.getUsername())) {
            throw new ValidationException("Username is already taken: " + user.getUsername());
        }

        // Rule: Password must be strong
        if (!validatePasswordStrength(user.getPassword())) {
            throw new ValidationException("Password does not meet strength requirements (min 8 chars, 1 upper, 1 lower, 1 digit).");
        }

        // Rule: Phone number validation
        if (user.getPhoneNumber() != null && !ValidatorUtil.isEmpty(user.getPhoneNumber()) 
                && !ValidatorUtil.isNumeric(user.getPhoneNumber())) {
            throw new ValidationException("Phone number must be numeric.");
        }

        // Attempt to create user
        boolean success = userDAO.createUser(user);
        if (!success) {
            throw new ValidationException("Registration failed due to database error.");
        }
    }
    
    /**
     * Checks password strength according to project rules.
     * (length, digit, uppercase, etc.)
     */
    // ASSIGNED TO: Team Leader
    public boolean validatePasswordStrength(String password) {
        if (password == null) return false;
        return ValidatorUtil.isStrongPassword(password);
    }
    
    /**
     * Checks whether given username already exists in database.
     */
    // ASSIGNED TO: Team Leader
    public boolean isUsernameUnique(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userDAO.findUserByUsername(username) == null;
    }
}
