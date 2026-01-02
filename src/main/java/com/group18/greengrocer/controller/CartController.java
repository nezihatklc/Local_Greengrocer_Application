package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.service.OrderService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.time.LocalDate;

import com.group18.greengrocer.service.DiscountService;
import com.group18.greengrocer.model.Coupon;
import com.group18.greengrocer.util.Constants;
/**
 * Controller class for the Shopping Cart and Checkout screen.
 * <p>
 * This controller manages the customer's cart view, allowing them to:
 * <ul>
 * <li>Review items, quantities, and prices.</li>
 * <li>Remove items from the cart.</li>
 * <li>Apply discount coupons.</li>
 * <li>View their loyalty status and progress.</li>
 * <li>Select delivery date and time.</li>
 * <li>Proceed to final checkout and order placement.</li>
 * </ul>
 *
 * @author Group18
 * @version 1.0
 */
public class CartController {

    // =====================
    // FXML COMPONENTS
    // =====================
    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> productNameColumn;

    @FXML
    private TableColumn<CartItem, Double> quantityColumn;

    @FXML
    private TableColumn<CartItem, Double> priceColumn;

    @FXML
    private TableColumn<CartItem, Double> totalColumn;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private ProgressBar loyaltyProgressBar;

    @FXML
    private Label loyaltyStatusLabel;

    @FXML
    private Label loyaltyTierLabel;

    @FXML
    private TextField couponField; // NEW

    @FXML
    private DatePicker deliveryDatePicker;

    @FXML
    private ComboBox<Integer> deliveryHourCombo;

    @FXML
    private ComboBox<Integer> deliveryMinuteCombo;

    @FXML
    private Button checkoutButton;

    @FXML
    private Button removeButton;

    // =====================
    // DATA
    // =====================
    private User currentUser;
    private Order cartOrder;
    private ObservableList<CartItem> cartItems;

    private final OrderService orderService = new OrderService();
    private final DiscountService discountService = new DiscountService(); // NEW

    // =====================
    // INITIALIZE
    // =====================
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     * Sets up table columns and populates the delivery time combo boxes.
     */
    @FXML
    public void initialize() {

        productNameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getProduct().getName()));

        quantityColumn.setCellValueFactory(
                data -> new SimpleObjectProperty<>(
                        data.getValue().getQuantity()));

        priceColumn.setCellValueFactory(
                data -> new SimpleObjectProperty<>(
                        data.getValue().getPriceAtPurchase()));

        totalColumn.setCellValueFactory(
                data -> new SimpleObjectProperty<>(
                        data.getValue().getTotalPrice()));

        // Initialize Time Combos
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++)
            hours.add(i);
        deliveryHourCombo.setItems(hours);
        deliveryHourCombo.getSelectionModel().select(Integer.valueOf(9)); // Default 9 AM

        ObservableList<Integer> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 15)
            minutes.add(i);
        deliveryMinuteCombo.setItems(minutes);
        deliveryMinuteCombo.getSelectionModel().selectFirst();
    }

    // =====================
    // INIT FROM CUSTOMER
    // =====================
    /**
     * Receives the current user and their active cart data from the main customer dashboard.
     * Initializes the table data and updates price calculations.
     *
     * @param user The logged-in customer.
     * @param cart The current 'Order' object acting as the shopping cart.
     */
    public void initData(User user, Order cart) {
        this.currentUser = user;
        this.cartOrder = cart;

        cartItems = FXCollections.observableArrayList(cart.getItems());
        cartTable.setItems(cartItems);

        updateTotalPriceLabel();

        checkoutButton.setDisable(cartItems.isEmpty());
    }

    // =====================
    // REMOVE ITEM
    // =====================

    /**
     * Handles the removal of a selected item from the cart.
     * Updates the database via OrderService and refreshes the UI (table and total price).
     */
    @FXML
    private void handleRemoveItem() {

        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Warning", "Please select an item to remove.");
            return;
        }

        orderService.removeFromCart(
                currentUser.getId(),
                selectedItem.getProduct().getId());

        cartItems.remove(selectedItem);

        // Sync items for calculation
        cartOrder.setItems(new java.util.ArrayList<>(cartItems));

        updateTotalPriceLabel();

        checkoutButton.setDisable(cartItems.isEmpty());
    }
