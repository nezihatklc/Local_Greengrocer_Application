package com.group18.greengrocer.model;

/**
 * Represents the category of a Product.
 * Corresponds to the 'category' ENUM column in the 'ProductInfo' table.
 * Mandatory categories: FRUIT, VEGETABLE.
 */
public enum Category {
    /**
     * Fruit products (e.g., Apple, Banana).
     */
    FRUIT,
    
    /**
     * Vegetable products (e.g., Potato, Tomato).
     */
    VEGETABLE;
    
    /**
     * Returns the capitalized string representation of the category for UI display.
     * Example: "Fruit", "Vegetable".
     * 
     * @return The formatted category name.
     */
    @Override
    public String toString() {
        String s = super.toString();
        return s.substring(0, 1) + s.substring(1).toLowerCase();
    }
    
    /**
     * Converts a string to a Category, handling case insensitivity.
     * 
     * @param str The category string (e.g., "Fruit" or "FRUIT").
     * @return The Category enum or null if not found.
     */
    public static Category fromString(String str) {
        if (str == null) return null;
        try {
            return Category.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
