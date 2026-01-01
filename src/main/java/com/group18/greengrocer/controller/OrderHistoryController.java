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
                data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId())
        );

        dateColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getRequestedDeliveryDate() != null
                                ? data.getValue().getRequestedDeliveryDate().toString()
                                : "-"
                )
        );

        statusColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatus().toString()
                )
        );

        totalColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getTotalCost()
                )
        );
    }

    // =====================
    // INIT DATA
    // =====================
    public void initData(User user) {
        this.currentUser = user;
        ordersTable.setItems(
                FXCollections.observableArrayList(
                        orderService.getOrdersByCustomer(user.getId())
                )
        );
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
                        + "\nTotal: " + selected.getTotalCost() + " TL"
        );
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
