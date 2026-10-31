package com.pacman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PacmanApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                PacmanApp.class.getResource("/com/pacman/pacman.fxml"));
        Parent root = loader.load();
        GameController controller = loader.getController();

        Scene scene = new Scene(root, Renderer.CANVAS_W, Renderer.CANVAS_H, Color.BLACK);
        controller.attach(scene);

        stage.setTitle("Pac-Man");
        stage.setScene(scene);

        // Enable window resizing and maximize/fullscreen controls
        stage.setResizable(true);
        stage.setMaximized(false);

        stage.show();

        controller.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}