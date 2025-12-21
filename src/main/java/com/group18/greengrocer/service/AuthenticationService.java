package com.group18.greengrocer.service;

import com.group18.greengrocer.model.User;
import com.group18.greengrocer.model.Role;

public class AuthenticationService {

    /**
     * Authenticates user with username and password.
     * 
     * - MUST return full User object including Role on success
     * - MUST return null if authentication fails
     * - NO UI logic here (alerts handled in controller)
     */
    public User login(String username, String password) {
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
    public void register(User user) { // throws ValidationException
        return null;
    }
    
    /**
     * Checks password strength according to project rules.
     * (length, digit, uppercase, etc.)
     */
    public boolean validatePasswordStrength(String password) {
        return false;
    }
    
    /**
     * Checks whether given username already exists in database.
     */
    public boolean isUsernameUnique(String username) {
        return false;
    }
}
