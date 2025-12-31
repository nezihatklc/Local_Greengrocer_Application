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

import java.time.LocalDate;

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
    }

    // =====================
    // CHECKOUT
    // =====================
    @FXML
    private void handleCheckout() {

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
    // UTIL
    // =====================
    private void updateTotalPriceLabel() {
        double total = cartItems.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
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
