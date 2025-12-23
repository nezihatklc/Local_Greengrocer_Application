package com.group18.greengrocer.model;

import java.sql.Timestamp;

/**
 * Represents a rating given by a Customer to a Carrier for a specific order.
 * Corresponds to the 'CarrierRatings' table in the database.
 */
public class CarrierRating {

    /**
     * Unique identifier for the rating.
     * Corresponds to 'id'.
     */
    private int id;

    /**
     * The ID of the order being rated.
     * Each order can have only one rating.
     * Corresponds to 'order_id'.
     */
    private int orderId;

    /**
     * The ID of the customer giving the rating.
     * Corresponds to 'customer_id'.
     */
    private int customerId;

    /**
     * The ID of the carrier being rated.
     * Corresponds to 'carrier_id'.
     */
    private int carrierId;

    /**
     * The numerical rating value (typically 1-5).
     * Corresponds to 'rating'.
     */
    private int rating;

    /**
     * Optional text comment describing the experience.
     * Corresponds to 'comment'.
     */
    private String comment;

    /**
     * The timestamp when the rating was created.
     * Corresponds to 'created_at'.
     */
    private Timestamp createdAt;

    /**
     * Default constructor.
     */
    public CarrierRating() {
    }

    /**
     * Constructor for creating a new Rating.
     *
     * @param orderId    The order ID.
     * @param customerId The customer ID.
     * @param carrierId  The carrier ID.
     * @param rating     The rating score (1-5).
     * @param comment    The optional comment.
     */
    public CarrierRating(int orderId, int customerId, int carrierId, int rating, String comment) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Full constructor for retrieving from database.
     *
     * @param id         The unique ID.
     * @param orderId    The order ID.
     * @param customerId The customer ID.
     * @param carrierId  The carrier ID.
     * @param rating     The rating score.
     * @param comment    The comment.
     * @param createdAt  The creation time.
     */
    public CarrierRating(int id, int orderId, int customerId, int carrierId, int rating, String comment, Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CarrierRating{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }

    /**
     * Checks equality based on ID.
     *
     * @param o Object to compare.
     * @return True if IDs match.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarrierRating)) return false;
        CarrierRating that = (CarrierRating) o;
        return id == that.id;
    }

    /**
     * HashCode based on ID.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
