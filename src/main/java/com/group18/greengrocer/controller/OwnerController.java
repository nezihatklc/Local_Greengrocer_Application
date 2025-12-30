package com.group18.greengrocer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class OwnerController {
    
    // Services
    // private ProductService productService;
    // private ReportService reportService;

    // FXML Fields
    @FXML
    private Label usernameLabel;
    @FXML
    private TableView<?> productTable; // Generic wildcard for now as Product isn't fully set up in this file
    @FXML
    private TableColumn<?, ?> idCol;
    @FXML
    private TableColumn<?, ?> nameCol;
    @FXML
    private TableColumn<?, ?> categoryCol;
    @FXML
    private TableColumn<?, ?> typeCol;
    @FXML
    private TableColumn<?, ?> unitCol;
    @FXML
    private TableColumn<?, ?> priceCol;
    @FXML
    private TableColumn<?, ?> stockCol;
    @FXML
    private TableColumn<?, ?> thresholdCol;
    @FXML
    private Label effectivePriceLabel;


    @FXML
    public void initialize() {
        // Load owner data
    }

    @FXML
    private void handleRefresh() {
        // TODO: Refresh logic
    }

    @FXML
    private void handleDelete() {
        // TODO: Delete logic
    }
}
