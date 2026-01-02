package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Message;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.User;
// Services
import com.group18.greengrocer.service.DiscountService;
import com.group18.greengrocer.service.MessageService;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.ProductService;
import com.group18.greengrocer.service.UserService;
// Utils
import com.group18.greengrocer.util.AlertUtil;
import com.group18.greengrocer.util.SessionManager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
<<<<<<< HEAD
import javafx.scene.control.*;
import javafx.stage.Stage;
=======
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.util.ValidatorUtil;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
>>>>>>> 6e070348b5d31ffe8f82250157c5ecfcc98110f8

import java.io.IOException;

/**
 * Controller class for the Owner Dashboard.
 * Manages Products, Carriers, Orders, and Messages.
 */
public class OwnerController {
<<<<<<< HEAD

    // =============================================================
    // SECTION: SERVICE DECLARATIONS
    // =============================================================
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final DiscountService discountService;
    private final MessageService messageService; // Corrected from MessagesService

    private User currentUser;

    // =============================================================
    // SECTION: FXML UI COMPONENTS
    // =============================================================

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;

    // --- Product Tab Elements ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, String> typeCol;
    @FXML private TableColumn<Product, String> unitCol;
    @FXML private TableColumn<Product, Double> priceCol;
    @FXML private TableColumn<Product, Double> stockCol;
    @FXML private TableColumn<Product, Double> thresholdCol;
    
    @FXML private Label effectivePriceLabel;
    @FXML private TextField nameField, typeField, unitField, priceField, stockField, thresholdField;
    @FXML private ComboBox<Category> categoryCombo;

    // --- Carrier Tab Elements ---
    @FXML private TableView<User> carrierTable;
    @FXML private TableColumn<User, Integer> carrierIdCol;
    @FXML private TableColumn<User, String> carrierNameCol;
    @FXML private TableColumn<User, String> carrierPhoneCol;
    @FXML private TableColumn<User, String> carrierAddressCol;
    @FXML private TextField carrierUsernameField, carrierPhoneField;
    @FXML private PasswordField carrierPasswordField;
    @FXML private TextArea carrierAddressArea;

    // --- Order Tab Elements ---
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> orderIdCol;
    @FXML private TableColumn<Order, Integer> orderCustomerCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, Double> orderTotalCol;
    @FXML private TableColumn<Order, String> orderStatusCol;
    @FXML private TableColumn<Order, Integer> orderCarrierCol;
    @FXML private TextArea orderDetailsArea;

    // --- Messages Tab Elements (Fixed Missing Definitions) ---
    @FXML private TableView<Message> messageTable;
    @FXML private TableColumn<Message, String> fromCol;
    @FXML private TableColumn<Message, String> dateCol;
    @FXML private TableColumn<Message, String> previewCol;
    
    @FXML private Label fromLabel;      
    @FXML private TextArea contentArea; 
    @FXML private TextArea replyField;
=======
    // --- Services ---

    private User currentUser;
    private final ProductService productService;
    private final UserService userService;
    private final DiscountService discountService;
>>>>>>> 6e070348b5d31ffe8f82250157c5ecfcc98110f8

    // =============================================================
    // SECTION: CONSTRUCTOR & INITIALIZATION
    // =============================================================

