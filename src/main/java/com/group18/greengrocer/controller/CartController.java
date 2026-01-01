package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
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

public class CartController {

    private static final double VAT_RATE = 0.10;
    private static final double MIN_CART_VALUE = 100.0;


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
    private DatePicker deliveryDatePicker;

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

    // =====================
    // INITIALIZE
    // =====================
    @FXML
    public void initialize() {

        productNameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getProduct().getName()
                )
        );

        quantityColumn.setCellValueFactory(
                data -> new SimpleObjectProperty<>(
                        data.getValue().getQuantity()
                )
        );

        priceColumn.setCellValueFactory(
                data -> new SimpleObjectProperty<>(
                        data.getValue().getPriceAtPurchase()
                )
        );

        totalColumn.setCellValueFactory(
                data -> new SimpleObjectProperty<>(
                        data.getValue().getTotalPrice()
                )
        );
    }

    // =====================
    // INIT FROM CUSTOMER
    // =====================
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
    @FXML
    private void handleRemoveItem() {

        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Warning", "Please select an item to remove.");
            return;
        }

        orderService.removeFromCart(
                currentUser.getId(),
                selectedItem.getProduct().getId()
        );

        cartItems.remove(selectedItem);
        updateTotalPriceLabel();

        checkoutButton.setDisable(cartItems.isEmpty());
    }

    // =====================
    // CHECKOUT
    // =====================
    @FXML
    private void handleCheckout() {

         double totalWithVat = calculateTotalWithVat();

        // MINIMUM CART CHECK
        if (totalWithVat < MIN_CART_VALUE) {
            showAlert(
                    "Minimum Cart Value",
                    String.format(
                            "Minimum cart value is %.2f TL.\nYour current total is %.2f TL.",
                            MIN_CART_VALUE,
                            totalWithVat
                    )
            );
            return;
        }


        LocalDate deliveryDate = deliveryDatePicker.getValue();
        if (deliveryDate == null) {
            showAlert("Error", "Please select a delivery date.");
            return;
        }

        if (deliveryDate.isBefore(LocalDate.now())) {
            showAlert("Error", "Delivery date cannot be in the past.");
            return;
        }

        if (deliveryDate.isAfter(LocalDate.now().plusDays(2))) {
            showAlert("Error", "Delivery must be within 48 hours.");
            return;
        }

        try {
            cartOrder.setRequestedDeliveryDate(
                Timestamp.valueOf(deliveryDate.atStartOfDay())
            );
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
    @FXML
    private void handleBack() {
        closeStage();
    }



    // =====================
    // PRICE CALCULATIONS
    // =====================
    private double calculateSubtotal() {
        return cartItems.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    private double calculateVat(double subtotal) {
        return subtotal * VAT_RATE;
    }

    private double calculateTotalWithVat() {
        double subtotal = calculateSubtotal();
        return subtotal + calculateVat(subtotal);
    }

    private void updateTotalPriceLabel() {
        double subtotal = cartItems.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
            
        double vatRate = 0.10; // %10 VAT
        double vatAmount = subtotal * vatRate;
        double totalWithVat = subtotal + vatAmount;

        totalPriceLabel.setText(
            String.format(
                    "Subtotal: %.2f TL | VAT (10%%): %.2f TL | Total: %.2f TL",
                    subtotal,
                    vatAmount,
                    totalWithVat
                )
        );
    }

    // =====================
    // UTIL
    // =====================
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
