package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.service.ProductService;
import com.group18.greengrocer.util.AlertUtil;
import com.group18.greengrocer.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class OwnerController {

    @FXML private Label usernameLabel;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, String> typeCol;
    @FXML private TableColumn<Product, String> unitCol;
    @FXML private TableColumn<Product, Double> priceCol;
    @FXML private TableColumn<Product, Double> stockCol;
    @FXML private TableColumn<Product, Double> thresholdCol;

    private final ProductService productService = new ProductService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Header username
        if (SessionManager.getInstance().getCurrentUser() != null) {
            usernameLabel.setText(SessionManager.getInstance().getCurrentUser().getUsername());
        }

        // Table column bindings (Week11: TableView + PropertyValueFactory)
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        thresholdCol.setCellValueFactory(new PropertyValueFactory<>("threshold"));

        productTable.setItems(productList);

        // Load data
        loadProducts();
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to delete.");
            return;
        }

        try {
            productService.removeProduct(selected.getId());
            loadProducts();
            AlertUtil.showInfo("Deleted", "Product deleted successfully.");
        } catch (Exception e) {
            AlertUtil.showError("Delete Failed", e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            List<Product> all = productService.getAllProductsForOwner(); // IMPORTANT
            productList.setAll(all);
        } catch (Exception e) {
            AlertUtil.showError("Load Failed", "Could not load products: " + e.getMessage());
        }
    }
}
