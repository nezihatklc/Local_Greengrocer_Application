package com.group18.greengrocer.controller;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Message;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.MessageService;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.ProductService;
import com.group18.greengrocer.service.UserService;
import com.group18.greengrocer.util.AlertUtil;
import com.group18.greengrocer.util.ValidatorUtil;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomerController {

    // =====================
    // SERVICES
    // =====================
    private ProductService productService;
    private OrderService orderService;
    private MessageService messageService;
    private UserService userService;

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
        userService = new UserService();

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
                // Ignore CANCELLED and COMPLETED (which acts as archived/dismissed)
                .filter(o -> o.getStatus() != Order.Status.CANCELLED && o.getStatus() != Order.Status.COMPLETED)
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
            case WAITING:
                statusReceived.setText("📝 Order Received");
                statusReceived.setStyle(activeStyle);
                statusPreparing.setText("📦 Order Preparing");
                break;
            case RECEIVED:
                statusReceived.setText("📝 Order Received");
                statusReceived.setStyle(doneStyle);
                statusPreparing.setText("📦 Order Preparing");
                statusPreparing.setStyle(activeStyle);
                break;
            case ON_THE_WAY:
                statusReceived.setText("📝 Order Received");
                statusReceived.setStyle(doneStyle);
                statusPreparing.setText("📦 Order Preparing");
                statusPreparing.setStyle(doneStyle);
                statusOnWay.setText("🛵 On the Way");
                statusOnWay.setStyle(activeStyle);
                break;
            case DELIVERED:
                statusReceived.setText("📝 Order Received");
                statusReceived.setStyle(doneStyle);
                statusPreparing.setText("📦 Order Preparing");
                statusPreparing.setStyle(doneStyle);
                statusOnWay.setText("🛵 On the Way");
                statusOnWay.setStyle(doneStyle);
                statusDelivered.setText("🏠 Delivered");
                statusDelivered.setStyle(activeStyle);
                break;
            default:
                break;
        }
    }

    @FXML
    private void handleCloseTracking() {
        if (orderTrackingBox != null && orderTrackingBox.isVisible()) {
            // Find the active order ID from the label or re-query (safer)
            List<Order> orders = orderService.getOrdersByCustomer(currentUser.getId());
            Order active = orders.stream()
                    .filter(o -> o.getStatus() != Order.Status.CANCELLED && o.getStatus() != Order.Status.COMPLETED)
                    .findFirst().orElse(null);

            if (active != null && active.getStatus() == Order.Status.DELIVERED) {
                orderService.dismissTracking(active.getId());
            }

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

        // SORT BY PRODUCT NAME (Case-Insensitive)
        products.sort(java.util.Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));

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

        // 2. Fallback to Classpath Resource (Specific name)
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

        // 3. Fallback to Category Defaults (Requested)
        if (image == null) {
            try {
                String defaultPath = "/com/group18/greengrocer/images/products/";
                if (product.getCategory() == Category.FRUIT) {
                    defaultPath += "furits.png"; // Per user request spelling
                } else {
                    defaultPath += "vegetables.png";
                }

                var stream = getClass().getResourceAsStream(defaultPath);
                if (stream != null) {
                    image = new Image(stream);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        // 4. Display if found
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-border-color: red;");
            box.getChildren().add(imageView);
        }

        // Calculate effective price (Double if stock <= threshold)
        double effectivePrice = (product.getStock() <= product.getThreshold())
                ? product.getPrice() * 2.0
                : product.getPrice();

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // RATING DISPLAY
        try {
            double avgRating = productService.getAverageProductRating(product.getId());
            Label ratingLabel = new Label();
            StringBuilder stars = new StringBuilder();
            int fullStars = (int) Math.round(avgRating);
            for (int i = 0; i < 5; i++) {
                if (i < fullStars)
                    stars.append("★");
                else
                    stars.append("☆");
            }
            if (avgRating > 0) {
                ratingLabel.setText(stars.toString() + " (" + String.format("%.1f", avgRating) + ")");
                ratingLabel.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 14px;"); // Gold color
            } else {
                ratingLabel.setText("No ratings");
                ratingLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 11px; -fx-font-style: italic;");
            }
            box.getChildren().add(ratingLabel);
        } catch (Exception e) {
            // Ignore rating load error
        }
        Label priceLabel = new Label(
                "Price: " + String.format("%.2f", effectivePrice) + " ₺ / " + product.getUnit());
        Label stockLabel = new Label("Stock: " + product.getStock());

        // AMOUNT INPUT
        TextField amountField = new TextField();
        amountField.setPromptText(product.getUnit());
        amountField.setMaxWidth(80);

        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> {
            try {
                String input = amountField.getText();

                if (input == null || input.isBlank()) {
                    showError("Please enter amount in " + product.getUnit() + ".");
                    return;
                }

                double amount = Double.parseDouble(input);

                if ("piece".equalsIgnoreCase(product.getUnit())) {
                    if (amount % 1 != 0) {
                        showError("Products measured in 'piece' must be ordered in whole numbers (no decimals).");
                        return;
                    }
                }

                if (amount <= 0) {
                    showError("Amount must be greater than 0.");
                    return;
                }

                // =====================
                // STOCK CHECK (IMPORTANT)
                // =====================
                if (amount > product.getStock()) {
                    showError(
                            "Not enough stock.\nAvailable stock: " + String.format("%.2f", product.getStock()) + " "
                                    + product.getUnit());
                    return;
                }

                orderService.addToCart(
                        currentUser.getId(),
                        product.getId(),
                        amount);

                Order cart = orderService.getCart(currentUser.getId());
                cartButton.setText("Cart (" + cart.getItems().size() + ")");

                showInfo(amount + " " + product.getUnit() + " of " + product.getName() + " added to cart.");
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
        // Determine base color based on category
        String baseBorderColor = (product.getCategory() == com.group18.greengrocer.model.Category.FRUIT)
                ? "#FF9800" // Orange for Fruit
                : "#4CAF50"; // Green for Veg/Default

        box.setStyle("""
                -fx-padding: 10;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-border-radius: 5;
                -fx-background-radius: 5;
                -fx-alignment: center;
                -fx-background-color: white;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);
                """.formatted(baseBorderColor));

        // Hover Effect
        box.setOnMouseEntered(e -> {
            box.setStyle("""
                    -fx-padding: 10;
                    -fx-border-color: %s;
                    -fx-border-width: 3;
                    -fx-border-radius: 5;
                    -fx-background-radius: 5;
                    -fx-alignment: center;
                    -fx-background-color: white;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);
                    -fx-scale-x: 1.05;
                    -fx-scale-y: 1.05;
                    """.formatted(baseBorderColor));
            box.setCursor(javafx.scene.Cursor.HAND);
        });

        box.setOnMouseExited(e -> {
            box.setStyle("""
                    -fx-padding: 10;
                    -fx-border-color: %s;
                    -fx-border-width: 2;
                    -fx-border-radius: 5;
                    -fx-background-radius: 5;
                    -fx-alignment: center;
                    -fx-background-color: white;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);
                    -fx-scale-x: 1.0;
                    -fx-scale-y: 1.0;
                    """.formatted(baseBorderColor));
            box.setCursor(javafx.scene.Cursor.DEFAULT);
        });

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
            stage.setMaximized(true);
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
            stage.setMaximized(true);
            stage.showAndWait();

            // Re-load products to reflect stock changes
            loadProducts();
            // Refresh tracking in case an order was cancelled
            refreshOrderTracking();

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

        // Filter: Delivered/Completed AND Not fully rated
        // "Fully Rated" defined as: Carrier is rated AND Products are rated.
        List<Order> eligibleOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED || o.getStatus() == Order.Status.COMPLETED)
                .filter(o -> {
                    boolean carrierDone = orderService.hasCarrierRating(o.getId());
                    boolean productsDone = orderService.hasProductRating(o.getId());
                    // Keep if NOT (both done) -> i.e. if at least one is missing
                    return !(carrierDone && productsDone);
                })
                .sorted(Comparator.comparing(Order::getOrderTime).reversed()) // Newest first
                .toList();

        if (eligibleOrders.isEmpty()) {
            AlertUtil.showInfo("Info", "You have no orders to rate.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Rate Delivery");

        showOrderSelection(stage, eligibleOrders);
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

        // Format: "Order #5 - 2024-05-10 14:30"
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        javafx.util.Callback<ListView<Order>, ListCell<Order>> cellFactory = param -> new ListCell<>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String dateStr = (item.getOrderTime() != null) ? sdf.format(item.getOrderTime()) : "N/A";
                    setText("Order #" + item.getId() + " - " + dateStr);
                }
            }
        };
        orderBox.setCellFactory(cellFactory);
        orderBox.setButtonCell(cellFactory.call(null));

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
        stage.setMaximized(true);
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

        // CHECK RATING STATUS
        boolean carrierRated = orderService.hasCarrierRating(order.getId());
        boolean productsRated = orderService.hasProductRating(order.getId());

        Button rateCarrierBtn = new Button(carrierRated ? "Rate Carrier (Done)" : "Rate Carrier");
        rateCarrierBtn.setMaxWidth(Double.MAX_VALUE);
        rateCarrierBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        rateCarrierBtn.setOnAction(e -> {
            if (carrierRated) {
                AlertUtil.showWarning("Warning", "You have already rated the carrier for this order.");
            } else {
                showCarrierRatingForm(stage, order, allOrders);
            }
        });
        // rateCarrierBtn.setDisable(carrierRated); // Disabled to allow clicking for
        // warning

        Button rateProductsBtn = new Button(productsRated ? "Rate Products (Done)" : "Rate Products");
        rateProductsBtn.setMaxWidth(Double.MAX_VALUE);
        rateProductsBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        rateProductsBtn.setOnAction(e -> {
            if (productsRated) {
                AlertUtil.showWarning("Warning", "You have already rated the products for this order.");
            } else {
                showProductRatingSelection(stage, order, allOrders);
            }
        });
        // rateProductsBtn.setDisable(productsRated); // Disabled to allow clicking for
        // warning

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
                AlertUtil.showInfo("Success", "Carrier rated successfully!");
                // Refresh logic: Go back to selection, order list might shrink if fully rated
                stage.close();
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

        // Format: Product Name only
        javafx.util.Callback<ListView<com.group18.greengrocer.model.CartItem>, ListCell<com.group18.greengrocer.model.CartItem>> prodCellFactory = param -> new ListCell<>() {
            @Override
            protected void updateItem(com.group18.greengrocer.model.CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getProduct() == null) {
                    setText(null);
                } else {
                    setText(item.getProduct().getName());
                }
            }
        };
        itemBox.setCellFactory(prodCellFactory);
        itemBox.setButtonCell(prodCellFactory.call(null));

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
                orderService.rateProduct(order.getId(), currentUser.getId(), item.getProduct().getId(), rating);
                AlertUtil.showInfo("Success", "Product rated!");

                // REFRESH UI to show updated ratings immediately
                loadProducts();
                // Refresh selection list or options?
                // The user requirement says "product ... bi kere".
                // Since our logic considers "Products Rated" as a whole block check, we treat
                // it as done.
                // Go back to main menu or step 2

                // If the user wants to rate ANOTHER product in the same order, they can't if we
                // lock it immediately.
                // However, the rule "only once" often implies the whole action.
                // Given the current checking structure (hasProductRating check on order), it
                // locks the whole order.
                // We will stick to that to satisfy "evaluate once".

                stage.close();
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

        // PASSWORD FIELDS
        javafx.scene.control.PasswordField currentPassField = new javafx.scene.control.PasswordField();
        currentPassField.setPromptText("Current Password");

        javafx.scene.control.PasswordField newPassField = new javafx.scene.control.PasswordField();
        newPassField.setPromptText("New Password");

        grid.add(new Separator(), 0, 2, 2, 1);
        grid.add(new Label("Change Password (Optional)"), 0, 3, 2, 1);

        grid.add(new Label("Current:"), 0, 4);
        grid.add(currentPassField, 1, 4);

        grid.add(new Label("New:"), 0, 5);
        grid.add(newPassField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // SAVE BUTTON VALIDATION
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);

        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String phone = phoneField.getText();

            if (!ValidatorUtil.isValidPhoneNumber(phone)) {
                showError("Invalid phone number format (10-13 digits).");
                event.consume();
                return;
            }

            // PASSWORD VALIDATION
            String curPass = currentPassField.getText();
            String newPass = newPassField.getText();

            if (!newPass.isEmpty()) {
                if (curPass.isEmpty()) {
                    showError("Please enter your current password to set a new one.");
                    event.consume();
                    return;
                }
                if (!curPass.equals(currentUser.getPassword())) {
                    showError("Current password is incorrect.");
                    event.consume();
                    return;
                }

                // NEW: Strong Password Check using ValidatorUtil
                if (!com.group18.greengrocer.util.ValidatorUtil.isStrongPassword(newPass)) {
                    showError("Password must be at least 8 chars, include upper/lower case and a digit.");
                    event.consume();
                    return;
                }
            }
        });

        // UPDATE + PERSIST
        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                try {
                    currentUser.setAddress(addressField.getText());
                    currentUser.setPhoneNumber(phoneField.getText());

                    String newPass = newPassField.getText();
                    if (!newPass.isEmpty()) {
                        currentUser.setPassword(newPass);
                    }

                    // Persist to Database
                    userService.updateUser(currentUser);

                    showInfo("Profile updated successfully.");
                } catch (Exception e) {
                    showError("Could not update profile: " + e.getMessage());
                }
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
                    getClass().getResource("/com/group18/greengrocer/fxml/goodbye.fxml"));
            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Group18 GreenGrocer - Login");
            stage.setMaximized(true);
            stage.show();
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
        stage.setTitle("Support Ticket - " + currentUser.getUsername());

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setPrefSize(500, 600);

        Label headerLabel = new Label("Support Conversation Listing");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Chat List (Ticket Log Style)
        ListView<Message> chatList = new ListView<>();
        VBox.setVgrow(chatList, Priority.ALWAYS);

        // Custom Cell Factory for Ticket Log
        chatList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: white; -fx-padding: 5;");
                } else {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String dateStr = (msg.getSentAt() != null) ? sdf.format(msg.getSentAt()) : "";

                    String senderName = (msg.getSenderId() == currentUser.getId()) ? "You" : "Support";

                    // Ticket Layout: [Date] Sender: Content
                    setText("[" + dateStr + "] " + senderName + ":\n" + msg.getContent());

                    // Simple styling, no bubbles
                    setStyle(
                            "-fx-font-family: 'Monospaced'; -fx-padding: 8; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
                    setWrapText(true);
                    setPrefWidth(0);
                }
            }
        });

        // INPUT AREA
        TextArea messageInput = new TextArea();
        messageInput.setPromptText("Type your message here to start/continue support ticket...");
        messageInput.setPrefRowCount(3);
        messageInput.setWrapText(true);

        Button sendBtn = new Button("Submit Ticket Message");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        // LOAD MESSAGES LOGIC
        final Runnable refreshChat = () -> {
            try {
                // Fetch LATEST (Latest might be CLOSED or OPEN)
                List<Message> msgs = messageService.getLatestConversationForCustomer(currentUser.getId());
                chatList.getItems().setAll(msgs);
                if (!msgs.isEmpty()) {
                    chatList.scrollTo(chatList.getItems().size() - 1);

                    // Optional: Check status of last message to hint user
                    Message last = msgs.get(msgs.size() - 1);
                    if ("CLOSED".equalsIgnoreCase(last.getConversationStatus())) {
                        headerLabel.setText("Support Ticket (CLOSED) - Send new message to start new ticket");
                    } else {
                        headerLabel.setText("Support Ticket (OPEN)");
                    }
                } else {
                    headerLabel.setText("Support Ticket (New)");
                }
            } catch (Exception e) {
                System.err.println("Error loading chat: " + e.getMessage());
            }
        };

        // SEND ACTION
        // SEND ACTION
        sendBtn.setOnAction(e -> {
            String content = messageInput.getText();
            if (content == null || content.trim().isEmpty()) {
                AlertUtil.showWarning("Validation", "Please enter a message.");
                return;
            }

            try {
                Message msg = new Message();
                msg.setContent(content.trim());
                messageService.sendMessage(msg);

                messageInput.clear();
                refreshChat.run();

            } catch (Exception ex) {
                AlertUtil.showError("Error", "Failed to send message: " + ex.getMessage());
            }
        });

        // Initial Load
        refreshChat.run();

        root.getChildren().addAll(headerLabel, chatList, messageInput, sendBtn);
        stage.setScene(new Scene(root));
        stage.show();
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
