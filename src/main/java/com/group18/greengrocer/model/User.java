package com.group18.greengrocer.model;

import java.time.LocalDateTime;

/**
 * Represents a User in the Local Greengrocer Application.
 * This class corresponds to the 'UserInfo' table in the database.
 * It holds authentication details, role information, and contact details.
 */
public class User {

    /**
     * Unique identifier for the user.
     * Corresponds to the 'id' column in the database.
     */
    private int id;

    /**
     * Unique username for authentication.
     * Corresponds to the 'username' column.
     */
    private String username;

    /**
     * Password for authentication.
     * Should be stored/handled securely (though this model just holds the String).
     * Corresponds to the 'password' column.
     */
    private String password;

    /**
     * The role of the user (CUSTOMER, CARRIER, OWNER).
     * Corresponds to the 'role' column.
     */
    private Role role;

    /**
     * The physical address of the user.
     * Corresponds to the 'address' column.
     */
    private String address;

    /**
     * The contact phone number of the user.
     * Corresponds to the 'phone_number' column.
     */
    private String phoneNumber;

    /**
     * The timestamp when the user was created.
     * Corresponds to the 'created_at' column.
     */
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Constructor for creating a new User object during registration (before ID is assigned by DB).
     *
     * @param username    The unique username.
     * @param password    The user's password.
     * @param role        The user's role.
     * @param address     The user's address.
     * @param phoneNumber The user's phone number.
     */
    public User(String username, String password, Role role, String address, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    /**
     * minimal constructor.
     * 
     * @param id The user ID.
     * @param username The username.
     * @param role The user role.
     */
    public User(int id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    /**
     * Full constructor for retrieving a User from the database.
     *
     * @param id          The unique identifier.
     * @param username    The unique username.
     * @param password    The user's password.
     * @param role        The user's role.
     * @param address     The user's address.
     * @param phoneNumber The user's phone number.
     * @param createdAt   The creation timestamp.
     */
    public User(int id, String username, String password, Role role, String address, String phoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id The new user ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The new username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The new password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user role.
     *
     * @return The user role.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the user role.
     *
     * @param role The new user role.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gets the user address.
     *
     * @return The user address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the user address.
     *
     * @param address The new address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the phone number.
     *
     * @return The phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number.
     *
     * @param phoneNumber The new phone number.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The new creation timestamp.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", address='" + address + '\'' +
                '}';
    }
}
