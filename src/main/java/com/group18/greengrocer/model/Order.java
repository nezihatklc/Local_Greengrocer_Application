package com.group18.greengrocer.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    
    public enum Status {
        AVAILABLE,
        SELECTED,
        COMPLETED,
        CANCELLED
    }

    private int id;
    private int customerId;
    private Integer carrierId; // Nullable
    private Timestamp orderTime;
    private Timestamp deliveryTime;
    private Status status;
    private double totalCost;
    private Integer usedCouponId; // Nullable
    private String invoice;
    
    private List<CartItem> items = new ArrayList<>();

    public Order() {
        this.status = Status.AVAILABLE;
        this.orderTime = new Timestamp(System.currentTimeMillis());
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

    public Integer getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Integer carrierId) {
        this.carrierId = carrierId;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public Timestamp getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Timestamp deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public Integer getUsedCouponId() {
        return usedCouponId;
    }

    public void setUsedCouponId(Integer usedCouponId) {
        this.usedCouponId = usedCouponId;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    
    public void addItem(CartItem item) {
        this.items.add(item);
    }
}
