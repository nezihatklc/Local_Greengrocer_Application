package com.group18.greengrocer.controller;

import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.service.AuthenticationService;
import com.group18.greengrocer.service.ValidationException;
import com.group18.greengrocer.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextArea addressArea;

    private AuthenticationService authService;

    public RegisterController() {
        this.authService = new AuthenticationService();
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : ""; // Don't trim password? Usually yes, but user might want spaces. But standard practice is usually no spaces. Let's keep password raw or trim? Rules don't say. Let's start with staying safe and only trimming username/phone/address.
        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        String address = addressArea.getText() != null ? addressArea.getText().trim() : "";

        // Create a new User object with CUSTOMER role
        User newUser = new User(username, password, Role.CUSTOMER, address, phone);

        try {
            authService.register(newUser);
            AlertUtil.showInfo("Registration Successful", "You have successfully registered! Please login.");
            navigateToLogin();
        } catch (ValidationException e) {
            AlertUtil.showWarning("Registration Failed", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("System Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            // We need to get the stage from a node in the scene.
            // Since we might be called from initialize or something where scene is not
            // ready, better use a field.
            // But here handleRegister/Back are triggered by buttons.
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", "Could not load login screen: " + e.getMessage());
        }
    }
}
