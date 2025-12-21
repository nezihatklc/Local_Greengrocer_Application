package com.group18.greengrocer.util;

/**
 * Application Constants
 */
public class Constants {
    // Database Config
    public static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer_db";
    public static final String DB_USER = "myuser@localhost"; // From project description
    public static final String DB_PASS = "1234";             // From project description
    
    // UI Config
    public static final String APP_TITLE = "Group18 GreenGrocer";
    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 540;
    
    // Business Logic Constants
    // Business Logic Constants
    public static final double VAT_RATE = 0.18; // 18% VAT
    
    // Loyalty Defaults
    public static final int DEFAULT_LOYALTY_MIN_ORDER_COUNT = 5;
    public static final double DEFAULT_LOYALTY_DISCOUNT_RATE = 10.0; // percentage
}
