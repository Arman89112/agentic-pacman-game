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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Controller backing {@code pacman.fxml}.
 * Manages the Classic Arcade Intro sequence, scaling, and game actions.
 */
public class GameController {

    @FXML private StackPane rootPane;
    @FXML private AnchorPane gameContainer;
    @FXML private Canvas canvas;
    @FXML private VBox menu;
    @FXML private VBox introOverlay;
    @FXML private HBox characterBox;
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

        menu.setVisible(false);
        menu.setManaged(false);
    }

    public void attach(Scene scene) {
        // Bind scaling and centering dynamically to window size changes (Fullscreen/Maximize)
        scene.widthProperty().addListener((obs, oldVal, newVal) -> updateScaling(scene));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> updateScaling(scene));

        scene.setOnKeyPressed(e -> {
            if (!running && introOverlay.isVisible()) {
                return; // No skipping allowed during intro
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

    private void updateScaling(Scene scene) {
        double windowW = scene.getWidth();
        double windowH = scene.getHeight();
        double baseW = Renderer.CANVAS_W;
        double baseH = Renderer.CANVAS_H;

        // Calculate scale factor to fit window while preserving aspect ratio
        double scaleX = windowW / baseW;
        double scaleY = windowH / baseH;
        double scale = Math.min(scaleX, scaleY);

        gameContainer.setScaleX(scale);
        gameContainer.setScaleY(scale);
    }

    public void playIntroSequence() {
        audioManager.playWelcomeVoice();
        audioManager.startIntroMusic();

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

        Timeline progressTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(loadingProgressBar.progressProperty(), 0.0)),
                new KeyFrame(Duration.seconds(5.0), new KeyValue(loadingProgressBar.progressProperty(), 1.0))
        );

        final double[] progressCounter = {0.0};
        Timeline percentageTimeline = new Timeline(
                new KeyFrame(Duration.millis(50), event -> {
                    progressCounter[0] += 1.0;
                    if (progressCounter[0] > 100.0) progressCounter[0] = 100.0;
                    percentageLabel.setText((int) progressCounter[0] + "%");
                })
        );
        percentageTimeline.setCycleCount(100);

        SequentialTransition fullIntro = new SequentialTransition(
                introGroup,
                new ParallelTransition(progressTimeline, percentageTimeline)
        );

        fullIntro.setOnFinished(event -> {
            audioManager.stopIntroMusic();
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
            case F:
            case F11:
                Window window = canvas.getScene().getWindow();
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    stage.setFullScreen(!stage.isFullScreen());
                }
                break;
            default:
                break;
        }
    }
}