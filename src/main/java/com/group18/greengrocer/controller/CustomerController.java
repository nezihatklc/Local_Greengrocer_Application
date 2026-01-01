package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.OrderService;
import com.group18.greengrocer.service.ProductService;

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
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



import java.io.IOException;
import java.util.List;

public class CustomerController {

    // =====================
    // SERVICES
    // =====================
    private ProductService productService;
    private OrderService orderService;

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



    // =====================
    // INITIALIZE
    // =====================
    @FXML
    public void initialize() {
        productService = new ProductService();
        orderService = new OrderService();

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
        }
    }

    // =====================
    // PRODUCT LIST
    // =====================
    private void loadProducts() {
        fruitPane.getChildren().clear();
        vegetablePane.getChildren().clear();

        List<Product> products = productService.getAllProducts();

        // SEARCH KEYWORD
        String keyword = "";

        if (searchField != null && searchField.getText() != null) {
            keyword = searchField.getText().toLowerCase();
        }

        for (Product product : products) {

            //FILTER BY NAME
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

        try {
        String imageName = product.getName().toLowerCase() + ".png";
        var stream = getClass().getResourceAsStream(
            "/com/group18/greengrocer/images/products/" + imageName
        );

        if (stream != null) {
            Image image = new Image(stream);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-border-color: red;");
            box.getChildren().add(imageView);
        }
    } catch (Exception e) {
        // deliberately ignore
    }
         
        


        Label nameLabel = new Label(product.getName());
        Label priceLabel = new Label(
                "Price: " + product.getPrice() + " ₺ / " + product.getUnit()
        );
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

                orderService.addToCart(
                    currentUser.getId(),
                    product.getId(),
                    amount
                );

                Order cart = orderService.getCart(currentUser.getId());
                cartButton.setText("Cart (" + cart.getItems().size() + ")");

                showInfo(amount + " kg of " + product.getName() + " added to cart.");
                amountField.clear();

            } catch (NumberFormatException ex) {
                 showError("Please enter a valid number.");
            } catch (Exception ex){
                showError(ex.getMessage());
            }
        });

        box.getChildren().addAll(
                nameLabel,
                priceLabel,
                stockLabel,
                amountField,
                addButton
        );
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
                    getClass().getResource("/com/group18/greengrocer/fxml/cart.fxml")
            );
            Parent root = loader.load();

            CartController controller = loader.getController();
            controller.initData(currentUser, cart);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Your Cart");
            stage.show();

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

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =====================
    // ORDER HISTORY
    // =====================
    @FXML
    private void handleMyOrders() {
        List<Order> orders = orderService.getOrdersByCustomer(currentUser.getId());
        showInfo("You have " + orders.size() + " past orders.");
    }


     // =====================
     // PROFILE UPDATE
     // =====================

     @FXML
     private void handleEditProfile() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");

        ButtonType saveButtonType =
                 new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
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
        Button saveButton =
                (Button) dialog.getDialogPane().lookupButton(saveButtonType);

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
                    getClass().getResource("/com/group18/greengrocer/fxml/login.fxml")
            );
            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Group18 GreenGrocer - Login");
        } catch (IOException e) {
            showError("Could not go back to login: " + e.getMessage());
        }
    }

    // =====================
    // UTIL
    // =====================
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
