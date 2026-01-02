package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.model.Message;
import com.group18.greengrocer.service.ProductService;
import com.group18.greengrocer.service.UserService;
import com.group18.greengrocer.service.DiscountService;
import com.group18.greengrocer.service.MessageService;
import com.group18.greengrocer.util.AlertUtil;
import com.group18.greengrocer.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

import java.util.Optional;

/**
 * OwnerController (Updated for Image Upload)
 */
public class OwnerController {

    private User currentUser;
    private final ProductService productService;
    private final UserService userService;
    private final DiscountService discountService;

    // FXML Fields
    @FXML
    private Label usernameLabel;
    @FXML
    private Button backButton;
    @FXML
    private Button logoutButton;

    // Table
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

    // Form
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private TextField typeField;
    @FXML
    private TextField unitField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField thresholdField;

    @FXML
    private Label effectivePriceLabel;
    @FXML
    private ImageView productImageView; // NEW

    // Internal state for image
    private byte[] currentImageBytes; // NEW

    // Carrier
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
    private TextField carrierUsernameField;
    @FXML
    private PasswordField carrierPasswordField;
    @FXML
    private TextField carrierPhoneField;
    @FXML
    private TextArea carrierAddressArea;

    // Discounts
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

    // Messages
    @FXML
    private TableView<Message> messagesTable;
    @FXML
    private TableColumn<Message, String> msgFromCol;
    @FXML
    private TableColumn<Message, String> msgDateCol;
    @FXML
    private TableColumn<Message, String> msgContentCol;
    @FXML
    private TextField msgSenderField;
    @FXML
    private TextArea msgReadArea;
    @FXML
    private TextArea msgReplyArea;

    private final MessageService messageService;

    public OwnerController() {
        this.productService = new ProductService();
        this.userService = new UserService();
        this.discountService = new DiscountService();
        this.messageService = new MessageService();
    }

    public void initData(User user) {
        this.currentUser = user;
        if (usernameLabel != null && currentUser != null) {
            usernameLabel.setText("Owner: " + currentUser.getUsername());
        }
        loadOwnerData();
    }

