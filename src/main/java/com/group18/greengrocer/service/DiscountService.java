package com.group18.greengrocer.service;

import com.group18.greengrocer.model.Coupon;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.util.ValidatorUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * DiscountService
 * Business logic for pricing rules.
 *
 * NOTE:
 * - DAO-less design: coupons + loyalty rules kept in-memory.
 * - For full rules compliance (Coupons table, past orders), you will eventually need DB access.
 */
public class DiscountService {

    // -------------------------
    // Configurable Rules (Owner changes)
    // -------------------------
    private double vatRate = 0.20; // default VAT (change if your project defines a different value)

    private int loyaltyMinOrderCount = 5;
    private double loyaltyDiscountRate = 10.0; // percent, e.g., 10.0 => %10

    // -------------------------
    // In-memory Coupon Store (code -> coupon)
    // -------------------------
    private final Map<String, Coupon> couponsByCode = new HashMap<>();

    /**
     * Calculates final order price.
     *
     * MUST include:
     * - VAT
     * - Threshold-based price doubling
     * - Coupon discount (if exists)
     * - Loyalty discount (if eligible)
     *
     * IMPORTANT:
     * Your current Order model is empty, so we cannot truly compute totals yet.
     * This method will compile and run, but you must later connect it to real Order fields.
     */
    // ASSIGNED TO: Owner (Pricing Rules)
    public double calculateFinalPrice(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null.");

        // 1) Base total (MUST be derived from Order)
        // TODO: Replace with a real getter like order.getSubtotal() or order.getTotal()
        double baseTotal = 0.0;

        // 2) Threshold-based doubling (requires order items + product thresholds)
        // TODO: Implement once Order has items and you can check stock/threshold rules.
        double afterThreshold = baseTotal;

        // 3) VAT
        double withVat = afterThreshold * (1.0 + vatRate);

        // 4) Loyalty discount (needs user's completed order count)
        // TODO: Replace 0 with getLoyaltyDiscount(order.getUserId()) once Order has userId
        double loyaltyPercent = 0.0;
        double afterLoyalty = applyPercentDiscount(withVat, loyaltyPercent);

        // 5) Coupon discount (needs coupon code stored in Order)
        // TODO: Replace null with order.getCouponCode() once Order has it
        String couponCode = null;
        if (!ValidatorUtil.isEmpty(couponCode)) {
            Coupon c = validateCoupon(couponCode.trim());
            if (c != null) {
                afterLoyalty = applyCouponDiscount(afterLoyalty, c);
            }
        }

        return Math.max(0.0, round2(afterLoyalty));
    }

    /**
     * Validates coupon code.
     *
     * @return Coupon if valid, null otherwise
     * Validity includes expiration date.
     */
    // ASSIGNED TO: Owner
    public Coupon validateCoupon(String code) {
        if (ValidatorUtil.isEmpty(code)) return null;

        String normalized = code.trim().toUpperCase();
        Coupon c = couponsByCode.get(normalized);
        if (c == null) return null;

        // Expiration check (Coupon has expiryDate)
        LocalDate exp = c.getExpiryDate();
        if (exp != null && exp.isBefore(LocalDate.now())) {
            return null;
        }

        // Discount must be > 0
        if (c.getDiscountAmount() <= 0) {
            return null;
        }

        return c;
    }

    /**
     * Creates a new coupon (Owner-only in controller/access control).
     * Discount value MUST be > 0.
     */
    // ASSIGNED TO: Owner
    public void createCoupon(Coupon coupon) {
        if (coupon == null) throw new IllegalArgumentException("Coupon cannot be null.");

        if (ValidatorUtil.isEmpty(coupon.getCode())) {
            throw new IllegalArgumentException("Coupon code cannot be empty.");
        }

        if (coupon.getDiscountAmount() <= 0) {
            throw new IllegalArgumentException("Discount amount must be > 0.");
        }

        // Optional: expiry can be null (no expiry). If you require it, enforce here.
        // if (coupon.getExpiryDate() == null) throw new IllegalArgumentException("Expiry date required.");

        couponsByCode.put(coupon.getCode().trim().toUpperCase(), coupon);
    }

    /**
     * Loyalty discount percent based on user's past completed orders.
     * DAO-less: you MUST plug a real completed-order-count source later.
     */
    // ASSIGNED TO: Owner
    public double getLoyaltyDiscount(int userId) {
        if (userId <= 0) return 0.0;

        int completedOrders = getCompletedOrderCountForUser(userId); // TODO hook later
        if (completedOrders >= loyaltyMinOrderCount) {
            return loyaltyDiscountRate;
        }
        return 0.0;
    }

    /**
     * Updates loyalty rules (Owner-only enforced by controller).
     */
    // ASSIGNED TO: Owner
    public void updateLoyaltyRules(int minOrderCount, double discountRate) {
        if (minOrderCount <= 0) throw new IllegalArgumentException("minOrderCount must be > 0.");
        if (discountRate <= 0) throw new IllegalArgumentException("discountRate must be > 0.");

        this.loyaltyMinOrderCount = minOrderCount;
        this.loyaltyDiscountRate = discountRate;
    }

    // -------------------------
    // Helpers
    // -------------------------

    private double applyPercentDiscount(double total, double percent) {
        if (percent <= 0) return total;
        if (percent > 100) throw new IllegalArgumentException("Discount percent cannot exceed 100.");
        return total * (1.0 - (percent / 100.0));
    }

    /**
     * With your current Coupon model, discountAmount can be either:
     * - fixed amount OR
     * - percent
     *
     * Since there's NO field that tells which one it is,
     * we must choose a convention.
     *
     * Convention (simple):
     * - if discountAmount <= 100 -> treat as PERCENT
     * - else -> treat as AMOUNT
     *
     * If you want it unambiguous, add a field like couponType (PERCENT/AMOUNT).
     */
    private double applyCouponDiscount(double total, Coupon coupon) {
        double d = coupon.getDiscountAmount();

        if (d <= 0) return total;

        if (d <= 100.0) { // percent
            return applyPercentDiscount(total, d);
        } else { // amount
            return Math.max(0.0, total - d);
        }
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    /**
     * DAO-less placeholder:
     * you need DB/OrderDAO later to count completed orders.
     */
    private int getCompletedOrderCountForUser(int userId) {
        return 0; // TODO
    }
}
