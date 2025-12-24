package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ProductInfo table.
 * Follows Rule 1.1: Storage Access only, no business logic.
 */
public class ProductDAO {

    private DatabaseAdapter dbAdapter;

    public ProductDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Retrieves all products sorted by name (Rule 4.1).
     * 
     * @return List of all products.
     */
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo ORDER BY name ASC";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Retrieves all available products (stock > 0), sorted by name.
     * Use this for Customer Interface (Rule 4.1).
     * 
     * @return List of available products.
     */
    public List<Product> findAvailableProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE stock > 0 ORDER BY name ASC";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Searches for products by name matching the keyword.
     * Case-insensitive search using LIKE %keyword%.
     * 
     * @param keyword The search keyword.
     * @return List of matching products.
     */
    public List<Product> searchByName(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE name LIKE ? ORDER BY name ASC";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Retrieves products filtered by category.
     * 
     * @param category The category to filter by (FRUIT or VEGETABLE).
     * @return List of products in the category.
     */
    public List<Product> findByCategory(Category category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE category = ? ORDER BY name ASC";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Finds a product by ID.
     * 
     * @param id The product ID.
     * @return The Product object or null if not found.
     */
    public Product findById(int id) {
        String sql = "SELECT * FROM ProductInfo WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts a new product into the database.
     * 
     * @param product The product to insert.
     * @return true if successful.
     */
    public boolean insert(Product product) {
        String sql = "INSERT INTO ProductInfo (name, category, type, price, stock, threshold, imagelocation, unit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory().name());
            stmt.setString(3, product.getType());
            stmt.setDouble(4, product.getPrice());
            stmt.setDouble(5, product.getStock());
            stmt.setDouble(6, product.getThreshold());
            stmt.setBytes(7, product.getImage()); // BLOB handling
            stmt.setString(8, product.getUnit());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates an existing product.
     * 
     * @param product The product to update.
     * @return true if successful.
     */
    public boolean update(Product product) {
        String sql = "UPDATE ProductInfo SET name = ?, category = ?, type = ?, price = ?, stock = ?, threshold = ?, imagelocation = ?, unit = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory().name());
            stmt.setString(3, product.getType());
            stmt.setDouble(4, product.getPrice());
            stmt.setDouble(5, product.getStock());
            stmt.setDouble(6, product.getThreshold());
            stmt.setBytes(7, product.getImage()); // BLOB handling
            stmt.setString(8, product.getUnit());
            stmt.setInt(9, product.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a product by ID.
     * 
     * @param id The product ID.
     * @return true if successful.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM ProductInfo WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        
        // Category Enum
        String catStr = rs.getString("category");
        if (catStr != null) {
            try {
                p.setCategory(Category.valueOf(catStr));
            } catch (IllegalArgumentException e) {
                p.setCategory(null);
            }
        }
        
        p.setType(rs.getString("type")); // String type
        
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getDouble("stock"));
        p.setThreshold(rs.getDouble("threshold"));
        p.setImage(rs.getBytes("imagelocation")); // BLOB retrieval
        p.setUnit(rs.getString("unit"));
        return p;
    }
}
