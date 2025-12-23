package com.group18.greengrocer.model;

/**
 * User Model
 */
public class User {
    private int id;
    private String username;
    private String password;
    private Role role;
    private String address;
    private String phoneNumber;

    public User() { }

    public User(int id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
