package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller for the Shopping Cart screen.
 * Handles display of cart items, removal of items, and checkout process.
 */
public class CartController {

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
    private DatePicker deliveryDatePicker;

    @FXML
    private Button checkoutButton;

    @FXML
    private Button removeButton;

    private final OrderService orderService;
    private ObservableList<CartItem> cartItems;

    public CartController() {
        this.orderService = new OrderService();
    }

    @FXML
    public void initialize() {
        // 1. Get Current User
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "User not logged in.");
            closeStage();
            return;
        }

        // 2. Load Cart Data
        Order cartOrder = orderService.getCart(currentUser.getId());
        cartItems = FXCollections.observableArrayList(cartOrder.getItems());

        // 3. Setup Table Columns
        productNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));

        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));

        priceColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPriceAtPurchase()));

        totalColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalPrice()));

        cartTable.setItems(cartItems);

        // 4. Update Total Price Label
        updateTotalPriceLabel();
    }

    @FXML
    private void handleRemoveItem() {
        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Warning", "Please select an item to remove.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            orderService.removeFromCart(currentUser.getId(), selectedItem.getProduct().getId());
            cartItems.remove(selectedItem);
            updateTotalPriceLabel();
        }
    }

    @FXML
    private void handleCheckout() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        // 1. Validate Delivery Date
        LocalDate deliveryDate = deliveryDatePicker.getValue();
        if (deliveryDate == null) {
            showAlert("Error", "Please select a delivery date.");
            return;
        }

        if (deliveryDate.isBefore(LocalDate.now())) {
            showAlert("Error", "Delivery date cannot be in the past.");
            return;
        }

        // Rule: Delivery date must be within 48 hours is implied/business rule,
        // but let's check basic validity first.
        // Assuming strict "within 48 hours" means <= now + 2 days.
        if (deliveryDate.isAfter(LocalDate.now().plusDays(2))) {
            showAlert("Error", "Delivery must be within 48 hours.");
            return;
        }

        // 2. Prepare Order Object for Checkout
        // OrderService.checkout(Order) expects an order object to populate/finalize.
        // We re-fetch the fresh cart state to be safe or construct one.
        Order orderToCheckout = orderService.getCart(currentUser.getId());

        // Convert LocalDate to Timestamp for deliveryTime
        // Note: The schema uses 'deliverytime' for both requested and actual.
        // We initialize it here as the requested date.
        java.sql.Timestamp deliveryTimestamp = java.sql.Timestamp.valueOf(deliveryDate.atStartOfDay());
        orderToCheckout.setDeliveryTime(deliveryTimestamp);

        try {
            orderService.checkout(orderToCheckout);
            showAlert("Success", "Order placed successfully!");
            // Close the cart window or clear it
            cartItems.clear();
            updateTotalPriceLabel();
            closeStage();
        } catch (IllegalStateException | IllegalArgumentException e) {
            showAlert("Checkout Failed", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        closeStage();
    }

    private void updateTotalPriceLabel() {
        double total = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
        totalPriceLabel.setText(String.format("Total: %.2f TL", total));
    }

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
