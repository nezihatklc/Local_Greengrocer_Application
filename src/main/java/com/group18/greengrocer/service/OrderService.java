package com.group18.greengrocer.service;

import com.group18.greengrocer.model.Order;
import java.util.List;
import java.util.Date;

public class OrderService {

    /**
     * Retrieves the current active shopping cart for a user.
     * If no active cart exists, creates a new pending order.
     * 
     * @param userId The ID of the customer.
     * @return The Order object representing the cart.
     */
    // ASSIGNED TO: Customer
    public Order getCart(int userId) {
        return null;
    }

    /**
     * Adds a product to the user's shopping cart.
     * Handles stock checks and merges duplicate items.
     * 
     * @param userId The ID of the customer.
     * @param productId The ID of the product.
     * @param amount The quantity to add (e.g., in kg).
     */
    // ASSIGNED TO: Customer
    public void addToCart(int userId, int productId, double amount) {
    }

    /**
     * Removes a specific product from the cart.
     * 
     * @param userId The ID of the customer.
     * @param productId The ID of the product to remove.
     */
    // ASSIGNED TO: Customer
    public void removeFromCart(int userId, int productId) {
    }

    /**
     * Updates the quantity of a product in the cart.
     * 
     * @param userId The ID of the customer.
     * @param productId The ID of the product.
     * @param amount The new quantity.
     */
    // ASSIGNED TO: Customer
    public void updateCartItem(int userId, int productId, double amount) {
    }

    /**
     * Finalizes the order.
     * Validates stock, calculates final price including VAT and discounts, and updates order status.
     * 
     * @param order The order to be checked out.
     */
    // ASSIGNED TO: Customer
    public void checkout(Order order) {
    }

    /**
     * Retrieves all orders that are ready to be picked up by carriers.
     * Order status must be 'AVAILABLE'.
     * 
     * @return List of pending orders.
     */
    // ASSIGNED TO: Carrier
    public List<Order> getPendingOrders() {
        return null;
    }

    /**
     * Assigns an available order to a carrier.
     * Order status changes to 'SELECTED'.
     * 
     * @param orderId The ID of the order.
     * @param carrierId The ID of the carrier.
     */
    // ASSIGNED TO: Carrier
    public void assignOrderToCarrier(int orderId, int carrierId) {
    }

    /**
     * Marks an order as completed and delivered.
     * Updates delivery time and payment status.
     * 
     * @param orderId The ID of the order.
     * @param deliveryDate The date/time when it was delivered.
     */
    // ASSIGNED TO: Carrier
    public void completeOrder(int orderId, Date deliveryDate) {
    }

    /**
     * Cancels an order.
     * Can only be cancelled if not yet delivered (depending on business rule).
     * 
     * @param orderId The ID of the order.
     */
    // ASSIGNED TO: Customer
    public void cancelOrder(int orderId) {
    }

    /**
     * Retrieves all orders for administrative view.
     * 
     * @return List of all orders in the system.
     */
    // ASSIGNED TO: Owner
    public List<Order> getAllOrders() {
        return null;
    }

    /**
     * Allows a customer to rate a completed order/carrier.
     * 
     * @param orderId The ID of the order.
     * @param rating Rating value (1-5).
     * @param comment Optional comment.
     */
    // ASSIGNED TO: Customer
    public void rateOrder(int orderId, int rating, String comment) {
    }

    /**
     * Retrieves past orders for a specific customer.
     * 
     * @param userId The ID of the customer.
     * @return List of customer's orders.
     */
    // ASSIGNED TO: Customer
    public List<Order> getOrdersByCustomer(int userId) {
        return null;
    }

    /**
     * Retrieves orders assigned to or completed by a carrier.
     * 
     * @param carrierId The ID of the carrier.
     * @return List of orders associated with the carrier.
     */
    // ASSIGNED TO: Carrier
    public List<Order> getOrdersByCarrier(int carrierId) {
        return null;
    }

    /**
     * Generates or retrieves the textual invoice for an order.
     * Invoice is stored as CLOB in DB.
     * 
     * @param orderId The ID of the order.
     * @return The invoice text content.
     */
    // ASSIGNED TO: Carrier (Documentation Specialist)
    public String getInvoice(int orderId) {
        return null;
    }
}
