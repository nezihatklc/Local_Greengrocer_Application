package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.UserService;
import com.group18.greengrocer.util.ValidatorUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Controller for the Carrier Dashboard.
 * Handles order selection, delivery completion, and history viewing.
 */
public class CarrierController {

    // Services
    private final OrderService orderService;
    private final UserService userService;

    // Current logged-in user
    private User currentUser;

    // UI Components (FXML)
    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutButton;

    // Available Orders Tab
    @FXML
    private TableView<Order> availableOrdersTable;
    @FXML
    private TableColumn<Order, Integer> colOrderId;
    @FXML
    private TableColumn<Order, String> colCustomerName;
    @FXML
    private TableColumn<Order, String> colAddress;
    @FXML
    private TableColumn<Order, String> colTotalPrice;
    @FXML
    private Button acceptOrderButton;

    // My Current Order Tab
    @FXML
    private Label currentOrderDetailsLabel;
    @FXML
    private DatePicker deliveryDatePicker;
    @FXML
    private Button completeDeliveryButton;

    // History Tab
    @FXML
    private TableView<Order> completedOrdersTable;
    @FXML
    private TableColumn<Order, Integer> colHistOrderId;
    @FXML
    private TableColumn<Order, String> colHistDate;
    @FXML
    private TableColumn<Order, String> colHistStatus;
    @FXML
    private TableColumn<Order, String> colHistRating;

    public CarrierController() {
        this.orderService = new OrderService();
        this.userService = new UserService();
    }

    /**
     * Called by the login screen to set the current user and load their data.
     * 
     * @param user The logged-in Carrier.
     */
    public void initData(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            usernameLabel.setText("Carrier: " + currentUser.getUsername());
            refreshAll();
        }
    }

    @FXML
    public void initialize() {
        setupAvailableOrdersTable();
        setupHistoryTable();
    }

    private void setupAvailableOrdersTable() {
        colOrderId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));

        colCustomerName.setCellValueFactory(cellData -> {
            int customerId = cellData.getValue().getCustomerId();
            User customer = userService.getUserById(customerId);
            return new SimpleStringProperty(customer != null ? customer.getUsername() : "Unknown");
        });

        colAddress.setCellValueFactory(cellData -> {
            int customerId = cellData.getValue().getCustomerId();
            User customer = userService.getUserById(customerId);
            return new SimpleStringProperty(customer != null ? customer.getAddress() : "Unknown");
        });

        colTotalPrice.setCellValueFactory(
                cellData -> new SimpleStringProperty("$" + String.format("%.2f", cellData.getValue().getTotalCost())));
    }

    private void setupHistoryTable() {
        colHistOrderId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));

        colHistDate.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDeliveryTime();
            return new SimpleStringProperty(date != null ? date.toString() : "-");
        });

        colHistStatus
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        colHistRating.setCellValueFactory(cellData -> {
            double rating = userService.getCarrierRating(currentUser.getId());
            // Ideally we'd want rating per order, but our Service/DAO structure
            // currently exposes an average rating or would require a specific lookup per
            // order.
            // For now, let's leave it as a placeholder or implement specific logic if DAO
            // supports it.
            // Since the column is "Rating" for specific order, we might need a fetch.
            // But existing code doesn't have `getRatingForOrder`.
            // Leaving as "-" for now to avoid N+1 queries without tailored Service method.
            return new SimpleStringProperty("-");
        });
    }

    private void refreshAll() {
        loadAvailableOrders();
        loadMyCurrentOrder();
        loadHistory();
    }

    private void loadAvailableOrders() {
        List<Order> orders = orderService.getPendingOrders();
        ObservableList<Order> observableOrders = FXCollections.observableArrayList(orders);
        availableOrdersTable.setItems(observableOrders);
    }

    private void loadMyCurrentOrder() {
        List<Order> myOrders = orderService.getOrdersByCarrier(currentUser.getId());

        // Find the active SELECTED order
        Order activeOrder = myOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.SELECTED)
                .findFirst()
                .orElse(null);

        if (activeOrder != null) {
            User customer = userService.getUserById(activeOrder.getCustomerId());
            String details = String.format("Order ID: %d\nCustomer: %s\nAddress: %s\nTotal: $%.2f",
                    activeOrder.getId(),
                    customer != null ? customer.getUsername() : "Unknown",
                    customer != null ? customer.getAddress() : "Unknown",
                    activeOrder.getTotalCost());

            currentOrderDetailsLabel.setText(details);
            deliveryDatePicker.setDisable(false);
            completeDeliveryButton.setDisable(false);
        } else {
            currentOrderDetailsLabel.setText("No active order selected. Go to 'Available Orders' to pick one.");
            deliveryDatePicker.setDisable(true);
            completeDeliveryButton.setDisable(true);
            deliveryDatePicker.setValue(null);
        }
    }

    private void loadHistory() {
        List<Order> myOrders = orderService.getOrdersByCarrier(currentUser.getId());

        // Filter for completed orders
        List<Order> completedOrders = myOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                .toList();

        completedOrdersTable.setItems(FXCollections.observableArrayList(completedOrders));
    }

    @FXML
    private void handleAcceptOrder() {
        Order selectedOrder = availableOrdersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("No Selection", "Please select an order to take.");
            return;
        }

        try {
            orderService.assignOrderToCarrier(selectedOrder.getId(), currentUser.getId());
            showAlert("Success", "You have taken Order #" + selectedOrder.getId());
            refreshAll();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleCompleteDelivery() {
        // We need to know WHICH order is active.
        // Since UI has a textual label, we should re-fetch the active order logic
        // or store the active order in a field. Let's re-fetch for safety.
        List<Order> myOrders = orderService.getOrdersByCarrier(currentUser.getId());
        Order activeOrder = myOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.SELECTED)
                .findFirst()
                .orElse(null);

        if (activeOrder == null) {
            showAlert("Error", "No active order to complete.");
            return;
        }

        if (deliveryDatePicker.getValue() == null) {
            showAlert("Missing Date", "Please select a delivery date.");
            return;
        }

        try {
            Date deliveryDate = Date
                    .from(deliveryDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            orderService.completeOrder(activeOrder.getId(), deliveryDate);
            showAlert("Success", "Order #" + activeOrder.getId() + " delivered!");

            // Clear inputs
            deliveryDatePicker.setValue(null);
            refreshAll();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        // Close current stage
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();

        // Ideally, redirect to Login Screen here (not handled in this file as per
        // rules, controller handles its own stage)
        // System.out.println("User logged out.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
