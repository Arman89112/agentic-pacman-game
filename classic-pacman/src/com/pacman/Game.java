package com.pacman;

import java.util.ArrayList;
import java.util.List;

public class Game {

    public static final double TILE = 20;

    public enum State { TITLE, READY, PLAYING, PAUSED, DYING, LEVEL_COMPLETE, GAME_OVER }

    public enum Mode { SCATTER, CHASE }

    public final Maze maze = new Maze();
    public final Pacman pacman;
    public final List<Ghost> ghosts = new ArrayList<>();
    public final SoundManager sound = new SoundManager();

    public State state = State.TITLE;
    public Mode globalMode = Mode.SCATTER;

    /** Debug overlay toggle (key G): traces each ghost's classic path. */
    public boolean showGraph;

    public int score;
    public int highScore;
    public int lives = 3;
    public int level = 1;

    public double time;
    public double stateTimer;
    public double frightTimer;
    public int ghostCombo;

    private double modeTimer;
    private int modeIndex;

    private static final Mode[] MODE_KIND = {
        Mode.SCATTER, Mode.CHASE, Mode.SCATTER, Mode.CHASE,
        Mode.SCATTER, Mode.CHASE, Mode.SCATTER, Mode.CHASE
    };
    private static final double[] MODE_DURATION = {
        7, 20, 7, 20, 5, 20, 5, Double.MAX_VALUE
    };

