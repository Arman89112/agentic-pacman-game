import com.pacman.Direction;
import com.pacman.Game;
import com.pacman.Ghost;

public class TraceReturn {
    static final double T = Game.TILE;
    static double cx(int c) { return c * T + T / 2; }
    static double cy(int r) { return r * T + T / 2; }

    public static void main(String[] args) {
        Game g = new Game();
        g.startNewGame();
        double dt = 1.0 / 60.0;
        for (int i = 0; i < 2.3 / dt; i++) g.update(dt);
        Ghost b = g.ghosts.get(0);
        b.state = Ghost.State.FRIGHTENED;
        b.x = cx(13);
        b.y = cy(23) + 4;
        g.pacman.dir = Direction.NONE;
        g.requestDirection(Direction.NONE);
        for (int i = 0; i < 60 * 20; i++) {
            g.update(dt);
            if (i % 30 == 0) {
                System.out.printf("t=%4.1fs state=%-10s tile=(%2d,%2d) x=%6.1f y=%6.1f dir=%-5s score=%d%n",
                        i / 60.0, b.state, b.tileX(), b.tileY(), b.x, b.y, b.dir, g.score);
            }
            if (b.state == Ghost.State.NORMAL && i > 60 * 12) break;
        }
    }
}