package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.UserService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.group18.greengrocer.util.SessionManager;

/**
 * Controller for Carrier Dashboard.
 * Handles order selection, delivery completion and history.
 */
public class CarrierController {

    // Services
    private final OrderService orderService = new OrderService();
    private final UserService userService = new UserService();

    // Logged in carrier
    private User currentUser;

    // ===== HEADER =====
    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutButton;

    // ===== AVAILABLE ORDERS =====
    @FXML
    private TableView<Order> availableOrdersTable;
    @FXML
    private TableColumn<Order, Integer> colOrderId;
    @FXML
    private TableColumn<Order, String> colCustomerName;
    @FXML
    private TableColumn<Order, String> colAddress;
    @FXML
    private TableColumn<Order, String> colAvProducts;
    @FXML
    private TableColumn<Order, String> colAvDate;
    @FXML
    private TableColumn<Order, String> colTotalPrice;
    @FXML
    private Button acceptOrderButton;

    // ===== CURRENT ORDERS =====
    // ===== CURRENT ORDERS =====
    @FXML private TableView<Order> currentOrdersTable;
    @FXML private TableColumn<Order, Integer> colCurOrderId;
    @FXML private TableColumn<Order, String> colCurCustomer;
    @FXML private TableColumn<Order, String> colCurAddress;
    @FXML private TableColumn<Order, String> colCurProducts;
    @FXML private TableColumn<Order, String> colCurTotal;
    @FXML private TableColumn<Order, String> colCurStatus;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<Integer> deliveryHourCombo;
    @FXML private ComboBox<Integer> deliveryMinuteCombo;
    @FXML private Button completeDeliveryButton;

