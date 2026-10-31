package com.pacman;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller backing {@code pacman.fxml}.
 * Manages the Classic Arcade Intro sequence before displaying the main game menu.
 */
public class GameController {

    @FXML private Canvas canvas;
    @FXML private VBox menu;
    @FXML private VBox introOverlay;
    @FXML private HBox characterBox; // Fixed: matched to HBox in FXML
    @FXML private Label arcadeHeaderLabel;
    @FXML private Label loadingTextLabel;
    @FXML private Label percentageLabel;
    @FXML private ProgressBar loadingProgressBar;

    @FXML private RadioButton soloGhost;
    @FXML private RadioButton allGhosts;
    @FXML private Button startButton;
    @FXML private Label statusLabel;

    private Game game;
    private Renderer renderer;
    private GraphicsContext gc;
    private long lastNano;
    private boolean running;
    private final AudioManager audioManager = new AudioManager();

    @FXML
    private void initialize() {
        game = new Game();
        renderer = new Renderer();
        canvas.setWidth(Renderer.CANVAS_W);
        canvas.setHeight(Renderer.CANVAS_H);
        gc = canvas.getGraphicsContext2D();

        // Ensure game menu is hidden initially while intro plays
        menu.setVisible(false);
        menu.setManaged(false);
    }

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // Block game inputs until intro finishes and game is running
            if (!running && introOverlay.isVisible()) {
                return; // No skipping allowed as requested!
            }
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

    /** Triggers the Classic Arcade Intro animation sequence upon launch. */
    public void playIntroSequence() {
        audioManager.playWelcomeVoice();
        audioManager.startIntroMusic();

        // Stage 1 & 2: Logo Zoom & Character Fade-in
        ScaleTransition logoZoom = new ScaleTransition(Duration.seconds(2.0), arcadeHeaderLabel);
        logoZoom.setFromX(0.2);
        logoZoom.setFromY(0.2);
        logoZoom.setToX(1.0);
        logoZoom.setToY(1.0);

        FadeTransition logoFade = new FadeTransition(Duration.seconds(2.0), arcadeHeaderLabel);
        logoFade.setFromValue(0.0);
        logoFade.setToValue(1.0);

        FadeTransition charFade = new FadeTransition(Duration.seconds(2.0), characterBox);
        charFade.setFromValue(0.0);
        charFade.setToValue(1.0);

        ParallelTransition introGroup = new ParallelTransition(logoZoom, logoFade, charFade);

        // Stage 3 & 4: Loading Progress Bar Animation (0% to 100% over 5 seconds)
        Timeline progressTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(loadingProgressBar.progressProperty(), 0.0)),
                new KeyFrame(Duration.seconds(5.0), new KeyValue(loadingProgressBar.progressProperty(), 1.0))
        );

        // Percentage counter updater
        final double[] progressCounter = {0.0};
        Timeline percentageTimeline = new Timeline(
                new KeyFrame(Duration.millis(50), event -> {
                    progressCounter[0] += 1.0;
                    if (progressCounter[0] > 100.0) progressCounter[0] = 100.0;
                    percentageLabel.setText((int) progressCounter[0] + "%");
                })
        );
        percentageTimeline.setCycleCount(100);

        // Sequential transition wrapping the whole intro
        SequentialTransition fullIntro = new SequentialTransition(
                introGroup,
                new ParallelTransition(progressTimeline, percentageTimeline)
        );

        fullIntro.setOnFinished(event -> {
            audioManager.stopIntroMusic();
            // Transition to Main Menu
            FadeTransition fadeOutIntro = new FadeTransition(Duration.seconds(1.0), introOverlay);
            fadeOutIntro.setFromValue(1.0);
            fadeOutIntro.setToValue(0.0);
            fadeOutIntro.setOnFinished(e -> {
                introOverlay.setVisible(false);
                introOverlay.setManaged(false);
                showMenu();
            });
            fadeOutIntro.play();
        });

        fullIntro.play();
    }

    public void start() {
        // Start the background intro sequence first
        playIntroSequence();

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