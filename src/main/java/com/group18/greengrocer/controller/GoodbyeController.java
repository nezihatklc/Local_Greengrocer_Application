package com.group18.greengrocer.controller;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
/**
 * Controller for the "Goodbye" or Logout confirmation screen.
 * <p>
 * This class handles the visual transition when a user finishes their session.
 * It plays a short animation of a shopping cart driving away and then
 * automatically redirects the application back to the Login screen.
 *
 * @author Group18
 * @version 1.0
 */
public class GoodbyeController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Group cartGroup;
/**
     * Initializes the controller and immediately starts the exit animation.
     * <p>
     * The animation logic:
     * <ol>
     * <li>Waits for a short delay (0.5s).</li>
     * <li>Moves the {@code cartGroup} 800 pixels to the right (off-screen).</li>
     * <li>Uses an EASE_IN interpolator for a natural acceleration effect.</li>
     * <li>Calls {@link #navigateToLogin()} automatically when the animation finishes.</li>
     * </ol>
     */
    @FXML
    public void initialize() {
        // Animation: Cart drives away to the right
        TranslateTransition driveAway = new TranslateTransition(Duration.seconds(1.5), cartGroup);
        driveAway.setDelay(Duration.seconds(0.5));
        driveAway.setToX(800); // Drives off screen to the right
        driveAway.setInterpolator(Interpolator.EASE_IN);

        driveAway.setOnFinished(event -> navigateToLogin());

        driveAway.play();
    }
/**
     * Loads the Login FXML view and resets the stage to the login screen.
     * This method is triggered automatically by the animation's onFinished event.
     */
    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            if (rootPane.getScene() != null) {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setMaximized(true);
                stage.setTitle("Group18 GreenGrocer - Login");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
