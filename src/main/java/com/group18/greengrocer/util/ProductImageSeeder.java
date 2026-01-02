package com.group18.greengrocer.util;

import com.group18.greengrocer.dao.DatabaseAdapter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility to seed product images into the database as BLOBs.
 * Reads images from src/main/resources/com/group18/greengrocer/images/products/
 */
public class ProductImageSeeder {

    private static final String RESOURCE_BASE_PATH = "/com/group18/greengrocer/images/products/";

    // Mapping: DB Product Name -> Resource Filename
    private static final Map<String, String> IMAGE_MAP = new LinkedHashMap<>() {{
        put("Tomato", "tomato.png");
        put("Potato", "Potato.png"); // Note: Capital P in filename
        put("Onion", "onion.png");
        put("Cucumber", "cucumber.png");
        put("Pepper", "pepper.png");
        put("Carrot", "carrot.png");
        put("Lettuce", "lettuce.png");
        put("Spinach", "spinach.png");
        put("Broccoli", "broccoli.png");
        put("Cauliflower", "cauliflower.png");
        put("Eggplant", "eggplant.png");
        put("Zucchini", "zucchini.png");
        put("Garlic", "garlic.png");

        put("Apple", "apple.png");
        put("Banana", "banana.png");
        put("Orange", "orange.png");
        put("Strawberry", "strawberry.png");
        put("Grapes", "grapes.png");
        put("Watermelon", "watermelon.png");
        put("Melon", "melon.png");
        put("Peach", "peach.png");
        put("Pear", "pear.png");
        put("Cherry", "cherry.png");
        put("Kiwi", "kiwi.png");
        put("Pineapple", "pineapple.png");
        put("Mango", "mango.png");
    }};

    /**
     * Seeds product images into the DB.
     * @param conn The database connection.
     * @throws Exception If an error occurs during seeding.
     */
    public static void seedProductImagesToDb(Connection conn) throws Exception {
        String sql = "UPDATE ProductInfo SET imagelocation=? WHERE name=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<String, String> e : IMAGE_MAP.entrySet()) {
                String productName = e.getKey();
                String fileName = e.getValue();
                String resourcePath = RESOURCE_BASE_PATH + fileName;

                try (InputStream is = ProductImageSeeder.class.getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.err.println("IMAGE NOT FOUND in resources: " + resourcePath + " for product: " + productName);
                        continue;
                    }
                    byte[] bytes = is.readAllBytes();

                    ps.setBytes(1, bytes);
                    ps.setString(2, productName);
                    int updated = ps.executeUpdate();

                    if (updated == 0) {
                        System.out.println("SKIPPED: Product not found in DB: " + productName);
                    } else {
                        System.out.println("SUCCESS: Seeded " + productName + " (" + bytes.length + " bytes)");
                    }
                }
            }
        }
    }

    /**
     * Main method to run the seeder independently.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        System.out.println("Starting Product Image Seeder...");
        try (Connection conn = DatabaseAdapter.getInstance().getConnection()) {
            seedProductImagesToDb(conn);
            System.out.println("Seeding completed successfully.");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Seeding error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
