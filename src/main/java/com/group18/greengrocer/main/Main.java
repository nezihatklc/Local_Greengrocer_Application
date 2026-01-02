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
            java.net.URL url = getClass().getResource("/com/group18/greengrocer/fxml/login.fxml");
            if (url == null) {
                System.err.println("Error: FXML file not found at /com/group18/greengrocer/fxml/login.fxml");
            } else {
                System.out.println("FXML found: " + url);
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            // Link CSS
            // scene.getStylesheets().add(getClass().getResource("/com/group01/greengrocer/css/style.css").toExternalForm());

            primaryStage.setTitle("Group18 GreenGrocer");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Run database migration
        try {
            com.group18.greengrocer.dao.SchemaPatcher.updateSchema();
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch(args);
    }
}
