package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.CarrierRatingDAO;
import com.group18.greengrocer.dao.OrderDAO;
import com.group18.greengrocer.dao.ProductDAO;
import com.group18.greengrocer.model.CarrierRating;
import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.util.PDFGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class OrderService {

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final DiscountService discountService;

    
    private final List<CartItem> cart;


    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        this.discountService = new DiscountService();
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
         if (amount <= 0) {
        throw new IllegalArgumentException("Amount must be greater than zero.");
    }

    Product product = productDAO.findById(productId);
    if (product == null) {
        throw new IllegalArgumentException("Product not found.");
    }

    double alreadyInCart = 0;
    for (CartItem item : cart) {
        if (item.getProduct().getId() == productId) {
            alreadyInCart = item.getQuantity();
            break;
        }
    }

    if (product.getStock() < alreadyInCart + amount) {
        throw new IllegalStateException("Insufficient stock.");
    }

    for (CartItem item : cart) {
        if (item.getProduct().getId() == productId) {
            item.setQuantity(item.getQuantity() + amount);
            return;
        }
    }

    cart.add(new CartItem(product, amount));
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
        if (amount <= 0) {
        throw new IllegalArgumentException("Amount must be greater than zero.");
    }

    Product product = productDAO.findById(productId);
    if (product == null) {
        throw new IllegalArgumentException("Product not found.");
    }

    if (product.getStock() < amount) {
        throw new IllegalStateException("Insufficient stock.");
    }

    for (CartItem item : cart) {
        if (item.getProduct().getId() == productId) {
            item.setQuantity(amount);
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
        if (cart.isEmpty()) {
        throw new IllegalStateException("Cart is empty.");
    }

    double subtotal = 0.0;

    // 1. Final stock check + price calculation
    for (CartItem item : cart) {

        Product product = productDAO.findById(item.getProduct().getId());

        if (product == null || product.getStock() < item.getQuantity()) {
            throw new IllegalStateException("Stock changed. Please review your cart.");
        }

        double price = product.getPrice();

        // Threshold rule
        if (product.getStock() <= product.getThreshold()) {
            price *= 2;
        }

        item.setPriceAtPurchase(price);
        subtotal += price * item.getQuantity();
    }

    // 2. Discounts
    double discountAmount = 0.0;

    // Coupon
    if (order.getUsedCouponId() != null) {
        discountAmount += discountService.applyCoupon(
                order.getUsedCouponId(),
                subtotal
        );
    }

    // Loyalty
    long completedOrdersCount =
            orderDAO.findOrdersByCustomerId(order.getCustomerId())
                    .stream()
                    .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                    .count();

    discountAmount += discountService.applyLoyaltyDiscount(
            (int) completedOrdersCount,
            subtotal
    );

    double discountedTotal = subtotal - discountAmount;

    // 3. VAT 18%
    double finalTotal = discountedTotal * 1.18;

    order.setTotalCost(finalTotal);
    order.setOrderTime(new java.sql.Timestamp(System.currentTimeMillis()));
    order.setStatus(Order.Status.AVAILABLE);
    order.setItems(new ArrayList<>(cart));

    // 4. PDF Invoice Generation 
    String invoiceBase64 = PDFGenerator.generateInvoicePDF(order);
    order.setInvoice(invoiceBase64);

    // 5. Create order in DB
    boolean created = orderDAO.createOrder(order);

    if (!created) {
        throw new IllegalStateException("Order could not be created.");
    }

    // 6. Decrease stock AFTER successful order creation
    for (CartItem item : cart) {
        productDAO.updateStock(
                item.getProduct().getId(),
                -item.getQuantity()
        );
    }

    // 7. Clear cart
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
        return orderDAO.findAvailableOrders();
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
        boolean success = orderDAO.selectOrder(orderId, carrierId);

    if (!success) {
        throw new IllegalStateException("Order is no longer available or already selected by another carrier.");
      }
        
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
        Order order = orderDAO.findOrderById(orderId);

    if (order == null) {
        throw new IllegalArgumentException("Order not found.");
    }
     

    if (order.getStatus() != Order.Status.SELECTED) {
        throw new IllegalStateException(
            "Only selected orders can be completed.");
    }

    java.sql.Timestamp deliveryTimestamp =
            new java.sql.Timestamp(deliveryDate.getTime());

    boolean success = orderDAO.completeOrder(orderId, deliveryTimestamp);

    if (!success) {
        throw new IllegalStateException("Failed to complete the order.");
      }
    }

    /**
     * Cancels an order.
     * Can only be cancelled if not yet delivered (depending on business rule).
     * 
     * @param orderId The ID of the order.
     */
    // ASSIGNED TO: Customer
    public void cancelOrder(int orderId) {
        Order order = orderDAO.findOrderById(orderId);

        if (order == null) {
            return;
        }
        
        if (orderDAO.cancelOrder(orderId)) {
            
            // RESTORE STOCK
            for (CartItem item : order.getItems()) {
                
                Product product = productDAO.findById(item.getProduct().getId());
                if (product == null) {
                continue;
            }

            double restoredStock = product.getStock() + item.getQuantity();
            product.setStock(restoredStock);

            productDAO.update(product);
        }
    }
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
        return orderDAO.findOrdersByCarrierId(carrierId);
   
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
        Order order = orderDAO.findOrderById(orderId);

    if (order == null) {
        throw new IllegalArgumentException("Order not found.");
    }

    return order.getInvoice();
       
    }

    private String generateInvoiceText(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("INVOICE\n");
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("Customer ID: ").append(order.getCustomerId()).append("\n\n");
        
        for (CartItem item : order.getItems()) {
            sb.append(item.getProduct().getName())
            .append(" x ")
            .append(item.getQuantity())
            .append(" = ")
            .append(item.getTotalPrice())
            .append("\n");
        }
        
        sb.append("\nTOTAL (VAT included): ")
        .append(order.getTotalCost());
        
        return sb.toString();
}


}
