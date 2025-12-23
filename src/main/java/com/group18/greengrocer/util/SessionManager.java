package com.group18.greengrocer.util;

import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;

/**
 * Manages the user session for the Local Greengrocer Application.
 * This class follows the Singleton pattern to ensure a single active session
 * throughout the application's lifecycle.
 *
 * Use this class to store and retrieve the currently logged-in user and to check their role.
 */
public class SessionManager {

    /**
     * The single instance of the SessionManager.
     */
    private static SessionManager instance;

    /**
     * The currently logged-in user. Null if no user is logged in.
     */
    private User currentUser;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private SessionManager() {}

    /**
     * Retrieves the singleton instance of the SessionManager.
     * Creates the instance if it does not already exist (Lazy Initialization).
     *
     * @return The singleton SessionManager instance.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Returns the currently logged-in user.
     *
     * @return The current User object, or null if no user is logged in.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the currently logged-in user.
     * This method should be called upon successful authentication.
     *
     * @param currentUser The user to set as logged in.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Logs out the current user by clearing the session data.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if the current user has the CUSTOMER role.
     *
     * @return true if the current user is a customer, false otherwise.
     */
    public boolean isCustomer() {
        return isLoggedIn() && currentUser.getRole() == Role.CUSTOMER;
    }

    /**
     * Checks if the current user has the CARRIER role.
     *
     * @return true if the current user is a carrier, false otherwise.
     */
    public boolean isCarrier() {
        return isLoggedIn() && currentUser.getRole() == Role.CARRIER;
    }

    /**
     * Checks if the current user has the OWNER role.
     *
     * @return true if the current user is the owner, false otherwise.
     */
    public boolean isOwner() {
        return isLoggedIn() && currentUser.getRole() == Role.OWNER;
    }
}
