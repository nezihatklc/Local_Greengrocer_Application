package com.group18.greengrocer.model;

import java.sql.Timestamp;

/**
 * Represents a rating given by a Customer to a Product.
 * Corresponds to the 'ProductRatings' table.
 */
public class ProductRating {

    private int id;
    private int customerId;
    private int productId;
    private int rating;
    private Timestamp createdAt;

    public ProductRating() {
    }

    public ProductRating(int customerId, int productId, int rating) {
        this.customerId = customerId;
        this.productId = productId;
        this.rating = rating;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
