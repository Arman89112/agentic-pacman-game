import com.pacman.Direction;
import com.pacman.Game;
import com.pacman.Ghost;

public class TestCycle {

    static final double T = Game.TILE;
    static final double DT = 1.0 / 60.0;

    static double cx(int c) { return c * T + T / 2; }
    static double cy(int r) { return r * T + T / 2; }

    static void play(Game g, double seconds) {
        for (int i = 0; i < (int) (seconds / DT); i++) {
            g.update(DT);
        }
    }

    public static void main(String[] args) {
        testGhostReturn();
        testGameOver();
        System.out.println("CYCLE TESTS PASSED");
    }

    static void check(boolean condition, String msg) {
        if (!condition) {
            System.out.println("FAIL: " + msg);
            System.exit(1);
        }
    }

    static void testGhostReturn() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);
        Ghost blinky = g.ghosts.get(0);
        int px = g.pacman.tileX(); // (13,23)
        blinky.state = Ghost.State.FRIGHTENED;
        blinky.x = cx(px) + 2;
        blinky.y = cy(23);
        blinky.dir = Direction.LEFT;
        g.pacman.dir = Direction.NONE;
        g.requestDirection(Direction.NONE);
        g.update(DT);
        check(blinky.state == Ghost.State.EATEN, "should be eaten first");

        boolean sawEntering = false, sawHouse = false, sawLeaving = false;
        boolean backOut = false;
        for (int i = 0; i < 60 * 20; i++) {
            g.update(DT);
            if (blinky.state == Ghost.State.ENTERING) sawEntering = true;
            if (blinky.state == Ghost.State.HOUSE) sawHouse = true;
            if (blinky.state == Ghost.State.LEAVING) sawLeaving = true;
            if (blinky.state == Ghost.State.NORMAL && sawHouse && blinky.tileY() <= 11) {
                backOut = true;
                break;
            }
        }
        check(sawEntering, "ghost entered the house");
        check(sawHouse, "ghost reached the house");
        check(backOut, "ghost came back out and moves again");
        System.out.println("  ghostReturn OK");
    }

    static void testGameOver() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);
        for (int i = 0; i < 60 * 180; i++) {
            g.update(DT); // pacman stays put
            if (g.state == Game.State.GAME_OVER) {
                break;
            }
        }
        check(g.state == Game.State.GAME_OVER, "game should end in GAME_OVER");
        check(g.highScore >= g.score, "high score tracked");
        System.out.println("  gameOver OK (lives=" + g.lives + ", score=" + g.score + ")");
    }
}