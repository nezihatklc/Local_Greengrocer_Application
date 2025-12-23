package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.ProductDAO;
import com.group18.greengrocer.model.Product;
import java.util.List;

public class ProductService {

   private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }


    /* ----- CUSTOMER USE ------ */
    /**
     * Retrieves all products available in the catalog.
     * 
     * @return List of all products.
     */
    // ASSIGNED TO: Customer (Browsing)
    public List<Product> getAllProducts() {
        return productDAO.findAvailableProducts();
    }

    /**
     * Retrieves products filtered by category (FRUIT or VEGETABLE).
     * 
     * @param category The category name.
     * @return List of products in the category.
     */
    // ASSIGNED TO: Customer (Browsing)
    public List<Product> getProductsByCategory(String category) {
       return productDAO.findAvailableProducts()
                .stream()
                .filter(p -> p.getCategory() != null &&
                             p.getCategory().name().equalsIgnoreCase(category))
                .toList();
    }

    /**
     * Retrieves a single product by its ID.
     * 
     * @param productId The ID of the product.
     * @return The Product object, or null if not found.
     */
    // ASSIGNED TO: Customer
    public Product getProductById(int productId) {
       return productDAO.findById(productId);
    }


     /**
     * Checks if there is enough stock for a requested quantity.
     * 
     * @param productId The ID of the product.
     * @param quantity The requested quantity.
     * @return true if stock is sufficient, false otherwise.
     */
    // ASSIGNED TO: Customer (Validation)
    public boolean checkStockAvailability(int productId, double quantity) {
        Product product = productDAO.findById(productId);
        return product != null && product.getStock() >= quantity;
    }


    /**
     * Calculates the effective price of a product based on its current stock level.
     * 
     * @param product the product whose effective price will be calculated
     * @return the effective price considering the stock threshold rule
     */
     // ASSIGNED TO: Owner (Pricing Logic)
     public double getEffectivePrice(Product product) {
        if (product.getStock() <= product.getThreshold()) {
            return product.getPrice() * 2;
        }
        return product.getPrice();
    }




     /**
     * Searches for products matching a keyword.
     * 
     * @param keyword The search term.
     * @return List of matching products.
     */
    // ASSIGNED TO: Customer
    public List<Product> searchProducts(String keyword) {
        return productDAO.findAvailableProducts()
                .stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }



      /* ----- OWNER USE ------ */

    /**
     * Adds a new product to the catalog.
     * 
     * @param product The product to add.
     */
    // ASSIGNED TO: Owner
    public void addProduct(Product product) {
        productDAO.insert(product);
    };

    /**
     * Updates an existing product's details.
     * 
     * @param product The product with updated information.
     */
    // ASSIGNED TO: Owner
    public void updateProduct(Product product) {
        productDAO.update(product);
    }

    /**
     * Removes a product from the catalog.
     * 
     * @param productId The ID of the product to remove.
     */
    // ASSIGNED TO: Owner
    public void removeProduct(int productId) {
        productDAO.delete(productId);
    }

    /**
     * Updates the stock quantity of a product.
     * 
     * @param productId The ID of the product.
     * @param quantity The amount to add (positive) or remove (negative).
     */
    // ASSIGNED TO: Owner (Stock Management)
    public void updateStock(int productId, double quantity) {
        Product product = productDAO.findById(productId);
        if (product == null) return;

        double newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        product.setStock(newStock);
        productDAO.update(product);
    }

   

    /**
     * Sets the price threshold for a product.
     * If stock falls below this threshold, price is doubled.
     * 
     * @param productId The ID of the product.
     * @param threshold The threshold quantity.
     */
    // ASSIGNED TO: Owner
    public void setPriceThreshold(int productId, double threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive");
        }

        Product product = productDAO.findById(productId);
        if (product == null) return;

        product.setThreshold(threshold);
        productDAO.update(product);
    }
    

   
}
