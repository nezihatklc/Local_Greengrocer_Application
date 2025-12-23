package com.group18.greengrocer.model;

/**
 * Represents the role of a User in the system.
 * This corresponds to the 'role' ENUM column in the 'UserInfo' table.
 */
public enum Role {
    /**
     * Regular customer who can browse and buy products.
     */
    CUSTOMER, 

    /**
     * Carrier responsible for delivering orders.
     */
    CARRIER, 

    /**
     * Owner (Administrator) who manages the system.
     */
    OWNER;
    
    /**
     * Returns the capitalized string representation of the role for UI display.
     * Example: "Customer", "Carrier", "Owner".
     * 
     * @return The formatted role name.
     */
    @Override
    public String toString() {
        String name = name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    /**
     * Converts a string representation of a role to the corresponding Role enum.
     * Use this when reading from the database or UI input.
     *
     * @param roleStr The string representing the role (case-insensitive).
     * @return The matching Role, or null if the string is invalid or null.
     */
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
