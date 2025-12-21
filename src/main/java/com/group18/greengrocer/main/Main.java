package com.group18.greengrocer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Entry point for the Greengrocer Application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the Login View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group01/greengrocer/fxml/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            // Link CSS
            scene.getStylesheets().add(getClass().getResource("/com/group01/greengrocer/css/style.css").toExternalForm());
            
            primaryStage.setTitle("CMPE343 Greengrocer Login");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
