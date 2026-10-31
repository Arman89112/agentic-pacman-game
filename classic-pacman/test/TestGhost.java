import com.pacman.Direction;
import com.pacman.Game;
import com.pacman.Ghost;
import com.pacman.Maze;

public class TestGhost {
    public static void main(String[] args) {
        Game g = new Game();
        g.startNewGame();
        double dt = 1.0 / 60.0;
        for (int i = 0; i < 2.3 / dt; i++) g.update(dt);

        for (int i = 0; i < 60 * 45; i++) {
            g.update(dt);
            if (i % 300 == 0) {
                Ghost b = g.ghosts.get(0);
                System.out.printf("t=%4.1fs mode=%s fright=%4.1f blinky=(%4.1f,%4.1f) tile=(%d,%d) state=%s pac=(%4.1f,%4.1f) lives=%d%n",
                        i * dt, g.globalMode, g.frightTimer, b.x, b.y, b.tileX(), b.tileY(), b.state, g.pacman.x, g.pacman.y, g.lives);
            }
            if (g.lives < 3) {
                System.out.println("DEATH at t=" + i * dt);
                break;
            }
        }
    }
}