    public OwnerController() {
        // Initialize all backend services
        this.productService = new ProductService();
        this.userService = new UserService();
        this.orderService = new OrderService();
        this.discountService = new DiscountService();
        this.messageService = new MessageService(); // Initialized correctly
    }

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText("Owner: " + currentUser.getUsername());
        }

        if (categoryCombo != null) {
            categoryCombo.getItems().setAll(Category.values());
        }

        // Messages Setup
        if (messagesTable != null) {
            msgFromCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSenderName()));
            msgDateCol.setCellValueFactory(cell -> {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                return new SimpleStringProperty(sdf.format(cell.getValue().getSentAt()));
            });
            msgContentCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getContent()));

            messagesTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> showMessageDetails(newV));
            handleRefreshMessages();
        }

        // Order Table
        if (ordersTable != null) {
            orderIdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
            orderCustomerCol.setCellValueFactory(
                    cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getCustomerId())));
            orderDateCol
                    .setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrderTime().toString()));
            orderTotalCol.setCellValueFactory(
                    cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getTotalCost())));
            orderStatusCol.setCellValueFactory(cell -> {
                com.group18.greengrocer.model.Order.Status status = cell.getValue().getStatus();
                if (status == com.group18.greengrocer.model.Order.Status.WAITING)
                    return new SimpleStringProperty("Waiting");
                if (status == com.group18.greengrocer.model.Order.Status.RECEIVED)
                    return new SimpleStringProperty("Received");
                return new SimpleStringProperty(status.toString());
            });

            ordersTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> showOrderDetails(newV));
        }

        // Bind Buttons to Selection
        if (addButton != null && updateButton != null && productTable != null) {
            addButton.disableProperty().bind(productTable.getSelectionModel().selectedItemProperty().isNotNull());
            updateButton.disableProperty().bind(productTable.getSelectionModel().selectedItemProperty().isNull());
        }

        loadOwnerData();
        loadCarrierData();
        handleRefreshReports();
        handleRefreshOrders();

        // Init Loyalty Fields with current values
        if (loyaltyMinOrderField != null) {
            loyaltyMinOrderField.setText(String.valueOf(discountService.getLoyaltyMinOrderCount()));
        }
        if (loyaltyRateField != null) {
            loyaltyRateField.setText(String.valueOf(discountService.getLoyaltyDiscountRate()));
        }
    }

    // ================= ORDER APPROVAL =================
    @FXML
    private void handleMarkAsRead() {
        // Validation: Check if a message is selected to prevent NullPointerException
        Message selected = messageTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("Selection Error", "Please select a message to mark as read.");
            return; // Exit method safely
        }

        try {
            // Database operation
            messageService.markAsRead(selected.getId());
            
            // UI Feedback
            AlertUtil.showInfo("Success", "Message marked as read.");
            
            // Refresh logic: Reload list to update status
            loadMessages(); 
            
            // Clear details
            fromLabel.setText("");
            contentArea.clear();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Operation Failed", "Could not update message status: " + e.getMessage());
        }
    }

    /**
     * Loads messages from the database and populates the table.
     */
    private void loadMessages() {
        if (messageTable != null) {
            messageTable.getItems().setAll(messageService.getAllMessages());
        }
    }

    /**
     * Handles sending a reply (Placeholder implementation).
     */
    @FXML
    private void handleSendReply() {
        Message selected = messageTable.getSelectionModel().getSelectedItem();
        String replyText = replyField.getText();

        if (selected == null || replyText.trim().isEmpty()) {
            AlertUtil.showWarning("Input Error", "Please select a message and enter a reply.");
            return;
        }

        try {
            // Future implementation: messageService.sendReply(...)
            AlertUtil.showInfo("Info", "Reply functionality is under development.\nDraft: " + replyText);
            replyField.clear();
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to send reply.");
        }
    }

    // =============================================================
    // SECTION: DATA REFRESH HELPERS
    // =============================================================

    private void refreshProductTable() {
        productTable.getItems().setAll(productService.getAllProducts());
    }
    
    private void refreshCarrierTable() {
        if (carrierTable != null) {
           carrierTable.getItems().setAll(userService.getAllCarriers());
        }
    }

    private void refreshOrderTable() {
        if (orderTable != null) {
            orderTable.getItems().setAll(orderService.getAllOrders());
        }
    }

    // =============================================================
    // SECTION: NAVIGATION
    // =============================================================

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group18/greengrocer/fxml/goodbye.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
<<<<<<< HEAD
            stage.setScene(new Scene(root));
        } catch (IOException e) {
=======
            stage.getScene().setRoot(root);
            stage.setTitle("Group18 GreenGrocer - Login");
            stage.show();
            stage.setMaximized(true);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not go to login screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to delete.");
            return;
        }

        Optional<ButtonType> result = AlertUtil.showConfirmation(
                "Delete Product",
                "Are you sure you want to delete " + selected.getName() + "?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.removeProduct(selected.getId());
                AlertUtil.showInfo("Success", "Product deleted successfully.");
                loadOwnerData();
            } catch (Exception e) {
                AlertUtil.showError("Error", "Could not delete product: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        if (ValidatorUtil.isEmpty(nameField.getText())) {
            AlertUtil.showWarning("Validation", "Name is required.");
            return false;
        }
        if (!ValidatorUtil.isValidName(nameField.getText())) {
            AlertUtil.showWarning("Validation", "Product Name must contain only letters and spaces.");
            return false;
        }

        if (categoryCombo.getValue() == null) {
            AlertUtil.showWarning("Validation", "Category is required.");
            return false;
        }

        // Optional: Validate Type if desired
        if (!ValidatorUtil.isEmpty(typeField.getText()) && !ValidatorUtil.isValidName(typeField.getText())) {
            AlertUtil.showWarning("Validation", "Type must contain only letters and spaces.");
            return false;
        }

        if (ValidatorUtil.isEmpty(priceField.getText()) ||
                ValidatorUtil.isEmpty(stockField.getText()) ||
                ValidatorUtil.isEmpty(thresholdField.getText())) {
            AlertUtil.showWarning("Validation", "Price, Stock and Threshold are required.");
            return false;
        }
        return true;
    }

    private void loadCarrierData() {
        if (carrierTable == null)
            return;
        carrierTable.getItems().setAll(userService.getAllCarriers());
    }

    @FXML
    private void handleRefreshCarriers() {
        loadCarrierData();
        AlertUtil.showInfo("Refreshed", "Carrier list refreshed.");
    }

    @FXML
    private void handleViewCarrierRatings() {
        User selectedCarrier = carrierTable.getSelectionModel().getSelectedItem();
        if (selectedCarrier == null) {
            AlertUtil.showWarning("No Selection", "Please select a carrier to view ratings.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Ratings for: " + selectedCarrier.getUsername());

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setPrefSize(400, 500);

        Label header = new Label("Customer Feedback");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> reviewList = new ListView<>();
        VBox.setVgrow(reviewList, Priority.ALWAYS);

        // Fetch ratings from DB
        java.util.List<com.group18.greengrocer.model.Order> carrierOrders = orderService
                .getOrdersByCarrier(selectedCarrier.getId());

        java.util.List<String> reviews = new java.util.ArrayList<>();
        double totalScore = 0;
        int count = 0;

        for (com.group18.greengrocer.model.Order o : carrierOrders) {
            if (o.getRating() > 0) {
                reviews.add("★ " + o.getRating() + "/5\n" + (o.getReview() == null ? "" : o.getReview()) +
                        "\n-------------------");
                totalScore += o.getRating();
                count++;
            }
        }

        if (reviews.isEmpty()) {
            reviews.add("No ratings yet.");
        } else {
            double avg = totalScore / count;
            header.setText("Average Rating: " + String.format("%.1f", avg) + " / 5.0");
        }

        reviewList.getItems().setAll(reviews);

        root.getChildren().addAll(header, reviewList);
        stage.setScene(new Scene(root));
        stage.setMaximized(true);
        stage.show();
    }

    @FXML
    private void handleHireCarrier() {
        String u = carrierUsernameField.getText();
        String p = carrierPasswordField.getText();
        String ph = carrierPhoneField.getText();
        String ad = carrierAddressArea.getText();

        if (ValidatorUtil.isEmpty(u) || ValidatorUtil.isEmpty(p) ||
                ValidatorUtil.isEmpty(ph) || ValidatorUtil.isEmpty(ad)) {
            AlertUtil.showWarning("Validation", "All fields are required.");
            return;
        }

        try {
            User carrier = new User();
            carrier.setUsername(u);
            carrier.setPassword(p);
            carrier.setPhoneNumber(ph);
            carrier.setAddress(ad);
            userService.addCarrier(carrier);
            AlertUtil.showInfo("Success", "New Carrier hired: " + u);
            carrierUsernameField.clear();
            carrierPasswordField.clear();
            carrierPhoneField.clear();
            carrierAddressArea.clear();
            loadCarrierData();
        } catch (Exception e) {
            AlertUtil.showError("Hire Error", e.getMessage());
        }
    }

    @FXML
    private void handleFireCarrier() {
        User selected = carrierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a carrier to fire.");
            return;
        }
        Optional<ButtonType> res = AlertUtil.showConfirmation("Fire Carrier",
                "Are you sure you want to fire " + selected.getUsername() + "?");
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                userService.removeCarrier(selected.getId());
                AlertUtil.showInfo("Success", "Carrier fired.");
                loadCarrierData();
            } catch (Exception e) {
                AlertUtil.showError("Fire Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCreateCoupon() {
        String code = couponCodeField.getText();
        String amtStr = couponAmountField.getText();
        java.time.LocalDate expiry = couponExpiryPicker.getValue();

        if (ValidatorUtil.isEmpty(code) || ValidatorUtil.isEmpty(amtStr) || expiry == null) {
            AlertUtil.showWarning("Validation", "All coupon fields are required.");
            return;
        }

        try {
            double amount = Double.parseDouble(amtStr);
            if (amount <= 0) {
                AlertUtil.showError("Error", "Discount amount must be positive.");
                return;
            }
            com.group18.greengrocer.model.Coupon c = new com.group18.greengrocer.model.Coupon();
            c.setCode(code.trim());
            c.setDiscountAmount(amount);
            c.setExpiryDate(java.sql.Date.valueOf(expiry));
            c.setActive(true);
            discountService.createCoupon(c);
            AlertUtil.showInfo("Success", "Coupon created: " + code);
            couponCodeField.clear();
            couponAmountField.clear();
            couponExpiryPicker.setValue(null);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Error", "Amount must be a number.");
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to create coupon: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateLoyalty() {
        String minStr = loyaltyMinOrderField.getText();
        String rateStr = loyaltyRateField.getText();

        if (ValidatorUtil.isEmpty(minStr) || ValidatorUtil.isEmpty(rateStr)) {
            AlertUtil.showWarning("Validation", "Both loyalty fields are required.");
            return;
        }

        try {
            int minOrder = Integer.parseInt(minStr);
            double rate = Double.parseDouble(rateStr);
            discountService.updateLoyaltyRules(minOrder, rate);
            AlertUtil.showInfo("Success", "Loyalty rules updated.");
        } catch (NumberFormatException e) {
            AlertUtil.showError("Error", "Please enter valid numbers.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    // Charts
    @FXML
    private javafx.scene.chart.PieChart categoryPieChart;
    @FXML
    private javafx.scene.chart.PieChart orderStatusChart; // NEW
    @FXML
    private javafx.scene.chart.BarChart<String, Number> productSalesChart;
    @FXML
    private javafx.scene.chart.LineChart<String, Number> revenueChart;

    // Summary Labels
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label activeCustomersLabel;
    @FXML
    private Label avgOrderValueLabel;

    @FXML
    private void handleRefreshReports() {
        if (categoryPieChart == null || productSalesChart == null || revenueChart == null)
            return;

        // 0. Summary Cards
        if (totalRevenueLabel != null) {
            double rev = orderService.getTotalRevenue();
            totalRevenueLabel.setText(String.format("%.2f TL", rev));
        }
        if (totalOrdersLabel != null) {
            totalOrdersLabel.setText(String.valueOf(orderService.getTotalOrdersCount()));
        }
        if (activeCustomersLabel != null) {
            activeCustomersLabel.setText(String.valueOf(orderService.getActiveCustomersCount()));
        }
        if (avgOrderValueLabel != null) {
            double rev = orderService.getTotalRevenue();
            int count = orderService.getTotalOrdersCount();
            double avg = (count > 0) ? rev / count : 0.0;
            avgOrderValueLabel.setText(String.format("%.2f TL", avg));
        }

        // 1. Pie Chart (Categories)
        java.util.Map<String, Double> catData = orderService.getSalesByCategory();
        categoryPieChart.getData().clear();
        catData.forEach((cat, val) -> {
            categoryPieChart.getData().add(new javafx.scene.chart.PieChart.Data(cat, val));
        });

        // 1.5 Pie Chart (Order Status)
        if (orderStatusChart != null) {
            java.util.Map<String, Integer> statusData = orderService.getOrderStatusDistribution();
            orderStatusChart.getData().clear();
            statusData.forEach((stat, val) -> {
                orderStatusChart.getData().add(new javafx.scene.chart.PieChart.Data(stat, val));
            });
        }

        // 2. Bar Chart (Top Products - Sorted & Limited)
        java.util.Map<String, Double> prodData = orderService.getRevenueByProduct();
        productSalesChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> seriesP = new javafx.scene.chart.XYChart.Series<>();
        seriesP.setName("Revenue");

        // Sort descending and take top 10
        prodData.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(e -> seriesP.getData().add(new javafx.scene.chart.XYChart.Data<>(e.getKey(), e.getValue())));

        productSalesChart.getData().add(seriesP);

        // 3. Line Chart (Time)
        java.util.Map<String, Double> timeData = orderService.getRevenueOverTime();
        revenueChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> seriesT = new javafx.scene.chart.XYChart.Series<>();
        seriesT.setName("Total Sales");
        timeData.forEach((date, val) -> {
            seriesT.getData().add(new javafx.scene.chart.XYChart.Data<>(date, val));
        });
        revenueChart.getData().add(seriesT);
    }

    // ================= MESSAGE LOGIC =================

    @FXML
    private void handleRefreshMessages() {
        if (messagesTable == null)
            return;
        try {
            messagesTable.getItems().setAll(messageService.getMessagesForOwner());
        } catch (Exception e) {
            // Might fail if not logged in as Owner, but this is OwnerController
            System.err.println("Failed to load messages: " + e.getMessage());
        }
    }

    private void showMessageDetails(Message msg) {
        if (msgSenderField == null || msgReadArea == null)
            return;

        if (msg == null) {
            msgSenderField.clear();
            msgReadArea.clear();
            msgReplyArea.clear();
            return;
        }

        msgSenderField.setText(msg.getSenderName() + " (ID: " + msg.getSenderId() + ")");

        // Load Conversation Log
        java.util.List<Message> conversation = messageService.getConversation(msg.getConversationId());
        StringBuilder sb = new StringBuilder();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm");

        for (Message m : conversation) {
            sb.append("[").append(sdf.format(m.getSentAt())).append("] ");
            sb.append(m.getSenderName()).append(": ");
            sb.append(m.getContent()).append("\n\n");
        }

        msgReadArea.setText(sb.toString());
        msgReadArea.setScrollTop(Double.MAX_VALUE); // Scroll to bottom

        if (msg.getConversationStatus() != null && msg.getConversationStatus().equalsIgnoreCase("CLOSED")) {
            msgReplyArea.setDisable(true);
            msgReplyArea.setPromptText("This conversation is CLOSED.");
        } else {
            msgReplyArea.setDisable(false);
            msgReplyArea.setPromptText("Type your reply here...");
        }

        // Auto-mark as read if unread
        if (!msg.isRead()) {
            try {
                messageService.markMessageAsRead(msg.getId());
                msg.setRead(true);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Handles the "Mark as Read" button action.
     */
    @FXML
    private void handleMarkAsRead() {
        // 1. Validation
        Message selected = messagesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showWarning("Selection Error", "Please select a message to mark as read.");
            return;
        }

        try {
            // 2. Database Update
            messageService.markMessageAsRead(selected.getId());
            
            // 3. User Feedback
            AlertUtil.showInfo("Success", "Message marked as read.");
            
            // 4. UI Refresh
            handleRefreshMessages(); 
            
            // 5. Cleanup
            if (msgSenderField != null) msgSenderField.clear();
            if (msgReadArea != null) msgReadArea.clear();
            if (msgReplyArea != null) msgReplyArea.clear();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Operation Failed", "Could not mark message as read: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendReply() {
        Message selected = messagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a message to reply to.");
            return;
        }

        String replyText = msgReplyArea.getText();
        if (ValidatorUtil.isEmpty(replyText)) {
            AlertUtil.showWarning("Validation", "Reply cannot be empty.");
            return;
        }

        try {
            messageService.replyToMessage(selected.getId(), replyText);

            msgReplyArea.clear();
            msgReplyArea.setPromptText("Reply sent successfully!");

            // Refresh table details to show new message in log
            showMessageDetails(selected);

        } catch (Throwable e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to send reply: " + e.getMessage());
        }
    }

    @FXML
    private void handleEndConversation() {
        Message selected = messagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a conversation to end.");
            return;
        }

        try {
            messageService.closeConversation(selected.getConversationId());
            AlertUtil.showInfo("Success", "Conversation closed.");
            handleRefreshMessages(); // Refresh list to update status

        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to close conversation: " + e.getMessage());
        }
    }
}
>>>>>>> 6e070348b5d31ffe8f82250157c5ecfcc98110f8
