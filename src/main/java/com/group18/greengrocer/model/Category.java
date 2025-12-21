package com.group18.greengrocer.model;

public enum Category {
    FRUIT,
    VEGETABLE;
    
    @Override
    public String toString() {
        // Capitalize only first letter
        String s = super.toString();
        return s.substring(0, 1) + s.substring(1).toLowerCase();
    }
}
