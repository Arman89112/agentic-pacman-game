import com.pacman.Direction;
import com.pacman.Game;

public class TestDebug {
    public static void main(String[] args) {
        Game g = new Game();
        g.startNewGame();
        double dt = 1.0 / 60.0;
        Direction lastAim = Direction.NONE;
        for (int f = 1; f <= 400; f++) {
            if (g.state == Game.State.PLAYING) {
                lastAim = TestSim.aim(g);
                g.requestDirection(lastAim);
            }
            g.update(dt);
            if (g.state == Game.State.PLAYING && f % 20 == 0) {
                System.out.printf("f=%3d px=%5.1f py=%5.1f tile=%d,%d dir=%s desired=%s aim=%s state1=%s score=%d pellets=%d%n",
                        f, g.pacman.x, g.pacman.y, g.pacman.tileX(), g.pacman.tileY(),
                        g.pacman.dir, g.pacman.desired, lastAim, g.state, g.score, g.maze.pelletsLeft());
            }
        }
    }
}