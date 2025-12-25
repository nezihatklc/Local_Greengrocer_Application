package com.group18.greengrocer.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Utility class for displaying JavaFX alerts.
 */
public class AlertUtil {

    /**
     * Shows an error alert with the specified title and message.
     *
     * @param title   The title of the alert window.
     * @param message The content message to display.
     */
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message);
    }

    /**
     * Shows an information alert with the specified title and message.
     *
     * @param title   The title of the alert window.
     * @param message The content message to display.
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }

    /**
     * Shows a warning alert with the specified title and message.
     *
     * @param title   The title of the alert window.
     * @param message The content message to display.
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }

    /**
     * Shows a confirmation alert and returns the result.
     * 
     * @param title   The title of the alert.
     * @param message The message to display.
     * @return The ButtonType clicked by the user.
     */
    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    private static void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
