package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.UserDAO;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.util.SessionManager;

public class AuthenticationService {
    
    private UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public boolean login(String username, String password) {
        // Business Rule: Validate credentials
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        User user = userDAO.findUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // Success
            SessionManager.getInstance().setCurrentUser(user);
            return true;
        }
        return false;
    }

    public boolean register(String username, String password, Role role) {
        // Business Rule: Check password strength, uniqueness, etc.
        if (userDAO.findUserByUsername(username) != null) {
            return false; // User already exists
        }
        // Proceed to create
        return true; 
    }
}
