package com.group18.greengrocer.model;

public class CartItem {
    private int id; // Optional (if tracking cart in DB separately, but OrderItems has its own ID)
    private Product product;
    private double quantity;
    private double priceAtPurchase; // To store price at the time of purchase

    public CartItem() {
    }

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = product.getPrice();
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
    
    // Calculated total price for this item
    public double getTotalPrice() {
        return priceAtPurchase * quantity;
    }
}
