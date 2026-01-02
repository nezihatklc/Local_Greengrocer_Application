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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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

        setupTableColumns();
        
        // Load initial data for all tabs
        refreshProductTable();
        refreshCarrierTable();
        refreshOrderTable();
        loadMessages(); // Load messages on startup

        // Listener: Update detail view when a message row is selected
        if (messageTable != null) {
            messageTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    fromLabel.setText(newVal.getSenderUsername());
                    contentArea.setText(newVal.getContent());
                }
            });
        }
    }

    /**
     * Configures the columns for all TableViews in the dashboard.
     */
    private void setupTableColumns() {
        // 1. Product Columns
        idCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        categoryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory().toString()));
        priceCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
        stockCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStock()));

        // 2. Message Columns
        if (fromCol != null) {
            fromCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSenderUsername()));
            dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSentAt().toString()));
            // Show only first 20 chars for preview
            previewCol.setCellValueFactory(cell -> {
                String content = cell.getValue().getContent();
                return new SimpleStringProperty(content.length() > 20 ? content.substring(0, 20) + "..." : content);
            });
        }
    }

    // =============================================================
    // SECTION: MESSAGE HANDLING LOGIC (Crash-Proof)
    // =============================================================

    /**
     * Handles the "Mark as Read" action.
     * Includes validation to prevent system crash if no message is selected.
     */
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}