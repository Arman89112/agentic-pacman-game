package com.pacman;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Controller backing {@code pacman.fxml}.
 *
 * <p>The layout (canvas + the start menu with the ghost-mode choice) lives in
 * the FXML file and is injected here.  The controller owns the model
 * ({@link Game}), the renderer and the per-frame {@link AnimationTimer} loop,
 * so {@link PacmanApp} only has to load the FXML and show the scene.
 */
public class GameController {

    @FXML private Canvas canvas;
    @FXML private VBox menu;
    @FXML private RadioButton soloGhost;
    @FXML private RadioButton allGhosts;
    @FXML private Button startButton;
    @FXML private Label statusLabel;

    private Game game;
    private Renderer renderer;
    private GraphicsContext gc;
    private long lastNano;
    private boolean running;

    /** Called automatically by the FXMLLoader after the fields are injected. */
    @FXML
    private void initialize() {
        game = new Game();
        renderer = new Renderer();
        canvas.setWidth(Renderer.CANVAS_W);
        canvas.setHeight(Renderer.CANVAS_H);
        gc = canvas.getGraphicsContext2D();
    }

    /** Wires keyboard input once the scene exists. */
    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (!running) {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    onStart();
                }
                return;
            }
            onKeyPressed(e.getCode());
            String text = e.getText();
            if (text != null && text.length() == 1) {
                game.feedCheat(text.charAt(0));
            }
        });
    }

    /** Starts the render/update loop.  The menu is shown until START is hit. */
    public void start() {
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
                if (running) {
                    game.update(dt);
                    if (game.state == Game.State.GAME_OVER) {
                        running = false;
                        showMenu();
                    }
                }
                renderer.render(gc, game);
            }
        };
        timer.start();
    }

    /** START button handler: reads the chosen ghost mode and begins a game. */
    @FXML
    private void onStart() {
        game.setGhostMode(soloGhost.isSelected()
                ? Game.GhostMode.SOLO
                : Game.GhostMode.ALL);
        game.startNewGame();
        menu.setVisible(false);
        menu.setManaged(false);
        statusLabel.setText("");
        running = true;
        lastNano = 0;
        canvas.requestFocus();
    }

    private void showMenu() {
        menu.setVisible(true);
        menu.setManaged(true);
        statusLabel.setText("GAME OVER     SCORE " + game.score);
        startButton.requestFocus();
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
}