/**
     * Validates and applies a discount coupon code entered by the user.
     * If valid, updates the total price reflecting the discount.
     */
    @FXML
    private void handleApplyCoupon() {
        String code = couponField.getText();
        if (code == null || code.trim().isEmpty()) {
            showAlert("Warning", "Please enter a coupon code.");
            return;
        }

        Coupon coupon = discountService.validateCoupon(code.trim());
        if (coupon != null) {
            cartOrder.setUsedCouponId(coupon.getId());
            updateTotalPriceLabel();
            showAlert("Success", "Coupon applied: " + coupon.getCode() + " (-" + coupon.getDiscountAmount() + " TL)");
        } else {
            showAlert("Error", "Invalid or expired coupon.");
        }
    }

    // =====================
    // CHECKOUT
    // =====================

    /**
     * Handles the final checkout process.
     * Performs multiple validations:
     * <ul>
     * <li>Checks for minimum cart value.</li>
     * <li>Validates delivery date/time (cannot be in the past).</li>
     * <li>Enforces delivery window constraints (e.g., within 48 hours).</li>
     * </ul>
     * If valid, prompts for confirmation and submits the order via OrderService.
     */
    @FXML
    private void handleCheckout() {

        // Sync items just in case
        cartOrder.setItems(new java.util.ArrayList<>(cartItems));
        double totalWithVat = discountService.calculateFinalPrice(cartOrder);

        // MINIMUM CART CHECK
        if (totalWithVat < Constants.MIN_CART_VALUE) {
            showAlert(
                    "Minimum Cart Value",
                    String.format(
                            "Minimum cart value is %.2f TL.\nYour current total is %.2f TL.",
                            Constants.MIN_CART_VALUE,
                            totalWithVat));
            return;
        }

        LocalDate deliveryDate = deliveryDatePicker.getValue();
        if (deliveryDate == null) {
            showAlert("Error", "Please select a delivery date.");
            return;
        }

        Integer hour = deliveryHourCombo.getValue();
        Integer minute = deliveryMinuteCombo.getValue();
        if (hour == null || minute == null) {
            showAlert("Error", "Please select a delivery time.");
            return;
        }

        if (deliveryDate.isBefore(LocalDate.now())) {
            // Allow if today and time is future?
            // Simple check: if date is before today -> Fail
            showAlert("Error", "Delivery date cannot be in the past.");
            return;
        }

        // Check strict past (Date + Time)
        java.time.LocalDateTime requestedDateTime = deliveryDate.atTime(hour, minute);
        if (requestedDateTime.isBefore(java.time.LocalDateTime.now())) {
            showAlert("Error", "Delivery time cannot be in the past.");
            return;
        }

        if (deliveryDate.isAfter(LocalDate.now().plusDays(Role.OWNER.equals(currentUser.getRole()) ? 30 : 2))) {
            // For regular user, 48 hours constraint from original code?
            // Original: isAfter(LocalDate.now().plusDays(2))
            // Keeping original logic for consistency unless I see User Type check
            // elsewhere.
            // Actually original code had hardcoded plusDays(2). Let's stick to it.
            showAlert("Error", "Delivery must be within 48 hours for standard shipping.");
            return;
        }

        // CONFIRMATION DIALOG
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Purchase");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to complete the order?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            cartOrder.setRequestedDeliveryDate(
                    Timestamp.valueOf(requestedDateTime));
            // Price is recalculated in service, but we've verified it here.
            orderService.checkout(cartOrder);
            showAlert("Success", "Order placed successfully!");
            closeStage();
        } catch (Exception e) {
            showAlert("Checkout Failed", e.getMessage());
        }
    }

    // =====================
    // BACK
    // =====================

    /**
     * Closes the cart window and returns to the previous screen.
     */
    @FXML
    private void handleBack() {
        closeStage();
    }

    // =====================
    // PRICE CALCULATIONS
    // =====================
/**
     * Recalculates the total price including VAT, coupons, and loyalty discounts.
     * Updates the UI labels and the Loyalty Progress Bar accordingly.
     */
    private void updateTotalPriceLabel() {
        if (cartOrder == null)
            return;

        // Sync
        cartOrder.setItems(new java.util.ArrayList<>(cartItems));

        double finalPrice = discountService.calculateFinalPrice(cartOrder);

        // Loyalty Check
        int completed = discountService.getCompletedOrderCount(currentUser.getId());

        // Progress bar (max 10 orders for full tier)
        loyaltyProgressBar.setProgress(Math.min(1.0, completed / 10.0));
        loyaltyStatusLabel.setText(completed + " / 10 Orders");

        if (completed >= 10) {
            loyaltyTierLabel.setText("Loyalty Active: Tier 2 (10+ orders) - 15% discount applied!");
            loyaltyTierLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        } else if (completed >= 5) {
            loyaltyTierLabel.setText("Loyalty Active: Tier 1 (5+ orders) - 5% discount applied! (Need "
                    + (10 - completed) + " more for 15%)");
            loyaltyTierLabel.setStyle("-fx-text-fill: #2E7D32;");
        } else {
            loyaltyTierLabel.setText("Next Tier: " + (5 - completed) + " more orders for 5% discount.");
            loyaltyTierLabel.setStyle("-fx-text-fill: #546E7A;");
        }

        totalPriceLabel.setText(
                String.format("Total: %.2f TL", finalPrice));

        // Calculate VAT for display
        // FinalPrice = Base * (1 + VAT)
        // Base = FinalPrice / (1 + VAT)
        // VAT_Amount = FinalPrice - Base
        double calculateVatRate = Constants.VAT_RATE;
        double basePrice = finalPrice / (1.0 + calculateVatRate);
        double vatAmount = finalPrice - basePrice;

        totalPriceLabel.setText(
                String.format("VAT (%.0f%%): %.2f TL | Total: %.2f TL",
                        calculateVatRate * 100, vatAmount, finalPrice));
    }

    // =====================
    // UTIL
    // =====================
    /**
     * Helper method to display information alerts.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) totalPriceLabel.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
