package com.pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class Renderer {

    public static final double TILE = Game.TILE;
    public static final double HUD = 48;
    public static final double BOARD_W = Maze.COLS * TILE;
    public static final double BOARD_H = Maze.ROWS * TILE;
    public static final double CANVAS_W = BOARD_W;
    public static final double CANVAS_H = BOARD_H + HUD;

    private static final Color WALL = Color.web("#2121DE");
    private static final Color DOOR = Color.web("#FFB8FF");
    private static final Color PELLET = Color.web("#FFC4A8");
    private static final Color PAC = Color.web("#FFE100");
    private static final Color TEXT = Color.web("#FFE100");
    private static final Color RED = Color.web("#FF3B3B");

    public void render(GraphicsContext gc, Game game) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        drawWalls(gc, game.maze);
        drawDoor(gc, game.maze);
        drawPellets(gc, game);
        if (game.showGraph) {
            drawGraph(gc, game);
        }
        drawPacman(gc, game);
        drawGhosts(gc, game);
        drawHud(gc, game);
        drawCheatTags(gc, game);
        drawOverlay(gc, game);
    }

    // ------------------------------------------------------------------
    // maze
    // ------------------------------------------------------------------

    private void drawWalls(GraphicsContext gc, Maze maze) {
        gc.setStroke(WALL);
        gc.setLineWidth(2.4);
        gc.setLineCap(StrokeLineCap.ROUND);
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                if (!maze.isWall(c, r)) {
                    continue;
                }
                double x0 = c * TILE;
                double y0 = r * TILE;
                double x1 = x0 + TILE;
                double y1 = y0 + TILE;
                if (!maze.isWall(c, r - 1)) {
                    line(gc, x0 + 1, y0 + 1, x1 - 1, y0 + 1);
                }
                if (!maze.isWall(c, r + 1)) {
                    line(gc, x0 + 1, y1 - 1, x1 - 1, y1 - 1);
                }
                if (!maze.isWall(c - 1, r)) {
                    line(gc, x0 + 1, y0 + 1, x0 + 1, y1 - 1);
                }
                if (!maze.isWall(c + 1, r)) {
                    line(gc, x1 - 1, y0 + 1, x1 - 1, y1 - 1);
                }
            }
        }
    }

    private void drawDoor(GraphicsContext gc, Maze maze) {
        gc.setStroke(DOOR);
        gc.setLineWidth(3);
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                if (maze.isDoor(c, r)) {
                    double y = r * TILE + TILE / 2.0;
                    line(gc, c * TILE, y, c * TILE + TILE, y);
                }
            }
        }
    }

    private void drawPellets(GraphicsContext gc, Game game) {
        boolean powerVisible = ((int) (game.time * 4)) % 2 == 0;
        gc.setFill(PELLET);
        if (game.maze.isCompressed()) {
            // Sparse mode: iterate only the survivors instead of all 868 tiles.
            SparseMatrix s = game.maze.sparse();
            for (int i = 0; i < s.size(); i++) {
                double cx = s.col(i) * TILE + TILE / 2.0;
                double cy = s.row(i) * TILE + TILE / 2.0;
                if (s.value(i) == SparseMatrix.PELLET) {
                    gc.fillOval(cx - 2.2, cy - 2.2, 4.4, 4.4);
                } else {
                    if (powerVisible) {
                        gc.fillOval(cx - 5.5, cy - 5.5, 11, 11);
                    }
                }
            }
            return;
        }
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                char ch = game.maze.tileAt(c, r);
                double cx = c * TILE + TILE / 2.0;
                double cy = r * TILE + TILE / 2.0;
                if (ch == Maze.PELLET) {
                    gc.fillOval(cx - 2.2, cy - 2.2, 4.4, 4.4);
                } else if (ch == Maze.POWER && powerVisible) {
                    gc.fillOval(cx - 5.5, cy - 5.5, 11, 11);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // actors
    // ------------------------------------------------------------------

    private void drawPacman(GraphicsContext gc, Game game) {
        Pacman p = game.pacman;
        double r = TILE * 0.47;
        double mouth;
        double base;
        if (game.state == Game.State.DYING) {
            double progress = 1 - Math.max(0, game.stateTimer) / 1.8;
            mouth = 6 + 348 * Math.min(1, progress);
        } else {
            mouth = p.moving() ? 16 + 22 * Math.abs(Math.sin(p.animTime * 9)) : 14;
        }
        switch (p.dir) {
            case UP: base = 270; break;
            case DOWN: base = 90; break;
            case LEFT: base = 180; break;
            default: base = 0; break;
        }

        gc.setFill(PAC);
        gc.beginPath();
        gc.moveTo(p.x, p.y);
        double start = base + mouth / 2.0;
        double end = base + 360 - mouth / 2.0;
        int steps = 28;
        for (int i = 0; i <= steps; i++) {
            double a = Math.toRadians(start + (end - start) * i / steps);
            gc.lineTo(p.x + r * Math.cos(a), p.y + r * Math.sin(a));
        }
        gc.closePath();
        gc.fill();
    }

    private void drawGhosts(GraphicsContext gc, Game game) {
        for (Ghost g : game.ghosts) {
            if (g.active) {
                drawGhost(gc, game, g);
            }
        }
    }

    private void drawGhost(GraphicsContext gc, Game game, Ghost g) {
        double r = TILE * 0.46;
        double x = g.x;
        double y = g.y;
        boolean frightened = g.state == Ghost.State.FRIGHTENED;
        boolean eaten = g.state == Ghost.State.EATEN || g.state == Ghost.State.ENTERING;

        if (!eaten) {
            Color body;
            if (frightened) {
                body = game.isFrightBlink() ? Color.web("#FFFFFF") : Color.web("#2121DE");
            } else {
                body = ghostColor(g.kind);
            }
            drawGhostBody(gc, x, y, r, body);
        }
        drawEyes(gc, x, y, r, g, frightened, eaten);
    }

    private void drawGhostBody(GraphicsContext gc, double x, double y, double r, Color color) {
        double bottom = y + r * 0.55;
        gc.setFill(color);
        gc.beginPath();
        gc.moveTo(x - r, bottom);
        for (int i = 0; i <= 20; i++) {
            double a = Math.PI + Math.PI * i / 20.0;
            gc.lineTo(x + r * Math.cos(a), y + r * Math.sin(a));
        }
        gc.lineTo(x + r, bottom);
        int segs = 3;
        double segW = (2 * r) / segs;
        double bump = r * 0.32;
        for (int i = 0; i < segs; i++) {
            double sx = x + r - i * segW;
            double ex = sx - segW;
            double mx = (sx + ex) / 2;
            gc.quadraticCurveTo(mx, bottom + bump, ex, bottom);
        }
        gc.closePath();
        gc.fill();
    }

    private void drawEyes(GraphicsContext gc, double x, double y, double r,
                          Ghost g, boolean frightened, boolean eaten) {
        double eyeDX = r * 0.34;
        double eyeDY = -r * 0.12;
        double eyeR = r * 0.27;
        double pupilR = r * 0.14;

        for (int s = -1; s <= 1; s += 2) {
            double ex = x + s * eyeDX;
            double ey = y + eyeDY;
            gc.setFill(Color.WHITE);
            gc.fillOval(ex - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);

            double px = ex + g.dir.dx * r * 0.12;
            double py = ey + g.dir.dy * r * 0.12;
            gc.setFill(frightened ? Color.web("#2121DE") : Color.web("#2121DE"));
            gc.fillOval(px - pupilR, py - pupilR, pupilR * 2, pupilR * 2);
        }

        if (frightened && !eaten) {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            double my = y + r * 0.35;
            gc.beginPath();
            gc.moveTo(x - r * 0.55, my);
            gc.lineTo(x - r * 0.28, my - r * 0.18);
            gc.lineTo(x, my);
            gc.lineTo(x + r * 0.28, my - r * 0.18);
            gc.lineTo(x + r * 0.55, my);
            gc.stroke();
        }
    }

    private static Color ghostColor(Ghost.Kind kind) {
        switch (kind) {
            case BLINKY: return Color.web("#FF2D2D");
            case PINKY: return Color.web("#FFB8FF");
            case INKY: return Color.web("#00E5FF");
            case CLYDE:
            default: return Color.web("#FFB852");
        }
    }

    // ------------------------------------------------------------------
    // agentic debug overlay
    // ------------------------------------------------------------------

    /** Draws the maze graph, each ghost's planned path and the tracker target. */
    private void drawGraph(GraphicsContext gc, Game game) {
        MazeGraph g = game.graph;

        gc.setStroke(Color.web("#00E5FF", 0.22));
        gc.setLineWidth(1.1);
        for (int u = 0; u < g.nodeCount(); u++) {
            for (int v : g.neighbors(u)) {
                if (v > u) {
                    line(gc, nodeX(g, u), nodeY(g, u), nodeX(g, v), nodeY(g, v));
                }
            }
        }

        for (Ghost gh : game.ghosts) {
            if (!gh.active) {
                continue;
            }
            List<Integer> path = gh.plannedPath;
            gc.setStroke(ghostColor(gh.kind));
            gc.setLineWidth(2.4);
            for (int i = 0; i + 1 < path.size(); i++) {
                int a = path.get(i);
                int b = path.get(i + 1);
                line(gc, nodeX(g, a), nodeY(g, a), nodeX(g, b), nodeY(g, b));
            }
            if (gh.bossPick >= 0) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                double x = nodeX(g, gh.bossPick);
                double y = nodeY(g, gh.bossPick);
                gc.strokeOval(x - 6, y - 6, 12, 12);
            }
            if (gh.interceptNode >= 0) {
                gc.setStroke(Color.web("#FF00AA"));
                gc.setLineWidth(2);
                double x = nodeX(g, gh.interceptNode);
                double y = nodeY(g, gh.interceptNode);
                gc.strokeLine(x - 5, y - 5, x + 5, y + 5);
                gc.strokeLine(x - 5, y + 5, x + 5, y - 5);
            }
        }

        gc.setFill(Color.web("#FFFFFF", 0.8));
        for (int u = 0; u < g.nodeCount(); u++) {
            gc.fillOval(nodeX(g, u) - 2.2, nodeY(g, u) - 2.2, 4.4, 4.4);
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#00E5FF"));
        gc.fillText("GRAPH " + g.nodeCount() + " nodes / " + g.edgeCount()
                + " edges   [G to hide]", 8, 16);
        gc.setFill(Color.web("#FFD700"));
        gc.fillText("MEMORY(10): " + arrowHistory(game.pacman), 8, 30);
    }

    private static String arrowHistory(Pacman p) {
        if (p.history.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p.history.size(); i++) {
            switch (p.history.get(i)) {
                case UP: sb.append('\u2191'); break;
                case DOWN: sb.append('\u2193'); break;
                case LEFT: sb.append('\u2190'); break;
                case RIGHT: sb.append('\u2192'); break;
                default: sb.append('.'); break;
            }
            sb.append(' ');
        }
        return sb.toString();
    }

    private static double nodeX(MazeGraph g, int node) {
        return g.col(node) * TILE + TILE / 2.0;
    }

    private static double nodeY(MazeGraph g, int node) {
        return g.row(node) * TILE + TILE / 2.0;
    }

    private void drawCheatTags(GraphicsContext gc, Game game) {
        StringBuilder sb = new StringBuilder();
        if (game.speedBoost()) {
            sb.append("SPEED ");
        }
        if (game.ghostsFrozen()) {
            sb.append("FREEZE ");
        }
        if (game.godMode()) {
            sb.append("GOD ");
        }
        if (sb.length() == 0) {
            return;
        }
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#8AE234"));
        gc.fillText("CHEAT " + sb.toString().trim(), 12, BOARD_H + 46);
    }

    // ------------------------------------------------------------------
    // hud + overlays
    // ------------------------------------------------------------------

    private void drawHud(GraphicsContext gc, Game game) {
        double top = BOARD_H;
        gc.setFill(Color.web("#05050A"));
        gc.fillRect(0, top, CANVAS_W, HUD);

        gc.setFill(TEXT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));

        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE " + game.score, 12, top + 30);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("LEVEL " + game.level, CANVAS_W - 12, top + 30);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(game.sound.isEnabled() ? Color.web("#8AE234") : Color.web("#666666"));
        gc.fillText(game.sound.isEnabled() ? "SOUND ON" : "MUTED", CANVAS_W / 2, top + 30);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(game.ghostMode == Game.GhostMode.SOLO
                ? Color.web("#FF3B3B") : Color.web("#8890BB"));
        gc.fillText(game.ghostMode == Game.GhostMode.SOLO ? "SOLO HUNTER" : "CLASSIC x4",
                CANVAS_W / 2, top + 44);

        for (int i = 0; i < game.lives; i++) {
            drawLifeIcon(gc, 14 + i * 26, top + 10);
        }
    }

    private void drawLifeIcon(GraphicsContext gc, double x, double y) {
        double r = 7;
        gc.setFill(PAC);
        gc.beginPath();
        gc.moveTo(x, y + r);
        double start = 30;
        double end = 330;
        for (int i = 0; i <= 18; i++) {
            double a = Math.toRadians(start + (end - start) * i / 18.0);
            gc.lineTo(x + r * Math.cos(a), y + r + r * Math.sin(a));
        }
        gc.closePath();
        gc.fill();
    }

    private void drawOverlay(GraphicsContext gc, Game game) {
        double cx = BOARD_W / 2;
        double cy = BOARD_H / 2;
        gc.setTextAlign(TextAlignment.CENTER);

        switch (game.state) {
            case TITLE:
                gc.setFill(Color.web("#00000099"));
                gc.fillRect(0, cy - 130, BOARD_W, 260);
                title(gc, "PAC-MAN", cx, cy - 55, 46, PAC);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
                gc.setFill(Color.WHITE);
                gc.fillText("ARROWS / WASD  MOVE", cx, cy + 5);
                gc.fillText("P  PAUSE       M  SOUND", cx, cy + 30);
                gc.setFill(PAC);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
                gc.fillText("PRESS SPACE TO START", cx, cy + 75);
                break;

            case READY:
                gc.setFill(TEXT);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 26));
                gc.fillText("READY!", cx, cy + 20);
                break;

            case PAUSED:
                gc.setFill(Color.web("#000000AA"));
                gc.fillRect(0, cy - 60, BOARD_W, 120);
                title(gc, "PAUSED", cx, cy, 34, Color.WHITE);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
                gc.setFill(TEXT);
                gc.fillText("PRESS P TO RESUME", cx, cy + 38);
                break;

            case LEVEL_COMPLETE:
                gc.setFill(TEXT);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 26));
                gc.fillText("LEVEL " + game.level + " COMPLETE", cx, cy + 20);
                break;

            case GAME_OVER:
                gc.setFill(Color.web("#000000AA"));
                gc.fillRect(0, cy - 90, BOARD_W, 180);
                title(gc, "GAME OVER", cx, cy - 20, 40, RED);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 17));
                gc.setFill(Color.WHITE);
                gc.fillText("SCORE  " + game.score, cx, cy + 20);
                gc.setFill(TEXT);
                gc.fillText("PRESS SPACE TO PLAY AGAIN", cx, cy + 55);
                break;

            default:
                break;
        }
    }

    private void title(GraphicsContext gc, String text, double x, double y, double size, Color color) {
        gc.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, size));
        gc.setFill(Color.web("#000000"));
        gc.fillText(text, x + 3, y + 3);
        gc.setFill(color);
        gc.fillText(text, x, y);
    }

    private void line(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
    }
}
