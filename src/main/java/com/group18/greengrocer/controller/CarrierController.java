package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.UserService;
import com.group18.greengrocer.util.AlertUtil;
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
import java.util.stream.Collectors;

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
    private TableColumn<Order, String> colAvProducts; // [NEW]
    @FXML
    private TableColumn<Order, String> colAvDate; // [NEW]
    @FXML
    private TableColumn<Order, String> colTotalPrice;
    @FXML
    private Button acceptOrderButton;

    // My Current Order Tab
    @FXML
    private TableView<Order> currentOrdersTable; // [NEW] Table instead of Label
    @FXML
    private TableColumn<Order, Integer> colCurOrderId;
    @FXML
    private TableColumn<Order, String> colCurCustomer;
    @FXML
    private TableColumn<Order, String> colCurAddress;
    @FXML
    private TableColumn<Order, String> colCurProducts;
    @FXML
    private TableColumn<Order, String> colCurTotal;
    @FXML
    private TableColumn<Order, String> colCurStatus;

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
        setupCurrentOrdersTable();
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

        // Format product list: "3.0kg Apple, 1.5kg Banana"
        colAvProducts.setCellValueFactory(cellData -> {
            List<CartItem> items = cellData.getValue().getItems();
            if (items == null || items.isEmpty())
                return new SimpleStringProperty("No details");
            String summary = items.stream()
                    .map(item -> String.format("%.1fkg %s", item.getQuantity(),
                            item.getProduct() != null ? item.getProduct().getName() : "?"))
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(summary);
        });

        colAvDate.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getOrderTime();
            return new SimpleStringProperty(date != null ? date.toString() : "-");
        });

        colTotalPrice.setCellValueFactory(
                cellData -> new SimpleStringProperty("$" + String.format("%.2f", cellData.getValue().getTotalCost())));
    }

    private void setupCurrentOrdersTable() {
        colCurOrderId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));

        colCurCustomer.setCellValueFactory(cellData -> {
            int customerId = cellData.getValue().getCustomerId();
            User customer = userService.getUserById(customerId);
            return new SimpleStringProperty(customer != null ? customer.getUsername() : "Unknown");
        });

        colCurAddress.setCellValueFactory(cellData -> {
            int customerId = cellData.getValue().getCustomerId();
            User customer = userService.getUserById(customerId);
            return new SimpleStringProperty(customer != null ? customer.getAddress() : "Unknown");
        });

        colCurProducts.setCellValueFactory(cellData -> {
            List<CartItem> items = cellData.getValue().getItems();
            if (items == null || items.isEmpty())
                return new SimpleStringProperty("No details");
            String summary = items.stream()
                    .map(item -> String.format("%.1fkg %s", item.getQuantity(),
                            item.getProduct() != null ? item.getProduct().getName() : "?"))
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(summary);
        });

        colCurTotal.setCellValueFactory(
                cellData -> new SimpleStringProperty("$" + String.format("%.2f", cellData.getValue().getTotalCost())));

        colCurStatus
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
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
            int rating = userService.getRatingForOrder(cellData.getValue().getId());
            return new SimpleStringProperty(rating > 0 ? String.valueOf(rating) : "Not Rated");
        });
    }

    private void refreshAll() {
        loadAvailableOrders();
        loadMyCurrentOrders();
        loadHistory();
    }

    private void loadAvailableOrders() {
        List<Order> orders = orderService.getPendingOrders();
        ObservableList<Order> observableOrders = FXCollections.observableArrayList(orders);
        availableOrdersTable.setItems(observableOrders);
    }

    private void loadMyCurrentOrders() {
        List<Order> myOrders = orderService.getOrdersByCarrier(currentUser.getId());

        // Filter for active SELECTED orders (One or Many)
        List<Order> activeOrders = myOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.SELECTED)
                .toList();

        currentOrdersTable.setItems(FXCollections.observableArrayList(activeOrders));

        // Disable buttons if no orders, but generally they are enabled for selection
        boolean hasOrders = !activeOrders.isEmpty();
        deliveryDatePicker.setDisable(!hasOrders);
        completeDeliveryButton.setDisable(!hasOrders);
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
            AlertUtil.showWarning("No Selection", "Please select an order to take.");
            return;
        }

        try {
            orderService.assignOrderToCarrier(selectedOrder.getId(), currentUser.getId());
            AlertUtil.showInfo("Success", "You have taken Order #" + selectedOrder.getId());
            refreshAll();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleCompleteDelivery() {
        Order selectedOrder = currentOrdersTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            AlertUtil.showWarning("No Selection", "Please select the order you are delivering from the list above.");
            return;
        }

        if (deliveryDatePicker.getValue() == null) {
            AlertUtil.showWarning("Missing Date", "Please select a delivery date.");
            return;
        }

        try {
            Date deliveryDate = Date
                    .from(deliveryDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            orderService.completeOrder(selectedOrder.getId(), deliveryDate);
            AlertUtil.showInfo("Success", "Order #" + selectedOrder.getId() + " delivered!");

            // Clear inputs
            deliveryDatePicker.setValue(null);
            refreshAll();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        // Close current stage
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }
}
