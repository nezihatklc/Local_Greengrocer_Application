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

public class GoodbyeController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Group cartGroup;

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

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
            if (rootPane.getScene() != null) {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.setTitle("Group18 GreenGrocer - Login");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