    // ===== HISTORY =====
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

    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Carrier: " + user.getUsername());
        refreshAll();
    }

    @FXML
    public void initialize() {
        colAvDate.setText("Req. Delivery");
        setupAvailableOrdersTable();
        setupCurrentOrdersTable();
        setupHistoryTable();

        // Initialize Time Combos
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) hours.add(i);
        deliveryHourCombo.setItems(hours);
        deliveryHourCombo.getSelectionModel().select(Integer.valueOf(12));

        ObservableList<Integer> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 15) minutes.add(i);
        deliveryMinuteCombo.setItems(minutes);
        deliveryMinuteCombo.getSelectionModel().selectFirst();
    }

    // ===== TABLE SETUPS =====

    private void setupAvailableOrdersTable() {

        colOrderId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getId()));

        colCustomerName.setCellValueFactory(cd -> {
            User c = userService.getUserById(cd.getValue().getCustomerId());
            return new SimpleStringProperty(c != null ? c.getUsername() : "Unknown");
        });

        colAddress.setCellValueFactory(cd -> {
            User c = userService.getUserById(cd.getValue().getCustomerId());
            return new SimpleStringProperty(c != null ? c.getAddress() : "Unknown");
        });

        colAvProducts.setCellValueFactory(cd -> {
            List<CartItem> items = cd.getValue().getItems();
            if (items == null || items.isEmpty())
                return new SimpleStringProperty("No details");

            String summary = items.stream()
                    .map(i -> String.format("%.1fkg %s",
                            i.getQuantity(),
                            i.getProduct() != null ? i.getProduct().getName() : "?"))
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(summary);
        });

        colAvDate.setCellValueFactory(cd -> {
            Date d = cd.getValue().getRequestedDeliveryDate();
            if (d == null)
                return new SimpleStringProperty("-");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(sdf.format(d));
        });

        colTotalPrice.setCellValueFactory(cd -> new SimpleStringProperty(
                String.format("%.2f", cd.getValue().getTotalCost())));
    }

    private void setupCurrentOrdersTable() {

        colCurOrderId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getId()));

        colCurCustomer.setCellValueFactory(cd -> {
            User c = userService.getUserById(cd.getValue().getCustomerId());
            return new SimpleStringProperty(c != null ? c.getUsername() : "Unknown");
        });

        colCurAddress.setCellValueFactory(cd -> {
            User c = userService.getUserById(cd.getValue().getCustomerId());
            return new SimpleStringProperty(c != null ? c.getAddress() : "Unknown");
        });

        colCurProducts.setCellValueFactory(cd -> {
            List<CartItem> items = cd.getValue().getItems();
            if (items == null || items.isEmpty())
                return new SimpleStringProperty("No details");

            String summary = items.stream()
                    .map(i -> String.format("%.1fkg %s",
                            i.getQuantity(),
                            i.getProduct() != null ? i.getProduct().getName() : "?"))
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(summary);
        });

        colCurTotal.setCellValueFactory(
                cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getTotalCost())));

        colCurStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().toString()));
    }

    private void setupHistoryTable() {

        colHistOrderId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getId()));

        colHistDate.setCellValueFactory(cd -> {
            Date d = cd.getValue().getDeliveryTime();
            return new SimpleStringProperty(d != null ? d.toString() : "-");
        });

        colHistStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().toString()));

        colHistRating.setCellValueFactory(cd -> {
            int r = userService.getRatingForOrder(cd.getValue().getId());
            return new SimpleStringProperty(r > 0 ? String.valueOf(r) : "Not Rated");
        });
    }

    // ===== DATA LOAD =====

    private void refreshAll() {
        availableOrdersTable.setItems(
                FXCollections.observableArrayList(orderService.getPendingOrders()));

        List<Order> active = orderService.getOrdersByCarrier(currentUser.getId())
                .stream()
                .filter(o -> o.getStatus() == Order.Status.ON_THE_WAY)
                .toList();

        currentOrdersTable.setItems(FXCollections.observableArrayList(active));

        completedOrdersTable.setItems(
                FXCollections.observableArrayList(
                        orderService.getOrdersByCarrier(currentUser.getId())
                                .stream()
                                .filter(o -> o.getStatus() == Order.Status.DELIVERED)
                                .toList()));

        boolean hasActive = !active.isEmpty();
        deliveryDatePicker.setDisable(!hasActive);
        completeDeliveryButton.setDisable(!hasActive);
    }

    // ===== ACTIONS =====

    @FXML
    private void handleAcceptOrder() {
        Order selected = availableOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an order.");
            return;
        }

        try {
            orderService.assignOrderToCarrier(selected.getId(), currentUser.getId());
            showAlert("Success", "Order #" + selected.getId() + " assigned.");
            refreshAll();
        } catch (IllegalStateException e) {
            showAlert("Order Taken", "This order has already been taken.");
        }
    }

    @FXML
    private void handleCompleteDelivery() {

        Order selected = currentOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an order.");
            return;
        }

        if (deliveryDatePicker.getValue() == null) {
            showAlert("Missing Date", "Please select a delivery date.");
            return;
        }

        Integer hour = deliveryHourCombo.getValue();
        Integer minute = deliveryMinuteCombo.getValue();
        
        if (hour == null || minute == null) {
            showAlert("Missing Time", "Please select a delivery time.");
            return;
        }

        Date deliveryDate = Date.from(
                deliveryDatePicker.getValue()
                        .atTime(hour, minute)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        if (selected.getOrderTime() != null &&
                deliveryDate.before(selected.getOrderTime())) {
            showAlert("Invalid Date", "Delivery date cannot be before order date.");
            return;
        }

        Date requested = selected.getRequestedDeliveryDate();
        if (requested != null && deliveryDate.before(requested)) {
            showAlert("Warning", "Delivery is earlier than requested date.");
            // We allow this, as early delivery is usually OK, just a warning?
            // Actually original code returned!
            // "Delivery is earlier than requested date." implies failure.
            // But carrier can deliver earlier if customer accepts.
            // Let's keep original behavior: return.
            return;
        }

        orderService.completeOrder(selected.getId(), deliveryDate);
        showAlert("Success", "Order delivered.");
        deliveryDatePicker.setValue(null);
        refreshAll();
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Group18 GreenGrocer - Login");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not return to login screen.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}