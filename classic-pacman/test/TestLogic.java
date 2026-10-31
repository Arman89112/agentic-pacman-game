import com.pacman.Direction;
import com.pacman.Game;
import com.pacman.Ghost;

public class TestLogic {

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
        testDeath();
        testEatGhost();
        testPowerPellet();
        testLevelComplete();
        testTunnelWrap();
        System.out.println("ALL LOGIC TESTS PASSED");
    }

    static void testDeath() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3); // past READY into PLAYING
        // pacman sits still; blinky should eventually reach it
        boolean lostLife = false;
        for (int i = 0; i < 60 * 45; i++) {
            g.update(DT);
            if (g.lives < 3) {
                lostLife = true;
                break;
            }
            if (g.state == Game.State.GAME_OVER) {
                break;
            }
        }
        check(lostLife, "testDeath: blinky should have caught a stationary pacman");
        System.out.println("  death OK (lives=" + g.lives + ", state=" + g.state + ", score=" + g.score + ")");
    }

    static void testEatGhost() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);
        Ghost blinky = g.ghosts.get(0);
        blinky.state = Ghost.State.FRIGHTENED;
        blinky.x = cx(12);
        blinky.y = cy(23);
        g.pacman.x = cx(12) + 2;
        g.pacman.y = cy(23);
        g.requestDirection(Direction.LEFT);
        int before = g.score;
        g.update(DT);
        check(blinky.state == Ghost.State.EATEN, "testEatGhost: ghost should change to EATEN");
        check(g.score >= before + 200, "testEatGhost: at least +200 points");
        check(g.ghostCombo == 1, "testEatGhost: combo counter=1");
        System.out.println("  eatGhost OK");
    }

    static void testPowerPellet() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);
        // place pacman on a power pellet at (1,3)
        g.pacman.x = cx(1);
        g.pacman.y = cy(3);
        g.pacman.dir = Direction.NONE;
        g.requestDirection(Direction.RIGHT);
        g.update(DT);
        check(g.frightTimer > 0, "testPowerPellet: fright timer started");
        boolean anyFright = false;
        for (Ghost gh : g.ghosts) {
            if (gh.state == Ghost.State.FRIGHTENED) {
                anyFright = true;
            }
        }
        check(anyFright, "testPowerPellet: at least one ghost frightened");
        System.out.println("  powerPellet OK (score=" + g.score + ")");
    }

    static void testLevelComplete() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);
        // eat every remaining pellet directly
        int guard = 0;
        while (g.maze.pelletsLeft() > 0 && guard++ < 10000) {
            outer:
            for (int r = 0; r < 31; r++) {
                for (int c = 0; c < 28; c++) {
                    if (g.maze.eat(c, r) != 0) {
                        break outer;
                    }
                }
            }
            g.update(DT);
        }
        check(g.state == Game.State.LEVEL_COMPLETE, "testLevelComplete: LEVEL_COMPLETE state");
        play(g, 4);
        check(g.level == 2, "testLevelComplete: advanced to level 2");
        check(g.state == Game.State.PLAYING || g.state == Game.State.READY,
                "testLevelComplete: playing again");
        System.out.println("  levelComplete OK");
    }

    static void testTunnelWrap() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);
        // put pacman in the left tunnel, moving left
        g.pacman.x = cx(1);
        g.pacman.y = cy(14);
        g.pacman.dir = Direction.LEFT;
        g.pacman.desired = Direction.LEFT;
        for (int i = 0; i < 120; i++) {
            g.update(DT);
        }
        check(g.pacman.x >= cx(14), "testTunnelWrap: pacman wrapped to the right side of the board (x=" + g.pacman.x + ")");
        check(g.pacman.tileY() == 14, "testTunnelWrap: stayed in tunnel row");
        System.out.println("  tunnelWrap OK (x=" + (int) g.pacman.x + ")");
    }

    static void check(boolean condition, String msg) {
        if (!condition) {
            System.out.println("FAIL: " + msg);
            System.exit(1);
        }
    }
}