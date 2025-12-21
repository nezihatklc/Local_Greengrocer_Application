package com.group18.greengrocer.controller;

import com.group18.greengrocer.service.AuthenticationService;
import com.group18.greengrocer.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

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
        if (user.isEmpty() || pass.isEmpty()) {
            // Show Alert
            System.out.println("Fields cannot be empty"); // Placeholder for AlertUtil
            return;
        }

        // Call Service
        boolean success = authService.login(user, pass);
        if (success) {
            System.out.println("Login Successful! Redirecting...");
            // Redirect to role-specific dashboard based on SessionManager.getInstance().getCurrentUser().getRole()
        } else {
            System.out.println("Login Failed."); // Placeholder for AlertUtil
        }
    }
}
