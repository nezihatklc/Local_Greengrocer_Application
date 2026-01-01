package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.ProductService;
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
import java.io.IOException;
import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.util.ValidatorUtil;

import java.util.Optional;

/**
 * OwnerController
 * Controller for the Owner dashboard UI.
 *
 * Responsibilities (Rules.md):
 * - Handles UI events (buttons, selection changes).
 * - MUST NOT contain SQL or complex business rules.
 * - Calls Service layer for business logic.
 */
public class OwnerController {

    private User currentUser;
    private final ProductService productService;

    // FXML Fields
    @FXML
    private Label usernameLabel;
    @FXML
    private Button backButton;
    @FXML
    private Button logoutButton;

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

    // -- FORM FIELDS --
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

    public OwnerController() {
        this.productService = new ProductService();
    }

    /**
     * Optional init method if you navigate with FXMLLoader.getController().
     * If not called, controller will still work by reading SessionManager in
     * initialize().
     *
     * @param user logged-in user (OWNER)
     */
    public void initData(User user) {
        this.currentUser = user;
        if (usernameLabel != null && currentUser != null) {
            usernameLabel.setText("Owner: " + currentUser.getUsername());
        }
        loadOwnerData();
    }

    /**
     * JavaFX lifecycle method.
     * Called automatically after FXML is loaded and @FXML fields are injected.
     */
    @FXML
    public void initialize() {
        // ---- Table column bindings (Week11: TableView + binding) ----
        idCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));

        // FIX: Null-safe category (prevents NullPointerException)
        categoryCol.setCellValueFactory(cell -> {
            Product p = cell.getValue();
            String cat = (p.getCategory() == null) ? "-" : p.getCategory().name();
            return new SimpleStringProperty(cat);
        });

        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        unitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnit()));
        priceCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
        stockCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStock()));
        thresholdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getThreshold()));

        // ---- Selection listener (Week11: ChangeListener/Listener) ----
        productTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> showProductDetails(newVal));

        // FIX: initData() çağrılmasa bile user adını set etmeye çalış
        if (this.currentUser == null) {
            this.currentUser = SessionManager.getInstance().getCurrentUser();
        }
        if (usernameLabel != null && currentUser != null) {
            usernameLabel.setText("Owner: " + currentUser.getUsername());
        }

        // Initialize Category Combo
        if (categoryCombo != null) {
            categoryCombo.getItems().setAll(Category.values());
        }

        // FIX: initData() hiç çağrılmasa bile tablo dolsun
        loadOwnerData();
    }

    /**
     * Loads all products for owner view (including out-of-stock).
     * Controller does not query DB directly: service handles it.
     */
    private void loadOwnerData() {
        if (productTable == null)
            return;
        productTable.getItems().setAll(productService.getAllProductsForOwner());
    }

    /**
     * Updates UI labels based on selected product.
     * Shows effective price computed by threshold rule in ProductService.
     *
     * @param product selected product or null
     */
    private void showProductDetails(Product product) {
        if (effectivePriceLabel == null)
            return;

        if (product == null) {
            effectivePriceLabel.setText("-");
            handleClear(); // Clear form when nothing selected
            return;
        }

        // Populate Form
        nameField.setText(product.getName());
        categoryCombo.setValue(product.getCategory());
        typeField.setText(product.getType());
        unitField.setText(product.getUnit());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        thresholdField.setText(String.valueOf(product.getThreshold()));

        try {
            double effectivePrice = productService.getEffectivePrice(product);
            effectivePriceLabel.setText(String.format("%.2f ₺", effectivePrice));
        } catch (Exception e) {
            effectivePriceLabel.setText("-");
        }
    }

    /**
     * Refresh button handler.
     */
    @FXML
    private void handleRefresh() {
        loadOwnerData();
        AlertUtil.showInfo("Refreshed", "Product list refreshed.");
    }

    /**
     * Handles the Back button action.
     * Delegates to handleLogout for now.
     */
    @FXML
    private void handleBack() {
        // In the current flow, Back from Owner Dashboard goes to Login (Logout)
        handleLogout();
    }

    /**
     * Handles the Logout button action.
     * Clears session and navigates to Login screen.
     */
    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();

            // Load Login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            Parent root = loader.load();

            // Navigate
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Group18 GreenGrocer - Login");
            stage.show();

        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not go to login screen: " + e.getMessage());
        }
    }

    /**
     * Delete selected product handler.
     */
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

    // ==========================================
    // ADD / UPDATE / CLEAR HANDLERS
    // ==========================================

    @FXML
    private void handleAdd() {
        if (!validateForm())
            return;

        try {
            Product newProduct = new Product();
            // ID is auto-generated by DB or DAO (-1 placeholder)
            newProduct.setId(-1);
            newProduct.setName(nameField.getText().trim());
            newProduct.setCategory(categoryCombo.getValue());
            newProduct.setType(typeField.getText().trim());
            newProduct.setUnit(unitField.getText().trim());
            newProduct.setPrice(Double.parseDouble(priceField.getText().trim()));
            newProduct.setStock(Double.parseDouble(stockField.getText().trim()));
            newProduct.setThreshold(Double.parseDouble(thresholdField.getText().trim()));

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
            // Update selected object
            selected.setName(nameField.getText().trim());
            selected.setCategory(categoryCombo.getValue());
            selected.setType(typeField.getText().trim());
            selected.setUnit(unitField.getText().trim());
            selected.setPrice(Double.parseDouble(priceField.getText().trim()));
            selected.setStock(Double.parseDouble(stockField.getText().trim()));
            selected.setThreshold(Double.parseDouble(thresholdField.getText().trim()));

            productService.updateProduct(selected);

            AlertUtil.showInfo("Success", "Product updated successfully.");
            loadOwnerData();
            // Keep selection? Or clear? Let's keep it to show updated data
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

        productTable.getSelectionModel().clearSelection();
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
}
