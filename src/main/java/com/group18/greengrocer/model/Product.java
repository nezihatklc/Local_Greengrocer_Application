package com.group18.greengrocer.model;


/**
 * Represents a Product in the inventory.
 * Corresponds to the 'ProductInfo' table in the database.
 * Products are categorized as FRUIT or VEGETABLE.
 */
public class Product {

    /**
     * Unique identifier for the product.
     * Corresponds to the 'id' column.
     */
    private int id;

    /**
     * The name of the product (e.g., "Tomato", "Apple").
     * Corresponds to the 'name' column.
     */
    private String name;

    /**
     * The category of the product (FRUIT or VEGETABLE).
     * Corresponds to the 'category' column.
     */
    private Category category;

    /**
     * The specific type/variety of the product (e.g., "Vine", "Gala").
     * Corresponds to the 'type' column.
     */
    private String type;

    /**
     * The price per unit of the product.
     * Corresponds to the 'price' column.
     */
    private double price;

    /**
     * The current stock quantity available.
     * Corresponds to the 'stock' column.
     */
    private double stock;

    /**
     * The threshold quantity below which the price is doubled.
     * Corresponds to the 'threshold' column.
     */
    private double threshold;

    /**
     * The image data for the product, stored as a byte array (BLOB).
     * Corresponds to the 'imagelocation' column.
     */
    private byte[] image;

    /**
     * The unit of measurement for the product (e.g., "kg").
     * Corresponds to the 'unit' column.
     */
    private String unit;

    /**
     * Default constructor.
     */
    public Product() {
    }

    /**
     * Full constructor for retrieving a Product from the database.
     *
     * @param id        The unique identifier.
     * @param name      The product name.
     * @param category  The product category.
     * @param type      The product type/variety.
     * @param price     The price per unit.
     * @param stock     The available stock.
     * @param threshold The price doubling threshold.
     * @param image     The image data.
     * @param unit      The unit of measurement.
     */
    public Product(int id, String name, Category category, String type, double price, double stock, double threshold, byte[] image, String unit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.image = image;
        this.unit = unit;
    }

    /**
     * Constructor for creating a new Product (before ID is assigned).
     *
     * @param name      The product name.
     * @param category  The product category.
     * @param type      The product type/variety.
     * @param price     The price per unit.
     * @param stock     The available stock.
     * @param threshold The price doubling threshold.
     * @param image     The image data.
     * @param unit      The unit of measurement.
     */
    public Product(String name, Category category, String type, double price, double stock, double threshold, byte[] image, String unit) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.image = image;
        this.unit = unit;
    }

    /**
     * Simplified constructor for basic product info.
     * 
     * @param id        The product ID.
     * @param name      The product name.
     * @param category  The product category.
     * @param price     The product price.
     * @param stock     The current stock.
     */
    public Product(int id, String name, Category category, double price, double stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    /**
     * Gets the product ID.
     *
     * @return The product ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the product ID.
     *
     * @param id The new product ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the product name.
     *
     * @return The product name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product name.
     *
     * @param name The new product name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the product category.
     *
     * @return The product category.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets the product category.
     *
     * @param category The new product category.
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Gets the product type (variety).
     *
     * @return The product type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the product type.
     *
     * @param type The new product type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the price per unit.
     *
     * @return The price.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price per unit.
     *
     * @param price The new price.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the available stock.
     *
     * @return The stock quantity.
     */
    public double getStock() {
        return stock;
    }

    /**
     * Sets the available stock.
     *
     * @param stock The new stock quantity.
     */
    public void setStock(double stock) {
        this.stock = stock;
    }

    /**
     * Gets the price doubling threshold.
     *
     * @return The threshold quantity.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Sets the price doubling threshold.
     *
     * @param threshold The new threshold quantity.
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Gets the product image data.
     *
     * @return The image as a byte array.
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the product image data.
     *
     * @param image The new image as a byte array.
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * Gets the unit of measurement.
     *
     * @return The unit.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit of measurement.
     *
     * @param unit The new unit.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", threshold=" + threshold +
                ", unit='" + unit + '\'' +
                '}';
    }

    /**
     * Checks if this product is equal to another object.
     * Equality is based on the unique ID of the product.
     *
     * @param o The object to compare with.
     * @return True if the objects represent the same product (same ID), false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    /**
     * Returns the hash code for this product.
     * The hash code is based on the unique ID to align with equals().
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
