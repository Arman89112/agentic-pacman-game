import com.pacman.Direction;
import com.pacman.Game;
import com.pacman.Ghost;
import com.pacman.Maze;

import java.util.Random;

public class TestSim {

    public static void main(String[] args) {
        Game g = new Game();
        g.startNewGame();
        double dt = 1.0 / 60.0;
        int maxFrames = 60 * 60 * 8; // 8 simulated minutes

        int lastPellet = g.maze.totalPellets();
        int frames = 0;
        int accepted;

        while (frames < maxFrames) {
            frames++;
            switch (g.state) {
                case TITLE:
                    g.primaryAction();
                    break;
                case PLAYING:
                    g.requestDirection(aim(g));
                    break;
                case GAME_OVER:
                    break;
                default:
                    break;
            }
            g.update(dt);

            // invariants while the game is live
            if (g.state == Game.State.PLAYING || g.state == Game.State.READY) {
                if (g.maze.isWall(g.pacman.tileX(), g.pacman.tileY())) {
                    System.out.println("FAIL: pacman inside wall tile at "
                            + (int) g.pacman.x + "," + (int) g.pacman.y);
                    System.exit(1);
                }
                for (Ghost gh : g.ghosts) {
                    if (g.maze.isWall(gh.tileX(), gh.tileY())) {
                        System.out.println("FAIL: " + gh.kind + " inside wall tile at "
                                + (int) gh.x + "," + (int) gh.y + " state=" + gh.state);
                        System.exit(1);
                    }
                }
            }

            if (g.state == Game.State.GAME_OVER) {
                break;
            }
        }

        accepted = g.score;
        System.out.println("frames=" + frames
                + " state=" + g.state
                + " level=" + g.level
                + " lives=" + g.lives
                + " score=" + g.score
                + " pelletsLeft=" + g.maze.pelletsLeft()
                + " pelletsEaten=" + (g.maze.totalPellets() - g.maze.pelletsLeft()));

        if (g.state == Game.State.GAME_OVER) {
            System.out.println("NOTE: game ended (game over)");
        }

        boolean ok = accepted > 0;
        if (g.level > 1) {
            System.out.println("NOTE: progressed to level " + g.level);
        }
        if (frames < maxFrames && !ok) {
            System.out.println("FAIL: game halted early with no score");
            System.exit(1);
        }
        System.out.println("PASS (" + (ok ? "score earned" : "no crash, simulated full run") + ")");
    }

    /** Crude bot: heads toward the nearest remaining pellet following walkable tiles. */
    static Direction aim(Game g) {
        int px = g.pacman.tileX();
        int py = g.pacman.tileY();
        int tx = px, ty = py;
        int bestD = Integer.MAX_VALUE;
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                char ch = g.maze.tileAt(c, r);
                if (ch == Maze.PELLET || ch == Maze.POWER) {
                    int dc = c - px, dr = r - py;
                    int d = dc * dc + dr * dr;
                    if (d < bestD) {
                        bestD = d;
                        tx = c;
                        ty = r;
                    }
                }
            }
        }
        Direction chosen = Direction.NONE;
        int bD = bestD;
        for (Direction d : new Direction[]{Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT}) {
            if (!g.maze.canMove(px, py, d, false)) {
                continue;
            }
            int nc = Maze.wrapCol(px + d.dx);
            int nr = py + d.dy;
            int dc = nc - tx, dr = nr - ty;
            int dist = dc * dc + dr * dr;
            if (dist <= bD) {
                bD = dist;
                chosen = d;
            }
        }
        if (chosen == Direction.NONE) {
            chosen = Direction.RIGHT;
        }
        return chosen;
    }
}