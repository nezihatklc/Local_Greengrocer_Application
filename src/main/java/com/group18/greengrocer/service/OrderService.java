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

    // === CUSTOMER CART STORAGE (USER-BASED) ===
    private static final java.util.Map<Integer, Order> userCarts = new java.util.HashMap<>();

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
        return userCarts.computeIfAbsent(userId, id -> {
            Order cartOrder = new Order();
            cartOrder.setCustomerId(id);
            cartOrder.setItems(new ArrayList<>());
            return cartOrder;
        });
    }

    /**
     * Adds a product to the user's shopping cart.
     * Handles stock checks and merges duplicate items.
     * 
     * @param userId    The ID of the customer.
     * @param productId The ID of the product.
     * @param amount    The quantity to add (e.g., in kg).
     */
    // ASSIGNED TO: Customer
    public void addToCart(int userId, int productId, double amount) {

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");

        Product product = productDAO.findById(productId);
        if (product == null)
            throw new IllegalArgumentException("Product not found.");

        Order cartOrder = getCart(userId);
        List<CartItem> cart = cartOrder.getItems();

        double alreadyInCart = 0;
        for (CartItem item : cart) {
            if (item.getProduct().getId() == productId) {
                alreadyInCart = item.getQuantity();
                break;
            }
        }

        if (product.getStock() < alreadyInCart + amount)
            throw new IllegalStateException("Insufficient stock.");

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
     * @param userId    The ID of the customer.
     * @param productId The ID of the product to remove.
     */
    // ASSIGNED TO: Customer
    public void removeFromCart(int userId, int productId) {
        Order cartOrder = getCart(userId);
        cartOrder.getItems().removeIf(
                item -> item.getProduct() != null &&
                        item.getProduct().getId() == productId);
    }

    /**
     * Updates the quantity of a product in the cart.
     * 
     * @param userId    The ID of the customer.
     * @param productId The ID of the product.
     * @param amount    The new quantity.
     */
    // ASSIGNED TO: Customer
    public void updateCartItem(int userId, int productId, double amount) {

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");

        Product product = productDAO.findById(productId);
        if (product == null)
            throw new IllegalArgumentException("Product not found.");

        if (product.getStock() < amount)
            throw new IllegalStateException("Insufficient stock.");

        Order cartOrder = getCart(userId);

        for (CartItem item : cartOrder.getItems()) {
            if (item.getProduct().getId() == productId) {
                item.setQuantity(amount);
                return;
            }
        }
    }

    /**
     * Finalizes the order.
     * Validates stock, calculates final price including VAT and discounts, and
     * updates order status.
     * 
     * @param order The order to be checked out.
     */
    // ASSIGNED TO: Customer
    public void checkout(Order order) {

        if (order == null || order.getItems().isEmpty())
            throw new IllegalStateException("Cart is empty.");

        List<CartItem> cart = order.getItems();

        // 1. Validation & Price Calculation
        // Note: DiscountService handles price logic, but we must ensure we are using
        // fresh data.
        for (CartItem item : cart) {
            Product product = productDAO.findById(item.getProduct().getId());
            if (product == null) {
                throw new IllegalStateException("Product no longer exists: " + item.getProduct().getName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for: " + product.getName());
            }
            // Update item with fresh product data for accurate pricing
            item.setProduct(product);
            item.setPriceAtPurchase(product.getPrice());
        }

        // 2. Final Price Calculation (includes Coupon & Loyalty & Thresholds)
        double finalTotal = discountService.calculateFinalPrice(order);
        order.setTotalCost(finalTotal);
        order.setOrderTime(new java.sql.Timestamp(System.currentTimeMillis()));
        order.setStatus(Order.Status.AVAILABLE);
        order.setItems(new ArrayList<>(cart));

        // 3. Coupon Consumption (Critical Step)
        Integer couponId = order.getUsedCouponId();
        if (couponId != null) {
            com.group18.greengrocer.dao.CouponDAO couponDAO = new com.group18.greengrocer.dao.CouponDAO();
            // Optional check: ensure it's still valid right now?
            // Deactivate it to prevent reuse (assuming single-use coupons)
            boolean processed = couponDAO.deactivateCoupon(couponId);
            if (!processed) {
                // Determine if this should block checkout?
                // If logic implies coupons are strictly one-time, we should block.
                // For now, let's log/throw to be safe.
                throw new IllegalStateException("Coupon could not be processed (maybe already used?).");
            }
        }

        // 4. Generate Invoice
        String invoiceBase64 = PDFGenerator.generateInvoice(order);
        order.setInvoice(invoiceBase64);

        // 5. Create Order in DB (Transaction safety would be ideal in DAO, but here we
        // coordinate)
        boolean created = orderDAO.createOrder(order);
        if (!created) {
            // Rollback coupon usage if order fails?
            if (couponId != null) {
                // Technically we should reactive it, but strict systems might require manual
                // intervention.
                // For this assignment, we throw exception.
            }
            throw new IllegalStateException("Order could not be created.");
        }

        // 6. Deduct Stock
        for (CartItem item : cart) {
            Product p = productDAO.findById(item.getProduct().getId());
            if (p != null) {
                // Logic check: stock might have changed since step 1?
                // In a high-concurrency real app, we'd need database-level locking.
                // Here, we do a best-effort update.
                double newStock = p.getStock() - item.getQuantity();
                if (newStock < 0)
                    newStock = 0; // Usage safety
                p.setStock(newStock);
                productDAO.update(p);
            }
        }

        // 7. Clear Cart
        userCarts.remove(order.getCustomerId());
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
     * @param orderId   The ID of the order.
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
     * @param orderId      The ID of the order.
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

        java.sql.Timestamp deliveryTimestamp = new java.sql.Timestamp(deliveryDate.getTime());

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
    public void cancelOrder(int orderId, int customerId) {
        Order order = orderDAO.findOrderById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        // CUSTOMER CHECK
        if (order.getCustomerId() != customerId) {
            throw new IllegalStateException("You cannot cancel this order.");
        }

        // STATUS CHECK
        if (order.getStatus() == Order.Status.COMPLETED || order.getStatus() == Order.Status.COMPLETED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled.");
        }

        if (order.getStatus() == Order.Status.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }

        // CANCEL ORDER
        orderDAO.cancelOrder(orderId);
        order.setStatus(Order.Status.CANCELLED);

        // RESTORE STOCK
        for (CartItem item : order.getItems()) {

            Product product = productDAO.findById(item.getProduct().getId());
            if (product == null) {
                continue;
            }

            product.setStock(product.getStock() + item.getQuantity());
            productDAO.update(product);
        }
    }

    
    // ASSIGNED TO: Owner
   /**
     * Retrieves all orders for administrative view (Owner).
     */
    public List<Order> getAllOrdersForOwner() { // İsmini Controller ile uyumlu yaptım
        return orderDAO.findAllOrders();
    }

    /**
     * Allows a customer to rate a completed order/carrier.
     * 
     * @param orderId The ID of the order.
     * @param rating  Rating value (1-5).
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
