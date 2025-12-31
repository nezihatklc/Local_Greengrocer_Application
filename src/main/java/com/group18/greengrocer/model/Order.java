package com.group18.greengrocer.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Customer Order.
 * Corresponds to the 'OrderInfo' table in the database.
 * Aggregates order details, status, and the list of items purchased.
 */
public class Order {
    
    /**
     * Enumeration of possible Order statuses.
     * Corresponds to the 'status' column in 'OrderInfo'.
     */
    public enum Status {
        /**
         * Order placed by customer, waiting to be picked up by a carrier.
         */
        AVAILABLE,
        
        /**
         * Order selected by a carrier and is currently in progress.
         */
        SELECTED,
        
        /**
         * Order successfully delivered to the customer.
         */
        COMPLETED,
        
        /**
         * Order cancelled.
         */
        CANCELLED;

        /**
         * Returns a user-friendly string representation of the status.
         * Example: "Available", "Selected".
         */
        @Override
        public String toString() {
            String name = name();
            return name.charAt(0) + name.substring(1).toLowerCase();
        }
    }

    /**
     * Unique identifier for the order.
     * Corresponds to 'id'.
     */
    private int id;

    /**
     * The ID of the customer who placed the order.
     * Corresponds to 'customer_id'.
     */
    private int customerId;

    /**
     * The ID of the carrier delivering the order (can be null if not yet selected).
     * Corresponds to 'carrier_id'.
     */
    private Integer carrierId; 

    /**
     * The time the order was placed.
     * Corresponds to 'ordertime'.
     */
    private Timestamp orderTime;

    /**
     * The time the order was delivered (can be null).
     * Corresponds to 'deliverytime'.
     */
    private Timestamp deliveryTime;

    /**
     * The requested delivery date by the customer.
     * Corresponds to 'requested_delivery_date'.
     */
    private Timestamp requestedDeliveryDate;

    /**
     * The current status of the order.
     * Corresponds to 'status'.
     */
    private Status status;

    /**
     * The total cost of the order (including VAT and discounts).
     * Corresponds to 'totalcost'.
     */
    private double totalCost;

    /**
     * The ID of the coupon used (if any).
     * Corresponds to 'used_coupon_id'.
     */
    private Integer usedCouponId; 

    /**
     * The invoice content, typically stored as a JSON or String representation.
     * Corresponds to 'invoice'.
     */
    private String invoice;
    
    /**
     * The list of products (items) included in this order.
     * This is populated from the 'OrderItems' table.
     */
    private List<CartItem> items = new ArrayList<>();

    /**
     * Default constructor.
     * Initializes status to AVAILABLE and orderTime to current time.
     */
    public Order() {
        this.status = Status.AVAILABLE;
        this.orderTime = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Full Constructor for retrieving an Order from the database.
     *
     * @param id           The order ID.
     * @param customerId   The customer ID.
     * @param carrierId    The carrier ID.
     * @param orderTime    The order placement time.
     * @param deliveryTime The delivery time.
     * @param status       The order status.
     * @param totalCost    The total cost.
     * @param usedCouponId The ID of the used coupon.
     * @param invoice      The invoice data.
     */
    public Order(int id, int customerId, Integer carrierId, Timestamp orderTime, Timestamp deliveryTime, 
                 Status status, double totalCost, Integer usedCouponId, String invoice) {
        this.id = id;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.orderTime = orderTime;
        this.deliveryTime = deliveryTime;
        this.status = status;
        this.totalCost = totalCost;
        this.usedCouponId = usedCouponId;
        this.invoice = invoice;
    }

    /**
     * Constructor for creating a new Order.
     *
     * @param customerId   The customer ID.
     * @param totalCost    The total cost.
     * @param usedCouponId The ID of the used coupon (can be null).
     */
    public Order(int customerId, double totalCost, Integer usedCouponId) {
        this.customerId = customerId;
        this.totalCost = totalCost;
        this.usedCouponId = usedCouponId;
        this.status = Status.AVAILABLE;
        this.orderTime = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters

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

    public Timestamp getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }

    public void setRequestedDeliveryDate(Timestamp requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
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
    
    /**
     * Adds an item to the order.
     * 
     * @param item The CartItem to add.
     */
    public void addItem(CartItem item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", status=" + status +
                ", totalCost=" + totalCost +
                ", items=" + items.size() +
                '}';
    }

    /**
     * Checks if this order is equal to another object.
     * Equality is based on the unique ID of the order.
     *
     * @param o The object to compare with.
     * @return True if objects represent the same order, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    /**
     * Returns the hash code for this order.
     * Based on the unique ID.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
