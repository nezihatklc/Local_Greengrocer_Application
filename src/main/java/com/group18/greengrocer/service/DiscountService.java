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

    // Owner-configurable loyalty rule (in-memory)
    private int loyaltyMinOrderCount = Constants.DEFAULT_LOYALTY_MIN_ORDER_COUNT;
    private double loyaltyDiscountRate = Constants.DEFAULT_LOYALTY_DISCOUNT_RATE; // percent (e.g., 10.0)

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
        if (order == null) throw new IllegalArgumentException("Order cannot be null.");

        List<CartItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty.");
        }

        // 1) Base subtotal (threshold-aware)
        double subtotal = 0.0;
        for (CartItem item : items) {
            subtotal += lineTotalWithThreshold(item);
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
     * @return Coupon if valid, null otherwise.
     */
    public Coupon validateCoupon(String code) {
        if (ValidatorUtil.isEmpty(code)) return null;

        Coupon c = couponDAO.findCouponByCode(code.trim());
        if (c == null) return null;

        return isCouponValid(c) ? c : null;
    }

    /**
     * Creates a new coupon.
     * ONLY owner can do this (enforce role in controller).
     * Discount amount MUST be > 0.
     */
    public void createCoupon(Coupon coupon) {
        if (coupon == null) throw new IllegalArgumentException("Coupon cannot be null.");
        if (ValidatorUtil.isEmpty(coupon.getCode())) throw new IllegalArgumentException("Coupon code cannot be empty.");
        if (coupon.getDiscountAmount() <= 0) throw new IllegalArgumentException("Discount amount must be > 0.");
        if (coupon.getExpiryDate() == null) throw new IllegalArgumentException("Expiry date is required.");

        // Expiry must not be in the past
        LocalDate exp = coupon.getExpiryDate().toLocalDate();
        if (exp.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon expiry date cannot be in the past.");
        }

        boolean ok = couponDAO.addCoupon(coupon);
        if (!ok) throw new IllegalStateException("Failed to create coupon.");
    }

    /**
     * Loyalty discount percent based on user's past COMPLETED orders.
     * @return percent (e.g., 10.0 for %10)
     */
    public double getLoyaltyDiscount(int userId) {
        if (userId <= 0) return 0.0;

        // DAO already exists: findOrdersByCustomerId
        List<Order> orders = orderDAO.findOrdersByCustomerId(userId);

        int completed = 0;
        for (Order o : orders) {
            if (o != null && o.getStatus() == Order.Status.COMPLETED) {
                completed++;
            }
        }

        return (completed >= loyaltyMinOrderCount) ? loyaltyDiscountRate : 0.0;
    }

    /**
     * Updates loyalty rule configuration (Owner-only in controller).
     */
    public void updateLoyaltyRules(int minOrderCount, double discountRate) {
        if (minOrderCount <= 0) throw new IllegalArgumentException("minOrderCount must be > 0.");
        if (discountRate <= 0) throw new IllegalArgumentException("discountRate must be > 0.");

        this.loyaltyMinOrderCount = minOrderCount;
        this.loyaltyDiscountRate = discountRate;
    }

    // -------------------------
    // Helpers
    // -------------------------

    private double lineTotalWithThreshold(CartItem item) {
        if (item == null) throw new IllegalArgumentException("Cart item cannot be null.");
        if (item.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be > 0.");

        Product p = item.getProduct();
        if (p == null) throw new IllegalArgumentException("Cart item product cannot be null.");

        double unit = item.getPriceAtPurchase(); // preserves purchase-time price
        if (unit < 0) throw new IllegalArgumentException("Invalid price.");

        // Threshold rule: if stock <= threshold -> price doubled
        double stock = p.getStock();
        double threshold = p.getThreshold();
        if (threshold <= 0) throw new IllegalArgumentException("Invalid threshold.");

        if (stock <= threshold) {
            unit *= 2.0;
        }

        return unit * item.getQuantity();
    }

    private boolean isCouponValid(Coupon c) {
        if (c == null) return false;
        if (!c.isActive()) return false;
        if (c.getDiscountAmount() <= 0) return false;

        if (c.getExpiryDate() != null) {
            LocalDate exp = c.getExpiryDate().toLocalDate();
            if (exp.isBefore(LocalDate.now())) return false;
        }
        return true;
    }

    private double applyPercentDiscount(double total, double percent) {
        if (percent <= 0) return total;
        if (percent > 100) throw new IllegalArgumentException("Discount percent cannot exceed 100.");
        return total * (1.0 - (percent / 100.0));
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
