package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.CouponDAO;
import com.group18.greengrocer.dao.OrderDAO;
import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Coupon;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.util.Constants;
import com.group18.greengrocer.util.ValidatorUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * DiscountService
 * Business logic for pricing rules:
 * - Threshold-based price doubling
 * - Coupon discount
 * - Loyalty discount
 * - VAT
 *
 * Controllers must call here; no SQL in controllers.
 */
public class DiscountService {

    private final CouponDAO couponDAO;
    private final OrderDAO orderDAO;

    // Owner-configurable loyalty rule (shared across instances)
    private static int loyaltyMinOrderCount = Constants.DEFAULT_LOYALTY_MIN_ORDER_COUNT;
    private static double loyaltyDiscountRate = Constants.DEFAULT_LOYALTY_DISCOUNT_RATE; // percent (e.g., 10.0)

    public DiscountService() {
        this.couponDAO = new CouponDAO();
        this.orderDAO = new OrderDAO();
    }

    // Optional constructor for tests
    public DiscountService(CouponDAO couponDAO, OrderDAO orderDAO) {
        this.couponDAO = couponDAO;
        this.orderDAO = orderDAO;
    }

    /**
     * Calculates final order price.
     *
     * MUST include:
     * - Threshold-based price doubling
     * - Coupon discount (if exists)
     * - Loyalty discount (if eligible)
     * - VAT
     */
    public double calculateFinalPrice(Order order) {
        if (order == null)
            throw new IllegalArgumentException("Order cannot be null.");

        List<CartItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty.");
        }

        // 1) Base subtotal (threshold-aware)
        // 1) Base subtotal & Price Adjustment (Threshold)
        // CRITICAL FIX: We must UPDATE item.priceAtPurchase if threshold is met,
        // so that OrderItems table and Invoice show the actual price paid (doubled).
        double subtotal = 0.0;
        for (CartItem item : items) {
            // This existing helper calculates total but we need to verify if it updates the
            // item
            // The helper 'lineTotalWithThreshold' was originally strictly read-only. We
            // should refactor it or do logic here.

            // Let's do the logic explicitly here to be safe and clear:
            Product p = item.getProduct();
            if (p != null && p.getThreshold() > 0 && p.getStock() <= p.getThreshold()) {
                // Double the price!
                // BUT we must be careful not to double it multiple times if called repeatedly.
                // However, OrderService constructs a fresh cart from DB products right before
                // calling checkout/calculate.
                // So item.priceAtPurchase starts as base price.
                item.setPriceAtPurchase(p.getPrice() * 2.0);
            } else if (p != null) {
                // Ensure it is base price (reset if needed, though usually fresh)
                item.setPriceAtPurchase(p.getPrice());
            }

            subtotal += item.getTotalPrice(); // quantity * priceAtPurchase
        }

        // 2) Coupon (fixed amount) if order.usedCouponId exists
        double afterCoupon = subtotal;
        Integer usedCouponId = order.getUsedCouponId();
        if (usedCouponId != null) {
            Coupon c = couponDAO.findCouponById(usedCouponId); // <-- needs the PATCH in CouponDAO
            if (c != null && isCouponValid(c)) {
                afterCoupon = Math.max(0.0, afterCoupon - c.getDiscountAmount());
            } else {
                // invalid coupon in order -> ignore it (or you can throw)
                // order.setUsedCouponId(null);
            }
        }

        // 3) Loyalty discount (percent)
        double loyaltyPercent = getLoyaltyDiscount(order.getCustomerId()); // returns percent
        double afterLoyalty = applyPercentDiscount(afterCoupon, loyaltyPercent);

        // 4) VAT (Constants)
        double withVat = afterLoyalty * (1.0 + Constants.VAT_RATE);

        double finalTotal = round2(withVat);

        // Optional: set order totalCost so OrderDAO can insert it
        order.setTotalCost(finalTotal);

        return finalTotal;
    }

    /**
     * Validates coupon code.
     * 
     * @return Coupon if valid, null otherwise.
     */
    public Coupon validateCoupon(String code) {
        if (ValidatorUtil.isEmpty(code))
            return null;

        Coupon c = couponDAO.findCouponByCode(code.trim());
        if (c == null)
            return null;

        return isCouponValid(c) ? c : null;
    }

    /**
     * Creates a new coupon.
     * ONLY owner can do this (enforce role in controller).
     * Discount amount MUST be > 0.
     */
    public void createCoupon(Coupon coupon) {
        if (coupon == null)
            throw new IllegalArgumentException("Coupon cannot be null.");
        if (ValidatorUtil.isEmpty(coupon.getCode()))
            throw new IllegalArgumentException("Coupon code cannot be empty.");
        if (coupon.getDiscountAmount() <= 0)
            throw new IllegalArgumentException("Discount amount must be > 0.");
        if (coupon.getExpiryDate() == null)
            throw new IllegalArgumentException("Expiry date is required.");

        // Expiry must not be in the past
        LocalDate exp = coupon.getExpiryDate().toLocalDate();
        if (exp.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon expiry date cannot be in the past.");
        }

        boolean ok = couponDAO.addCoupon(coupon);
        if (!ok)
            throw new IllegalStateException("Failed to create coupon.");
    }

    /**
     * Loyalty discount percent based on user's past COMPLETED orders.
     * Tier 1: 5+ orders -> 5%
     * Tier 2: 10+ orders -> 15%
     * 
     * @return percent (e.g., 5.0 or 15.0)
     */
    public double getLoyaltyDiscount(int userId) {
        int completed = getCompletedOrderCount(userId);

        if (completed >= 10)
            return 15.0;
        if (completed >= 5)
            return 5.0;
        return 0.0;
    }

    public int getCompletedOrderCount(int userId) {
        if (userId <= 0)
            return 0;
        List<Order> orders = orderDAO.findOrdersByCustomerId(userId);
        int completed = 0;
        for (Order o : orders) {
            if (o != null && (o.getStatus() == Order.Status.DELIVERED || o.getStatus() == Order.Status.COMPLETED)) {
                completed++;
            }
        }
        return completed;
    }

    /**
     * Updates loyalty rule configuration (Owner-only in controller).
     */
    public void updateLoyaltyRules(int minOrderCount, double discountRate) {
        if (minOrderCount <= 0)
            throw new IllegalArgumentException("minOrderCount must be > 0.");
        if (discountRate <= 0)
            throw new IllegalArgumentException("discountRate must be > 0.");

        DiscountService.loyaltyMinOrderCount = minOrderCount;
        DiscountService.loyaltyDiscountRate = discountRate;
    }

    public int getLoyaltyMinOrderCount() {
        return loyaltyMinOrderCount;
    }

    public double getLoyaltyDiscountRate() {
        return loyaltyDiscountRate;
    }

    // -------------------------
    // Helpers
    // -------------------------

    // Helper removed as logic is now inline in calculateFinalPrice to ensure item
    // update
    // private double lineTotalWithThreshold(CartItem item) { ... }

    private boolean isCouponValid(Coupon c) {
        if (c == null)
            return false;
        if (!c.isActive())
            return false;
        if (c.getDiscountAmount() <= 0)
            return false;

        if (c.getExpiryDate() != null) {
            LocalDate exp = c.getExpiryDate().toLocalDate();
            if (exp.isBefore(LocalDate.now()))
                return false;
        }
        return true;
    }

    private double applyPercentDiscount(double total, double percent) {
        if (percent <= 0)
            return total;
        if (percent > 100)
            throw new IllegalArgumentException("Discount percent cannot exceed 100.");
        return total * (1.0 - (percent / 100.0));
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
