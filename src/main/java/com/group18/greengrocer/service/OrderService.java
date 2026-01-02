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

        double price = product.getPrice();
        // Apple threshold logic immediately for better UX
        if (product.getStock() <= product.getThreshold()) {
            price *= 2.0;
        }

        CartItem newItem = new CartItem(product, amount);
        newItem.setPriceAtPurchase(price);
        cart.add(newItem);
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
            double currentPrice = product.getPrice();

            // Logic moved to DiscountService
            // if (product.getStock() <= product.getThreshold()) {
            // currentPrice *= 2.0;
            // }

            item.setPriceAtPurchase(currentPrice);

        }

        // 2. Final Price Calculation (includes Coupon & Loyalty & Thresholds)
        double finalTotal = discountService.calculateFinalPrice(order);
        order.setTotalCost(finalTotal);
        order.setOrderTime(new java.sql.Timestamp(System.currentTimeMillis()));
        order.setStatus(Order.Status.WAITING);
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
     * Approves an order (Owner Only).
     * 
     * @param orderId The ID of the order.
     */
    // ASSIGNED TO: Owner
    public void approveOrder(int orderId) {
        Order order = orderDAO.findOrderById(orderId);
        if (order == null)
            throw new IllegalArgumentException("Order not found");

        if (order.getStatus() != Order.Status.WAITING) {
            throw new IllegalStateException("Order must be in WAITING state to approve.");
        }

        boolean success = orderDAO.approveOrder(orderId);
        if (!success)
            throw new IllegalStateException("Could not approve order.");
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
            throw new IllegalStateException(
                    "Order is no longer available (RECEIVED) or already selected by another carrier.");
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

        if (order.getStatus() != Order.Status.ON_THE_WAY) {
            throw new IllegalStateException(
                    "Only 'On the Way' orders can be completed.");
        }

        java.sql.Timestamp deliveryTimestamp = new java.sql.Timestamp(deliveryDate.getTime());

        boolean success = orderDAO.completeOrder(orderId, deliveryTimestamp);

        if (!success) {
            throw new IllegalStateException("Failed to complete the order.");
        }
    }

    /**
     * Dismisses the order tracking notification for a delivered order.
     * Changes status from DELIVERED to COMPLETED.
     * 
     * @param orderId The ID of the order.
     */
    // ASSIGNED TO: Customer
    public void dismissTracking(int orderId) {
        boolean success = orderDAO.dismissOrder(orderId);
        if (!success) {
            // It might have failed if status wasn't DELIVERED, which is fine, we just
            // ignore.
            System.out.println("Could not dismiss order #" + orderId + " (maybe already dismissed?)");
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

        if (order.getStatus() != Order.Status.WAITING) {
            throw new IllegalStateException("You cannot cancel an order that has been approved or is being prepared.");
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
        if (order.getStatus() != Order.Status.DELIVERED) {
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
     * Checks if an order has already been rated by the customer.
     * 
     * @param orderId The ID of the order.
     * @return true if already rated, false otherwise.
     */
    public boolean isOrderRated(int orderId) {
        CarrierRatingDAO ratingDAO = new CarrierRatingDAO();
        return ratingDAO.hasRated(orderId);
    }

    /**
     * Allows a customer to rate a purchased product.
     * 
     * @param customerId The ID of the customer.
     * @param productId  The ID of the product.
     * @param rating     Rating value (1-5).
     */
    // ASSIGNED TO: Customer
    public void rateProduct(int orderId, int customerId, int productId, int rating) {
        if (rating < 1 || rating > 5)
            return;

        com.group18.greengrocer.dao.ProductRatingDAO dao = new com.group18.greengrocer.dao.ProductRatingDAO();
        com.group18.greengrocer.model.ProductRating pr = new com.group18.greengrocer.model.ProductRating(orderId, customerId,
                productId, rating);
        dao.addRating(pr);
    }
    
    /**
     * Checks if the carrier for an order has effectively been rated.
     * @param orderId The ID of the order.
     * @return true if carrier rating exists.
     */
    public boolean hasCarrierRating(int orderId) {
        com.group18.greengrocer.dao.CarrierRatingDAO dao = new com.group18.greengrocer.dao.CarrierRatingDAO();
        return dao.hasRated(orderId);
    }

    /**
     * Checks if any products in an order have been rated.
     * @param orderId The ID of the order.
     * @return true if product rating(s) exist.
     */
    public boolean hasProductRating(int orderId) {
        com.group18.greengrocer.dao.ProductRatingDAO dao = new com.group18.greengrocer.dao.ProductRatingDAO();
        return dao.hasRatedOrder(orderId);
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

        // Regenerate invoice dynamically to reflect current status (e.g. Cancelled)
        return PDFGenerator.generateInvoice(order);
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

    // ============================================
    // REPORTING METHODS
    // ============================================

    /**
     * Aggregates total revenue (quantity * price) per product.
     * 
     * @return Map<ProductName, TotalRevenue>
     */
    public java.util.Map<String, Double> getRevenueByProduct() {
        List<Order> allOrders = orderDAO.findAllOrders();
        java.util.Map<String, Double> map = new java.util.HashMap<>();

        for (Order o : allOrders) {
            if (o.getStatus() == Order.Status.CANCELLED)
                continue; // Skip cancelled

            for (CartItem item : o.getItems()) {
                if (item.getProduct() != null) {
                    String name = item.getProduct().getName();
                    double revenue = item.getQuantity() * item.getPriceAtPurchase();
                    map.put(name, map.getOrDefault(name, 0.0) + revenue);
                }
            }
        }
        return map;
    }

    /**
     * Aggregates total revenue per day.
     * 
     * @return Map<DateString, TotalRevenue> sorted by date
     */
    public java.util.Map<String, Double> getRevenueOverTime() {
        List<Order> allOrders = orderDAO.findAllOrders();
        java.util.TreeMap<String, Double> map = new java.util.TreeMap<>(); // Sorted by date string

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");

        for (Order o : allOrders) {
            if (o.getStatus() == Order.Status.CANCELLED)
                continue; // Skip cancelled

            if (o.getOrderTime() != null) {
                String dateKey = sdf.format(o.getOrderTime());
                map.put(dateKey, map.getOrDefault(dateKey, 0.0) + o.getTotalCost());
            }
        }
        return map;
    }

    /**
     * Aggregates revenue by product category.
     * 
     * @return Map<CategoryName, TotalRevenue>
     */
    public java.util.Map<String, Double> getSalesByCategory() {
        List<Order> allOrders = orderDAO.findAllOrders();
        java.util.Map<String, Double> map = new java.util.HashMap<>();

        for (Order o : allOrders) {
            if (o.getStatus() == Order.Status.CANCELLED)
                continue; // Skip cancelled

            for (CartItem item : o.getItems()) {
                if (item.getProduct() != null && item.getProduct().getCategory() != null) {
                    String cleanName = item.getProduct().getCategory().name();
                    double revenue = item.getQuantity() * item.getPriceAtPurchase();
                    map.put(cleanName, map.getOrDefault(cleanName, 0.0) + revenue);
                }
            }
        }
        return map;
    }

    /**
     * Calculates total revenue from all non-cancelled orders.
     */
    public double getTotalRevenue() {
        return orderDAO.findAllOrders().stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .mapToDouble(Order::getTotalCost)
                .sum();
    }

    /**
     * Counts total non-cancelled orders.
     */
    public int getTotalOrdersCount() {
        return (int) orderDAO.findAllOrders().stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .count();
    }

    /**
     * Counts unique customers who have placed at least one non-cancelled order.
     */
    public int getActiveCustomersCount() {
        return (int) orderDAO.findAllOrders().stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .map(Order::getCustomerId)
                .distinct()
                .count();
    }

    /**
     * Calculates order status distribution.
     */
    public java.util.Map<String, Integer> getOrderStatusDistribution() {
        List<Order> allOrders = orderDAO.findAllOrders();
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        
        for (Order o : allOrders) {
            String status = o.getStatus().toString();
            map.put(status, map.getOrDefault(status, 0) + 1);
        }
        return map;
    }

}
