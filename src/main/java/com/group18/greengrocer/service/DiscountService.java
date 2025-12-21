package com.group18.greengrocer.service;

import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Coupon;

public class DiscountService {
    /**
     * Calculates final order price.
     *
     * MUST include:
     * - VAT
     * - Threshold-based price doubling
     * - Coupon discount (if exists)
     * - Loyalty discount (if eligible)
     */
    public double calculateFinalPrice(Order order) {
        return 0.0;
    }

    /**
     * Validates coupon code.
     *
     * @return Coupon if valid, null otherwise
     * Coupon validity includes expiration and usage rules.
     */
    public Coupon validateCoupon(String code) {
    }

    /**
     * Creates a new coupon.
     *
     * ONLY owner can create coupons.
     * Discount value MUST be greater than zero.
     */
    public void createCoupon(Coupon coupon) {
    }

    /**
     * Calculates loyalty discount rate based on user's past completed orders.
     *
     * @return discount percentage (e.g. 10.0 for %10)
     */
    public double getLoyaltyDiscount(int userId) {
        return 0.0;
    }

    /**
     * Updates loyalty rule configuration.
     *
     * - minOrderCount > 0
     * - discountRate > 0
     * - OWNER only
     */
    public void updateLoyaltyRules(int minOrderCount, double discountRate) {
    }
}
