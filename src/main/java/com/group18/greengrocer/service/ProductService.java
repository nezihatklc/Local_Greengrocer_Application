package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.ProductDAO;
import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.util.ValidatorUtil;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * ProductService
 * Business logic for product browsing and owner management.
 *
 * Rules:
 * - Controllers MUST NOT run SQL.
 * - DAO does raw DB operations.
 * - Service does validation + business rules.
 */
public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /* -------------------------
       CUSTOMER USE (Browsing)
       ------------------------- */

    /**
     * Retrieves all products available in the catalog.
     * Rule: products with zero stock must not be displayed -> handled by DAO findAvailableProducts().
     */
    public List<Product> getAllProducts() {
        return productDAO.findAvailableProducts();
    }

    /**
     * Retrieves products filtered by category (FRUIT or VEGETABLE).
     * Safer overload: use Category enum.
     */
    public List<Product> getProductsByCategory(Category category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null.");

        return productDAO.findAvailableProducts()
                .stream()
                .filter(p -> p.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * String-based category filter (for UI usage).
     * Accepts: "FRUIT" or "VEGETABLE" (case-insensitive).
     */
    public List<Product> getProductsByCategory(String category) {
        if (ValidatorUtil.isEmpty(category)) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }

        final Category parsed;
        try {
            parsed = Category.valueOf(category.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category. Use FRUIT or VEGETABLE.");
        }

        return getProductsByCategory(parsed);
    }

    /**
     * Retrieves a single product by its ID.
     */
    public Product getProductById(int productId) {
        if (productId <= 0) throw new IllegalArgumentException("Invalid product id.");
        return productDAO.findById(productId);
    }

    /**
     * Checks if there is enough stock for a requested quantity.
     * Required by rules: zero/negative quantity must be handled.
     */
    public boolean checkStockAvailability(int productId, double quantity) {
        if (productId <= 0) throw new IllegalArgumentException("Invalid product id.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0.");

        Product product = productDAO.findById(productId);
        return product != null && product.getStock() >= quantity;
    }

    /**
     * Searches for products matching a keyword (case-insensitive).
     * If keyword is empty -> return all available products (simple UX).
     */
    public List<Product> searchProducts(String keyword) {
        if (ValidatorUtil.isEmpty(keyword)) {
            return getAllProducts();
        }

        String key = keyword.trim().toLowerCase(Locale.ROOT);

        return productDAO.findAvailableProducts()
                .stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(key))
                .collect(Collectors.toList());
    }

    /* -------------------------
       OWNER USE (Management)
       ------------------------- */

       

    /**
    * Retrieves ALL products (including out-of-stock ones).
    * Rule: Owner must be able to view and manage all products.
    */
     
    public List<Product> getAllProductsForOwner() {
         return productDAO.findAll();
    }


    /**
     * Calculates effective price based on threshold rule.
     * Rule: If stock <= threshold -> price doubled.
     * Rule: threshold <= 0 must be handled explicitly.
     */
    public double getEffectivePrice(Product product) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null.");
        if (product.getPrice() < 0) throw new IllegalArgumentException("Price cannot be negative.");
        if (product.getThreshold() <= 0) throw new IllegalArgumentException("Threshold must be > 0.");

        return (product.getStock() <= product.getThreshold())
                ? product.getPrice() * 2.0
                : product.getPrice();
    }

    /**
     * Adds a new product to the catalog.
     * Rules: no magic numbers, validate input; stock/threshold must be non-negative/positive.
     */
    public void addProduct(Product product) {
        validateProductForUpsert(product, false);

        productDAO.insert(product);
        // Exception will be thrown if failed
    }

    /**
     * Updates an existing product's details.
     */
    public void updateProduct(Product product) {
        validateProductForUpsert(product, true);

        boolean ok = productDAO.update(product);
        if (!ok) throw new IllegalStateException("Failed to update product.");
    }

    /**
     * Removes a product from the catalog.
     */
    public void removeProduct(int productId) {
        if (productId <= 0) throw new IllegalArgumentException("Invalid product id.");

        // Check if product is in use to prevent FK Violation
        if (productDAO.isProductInUse(productId)) {
            // Soft delete (Archive) instead of throwing error
            boolean ok = productDAO.softDelete(productId);
            if (!ok) throw new IllegalStateException("Failed to archive product.");
            return;
        }

        boolean ok = productDAO.delete(productId);
        if (!ok) throw new IllegalStateException("Failed to remove product.");
    }

    /**
     * Updates the stock quantity of a product.
     * quantity can be positive (add) or negative (remove).
     * Rule: stock must not become negative.
     */
    public void updateStock(int productId, double quantity) {
        if (productId <= 0) throw new IllegalArgumentException("Invalid product id.");

        Product product = productDAO.findById(productId);
        if (product == null) throw new IllegalArgumentException("Product not found.");

        double newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }

        product.setStock(newStock);
        boolean ok = productDAO.update(product);
        if (!ok) throw new IllegalStateException("Failed to update stock.");
    }

    /**
     * Sets the price threshold for a product.
     * Rule: threshold must be > 0, otherwise invalid.
     */
    public void setPriceThreshold(int productId, double threshold) {
        if (productId <= 0) throw new IllegalArgumentException("Invalid product id.");
        if (threshold <= 0) throw new IllegalArgumentException("Threshold must be > 0.");

        Product product = productDAO.findById(productId);
        if (product == null) throw new IllegalArgumentException("Product not found.");

        product.setThreshold(threshold);
        boolean ok = productDAO.update(product);
        if (!ok) throw new IllegalStateException("Failed to update threshold.");
    }

    /* -------------------------
       Internal validation
       ------------------------- */

    private void validateProductForUpsert(Product product, boolean requireId) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null.");

        if (requireId && product.getId() <= 0) {
            throw new IllegalArgumentException("Product id must be set for update.");
        }

        if (ValidatorUtil.isEmpty(product.getName())) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }

        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Product category is required.");
        }

        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be > 0.");
        }

        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative.");
        }

        if (product.getThreshold() <= 0) {
            throw new IllegalArgumentException("Product threshold must be > 0.");
        }

    }
}
