package com.group18.greengrocer.util;

/**
 * Global configuration constants for the GreenGrocer application.
 * <p>
 * This class serves as a central repository for application-wide settings, ensuring
 * consistency and easier maintenance. It includes configurations for:
 * <ul>
 * <li>Database connection credentials (JDBC).</li>
 * <li>UI dimensions and titles.</li>
 * <li>Business rules (Tax rates, Minimum cart values).</li>
 * <li>Loyalty program parameters.</li>
 * </ul>
 * @version 1.0
 */
public class Constants {
    // Database Config
    /**
     * The JDBC URL for connecting to the local MySQL database.
     */
    public static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
    /**
     * The database username.
     * <p>
     * Note: Project requirements specify 'myuser'@'localhost'. In JDBC, we provide
     * just the username ("myuser"), as the host is handled by the connection string.
     */
    public static final String DB_USER = "myuser"; // Project requires 'myuser'@'localhost', so we use "myuser" here
   /**
     * The database password.
     * <p>
     * <b>Security Note:</b> In a real production environment, passwords should not be
     * hardcoded in source files. This is acceptable for academic project scope.
     */
    public static final String DB_PASS = "1234";             // From project description

   /** The title displayed on the main application window header. */
    public static final String APP_TITLE = "Group18 GreenGrocer";
    /** Standard width for the application window in pixels. */
    public static final int WINDOW_WIDTH = 960;
    /** Standard height for the application window in pixels. */
    public static final int WINDOW_HEIGHT = 540;

   /**
     * Value Added Tax (VAT) rate.
     * <p>
     * Represented as a decimal. For example, 0.10 represents a 10% tax rate.
     * Used in final price calculations.
     */
    public static final double VAT_RATE = 0.10; // 10% VAT
    /**
     * The minimum monetary value (in TL) required to proceed to checkout.
     */
    public static final double MIN_CART_VALUE = 100.0;

   /**
     * The minimum number of completed orders required to reach the first loyalty tier.
     */
    public static final int DEFAULT_LOYALTY_MIN_ORDER_COUNT = 5;
    /**
     * The default discount percentage applied for loyalty members.
     * <p>
     * Note: This is stored as a whole number (e.g., 10.0 for 10%).
     */
    public static final double DEFAULT_LOYALTY_DISCOUNT_RATE = 10.0; // percentage
}
