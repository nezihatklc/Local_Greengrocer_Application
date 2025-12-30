package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.ProductService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class CustomerController {

    // =====================
    // SERVICES
    // =====================
    private ProductService productService;
    private OrderService orderService;

    // Normally comes from LoginController
    private int customerId = 1;

    // =====================
    // FXML COMPONENTS
    // =====================
    @FXML
    private Label usernameLabel;

    @FXML
    private Button cartButton;

    @FXML
    private TilePane fruitPane;

    @FXML
    private TilePane vegetablePane;

    @FXML
    private TextField productIdField;

    @FXML
    private TextField quantityField;

    // =====================
    // INITIALIZE
    // =====================
    @FXML
    public void initialize() {
        productService = new ProductService();
        orderService = new OrderService();

        loadProducts();
    }

    // =====================
    // PRODUCT LIST
    // =====================
    private void loadProducts() {
        List<Product> products = productService.getAllProducts();

    }

    // =====================
    // CART OPERATIONS
    // =====================
    @FXML
    private void handleAddToCart() {
        try {
            int productId = Integer.parseInt(productIdField.getText());
            double quantity = Double.parseDouble(quantityField.getText());

            orderService.addToCart(customerId, productId, quantity);
            showInfo("Product added to cart.");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleViewCart() {
        Order cart = orderService.getCart(customerId);
        showInfo("Cart has " + cart.getItems().size() + " items.");
    }

    @FXML
    private void handleRemoveFromCart() {
        try {
            int productId = Integer.parseInt(productIdField.getText());
            orderService.removeFromCart(customerId, productId);
            showInfo("Product removed from cart.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =====================
    // CHECKOUT
    // =====================
    @FXML
    private void handleCheckout() {
        try {
            Order cart = orderService.getCart(customerId);
            orderService.checkout(cart);
            showInfo("Order completed successfully.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =====================
    // ORDER HISTORY
    // =====================
    @FXML
    private void handleMyOrders() {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        showInfo("You have " + orders.size() + " past orders.");
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Group18 GreenGrocer - Login");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not go back to login: " + e.getMessage());
        }
    }

    // =====================
    // UTIL
    // =====================
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
