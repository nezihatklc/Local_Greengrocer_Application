package com.group18.greengrocer.service;

import com.group18.greengrocer.model.Product;
import java.util.List;

public class ProductService {

    /**
     * Retrieves all products available in the catalog.
     * 
     * @return List of all products.
     */
    public List<Product> getAllProducts() {
        return null;
    }

    /**
     * Retrieves products filtered by category (FRUIT or VEGETABLE).
     * 
     * @param category The category name.
     * @return List of products in the category.
     */
    public List<Product> getProductsByCategory(String category) {
        return null;
    }

    /**
     * Retrieves a single product by its ID.
     * 
     * @param productId The ID of the product.
     * @return The Product object, or null if not found.
     */
    public Product getProductById(int productId) {
        return null;
    }

    /**
     * Adds a new product to the catalog.
     * 
     * @param product The product to add.
     */
    public void addProduct(Product product) {
    }

    /**
     * Updates an existing product's details.
     * 
     * @param product The product with updated information.
     */
    public void updateProduct(Product product) {
    }

    /**
     * Removes a product from the catalog.
     * 
     * @param productId The ID of the product to remove.
     */
    public void removeProduct(int productId) {
    }

    /**
     * Updates the stock quantity of a product.
     * 
     * @param productId The ID of the product.
     * @param quantity The amount to add (positive) or remove (negative).
     */
    public void updateStock(int productId, double quantity) {
    }

    /**
     * Checks if there is enough stock for a requested quantity.
     * 
     * @param productId The ID of the product.
     * @param quantity The requested quantity.
     * @return true if stock is sufficient, false otherwise.
     */
    public boolean checkStockAvailability(int productId, double quantity) {
        return false;
    }

    /**
     * Sets the price threshold for a product.
     * If stock falls below this threshold, price is doubled.
     * 
     * @param productId The ID of the product.
     * @param threshold The threshold quantity.
     */
    public void setPriceThreshold(int productId, double threshold) {
    }

    /**
     * Searches for products matching a keyword.
     * 
     * @param keyword The search term.
     * @return List of matching products.
     */
    public List<Product> searchProducts(String keyword) {
        return null;
    }
}
