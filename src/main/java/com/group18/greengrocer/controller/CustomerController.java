package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.model.Message;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.ProductService;
import com.group18.greengrocer.service.MessageService;
import com.group18.greengrocer.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Separator;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class CustomerController {

    // =====================
    // SERVICES
    // =====================
    private ProductService productService;
    private OrderService orderService;
    private MessageService messageService;

    // Logged-in user
    private User currentUser;

    // =====================
    // FXML COMPONENTS
    // =====================
    @FXML
    private Label usernameLabel;

    @FXML
    private Button cartButton;

    @FXML
    private TilePane fruitPane;

    @FXML
    private TilePane vegetablePane;

    @FXML
    private TextField searchField;

    // Order Tracking
    @FXML
    private VBox orderTrackingBox;
    @FXML
    private Label statusReceived;
    @FXML
    private Label statusPreparing;
    @FXML
    private Label statusOnWay;
    @FXML
    private Label statusDelivered;
    @FXML
    private Label trackingOrderIdLabel;
    @FXML
    private Button closeTrackingButton;

    // =====================
    // INITIALIZE
    // =====================
    @FXML
    public void initialize() {
        productService = new ProductService();
        orderService = new OrderService();
        messageService = new MessageService();

        // Default cart label
        cartButton.setText("Cart (0)");
    }

    @FXML
    private void handleSearch() {
        loadProducts();
    }

    // =====================
    // USER FROM LOGIN
    // =====================
    public void initData(User user) {
        this.currentUser = user;

        if (currentUser != null) {
            usernameLabel.setText("Customer: " + currentUser.getUsername());
            loadProducts();
            refreshOrderTracking();
        }
    }

    private void refreshOrderTracking() {
        if (orderTrackingBox == null)
            return;

        List<Order> orders = orderService.getOrdersByCustomer(currentUser.getId());

        // Find latest active order
        Order activeOrder = orders.stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .findFirst() // Sorted by date desc in DAO
                .orElse(null);

        if (activeOrder == null) {
            orderTrackingBox.setVisible(false);
            orderTrackingBox.setManaged(false);
            return;
        }

        orderTrackingBox.setVisible(true);
        orderTrackingBox.setManaged(true);
        trackingOrderIdLabel.setText("Order #" + activeOrder.getId() + " - " + activeOrder.getOrderTime());

        // Reset Styles
        String defaultStyle = "-fx-padding: 5 10; -fx-background-radius: 15; -fx-background-color: #E0E0E0;";
        String activeStyle = "-fx-padding: 5 10; -fx-background-radius: 15; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;";
        String doneStyle = "-fx-padding: 5 10; -fx-background-radius: 15; -fx-background-color: #81C784; -fx-text-fill: white;";

        statusReceived.setStyle(defaultStyle);
        statusPreparing.setStyle(defaultStyle);
        statusOnWay.setStyle(defaultStyle);
        statusDelivered.setStyle(defaultStyle);

        Order.Status s = activeOrder.getStatus();

        if (closeTrackingButton != null) {
            closeTrackingButton.setVisible(s == Order.Status.DELIVERED);
        }

        switch (s) {
            case RECEIVED:
                statusReceived.setStyle(activeStyle);
                break;
            case PREPARING:
                statusReceived.setStyle(doneStyle);
                statusPreparing.setStyle(activeStyle);
                break;
            case ON_THE_WAY:
                statusReceived.setStyle(doneStyle);
                statusPreparing.setStyle(doneStyle);
                statusOnWay.setStyle(activeStyle);
                break;
            case DELIVERED:
                statusReceived.setStyle(doneStyle);
                statusPreparing.setStyle(doneStyle);
                statusOnWay.setStyle(doneStyle);
                statusDelivered.setStyle(activeStyle);
                break;
            default:
                break;
        }
    }

    @FXML
    private void handleCloseTracking() {
        if (orderTrackingBox != null) {
            orderTrackingBox.setVisible(false);
            orderTrackingBox.setManaged(false);
        }
    }

    // =====================
    // PRODUCT LIST
    // =====================
    private void loadProducts() {
        fruitPane.getChildren().clear();
        vegetablePane.getChildren().clear();

        List<Product> products = productService.getAllProducts();

        // SORT BY PRODUCT NAME
        products.sort(Comparator.comparing(Product::getName));

        // SEARCH KEYWORD
        String keyword = "";

        if (searchField != null && searchField.getText() != null) {
            keyword = searchField.getText().toLowerCase();
        }

        for (Product product : products) {

            // FILTER BY NAME
            if (!product.getName().toLowerCase().contains(keyword)) {
                continue;
            }

            VBox card = createProductCard(product);

            if (product.getCategory() == Category.FRUIT) {
                fruitPane.getChildren().add(card);
            } else if (product.getCategory() == Category.VEGETABLE) {
                vegetablePane.getChildren().add(card);
            }
        }
    }

    private VBox createProductCard(Product product) {

        VBox box = new VBox(6);

        // =====================
        // SAFE IMAGE LOAD
        // =====================

        // =====================
        // SAFE IMAGE LOAD (BLOB Priority)
        // =====================
        Image image = null;

        // 1. Try DB BLOB
        if (product.getImage() != null && product.getImage().length > 0) {
            try {
                image = new Image(new java.io.ByteArrayInputStream(product.getImage()));
            } catch (Exception e) {
                // Ignore corrupt BLOB, fallback
            }
        }

        // 2. Fallback to Classpath Resource
        if (image == null) {
            try {
                String imageName = product.getName().toLowerCase() + ".png";
                var stream = getClass().getResourceAsStream(
                        "/com/group18/greengrocer/images/products/" + imageName);

                if (stream != null) {
                    image = new Image(stream);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        // 3. Display if found
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-border-color: red;");
            box.getChildren().add(imageView);
        }

        Label nameLabel = new Label(product.getName());
        Label priceLabel = new Label(
                "Price: " + product.getPrice() + " ₺ / " + product.getUnit());
        Label stockLabel = new Label("Stock: " + product.getStock());

        // AMOUNT INPUT
        TextField amountField = new TextField();
        amountField.setPromptText("kg");
        amountField.setMaxWidth(80);

        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> {
            try {
                String input = amountField.getText();

                if (input == null || input.isBlank()) {
                    showError("Please enter amount in kg.");
                    return;
                }

                double amount = Double.parseDouble(input);

                if (amount <= 0) {
                    showError("Amount must be greater than 0.");
                    return;
                }

                // =====================
                // STOCK CHECK (IMPORTANT)
                // =====================
                if (amount > product.getStock()) {
                    showError(
                            "Not enough stock.\nAvailable stock: " + String.format("%.2f", product.getStock()) + " kg");
                    return;
                }

                orderService.addToCart(
                        currentUser.getId(),
                        product.getId(),
                        amount);

                Order cart = orderService.getCart(currentUser.getId());
                cartButton.setText("Cart (" + cart.getItems().size() + ")");

                showInfo(amount + " kg of " + product.getName() + " added to cart.");
                amountField.clear();

            } catch (NumberFormatException ex) {
                showError("Please enter a valid number.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        box.getChildren().addAll(
                nameLabel,
                priceLabel,
                stockLabel,
                amountField,
                addButton);
        box.setStyle("""
                -fx-padding: 10;
                -fx-border-color: lightgray;
                -fx-border-radius: 5;
                -fx-background-radius: 5;
                -fx-alignment: center;
                """);

        return box;
    }

    // =====================
    // CART
    // =====================
    @FXML
    private void handleViewCart() {
        Order cart = orderService.getCart(currentUser.getId());

        if (cart.getItems().isEmpty()) {
            showInfo("Your cart is empty.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/group18/greengrocer/fxml/cart.fxml"));
            Parent root = loader.load();

            CartController controller = loader.getController();
            controller.initData(currentUser, cart);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Your Cart");
            stage.showAndWait();

            // 1. Refresh cart count (in case items removed)
            Order updatedCart = orderService.getCart(currentUser.getId());
            cartButton.setText("Cart (" + updatedCart.getItems().size() + ")");

            // 2. Refresh product list (to show updated stock immediately)
            loadProducts();

        } catch (IOException e) {
            showError("Could not open cart: " + e.getMessage());
        }
    }

    // =====================
    // CHECKOUT (OPTIONAL SHORTCUT)
    // =====================
    @FXML
    private void handleCheckout() {
        try {
            Order cart = orderService.getCart(currentUser.getId());
            orderService.checkout(cart);

            cartButton.setText("Cart (0)");
            showInfo("Order completed successfully.");
            refreshOrderTracking();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =====================
    // ORDER HISTORY
    // =====================
    @FXML

    private void handleMyOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/group18/greengrocer/fxml/order_history.fxml"));

            Parent root = loader.load();

            OrderHistoryController controller = loader.getController();
            controller.initData(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Order History");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Re-load products to reflect stock changes if an order was cancelled
            loadProducts();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load order history: " + e.getMessage() + "\n" + e.toString());
        }
    }

    // =====================
    // RATE DELIVERY (CARRIER & PRODUCTS)
    // =====================
    @FXML
    private void handleRateCarrier() {
        List<Order> orders = orderService.getOrdersByCustomer(currentUser.getId());
        List<Order> deliveredOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED)
                .toList();

        if (deliveredOrders.isEmpty()) {
            AlertUtil.showInfo("Info", "You have no delivered orders to rate.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Rate Delivery");

        showOrderSelection(stage, deliveredOrders);
    }

    // Step 1: Select Order
    private void showOrderSelection(Stage stage, List<Order> orders) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setPrefSize(350, 300);
        root.setStyle("-fx-background-color: white;");

        Label header = new Label("Select Delivery");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Order> orderBox = new ComboBox<>(FXCollections.observableArrayList(orders));
        orderBox.setMaxWidth(Double.MAX_VALUE);
        orderBox.setPromptText("Choose an order...");

        Button nextBtn = new Button("Next");
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold;");

        nextBtn.setOnAction(e -> {
            Order selected = orderBox.getValue();
            if (selected == null) {
                AlertUtil.showWarning("Validation", "Please select an order.");
            } else {
                showRatingOptions(stage, selected, orders);
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(header, new Label("Which order do you want to rate?"), orderBox, new Separator(),
                nextBtn, backBtn);
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Step 2: Choose Rating Type
    private void showRatingOptions(Stage stage, Order order, List<Order> allOrders) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setPrefSize(350, 350);
        root.setStyle("-fx-background-color: white;");

        Label header = new Label("Order #" + order.getId());
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button rateCarrierBtn = new Button("Rate Carrier");
        rateCarrierBtn.setMaxWidth(Double.MAX_VALUE);
        rateCarrierBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        rateCarrierBtn.setOnAction(e -> showCarrierRatingForm(stage, order, allOrders));

        Button rateProductsBtn = new Button("Rate Products");
        rateProductsBtn.setMaxWidth(Double.MAX_VALUE);
        rateProductsBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        rateProductsBtn.setOnAction(e -> showProductRatingSelection(stage, order, allOrders));

        Button backBtn = new Button("Back");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> showOrderSelection(stage, allOrders)); // Go back to Step 1

        root.getChildren().addAll(header, new Label("What would you like to rate?"), rateCarrierBtn, rateProductsBtn,
                new Separator(), backBtn);
        stage.setScene(new Scene(root));
    }

    // Step 3a: Rate Carrier
    private void showCarrierRatingForm(Stage stage, Order order, List<Order> allOrders) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefSize(400, 450);
        root.setStyle("-fx-background-color: white;");

        Label header = new Label("Rate Carrier");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label ratingLabel = new Label("Rating:");
        HBox ratingBox = new HBox(10);
        ToggleGroup group = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            RadioButton rb = new RadioButton(String.valueOf(i));
            rb.setUserData(i);
            rb.setToggleGroup(group);
            ratingBox.getChildren().add(rb);
        }

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your experience...");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        Button submitBtn = new Button("Submit");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        submitBtn.setOnAction(e -> {
            if (group.getSelectedToggle() == null) {
                AlertUtil.showWarning("Validation", "Please select a rating score.");
                return;
            }
            int rating = (int) group.getSelectedToggle().getUserData();
            try {
                orderService.rateOrder(order.getId(), rating, commentArea.getText());
                stage.close();
                AlertUtil.showInfo("Success", "Carrier rated successfully!");
            } catch (Exception ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> showRatingOptions(stage, order, allOrders));

        root.getChildren().addAll(header, ratingLabel, ratingBox, new Label("Comment:"), commentArea, new Separator(),
                submitBtn, backBtn);
        stage.setScene(new Scene(root));
    }

    // Step 3b: Select Product to Rate
    private void showProductRatingSelection(Stage stage, Order order, List<Order> allOrders) {
        if (order.getItems().isEmpty()) {
            AlertUtil.showWarning("Info", "No items in this order.");
            return;
        }

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefSize(400, 400);

        Label header = new Label("Rate Products");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<com.group18.greengrocer.model.CartItem> itemBox = new ComboBox<>(
                FXCollections.observableArrayList(order.getItems()));
        itemBox.setMaxWidth(Double.MAX_VALUE);
        itemBox.setPromptText("Select a product...");

        HBox ratingBox = new HBox(10);
        ToggleGroup group = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            RadioButton rb = new RadioButton(String.valueOf(i));
            rb.setUserData(i);
            rb.setToggleGroup(group);
            ratingBox.getChildren().add(rb);
        }

        Button submitBtn = new Button("Rate");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        submitBtn.setOnAction(e -> {
            com.group18.greengrocer.model.CartItem item = itemBox.getValue();
            if (item == null) {
                AlertUtil.showWarning("Validation", "Select a product.");
                return;
            }
            if (group.getSelectedToggle() == null) {
                AlertUtil.showWarning("Validation", "Select a rating.");
                return;
            }
            int rating = (int) group.getSelectedToggle().getUserData();
            try {
                orderService.rateProduct(currentUser.getId(), item.getProduct().getId(), rating);
                AlertUtil.showInfo("Success", "Product rated!");
                // Optionally reset form
                group.selectToggle(null);
            } catch (Exception ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> showRatingOptions(stage, order, allOrders));

        root.getChildren().addAll(header, new Label("Product:"), itemBox, new Label("Score:"), ratingBox,
                new Separator(), submitBtn, backBtn);
        stage.setScene(new Scene(root));
    }

    // =====================
    // PROFILE UPDATE
    // =====================

    @FXML
    private void handleEditProfile() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField addressField = new TextField(currentUser.getAddress());
        TextField phoneField = new TextField();

        String cleanPhone = currentUser.getPhoneNumber().replaceAll("\\D", "");
        phoneField.setText(cleanPhone);

        phoneField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*") && newText.length() <= 11) {
                return change;
            }
            return null;
        }));

        grid.add(new Label("Address:"), 0, 0);
        grid.add(addressField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // SAVE BUTTON VALIDATION
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);

        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String phone = phoneField.getText();

            if (phone.length() != 10 && phone.length() != 11) {
                showError("Phone number must be 10 or 11 digits.");
                event.consume();
            }
        });

        // UPDATE + INFO
        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                currentUser.setAddress(addressField.getText());
                currentUser.setPhoneNumber(phoneField.getText());
                showInfo("Profile updated successfully.");
            }
            return null;
        });

        dialog.showAndWait();
    }

    // =====================
    // LOGOUT
    // =====================
    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Group18 GreenGrocer - Login");
        } catch (IOException e) {
            showError("Could not go back to login: " + e.getMessage());
        }
    }

    // =====================
    // MESSAGE OWNER
    // =====================
    @FXML
    private void handleMessageOwner() {
        Stage stage = new Stage();
        stage.setTitle("Support Chat - " + currentUser.getUsername());

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setPrefSize(400, 500);

        // Chat List
        ListView<Message> chatList = new ListView<>();
        VBox.setVgrow(chatList, Priority.ALWAYS);

        // Custom Cell Factory for Chat Bubbles
        chatList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                    setStyle("");
                } else {
                    boolean isMe = (msg.getSenderId() == currentUser.getId());
                    String sender = isMe ? "You" : "Owner";
                    String time = "";
                    if (msg.getSentAt() != null) {
                        time = new java.text.SimpleDateFormat("dd/MM HH:mm").format(msg.getSentAt());
                    }

                    setText(sender + " (" + time + "):\n" + msg.getContent());

                    if (isMe) {
                        // User message: Aligned right (text), Blueish background
                        setStyle(
                                "-fx-control-inner-background: #E3F2FD; -fx-alignment: center-right; -fx-text-alignment: right;");
                    } else {
                        // Owner message: Aligned left, White/Gray background
                        setStyle("-fx-control-inner-background: #FFFFFF; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Input Area
        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Type a message...");
        inputArea.setPrefRowCount(3);
        inputArea.setWrapText(true);
        inputArea.setMaxHeight(80);

        // Buttons
        HBox bottom = new HBox(10);
        Button refreshBtn = new Button("Refresh");
        Button sendBtn = new Button("Send");
        sendBtn.setDefaultButton(true); // Enter triggers send if focused (careful with TextArea)

        bottom.getChildren().addAll(refreshBtn, inputArea, sendBtn);
        HBox.setHgrow(inputArea, Priority.ALWAYS);

        // Load Messages Logic
        Runnable loadMessages = () -> {
            try {
                List<Message> history = messageService.getMessagesForCustomer(currentUser.getId());
                chatList.getItems().setAll(history);
                if (!history.isEmpty()) {
                    chatList.scrollTo(history.size() - 1);
                }
            } catch (Exception e) {
                System.err.println("Load failed: " + e.getMessage());
            }
        };

        // Actions
        refreshBtn.setOnAction(e -> loadMessages.run());

        sendBtn.setOnAction(e -> {
            String txt = inputArea.getText().trim();
            if (txt.isEmpty())
                return;

            try {
                Message m = new Message();
                m.setSenderId(currentUser.getId());
                m.setContent(txt);
                messageService.sendMessage(m);
                inputArea.clear();
                loadMessages.run(); // Refresh to see sent message
            } catch (Exception ex) {
                AlertUtil.showError("Error", "Could not send: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(new Label("Chat History:"), chatList, bottom);
        stage.setScene(new Scene(root));
        stage.show();

        // Initial Load
        loadMessages.run();
    }

    // =====================
    // UTIL
    // =====================
    private void showInfo(String message) {
        AlertUtil.showInfo("Info", message);
    }

    private void showError(String message) {
        AlertUtil.showError("Error", message);
    }
}
