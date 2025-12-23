package com.group18.greengrocer.model;

import java.sql.Date;

/**
 * Represents a Discount Coupon.
 * Corresponds to the 'Coupons' table in the database.
 */
public class Coupon {

    /**
     * Unique identifier for the coupon.
     * Corresponds to 'id' column.
     */
    private int id;

    /**
     * The unique code string (e.g., "WELCOME20").
     * Corresponds to 'code' column.
     */
    private String code;

    /**
     * The fixed discount amount (currency value).
     * Corresponds to 'discount_amount' column.
     */
    private double discountAmount;

    /**
     * The expiration date of the coupon.
     * Corresponds to 'expiry_date' column.
     */
    private Date expiryDate;

    /**
     * Indicates if the coupon is currently active.
     * Corresponds to 'is_active' column.
     */
    private boolean isActive;

    /**
     * Default constructor.
     * Sets active to true by default.
     */
    public Coupon() {
        this.isActive = true;
    }

    /**
     * Constructor for creating a new Coupon.
     *
     * @param code           The unique code.
     * @param discountAmount The discount amount.
     * @param expiryDate     The expiration date.
     */
    public Coupon(String code, double discountAmount, Date expiryDate) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.expiryDate = expiryDate;
        this.isActive = true;
    }

    /**
     * Full constructor for retrieving from database.
     *
     * @param id             The unique ID.
     * @param code           The unique code.
     * @param discountAmount The discount amount.
     * @param expiryDate     The expiration date.
     * @param isActive       Active status.
     */
    public Coupon(int id, String code, double discountAmount, Date expiryDate, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountAmount = discountAmount;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", discountAmount=" + discountAmount +
                ", expiryDate=" + expiryDate +
                ", isActive=" + isActive +
                '}';
    }

    /**
     * Checks equality based on the unique ID.
     *
     * @param o Object to compare.
     * @return True if IDs are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coupon)) return false;
        Coupon coupon = (Coupon) o;
        return id == coupon.id;
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
