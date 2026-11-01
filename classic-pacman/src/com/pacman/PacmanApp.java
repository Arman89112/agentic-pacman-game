package com.pacman;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PacmanApp extends Application {

    private Game game;
    private Renderer renderer;
    private long lastNano;

    @Override
    public void start(Stage stage) {
        game = new Game();
        renderer = new Renderer();

        Canvas canvas = new Canvas(Renderer.CANVAS_W, Renderer.CANVAS_H);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, Renderer.CANVAS_W, Renderer.CANVAS_H, Color.BLACK);
        scene.setOnKeyPressed(e -> onKeyPressed(e.getCode()));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNano == 0) {
                    lastNano = now;
                    return;
                }
                double dt = (now - lastNano) / 1_000_000_000.0;
                lastNano = now;
                if (dt > 0.05) {
                    dt = 0.05;
                }
                game.update(dt);
                renderer.render(gc, game);
            }
        };
        timer.start();

        stage.setTitle("Pac-Man");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        root.requestFocus();
    }

    private void onKeyPressed(KeyCode code) {
        switch (code) {
            case UP:
            case W:
                game.requestDirection(Direction.UP);
                break;
            case DOWN:
            case S:
                game.requestDirection(Direction.DOWN);
                break;
            case LEFT:
            case A:
                game.requestDirection(Direction.LEFT);
                break;
            case RIGHT:
            case D:
                game.requestDirection(Direction.RIGHT);
                break;
            case SPACE:
            case ENTER:
                game.primaryAction();
                break;
            case P:
                game.togglePause();
                break;
            case M:
                game.toggleMute();
                break;
            case G:
                game.showGraph = !game.showGraph;
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
