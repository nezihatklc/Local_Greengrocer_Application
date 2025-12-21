package com.group18.greengrocer.model;

public enum Role {
    CUSTOMER, 
    CARRIER, 
    OWNER;
    
    public static Role fromString(String roleStr) {
        if (roleStr != null) {
            try {
                return Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
