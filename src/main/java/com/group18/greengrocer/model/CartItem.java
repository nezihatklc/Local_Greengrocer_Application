package com.group18.greengrocer.model;

/**
 * Represents an item in a shopping cart or a line item in an order.
 * Corresponds to the 'OrderItems' table in the database.
 * Links a Product to a specific Quantity and Price.
 */
public class CartItem {

    /**
     * Unique identifier for this item line.
     * Corresponds to 'id' in 'OrderItems'.
     */
    private int id;

    /**
     * The product being purchased.
     * Corresponds to 'product_id'.
     */
    private Product product;

    /**
     * The quantity of the product (e.g., kg).
     * Corresponds to 'quantity'.
     */
    private double quantity;

    /**
     * The price of the product at the moment of purchase.
     * Use this, not product.getPrice(), for order history to preserve historical pricing.
     * Corresponds to 'price_at_purchase'.
     */
    private double priceAtPurchase;
    
    /**
     * The ID of the order this item belongs to.
     * Null if the item is in a transient shopping cart.
     * Corresponds to 'order_id'.
     */
    private Integer orderId; 

    /**
     * Default constructor.
     */
    public CartItem() {
    }

    /**
     * Constructor for creating a new item in a shopping cart.
     * Price is automatically obtained from the product.
     *
     * @param product  The product to add.
     * @param quantity The quantity.
     */
    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = product != null ? product.getPrice() : 0.0;
    }

    /**
     * Full constructor for retrieving from the database.
     *
     * @param id              The order item ID.
     * @param orderId         The order ID.
     * @param product         The product object.
     * @param quantity        The quantity.
     * @param priceAtPurchase The historical price.
     */
    public CartItem(int id, Integer orderId, Product product, double quantity, double priceAtPurchase) {
        this.id = id;
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(double priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
    
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    /**
     * Calculates the total price for this line item.
     *
     * @return quantity * priceAtPurchase
     */
    public double getTotalPrice() {
        return priceAtPurchase * quantity;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "product=" + (product != null ? product.getName() : "null") +
                ", quantity=" + quantity +
                ", price=" + priceAtPurchase +
                ", total=" + getTotalPrice() +
                '}';
    }

    /**
     * Checks equality based on the Product.
     * Optimized for shopping cart logic: two items with the same product
     * are considered the "same item" (and should be merged).
     *
     * @param o Object to compare.
     * @return True if products are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem cartItem = (CartItem) o;
        return product != null ? product.equals(cartItem.product) : cartItem.product == null;
    }

    /**
     * HashCode based on Product.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return product != null ? product.hashCode() : 0;
    }
}
