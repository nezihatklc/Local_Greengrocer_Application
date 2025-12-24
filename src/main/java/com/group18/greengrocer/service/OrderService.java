package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.CarrierRatingDAO;
import com.group18.greengrocer.dao.OrderDAO;
import com.group18.greengrocer.model.CarrierRating;
import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;


public class OrderService {

    private final OrderDAO orderDAO;

    private final List<CartItem> cart;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.cart = new ArrayList<>();
    }

    /**
     * Retrieves the current active shopping cart for a user.
     * If no active cart exists, creates a new pending order.
     * 
     * @param userId The ID of the customer.
     * @return The Order object representing the cart.
     */
    // ASSIGNED TO: Customer
    public Order getCart(int userId) {
        Order cartOrder = new Order();
        cartOrder.setCustomerId(userId);
        cartOrder.setItems(cart);
        return cartOrder;
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
        for (CartItem item : cart) {
            if (item.getProduct() != null && item.getProduct().getId() == productId) {
                item.setQuantity(item.getQuantity() + amount);
                return;
            }
    }

        CartItem newItem = new CartItem();
        newItem.setQuantity(amount);
        cart.add(newItem);
    }


    /**
     * Removes a specific product from the cart.
     * 
     * @param userId The ID of the customer.
     * @param productId The ID of the product to remove.
     */
    // ASSIGNED TO: Customer
    public void removeFromCart(int userId, int productId) {
        cart.removeIf(item ->
                item.getProduct() != null && item.getProduct().getId() == productId
        );
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
                for (CartItem item : cart) {
            if (item.getProduct() != null && item.getProduct().getId() == productId) {
                if (amount <= 0) {
                    cart.remove(item);
                } else {
                    item.setQuantity(amount);
                }
                return;
            }
        }
    }

    /**
     * Finalizes the order.
     * Validates stock, calculates final price including VAT and discounts, and updates order status.
     * 
     * @param order The order to be checked out.
     */
    // ASSIGNED TO: Customer
    public void checkout(Order order) {
        order.setOrderTime(new Timestamp(System.currentTimeMillis()));
        order.setStatus(Order.Status.AVAILABLE);
        orderDAO.createOrder(order);
        cart.clear();
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
        orderDAO.cancelOrder(orderId);
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
        // 1. Check if the order exists
    Order order = orderDAO.findOrderById(orderId);
    if (order == null) {
        return;
    }

    // 2. Only COMPLETED orders can be rated
    if (order.getStatus() != Order.Status.COMPLETED) {
        return;
    }

    CarrierRatingDAO ratingDAO = new CarrierRatingDAO();

    // 3. Prevent duplicate ratings for the same order
    if (ratingDAO.hasRated(orderId)) {
        return;
    }

    // 4. Validate rating range (1–5)
    if (rating < 1 || rating > 5) {
        return;
    }

    // 5. Create CarrierRating object using existing setters
    CarrierRating carrierRating = new CarrierRating();
    carrierRating.setOrderId(order.getId());
    carrierRating.setCustomerId(order.getCustomerId());
    carrierRating.setCarrierId(order.getCarrierId());
    carrierRating.setRating(rating);
    carrierRating.setComment(comment);
    // created_at is optional; DB may also set it automatically
    carrierRating.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

    // 6. Save rating to database
    ratingDAO.addRating(carrierRating);
    }

    /**
     * Retrieves past orders for a specific customer.
     * 
     * @param userId The ID of the customer.
     * @return List of customer's orders.
     */
    // ASSIGNED TO: Customer
    public List<Order> getOrdersByCustomer(int userId) {
        return orderDAO.findOrdersByCustomerId(userId);
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
