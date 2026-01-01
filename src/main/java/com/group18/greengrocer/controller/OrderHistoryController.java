package com.group18.greengrocer.controller;

import javafx.collections.FXCollections;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.OrderService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class OrderHistoryController {

        // =====================
        // FXML COMPONENTS
        // =====================
        @FXML
        private TableView<Order> ordersTable;

        @FXML
        private TableColumn<Order, Integer> idColumn;

        @FXML
        private TableColumn<Order, String> dateColumn;

        @FXML
        private TableColumn<Order, String> statusColumn;

        @FXML
        private TableColumn<Order, Double> totalColumn;

        // =====================
        // DATA
        // =====================
        private User currentUser;
        private final OrderService orderService = new OrderService();

        // =====================
        // INITIALIZE
        // =====================
        @FXML
        public void initialize() {

                idColumn.setCellValueFactory(
                                data -> {
                                        if (data.getValue() == null)
                                                return null;
                                        return new javafx.beans.property.SimpleObjectProperty<>(
                                                        data.getValue().getId());
                                });

                dateColumn.setCellValueFactory(
                                data -> {
                                        if (data.getValue() == null)
                                                return null;
                                        java.sql.Timestamp ts = data.getValue().getOrderTime();
                                        String text = "-";
                                        if (ts != null) {
                                                try {
                                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                                                                        "yyyy-MM-dd HH:mm");
                                                        text = sdf.format(ts);
                                                } catch (Exception e) {
                                                        text = ts.toString();
                                                }
                                        }
                                        return new javafx.beans.property.SimpleStringProperty(text);
                                });

                statusColumn.setCellValueFactory(
                                data -> {
                                        if (data.getValue() == null || data.getValue().getStatus() == null)
                                                return new javafx.beans.property.SimpleStringProperty("-");
                                        return new javafx.beans.property.SimpleStringProperty(
                                                        data.getValue().getStatus().toString());
                                });

                totalColumn.setCellValueFactory(
                                data -> {
                                        if (data.getValue() == null)
                                                return null;
                                        return new javafx.beans.property.SimpleObjectProperty<>(
                                                        data.getValue().getTotalCost());
                                });
        }

        // =====================
        // INIT DATA
        // =====================
        public void initData(User user) {
                this.currentUser = user;
                ordersTable.setItems(
                                FXCollections.observableArrayList(
                                                orderService.getOrdersByCustomer(user.getId())));
        }

        // =====================
        // ACTIONS
        // =====================
        @FXML
        private void handleViewDetails() {
                Order selected = ordersTable.getSelectionModel().getSelectedItem();
                if (selected == null) {
                        showAlert("Warning", "Please select an order.");
                        return;
                }

                showAlert(
                                "Order Details",
                                "Order ID: " + selected.getId()
                                                + "\nStatus: " + selected.getStatus()
                                                + "\nTotal: " + selected.getTotalCost() + " TL");
        }

        @FXML
        private void handleCancelOrder() {
                Order selected = ordersTable.getSelectionModel().getSelectedItem();
                if (selected == null) {
                        showAlert("Warning", "Please select an order to cancel.");
                        return;
                }

                if (selected.getStatus() == Order.Status.COMPLETED) {
                        showAlert("Error", "You cannot cancel a delivered order.");
                        return;
                }

                if (selected.getStatus() == Order.Status.CANCELLED) {
                        showAlert("Error", "You cannot cancel a cancelled order.");
                        return;
                }

                // 1-HOUR CANCELLATION LIMIT
                if (selected.getOrderTime() != null) {
                        long diff = System.currentTimeMillis() - selected.getOrderTime().getTime();
                        if (diff > 3600000) { // 1 hour
                                showAlert("Error", "You cannot cancel an order placed more than 1 hour ago.");
                                return;
                        }
                }

                try {
                        orderService.cancelOrder(selected.getId(), currentUser.getId());
                        showAlert("Success", "Order #" + selected.getId() + " has been cancelled.");

                        // Refresh table
                        initData(currentUser);

                } catch (Exception e) {
                        showAlert("Error", "Could not cancel order: " + e.getMessage());
                }
        }

        @FXML
        private void handleClose() {
                Stage stage = (Stage) ordersTable.getScene().getWindow();
                stage.close();
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
}
