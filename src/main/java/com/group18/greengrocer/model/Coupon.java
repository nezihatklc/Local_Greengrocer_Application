package com.group18.greengrocer.model;

import java.sql.Date; // Using sql.Date for JDBC compatibility, or LocalDate and conversion

public class Coupon {
    private int id;
    private String code;
    private double discountAmount;
    private Date expiryDate;
    private boolean isActive;

    public Coupon() {
        this.isActive = true;
    }

    public Coupon(String code, double discountAmount, Date expiryDate) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.expiryDate = expiryDate;
        this.isActive = true;
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
}
