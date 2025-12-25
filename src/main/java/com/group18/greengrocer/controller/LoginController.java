package com.group18.greengrocer.controller;

import com.group18.greengrocer.service.AuthenticationService;
import com.group18.greengrocer.util.AlertUtil;
import com.group18.greengrocer.util.SessionManager;
import com.group18.greengrocer.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    private AuthenticationService authService;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    @FXML
    public void initialize() {
        // Init logic
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        // UI Validation
        if (user == null || user.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Username and password cannot be empty.");
            return;
        }

        // Call Service
        User authenticatedUser = authService.login(user, pass);

        if (authenticatedUser != null) {
            System.out.println("Login Successful! Redirecting...");
            SessionManager.getInstance().setCurrentUser(authenticatedUser);

            // Redirect based on role
            String dashboardFxml = "";
            switch (authenticatedUser.getRole()) {
                case CUSTOMER:
                    dashboardFxml = "/com/group18/greengrocer/fxml/customer_dashboard.fxml";
                    break;
                case CARRIER:
                    dashboardFxml = "/com/group18/greengrocer/fxml/carrier_dashboard.fxml";
                    break;
                case OWNER:
                    dashboardFxml = "/com/group18/greengrocer/fxml/owner_dashboard.fxml";
                    break;
            }

            redirectToDashboard(dashboardFxml);

        } else {
            AlertUtil.showError("Login Failed", "Invalid username or password.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/group18/greengrocer/fxml/register.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", "Could not load registration screen: " + e.getMessage());
        }
    }

    private void redirectToDashboard(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Group18 GreenGrocer - " + SessionManager.getInstance().getCurrentUser().getUsername());
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }
}