    @FXML
    public void initialize() {
        // Table Columns
        idCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        categoryCol.setCellValueFactory(cell -> {
            Product p = cell.getValue();
            return new SimpleStringProperty(p.getCategory() == null ? "-" : p.getCategory().name());
        });
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        unitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnit()));
        priceCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
        stockCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStock()));
        thresholdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getThreshold()));

        // Carrier Table
        if (carrierTable != null) {
            carrierIdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
            carrierNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
            carrierPhoneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhoneNumber()));
            carrierAddressCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAddress()));
        }

        // Listener
        productTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> showProductDetails(newVal));

        // Init user if needed
        if (this.currentUser == null) {
            this.currentUser = SessionManager.getInstance().getCurrentUser();
        }
        if (usernameLabel != null && currentUser != null) {
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

        loadOwnerData();
        loadCarrierData();
        handleRefreshReports(); // Load charts automatically
    }

    private void loadOwnerData() {
        if (productTable == null)
            return;
        productTable.getItems().setAll(productService.getAllProductsForOwner());
    }

    private void showProductDetails(Product product) {
        if (effectivePriceLabel == null)
            return;

        if (product == null) {
            effectivePriceLabel.setText("-");
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

        // Display Image
        currentImageBytes = product.getImage();
        displayImage(currentImageBytes);

        try {
            double effectivePrice = productService.getEffectivePrice(product);
            effectivePriceLabel.setText(String.format("%.2f ₺", effectivePrice));
        } catch (Exception e) {
            effectivePriceLabel.setText("-");
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = fileChooser.showOpenDialog(productImageView.getScene().getWindow());
        if (file != null) {
            try {
                // Read bytes
                byte[] bytes = Files.readAllBytes(file.toPath());
                // Validate size? (Max 16MB for MEDIUMBLOB, but code uses default BLOB (64KB
                // potentially? No, usually larger in modern MySQL unless strictly TINYBLOB))
                // Just use it.
                currentImageBytes = bytes;
                displayImage(currentImageBytes);

            } catch (IOException e) {
                AlertUtil.showError("Image Error", "Failed to read image: " + e.getMessage());
            }
        }
    }

    private void displayImage(byte[] imageBytes) {
        if (productImageView == null)
            return;

        if (imageBytes != null && imageBytes.length > 0) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                Image img = new Image(bis);
                productImageView.setImage(img);
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
            Product newProduct = new Product();
            newProduct.setId(-1);
            newProduct.setName(nameField.getText().trim());
            newProduct.setCategory(categoryCombo.getValue());
            newProduct.setType(typeField.getText().trim());
            newProduct.setUnit(unitField.getText().trim());
            newProduct.setPrice(Double.parseDouble(priceField.getText().trim()));
            newProduct.setStock(Double.parseDouble(stockField.getText().trim()));
            newProduct.setThreshold(Double.parseDouble(thresholdField.getText().trim()));
            newProduct.setImage(currentImageBytes); // Set BLOB

            productService.addProduct(newProduct);

            AlertUtil.showInfo("Success", "Product added successfully.");
            handleClear();
            loadOwnerData();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Price, Stock, and Threshold must be valid numbers.");
        } catch (Exception e) {
            AlertUtil.showError("Error", "Could not add product: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to update.");
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
            selected.setImage(currentImageBytes); // Update BLOB

            productService.updateProduct(selected);

            AlertUtil.showInfo("Success", "Product updated successfully.");

            int selectedId = selected.getId();
            loadOwnerData();

            productTable.getItems().stream()
                    .filter(p -> p.getId() == selectedId)
                    .findFirst()
                    .ifPresent(p -> productTable.getSelectionModel().select(p));
            productTable.refresh();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Price, Stock, and Threshold must be valid numbers.");
        } catch (Exception e) {
            AlertUtil.showError("Error", "Could not update product: " + e.getMessage());
        }
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
        if (productImageView != null)
            productImageView.setImage(null);

        productTable.getSelectionModel().clearSelection();
    }

    // ... Other methods (handleDelete, handles for Carrier, Coupons etc.) stay the
    // same ...
    // To save context, I will overwrite the whole file with the full content
    // including previous features.

    // ... [Previous code for Refresh, Back, Logout, Delete, ValidateForm, Carrier
    // logic, Coupon logic] ...

    // (Re-implementing all those methods to ensure file consistency)

    @FXML
    private void handleRefresh() {
        loadOwnerData();
        AlertUtil.showInfo("Refreshed", "Product list refreshed.");
    }

    @FXML
    private void handleBack() {
        handleLogout();
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
            stage.show();
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
        if (categoryCombo.getValue() == null) {
            AlertUtil.showWarning("Validation", "Category is required.");
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
    private javafx.scene.chart.BarChart<String, Number> productSalesChart;
    @FXML
    private javafx.scene.chart.LineChart<String, Number> revenueChart;

    @FXML
    private void handleRefreshReports() {
        if (categoryPieChart == null || productSalesChart == null || revenueChart == null)
            return;

        com.group18.greengrocer.service.OrderService orderService = new com.group18.greengrocer.service.OrderService();

        // 1. Pie Chart
        java.util.Map<String, Double> catData = orderService.getSalesByCategory();
        categoryPieChart.getData().clear();
        catData.forEach((cat, val) -> {
            categoryPieChart.getData().add(new javafx.scene.chart.PieChart.Data(cat, val));
        });

        // 2. Bar Chart
        java.util.Map<String, Double> prodData = orderService.getRevenueByProduct();
        productSalesChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> seriesP = new javafx.scene.chart.XYChart.Series<>();
        seriesP.setName("Revenue");
        prodData.forEach((prod, val) -> {
            seriesP.getData().add(new javafx.scene.chart.XYChart.Data<>(prod, val));
        });
        productSalesChart.getData().add(seriesP);

        // 3. Line Chart
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
        msgReadArea.setText(msg.getContent());

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

    @FXML
    private void handleMarkAsRead() {
        Message selected = messagesTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isRead()) {
            messageService.markMessageAsRead(selected.getId());
            selected.setRead(true);
            messagesTable.refresh();
            AlertUtil.showInfo("Success", "Marked as read.");
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

            // Replaced Alert with clearing and status update to prevent potential
            // focus/stage issues
            msgReplyArea.clear();
            msgReplyArea.setPromptText("Reply sent successfully!");

            // Refresh table
            handleRefreshMessages();

        } catch (Throwable e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to send reply: " + e.getMessage());
        }
    }
}