    public Game() {
        pacman = new Pacman(maze, TILE);
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.BLINKY));
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.PINKY));
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.INKY));
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.CLYDE));
        ghosts.get(0).scatterX = 25; ghosts.get(0).scatterY = 0;
        ghosts.get(1).scatterX = 2;  ghosts.get(1).scatterY = 0;
        ghosts.get(2).scatterX = 27; ghosts.get(2).scatterY = 30;
        ghosts.get(3).scatterX = 0;  ghosts.get(3).scatterY = 30;
        resetActors();
    }

    private static double center(int i) {
        return i * TILE + TILE / 2.0;
    }

    // ------------------------------------------------------------------
    // level / life management
    // ------------------------------------------------------------------

    public void startNewGame() {
        score = 0;
        lives = 3;
        level = 1;
        maze.reset();
        resetActors();
        state = State.READY;
        stateTimer = 2.2;
    }

    public void nextLevel() {
        level++;
        maze.reset();
        resetActors();
        state = State.READY;
        stateTimer = 2.0;
    }

    private void resetActors() {
        pacman.reset(center(13), center(23));
        pacman.speed = 128 + Math.min(level - 1, 3) * 2;

        ghosts.get(0).reset(center(13), center(11), Ghost.State.NORMAL, 0);
        ghosts.get(1).reset(center(13), center(14), Ghost.State.HOUSE, 2.0);
        ghosts.get(2).reset(center(11), center(14), Ghost.State.HOUSE, 6.0);
        ghosts.get(3).reset(center(15), center(14), Ghost.State.HOUSE, 10.0);

        double ghostSpeed = 104 + Math.min(level - 1, 8) * 3;
        for (Ghost g : ghosts) {
            g.baseSpeed = ghostSpeed;
        }

        globalMode = Mode.SCATTER;
        modeIndex = 0;
        modeTimer = MODE_DURATION[0];
        frightTimer = 0;
        ghostCombo = 0;
    }

    private void die() {
        lives--;
        state = State.DYING;
        stateTimer = 1.8;
        sound.death();
    }

    // ------------------------------------------------------------------
    // input
    // ------------------------------------------------------------------

    public void requestDirection(Direction d) {
        if (state == State.PLAYING || state == State.READY) {
            pacman.desired = d;
        }
    }

    public void primaryAction() {
        switch (state) {
            case TITLE:
            case GAME_OVER:
                startNewGame();
                break;
            case PAUSED:
                state = State.PLAYING;
                break;
            case PLAYING:
                state = State.PAUSED;
                break;
            default:
                break;
        }
    }

    public void togglePause() {
        if (state == State.PLAYING) {
            state = State.PAUSED;
        } else if (state == State.PAUSED) {
            state = State.PLAYING;
        }
    }

    public void toggleMute() {
        sound.toggle();
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    public void update(double dt) {
        time += dt;
        switch (state) {
            case READY:
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    state = State.PLAYING;
                }
                break;
            case PLAYING:
                updatePlaying(dt);
                break;
            case DYING:
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    if (lives <= 0) {
                        state = State.GAME_OVER;
                        if (score > highScore) {
                            highScore = score;
                        }
                    } else {
                        resetActors();
                        state = State.READY;
                        stateTimer = 1.5;
                    }
                }
                break;
            case LEVEL_COMPLETE:
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    nextLevel();
                }
                break;
            default:
                break;
        }
    }

    private void updatePlaying(double dt) {
        if (frightTimer > 0) {
            frightTimer -= dt;
            if (frightTimer <= 0) {
                frightTimer = 0;
                for (Ghost g : ghosts) {
                    if (g.state == Ghost.State.FRIGHTENED) {
                        g.state = Ghost.State.NORMAL;
                    }
                }
            }
        } else {
            modeTimer -= dt;
            if (modeTimer <= 0) {
                advanceMode();
            }
        }

        pacman.update(dt);
        eatAtPacman();

        for (Ghost g : ghosts) {
            g.update(dt, this);
        }

        if (showGraph) {
            for (Ghost g : ghosts) {
                g.computePlannedPath(this);
            }
        }

        checkCollisions();

        if (maze.pelletsLeft() == 0) {
            state = State.LEVEL_COMPLETE;
            stateTimer = 2.4;
            sound.levelUp();
        }
    }

    private void eatAtPacman() {
        int e = maze.eat(pacman.tileX(), pacman.tileY());
        if (e == 1) {
            score += 10;
            sound.chomp();
        } else if (e == 2) {
            score += 50;
            startFright();
            sound.power();
        }
    }

    private void startFright() {
        ghostCombo = 0;
        frightTimer = Math.max(2.0, 8.0 - (level - 1) * 0.5);
        for (Ghost g : ghosts) {
            if (g.state == Ghost.State.NORMAL) {
                g.state = Ghost.State.FRIGHTENED;
                g.reverse();
            }
        }
    }

    private void advanceMode() {
        if (modeIndex < MODE_KIND.length - 1) {
            modeIndex++;
        }
        globalMode = MODE_KIND[modeIndex];
        modeTimer = MODE_DURATION[modeIndex];
        for (Ghost g : ghosts) {
            if (g.state == Ghost.State.NORMAL) {
                g.reverse();
            }
        }
    }

    private void checkCollisions() {
        double hit = TILE * 0.65;
        double hit2 = hit * hit;
        for (Ghost g : ghosts) {
            if (g.state == Ghost.State.EATEN
                    || g.state == Ghost.State.ENTERING
                    || g.state == Ghost.State.HOUSE) {
                continue;
            }
            double dx = pacman.x - g.x;
            double dy = pacman.y - g.y;
            if (dx * dx + dy * dy < hit2) {
                if (g.state == Ghost.State.FRIGHTENED) {
                    g.state = Ghost.State.EATEN;
                    ghostCombo++;
                    int pts = 200 * (1 << (ghostCombo - 1));
                    score += pts;
                    sound.eatGhost();
                } else if (g.state == Ghost.State.NORMAL) {
                    die();
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // ghost targeting
    // ------------------------------------------------------------------

    public int[] targetFor(Ghost g, int col, int row) {
        if (g.state == Ghost.State.EATEN) {
            return new int[]{13, 11};
        }
        if (globalMode == Mode.SCATTER) {
            return new int[]{g.scatterX, g.scatterY};
        }

        int px = pacman.tileX();
        int py = pacman.tileY();
        int pdx = pacman.dir.dx;
        int pdy = pacman.dir.dy;

        switch (g.kind) {
            case BLINKY:
                return new int[]{px, py};
            case PINKY:
                return new int[]{px + pdx * 4, py + pdy * 4};
            case INKY: {
                Ghost blinky = ghosts.get(0);
                int ax = px + pdx * 2;
                int ay = py + pdy * 2;
                return new int[]{ax * 2 - blinky.tileX(), ay * 2 - blinky.tileY()};
            }
            case CLYDE:
            default: {
                double dist = Math.hypot(px - col, py - row);
                if (dist > 8) {
                    return new int[]{px, py};
                }
                return new int[]{g.scatterX, g.scatterY};
            }
        }
    }

    public boolean isFrightBlink() {
        return frightTimer > 0 && frightTimer < 2.0 && ((int) (frightTimer * 6)) % 2 == 0;
    }
}
