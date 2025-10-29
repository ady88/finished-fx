package com.adrian;

import com.adrian.finished.ui.controller.GameController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for the Finished! card game.
 */
public class FinishedFxApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create the game controller which sets up the entire UI
            GameController gameController = new GameController(primaryStage);

            // Show the stage
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
