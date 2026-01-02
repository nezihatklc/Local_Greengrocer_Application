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
import com.group18.greengrocer.util.ValidatorUtil;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Controller class for the Owner Dashboard.
 * Manages Products, Carriers, Orders, and Messages.
 */
public class OwnerController {

    // =============================================================
    // SECTION: SERVICE DECLARATIONS
    // =============================================================
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final DiscountService discountService;
    private final MessageService messageService;

    private User currentUser;

    // =============================================================
    // SECTION: FXML UI COMPONENTS
    // =============================================================

    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutButton;

    // --- Product Tab Elements ---
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> idCol;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, String> typeCol;
    @FXML
    private TableColumn<Product, String> unitCol;
    @FXML
    private TableColumn<Product, Double> priceCol;
    @FXML
    private TableColumn<Product, Double> stockCol;
    @FXML
    private TableColumn<Product, Double> thresholdCol;

    @FXML
    private Label effectivePriceLabel;
    @FXML
    private TextField nameField, typeField, unitField, priceField, stockField, thresholdField;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private ImageView productImageView;
    @FXML
    private Button addButton, updateButton;

    // Internal state for image
    private byte[] currentImageBytes;

    // --- Carrier Tab Elements ---
    @FXML
    private TableView<User> carrierTable;
    @FXML
    private TableColumn<User, Integer> carrierIdCol;
    @FXML
    private TableColumn<User, String> carrierNameCol;
    @FXML
    private TableColumn<User, String> carrierPhoneCol;
    @FXML
    private TableColumn<User, String> carrierAddressCol;
    @FXML
    private TextField carrierUsernameField, carrierPhoneField;
    @FXML
    private PasswordField carrierPasswordField;
    @FXML
    private TextArea carrierAddressArea;

    // --- Order Tab Elements ---
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Integer> orderIdCol;
    @FXML
    private TableColumn<Order, Integer> orderCustomerCol;
    @FXML
    private TableColumn<Order, String> orderDateCol;
    @FXML
    private TableColumn<Order, Double> orderTotalCol;
    @FXML
    private TableColumn<Order, String> orderStatusCol;
    @FXML
    private TableColumn<Order, Integer> orderCarrierCol;
    @FXML
    private TextArea orderDetailsArea;

    // Charts
    @FXML
    private javafx.scene.chart.PieChart categoryPieChart;
    @FXML
    private javafx.scene.chart.PieChart orderStatusChart;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> productSalesChart;
    @FXML
    private javafx.scene.chart.LineChart<String, Number> revenueChart;

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label activeCustomersLabel;
    @FXML
    private Label avgOrderValueLabel;

    // Coupons / Loyalty (If elements exist in FXML)
    @FXML
    private TextField couponCodeField;
    @FXML
    private TextField couponAmountField;
    @FXML
    private DatePicker couponExpiryPicker;
    @FXML
    private TextField loyaltyMinOrderField;
    @FXML
    private TextField loyaltyRateField;

    // --- Messages Tab Elements ---
    @FXML
    private TableView<Message> messageTable;
    @FXML
    private TableColumn<Message, String> fromCol;
    @FXML
    private TableColumn<Message, String> dateCol;
    @FXML
    private TableColumn<Message, String> previewCol;

    @FXML
    private Label fromLabel;
    @FXML
    private TextArea contentArea;
    @FXML
    private TextArea replyField;

    // =============================================================
    // SECTION: CONSTRUCTOR & INITIALIZATION
    // =============================================================

    public OwnerController() {
        // Initialize all backend services
        this.productService = new ProductService();
        this.userService = new UserService();
        this.orderService = new OrderService();
        this.discountService = new DiscountService();
        this.messageService = new MessageService();
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
        if (messageTable != null) {
            fromCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSenderName()));
            dateCol.setCellValueFactory(cell -> {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                return new SimpleStringProperty(sdf.format(cell.getValue().getSentAt()));
            });
            previewCol.setCellValueFactory(cell -> {
                String status = cell.getValue().getConversationStatus();
                if (status == null)
                    status = "OPEN";
                return new SimpleStringProperty("[" + status + "] " + cell.getValue().getContent());
            });

            messageTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> showMessageDetails(newV));
            handleRefreshMessages();
        }

        // Order Table
        if (orderTable != null) {
            orderIdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
            orderCustomerCol.setCellValueFactory(
                    cell -> new SimpleObjectProperty<>(cell.getValue().getCustomerId()));
            orderDateCol
                    .setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrderTime().toString()));
            orderTotalCol.setCellValueFactory(
                    cell -> new SimpleObjectProperty<>(cell.getValue().getTotalCost()));
            orderStatusCol.setCellValueFactory(cell -> {
                com.group18.greengrocer.model.Order.Status status = cell.getValue().getStatus();
                if (status == com.group18.greengrocer.model.Order.Status.WAITING)
                    return new SimpleStringProperty("Waiting");
                if (status == com.group18.greengrocer.model.Order.Status.RECEIVED)
                    return new SimpleStringProperty("Received");
                return new SimpleStringProperty(status.toString());
            });

            orderTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> showOrderDetails(newV));
        }

        // Products Setup
        if (productTable != null) {
            idCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
            nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
            categoryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory().name()));
            typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
            unitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnit()));
            priceCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
            stockCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStock()));
            thresholdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getThreshold()));

            productTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> showProductDetails(newV));

            // Add Rating Column dynamically
            TableColumn<Product, String> ratingCol = new TableColumn<>("Rating");
            ratingCol.setCellValueFactory(cell -> {
                double avg = productService.getAverageProductRating(cell.getValue().getId());
                return new SimpleStringProperty(avg > 0 ? String.format("%.1f/5", avg) : "-");
            });
            productTable.getColumns().add(ratingCol);

            // Context Menu for Ratings
            ContextMenu cm = new ContextMenu();
            MenuItem viewRatingsItem = new MenuItem("View Ratings");
            viewRatingsItem.setOnAction(e -> handleViewProductRatings());
            cm.getItems().add(viewRatingsItem);
            productTable.setContextMenu(cm);
        }

        // Carrier Table Setup
        if (carrierTable != null) {
            carrierIdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
            carrierNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
            carrierPhoneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhoneNumber()));
            carrierAddressCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAddress()));
            
            // Context Menu for Carrier Ratings
            ContextMenu carrierCm = new ContextMenu();
            MenuItem viewCarrierRatingsItem = new MenuItem("View Ratings");
            viewCarrierRatingsItem.setOnAction(e -> handleViewCarrierRatings());
            carrierCm.getItems().add(viewCarrierRatingsItem);
            carrierTable.setContextMenu(carrierCm);
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

    // ================= REFRESH HELPERS =================
    private void loadOwnerData() {
        refreshProductTable();
    }

    private void refreshProductTable() {
        if (productTable != null)
            productTable.getItems().setAll(productService.getAllProductsForOwner());
    }

    private void loadCarrierData() {
        refreshCarrierTable();
    }

    private void refreshCarrierTable() {
        if (carrierTable != null) {
            carrierTable.getItems().setAll(userService.getAllCarriers());
        }
    }

    @FXML
    private void handleRefreshCarriers() {
        refreshCarrierTable();
        AlertUtil.showInfo("Refreshed", "Carrier list refreshed.");
    }

    @FXML
    private void handleRefreshOrders() {
        if (orderTable != null) {
            java.util.List<com.group18.greengrocer.model.Order> allOrders = orderService.getAllOrdersForOwner();
            java.util.List<com.group18.greengrocer.model.Order> pending = allOrders.stream()
                    .filter(o -> o.getStatus() == com.group18.greengrocer.model.Order.Status.WAITING ||
                            o.getStatus() == com.group18.greengrocer.model.Order.Status.RECEIVED)
                    .toList();
            orderTable.getItems().setAll(pending);
        }
    }

    // ================= PRODUCTS =================
    private void showProductDetails(Product product) {
        if (effectivePriceLabel == null)
            return;
        if (product == null) {
            handleClear();
            return;
        }
        nameField.setText(product.getName());
        categoryCombo.setValue(product.getCategory());
        typeField.setText(product.getType());
        unitField.setText(product.getUnit());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        thresholdField.setText(String.valueOf(product.getThreshold()));

        currentImageBytes = product.getImage();
        displayImage(currentImageBytes);

        try {
            double eff = productService.getEffectivePrice(product);
            effectivePriceLabel.setText(String.format("%.2f TL", eff));
        } catch (Exception e) {
            effectivePriceLabel.setText("-");
        }
    }

    @FXML
    private void handleRefresh() {
        refreshProductTable();
        handleClear();
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        typeField.clear();
        unitField.clear();
        priceField.clear();
        stockField.clear();
        thresholdField.clear();
        currentImageBytes = null;
        displayImage(null);
        if (productTable != null)
            productTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(usernameLabel.getScene().getWindow());
        if (file != null) {
            try {
                currentImageBytes = Files.readAllBytes(file.toPath());
                displayImage(currentImageBytes);
            } catch (IOException e) {
                AlertUtil.showError("Image Error", "Failed to read image.");
            }
        }
    }

    private void displayImage(byte[] data) {
        if (productImageView == null)
            return;
        if (data != null && data.length > 0) {
            try {
                productImageView.setImage(new Image(new ByteArrayInputStream(data)));
            } catch (Exception e) {
                productImageView.setImage(null);
            }
        } else {
            productImageView.setImage(null);
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateForm())
            return;
        try {
            Product p = new Product();
            p.setId(-1);
            p.setName(nameField.getText().trim());
            p.setCategory(categoryCombo.getValue());
            p.setType(typeField.getText().trim());
            p.setUnit(unitField.getText().trim());
            p.setPrice(Double.parseDouble(priceField.getText().trim()));
            p.setStock(Double.parseDouble(stockField.getText().trim()));
            p.setThreshold(Double.parseDouble(thresholdField.getText().trim()));
            p.setImage(currentImageBytes);
            productService.addProduct(p);

            AlertUtil.showInfo("Success", "Product added.");
            handleClear();
            refreshProductTable();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Error", "Select product to update.");
            return;
        }
        if (!validateForm())
            return;
        try {
            selected.setName(nameField.getText().trim());
            selected.setCategory(categoryCombo.getValue());
            selected.setType(typeField.getText().trim());
            selected.setUnit(unitField.getText().trim());
            selected.setPrice(Double.parseDouble(priceField.getText().trim()));
            selected.setStock(Double.parseDouble(stockField.getText().trim()));
            selected.setThreshold(Double.parseDouble(thresholdField.getText().trim()));
            selected.setImage(currentImageBytes);
            productService.updateProduct(selected);
            AlertUtil.showInfo("Success", "Product updated.");
            refreshProductTable();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }

    }

    private void handleViewProductRatings() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Error", "Please select a product to view ratings.");
            return;
        }

        try {
            double avg = productService.getAverageProductRating(selected.getId());
            java.util.List<com.group18.greengrocer.model.ProductRating> ratings = productService.getProductRatings(selected.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("Product: ").append(selected.getName()).append("\n");
            sb.append("Average Rating: ").append(String.format("%.1f", avg)).append(" / 5.0\n");
            sb.append("Total Ratings: ").append(ratings.size()).append("\n\n");

            sb.append("--- Reviews ---\n");
            if (ratings.isEmpty()) {
                sb.append("No reviews yet.");
            } else {
                for (com.group18.greengrocer.model.ProductRating r : ratings) {
                    sb.append("Rating: ").append(r.getRating()).append("/5\n");
                    // Assuming date is available
                    if (r.getCreatedAt() != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                        sb.append("Date: ").append(sdf.format(r.getCreatedAt())).append("\n");
                    }
                    sb.append("----------------\n");
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Product Ratings");
            alert.setHeaderText("Reviews for " + selected.getName());

            TextArea area = new TextArea(sb.toString());
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefWidth(400);
            area.setPrefHeight(300);

            alert.getDialogPane().setContent(area);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load ratings: " + e.getMessage());
        }
    }


    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Optional<ButtonType> res = AlertUtil.showConfirmation("Delete", "Delete " + selected.getName() + "?");
            if (res.isPresent() && res.get() == ButtonType.OK) {
                productService.removeProduct(selected.getId());
                refreshProductTable();
                handleClear();
            }
        }
    }

    private boolean validateForm() {
        if (ValidatorUtil.isEmpty(nameField.getText()))
            return false;
        if (categoryCombo.getValue() == null)
            return false;
        return true;
    }

    // ================= ORDERS =================
    private void showOrderDetails(Order order) {
        if (orderDetailsArea == null)
            return;
        if (order == null) {
            orderDetailsArea.clear();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("Items: \n");
        order.getItems().forEach(i -> sb.append("- ").append(i.getProduct().getName()).append(" x ")
                .append(i.getQuantity()).append("\n"));
        orderDetailsArea.setText(sb.toString());
    }

    @FXML
    private void handleApproveOrder() {
        Order o = orderTable.getSelectionModel().getSelectedItem();
        if (o != null) {
            orderService.approveOrder(o.getId());
            handleRefreshOrders();
            orderDetailsArea.clear();
        }
    }

    // ================= REPORTS =================
    @FXML
    private void handleRefreshReports() {
        if (categoryPieChart == null)
            return;
        
        // --- 1. Top Cards Stats ---
        if (totalRevenueLabel != null) {
            totalRevenueLabel.setText(String.format("%.2f TL", orderService.getTotalRevenue()));
        }
        if (totalOrdersLabel != null) {
            totalOrdersLabel.setText(String.valueOf(orderService.getTotalOrdersCount()));
        }
        if (activeCustomersLabel != null) {
            activeCustomersLabel.setText(String.valueOf(orderService.getActiveCustomersCount()));
        }
        if (avgOrderValueLabel != null) {
             int totalOrders = orderService.getTotalOrdersCount();
             double totalRev = orderService.getTotalRevenue();
             double avg = totalOrders > 0 ? totalRev / totalOrders : 0.0;
             avgOrderValueLabel.setText(String.format("%.2f TL", avg));
        }

        // --- 2. Category Pie Chart ---
        java.util.Map<String, Double> catData = orderService.getSalesByCategory();
        categoryPieChart.getData().clear();
        catData.forEach((cat, val) -> categoryPieChart.getData().add(new javafx.scene.chart.PieChart.Data(cat, val)));
        
        // --- 3. Order Status Pie Chart ---
        if (orderStatusChart != null) {
            java.util.Map<String, Integer> statusData = orderService.getOrderStatusDistribution();
            orderStatusChart.getData().clear();
            statusData.forEach((status, count) -> 
                orderStatusChart.getData().add(new javafx.scene.chart.PieChart.Data(status, count)));
        }

        // --- 4. Product Sales Bar Chart ---
        if (productSalesChart != null) {
            productSalesChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Revenue");
            
            java.util.Map<String, Double> prodData = orderService.getRevenueByProduct();
            // Sort top 10 for better visualization? For now show all.
            prodData.forEach((prod, rev) -> series.getData().add(new javafx.scene.chart.XYChart.Data<>(prod, rev)));
            
            productSalesChart.getData().add(series);
        }

        // --- 5. Revenue Line Chart ---
        if (revenueChart != null) {
            revenueChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Daily Revenue");
            
            java.util.Map<String, Double> timeData = orderService.getRevenueOverTime();
            timeData.forEach((date, val) -> series.getData().add(new javafx.scene.chart.XYChart.Data<>(date, val)));
            
            revenueChart.getData().add(series);
        }
    }

    @FXML
    private void handleCreateCoupon() {
        try {
            String code = couponCodeField.getText();
            String amountText = couponAmountField.getText();

            if (ValidatorUtil.isEmpty(code) || ValidatorUtil.isEmpty(amountText) || couponExpiryPicker.getValue() == null) {
                AlertUtil.showWarning("Validation Error", "All coupon fields (Code, Amount, Expiry) are required.");
                return;
            }

            double amt;
            try {
                amt = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                AlertUtil.showWarning("Validation Error", "Invalid amount. Please enter a valid number.");
                return;
            }

            com.group18.greengrocer.model.Coupon c = new com.group18.greengrocer.model.Coupon();
            c.setCode(code.trim());
            c.setDiscountAmount(amt);
            c.setExpiryDate(java.sql.Date.valueOf(couponExpiryPicker.getValue()));
            c.setActive(true);
            
            discountService.createCoupon(c);
            
            AlertUtil.showInfo("Success", "Coupon created successfully.");
            
            // Clear fields
            couponCodeField.clear();
            couponAmountField.clear();
            couponExpiryPicker.setValue(null);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to create coupon: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateLoyalty() {
        try {
            String minOrderText = loyaltyMinOrderField.getText();
            String discountRateText = loyaltyRateField.getText();

            if (ValidatorUtil.isEmpty(minOrderText) || ValidatorUtil.isEmpty(discountRateText)) {
                AlertUtil.showWarning("Validation Error", "Min Order Count and Discount Rate are required.");
                return;
            }

            int minOrder = Integer.parseInt(minOrderText);
            double rate = Double.parseDouble(discountRateText);

            discountService.updateLoyaltyRules(minOrder, rate);
            AlertUtil.showInfo("Success", "Loyalty rules updated.");
            
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Please enter valid numbers for loyalty rules.");
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to update loyalty rules: " + e.getMessage());
        }
    }

    @FXML
    private void handleHireCarrier() {
        if (ValidatorUtil.isEmpty(carrierUsernameField.getText()) || 
            ValidatorUtil.isEmpty(carrierPasswordField.getText())) {
            AlertUtil.showWarning("Validation Error", "Username and Password are required.");
            return;
        }

        try {
            User u = new User();
            u.setUsername(carrierUsernameField.getText().trim());
            u.setPassword(carrierPasswordField.getText().trim());
            u.setPhoneNumber(carrierPhoneField.getText().trim());
            u.setAddress(carrierAddressArea.getText().trim());
            
            userService.addCarrier(u);
            AlertUtil.showInfo("Success", "Carrier hired successfully.");
            
            // Clear fields
            carrierUsernameField.clear();
            carrierPasswordField.clear();
            carrierPhoneField.clear();
            carrierAddressArea.clear();
            
            refreshCarrierTable();
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to hire carrier: " + e.getMessage());
        }
    }

    @FXML
    private void handleFireCarrier() {
        User u = carrierTable.getSelectionModel().getSelectedItem();
        if (u == null) {
            AlertUtil.showWarning("Selection Error", "Please select a carrier to fire.");
            return;
        }
        
        Optional<ButtonType> res = AlertUtil.showConfirmation("Fire Carrier", "Are you sure you want to fire " + u.getUsername() + "?");
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                userService.removeCarrier(u.getId());
                AlertUtil.showInfo("Success", "Carrier fired successfully.");
                refreshCarrierTable();
            } catch (Exception e) {
                AlertUtil.showError("Error", "Failed to fire carrier: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewCarrierRatings() {
        User u = carrierTable.getSelectionModel().getSelectedItem();
        if (u == null) {
            AlertUtil.showWarning("Selection Error", "Please select a carrier to view ratings.");
            return;
        }
        
        try {
            double avgRating = userService.getCarrierRating(u.getId());
            java.util.List<com.group18.greengrocer.model.CarrierRating> ratings = userService.getCarrierRatings(u.getId());
            
            StringBuilder sb = new StringBuilder();
            sb.append("Carrier: ").append(u.getUsername()).append("\n");
            sb.append("Average Rating: ").append(String.format("%.2f", avgRating)).append(" / 5.0\n");
            sb.append("Total Ratings: ").append(ratings.size()).append("\n\n");
            
            sb.append("--- Details ---\n");
            if (ratings.isEmpty()) {
                sb.append("No ratings yet.");
            } else {
                for (com.group18.greengrocer.model.CarrierRating r : ratings) {
                    sb.append("Rating: ").append(r.getRating()).append("/5\n");
                    if (r.getComment() != null && !r.getComment().isEmpty()) {
                        sb.append("Comment: ").append(r.getComment()).append("\n");
                    } else {
                        sb.append("Comment: -\n");
                    }
                    if (r.getCreatedAt() != null) {
                         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                         sb.append("Date: ").append(sdf.format(r.getCreatedAt())).append("\n");
                    }
                    sb.append("----------------\n");
                }
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Carrier Ratings");
            alert.setHeaderText("Ratings for " + u.getUsername());
            
            TextArea area = new TextArea(sb.toString());
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefWidth(400);
            area.setPrefHeight(300);
            
            alert.getDialogPane().setContent(area);
            alert.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to retrieve ratings: " + e.getMessage());
        }
    }

    // ================= MESSAGES (Fixed for Merge + Ticket System)
    // =================

    @FXML
    private void handleRefreshMessages() {
        if (messageTable == null)
            return;
        try {
            messageTable.getItems().setAll(messageService.getMessagesForOwner());
        } catch (Exception e) {
            System.err.println("Failed to load messages: " + e.getMessage());
        }
    }

    private void showMessageDetails(Message msg) {
        if (fromLabel == null || contentArea == null)
            return;

        if (msg == null) {
            fromLabel.setText("");
            contentArea.clear();
            replyField.clear();
            return;
        }

        fromLabel.setText(msg.getSenderName());

        // Load Conversation Log
        java.util.List<Message> conversation = messageService.getConversation(msg.getConversationId());
        StringBuilder sb = new StringBuilder();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm");

        for (Message m : conversation) {
            sb.append("[").append(sdf.format(m.getSentAt())).append("] ");
            sb.append(m.getSenderName()).append(": ");
            sb.append(m.getContent()).append("\n\n");
        }

        contentArea.setText(sb.toString());
        contentArea.setScrollTop(Double.MAX_VALUE); // Scroll to bottom

        // Logic to disable reply if closed
        if (msg.getConversationStatus() != null && msg.getConversationStatus().equalsIgnoreCase("CLOSED")) {
            replyField.setDisable(true);
            replyField.setPromptText("This conversation is CLOSED.");
        } else {
            replyField.setDisable(false);
            replyField.setPromptText("Type your reply here...");
        }

        // Auto-mark as read
        if (!msg.isRead()) {
            try {
                messageService.markMessageAsRead(msg.getId());
                msg.setRead(true);
            } catch (Exception e) {
            }
        }
    }

    @FXML
    private void handleSendReply() {
        Message selected = messageTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a message to reply to.");
            return;
        }

        String replyText = replyField.getText();
        if (ValidatorUtil.isEmpty(replyText)) {
            AlertUtil.showWarning("Validation", "Reply cannot be empty.");
            return;
        }

        try {
            messageService.replyToMessage(selected.getId(), replyText);

            replyField.clear();
            replyField.setPromptText("Reply sent successfully!");

            // Refresh table details to show new message in log
            showMessageDetails(selected);

        } catch (Throwable e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to send reply: " + e.getMessage());
        }
    }

    @FXML
    private void handleEndConversation() {
        Message selected = messageTable.getSelectionModel().getSelectedItem();
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

    @FXML
    private void handleBack() {
        handleLogout();
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group18/greengrocer/fxml/goodbye.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Use setRoot to preserve the stage properties (like maximization)
            stage.getScene().setRoot(root);
            
            // Ensure full screen
            stage.setMaximized(true);
            
            stage.show();
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not go to login screen: " + e.getMessage());
        }
    }
}
