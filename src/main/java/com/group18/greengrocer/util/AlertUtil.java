package com.group18.greengrocer.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
/**
 * Utility class providing static helper methods to display various JavaFX alerts.
 * <p>
 * This class centralizes the creation of dialog boxes (Error, Info, Warning, Confirmation),
 * ensuring a consistent look and feel across the application. It also handles null-safety
 * for message strings.
 * <p>
 * <b>Note:</b> This is a final class with a private constructor and cannot be instantiated.
 *
 * @author Group18
 * @version 1.0
 */
public final class AlertUtil {
/**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AlertUtil() { }
/**
     * Displays an error dialog box.
     * <p>
     * Use this for critical failures, exceptions, or operations that could not be completed.
     *
     * @param title   The title of the alert window.
     * @param message The content message explaining the error.
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }
/**
     * Displays an informational dialog box.
     * <p>
     * Use this for success messages (e.g., "Order Placed") or general status updates.
     *
     * @param title   The title of the alert window.
     * @param message The content message.
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }
/**
     * Displays a warning dialog box.
     * <p>
     * Use this for validation issues (e.g., "Missing password") or non-critical problems.
     *
     * @param title   The title of the alert window.
     * @param message The content message warning the user.
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }
/**
     * Displays a confirmation dialog requesting user approval.
     * <p>
     * This blocks the execution until the user responds.
     *
     * @param title   The title of the confirmation window.
     * @param message The question asking for confirmation (e.g., "Are you sure?").
     * @return An {@link Optional} containing the {@link ButtonType} clicked (usually OK or CANCEL).
     */
    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(safe(title));
        alert.setHeaderText(null);
        alert.setContentText(safe(message));
        return alert.showAndWait();
    }
/**
     * Internal helper to construct and show a basic alert.
     *
     * @param type    The {@link javafx.scene.control.Alert.AlertType} (ERROR, INFO, etc.).
     * @param title   The title string.
     * @param message The content message.
     */
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(safe(title));
        alert.setHeaderText(null);
        alert.setContentText(safe(message));
        alert.showAndWait();
    }
/**
     * Null-safe string normalization.
     * <p>
     * Ensures that null strings are converted to empty strings and whitespace is trimmed.
     * This prevents the Alert box from crashing or showing "null" text.
     *
     * @param s The input string.
     * @return A trimmed string, or an empty string if input was null.
     */
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
