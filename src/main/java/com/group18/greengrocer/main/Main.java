package com.group18.greengrocer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Entry point for the Group18 GreenGrocer Application.
 * <p>
 * This class extends the JavaFX {@link Application} class and serves as the bootstrap
 * for the entire project. It is responsible for:
 * <ol>
 * <li>Executing database schema updates/patches (migrations) at startup.</li>
 * <li>Initializing the JavaFX toolkit.</li>
 * <li>Loading the initial "Splash" screen to welcome the user.</li>
 * </ol>
 * @version 1.0
 */
public class Main extends Application {
/**
     * The main entry point for all JavaFX applications.
     * <p>
     * This method is called after the {@code init} method has returned, and after
     * the system is ready for the application to begin running.
     * <p>
     * It loads the {@code splash.fxml} file, sets up the primary window (Stage),
     * and displays it in maximized mode.
     *
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the Splash View
            java.net.URL url = getClass().getResource("/com/group18/greengrocer/fxml/splash.fxml");
            if (url == null) {
                System.err.println("Error: FXML file not found at /com/group18/greengrocer/fxml/splash.fxml");
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
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/**
     * The standard main() method for Java applications.
     * <p>
     * Before launching the UI, this method explicitly calls the {@link com.group18.greengrocer.dao.SchemaPatcher}
     * to ensure the database schema is up-to-date with the latest code requirements (e.g., Enum updates).
     * After the database check, it hands over control to the JavaFX {@code launch} method.
     *
     * @param args Command line arguments passed to the application.
     */
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
