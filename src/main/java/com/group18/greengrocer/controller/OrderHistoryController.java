package com.group18.greengrocer.controller;

import javafx.collections.FXCollections;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.OrderService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
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
                        showAlert("Success", "Order cancelled successfully.");

                        // Force refresh
                        ordersTable.getItems().clear();
                        initData(currentUser);
                        ordersTable.refresh();
                } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error", "Could not cancel order: " + e.getMessage());
                }
        }

        @FXML
        private void handleDownloadInvoice() {
                Order selected = ordersTable.getSelectionModel().getSelectedItem();
                if (selected == null) {
                        showAlert("Warning", "Please select an order.");
                        return;
                }

                String invoiceBase64 = selected.getInvoice();
                if (invoiceBase64 == null || invoiceBase64.isEmpty()) {
                        showAlert("Info", "No invoice available for this order.");
                        return;
                }

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Invoice");
                fileChooser.setInitialFileName("Invoice_" + selected.getId() + ".pdf");
                fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

                File file = fileChooser.showSaveDialog(ordersTable.getScene().getWindow());
                if (file != null) {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                                byte[] pdfBytes = Base64.getDecoder().decode(invoiceBase64);
                                fos.write(pdfBytes);
                                showAlert("Success", "Invoice saved to " + file.getAbsolutePath());
                        } catch (Exception e) {
                                e.printStackTrace();
                                showAlert("Error", "Failed to save invoice: " + e.getMessage());
                        }
                }
        }

        @FXML
        private void handleRateProducts() {
                Order selected = ordersTable.getSelectionModel().getSelectedItem();
                if (selected == null) {
                        showAlert("Warning", "Please select an order to rate products.");
                        return;
                }

                if (selected.getStatus() != Order.Status.COMPLETED) {
                        showAlert("Error", "You can only rate products from delivered orders.");
                        return;
                }

                if (selected.getItems().isEmpty()) {
                        showAlert("Error", "No items in this order.");
                        return;
                }

                // Show list of products to rate
                ChoiceDialog<com.group18.greengrocer.model.CartItem> itemDialog = new ChoiceDialog<>(
                                selected.getItems().get(0), selected.getItems());
                itemDialog.setTitle("Rate Product");
                itemDialog.setHeaderText("Select a product to rate");
                itemDialog.setContentText("Product:");

                itemDialog.showAndWait().ifPresent(item -> {
                        // Ask for rating
                        ChoiceDialog<Integer> ratingDialog = new ChoiceDialog<>(5, java.util.List.of(1, 2, 3, 4, 5));
                        ratingDialog.setTitle("Rate Product: " + item.getProduct().getName());
                        ratingDialog.setHeaderText("Rate this product (1-5)");
                        ratingDialog.setContentText("Stars:");

                        ratingDialog.showAndWait().ifPresent(rating -> {
                                try {
                                        orderService.rateProduct(currentUser.getId(), item.getProduct().getId(),
                                                        rating);
                                        showAlert("Success", "Product rated successfully!");
                                } catch (Exception e) {
                                        e.printStackTrace();
                                        showAlert("Error", "Failed to rate product: " + e.getMessage());
                                }
                        });
                });
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
