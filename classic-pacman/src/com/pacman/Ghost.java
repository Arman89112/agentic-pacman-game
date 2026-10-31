package com.pacman;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghost extends Entity {

    public enum Kind { BLINKY, PINKY, INKY, CLYDE }

    public enum State { HOUSE, LEAVING, NORMAL, FRIGHTENED, EATEN, ENTERING }

    private static final Direction[] ORDER = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };

    /** How many tiles ahead the debug overlay traces the greedy path. */
    private static final int LOOKAHEAD = 14;

    public final Kind kind;
    public State state = State.NORMAL;

    // ----- read-only debug overlay data (populated only while G is held) -----
    /** Next tiles the classic greedy AI will walk, as {col,row} pairs. */
    public List<int[]> plannedPath = new ArrayList<>();
    /** Tile the AI is currently steering towards; -1 while inactive. */
    public int targetC = -1;
    public int targetR = -1;

    public double houseTimer;
    public double baseSpeed = 108;

    public int scatterX;
    public int scatterY;

    public double homeX;
    public double homeY;

    private double bobDir = 1;
    private boolean justDecided;
    private final Random rng = new Random();

    public Ghost(Maze maze, double tile, Kind kind) {
        super(maze, tile);
        this.kind = kind;
    }

    public void reset(double startX, double startY, State startState, double release) {
        x = startX;
        y = startY;
        homeX = startX;
        homeY = startY;
        state = startState;
        houseTimer = release;
        dir = Direction.LEFT;
        bobDir = 1;
        plannedPath = new ArrayList<>();
        targetC = -1;
        targetR = -1;
    }

    // ------------------------------------------------------------------
    // debug overlay: trace what the classic 1980 AI will do next
    // ------------------------------------------------------------------

    /**
     * Simulates the ghost's own greedy targeting a few tiles ahead and stores
     * the tiles it would visit.  This is purely a visualisation of the classic
     * 1980 rule set the ghost already follows; it never steers the ghost.
     */
    public void computePlannedPath(Game game) {
        plannedPath = new ArrayList<>();
        targetC = -1;
        targetR = -1;
        if (state != State.NORMAL && state != State.EATEN) {
            return;   // house / leaving / frightened move by their own rules
        }
        int[] target = game.targetFor(this, tileX(), tileY());
        targetC = Maze.wrapCol(target[0]);
        targetR = target[1];

        int c = tileX();
        int r = tileY();
        Direction cur = (dir == Direction.NONE) ? Direction.LEFT : dir;
        for (int i = 0; i < LOOKAHEAD; i++) {
            Direction next = classicChoice(c, r, cur, target);
            if (next == Direction.NONE) {
                break;
            }
            c = Maze.wrapCol(c + next.dx);
            r += next.dy;
            plannedPath.add(new int[]{c, r});
            cur = next;
            if (c == targetC && r == targetR) {
                break;
            }
        }
    }

    /**
     * One no-reversal, minimum-distance decision - exactly the rule a 1980
     * ghost applies at a tile centre (ties resolved UP, LEFT, DOWN, RIGHT).
     */
    private Direction classicChoice(int c, int r, Direction cur, int[] target) {
        Direction reverse = cur.opposite();
        Direction best = Direction.NONE;
        double bestDist = Double.MAX_VALUE;
        for (Direction d : ORDER) {
            if (d == reverse || !maze.canMove(c, r, d, false)) {
                continue;
            }
            int nc = Maze.wrapCol(c + d.dx);
            int nr = r + d.dy;
            double ddx = nc - target[0];
            double ddy = nr - target[1];
            double dist = ddx * ddx + ddy * ddy;
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }
        if (best == Direction.NONE) {
            best = reverse;   // dead end: the only legal move is back
        }
        return best;
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    public void update(double dt, Game game) {
        applySpeed();
        switch (state) {
            case HOUSE:
                updateInHouse(dt);
                break;
            case LEAVING:
                updateLeaving(dt);
                break;
            case ENTERING:
                updateEntering(dt);
                break;
            case EATEN:
                gridMove(dt, game, true);
                break;
            default:
                gridMove(dt, game, false);
                break;
        }
    }

    private void applySpeed() {
        switch (state) {
            case FRIGHTENED:
                speed = baseSpeed * 0.58;
                break;
            case EATEN:
                speed = baseSpeed * 1.9;
                break;
            case HOUSE:
            case LEAVING:
            case ENTERING:
                speed = baseSpeed * 0.55;
                break;
            default:
                speed = baseSpeed;
        }
    }

    private void updateInHouse(double dt) {
        houseTimer -= dt;
        if (houseTimer <= 0) {
            state = State.LEAVING;
            return;
        }
        y += bobDir * 22 * dt;
        if (y > homeY + tile * 0.28) {
            y = homeY + tile * 0.28;
            bobDir = -1;
        } else if (y < homeY - tile * 0.28) {
            y = homeY - tile * 0.28;
            bobDir = 1;
        }
    }

    private void updateLeaving(double dt) {
        double step = speed * dt;
        double exitX = centerX(13);
        if (Math.abs(x - exitX) > step) {
            x += Math.signum(exitX - x) * step;
            return;
        }
        x = exitX;
        double exitY = centerY(11);
        if (y > exitY) {
            y -= step;
            if (y < exitY) {
                y = exitY;
            }
            return;
        }
        y = exitY;
        state = State.NORMAL;
        justDecided = false;
        dir = rng.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
    }

    private void updateEntering(double dt) {
        double step = speed * dt;
        double exitX = centerX(13);
        if (Math.abs(x - exitX) > step) {
            x += Math.signum(exitX - x) * step;
            return;
        }
        x = exitX;
        double targetY = centerY(14);
        if (y < targetY) {
            y += step;
            if (y > targetY) {
                y = targetY;
            }
            return;
        }
        y = targetY;
        state = State.HOUSE;
        houseTimer = 0.9;
        bobDir = 1;
        dir = Direction.UP;
    }

    private void gridMove(double dt, Game game, boolean canUseDoor) {
        double step = speed * dt;
        int col = tileX();
        int row = tileY();
        double ccx = centerX(col);
        double ccy = centerY(row);
        boolean centered = Math.abs(x - ccx) <= step + 0.01
                && Math.abs(y - ccy) <= step + 0.01;

        if (centered) {
            // Decide only once per tile-centre visit. Re-deciding (and re-snapping)
            // on frames while still inside the centre tolerance made ghosts jitter in
            // place at symmetric grid points.
            if (!justDecided) {
                snapToCenter();
                col = tileX();
                row = tileY();
                if (state == State.EATEN && col == 13 && row == 11) {
                    state = State.ENTERING;
                    justDecided = false;
                    return;
                }
                chooseDirection(game, col, row, canUseDoor);
                justDecided = true;
            }
        } else {
            justDecided = false;
        }

        if (dir == Direction.NONE) {
            return;
        }
        x += dir.dx * step;
        y += dir.dy * step;
        wrapX();
    }

    private void chooseDirection(Game game, int col, int row, boolean canUseDoor) {
        Direction reverse = dir.opposite();
        List<Direction> options = new ArrayList<>(4);
        for (Direction d : ORDER) {
            if (d == reverse) {
                continue;
            }
            if (maze.canMove(col, row, d, canUseDoor)) {
                options.add(d);
            }
        }
        if (options.isEmpty()) {
            dir = reverse;
            return;
        }
        if (state == State.FRIGHTENED) {
            dir = options.get(rng.nextInt(options.size()));
            return;
        }

        int[] target = game.targetFor(this, col, row);
        Direction best = options.get(0);
        double bestDist = Double.MAX_VALUE;
        for (Direction d : options) {
            int nc = Maze.wrapCol(col + d.dx);
            int nr = row + d.dy;
            double ddx = nc - target[0];
            double ddy = nr - target[1];
            double dist = ddx * ddx + ddy * ddy;
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }
        dir = best;
    }

    public void reverse() {
        if (dir != Direction.NONE) {
            dir = dir.opposite();
        }
        justDecided = false;
    }
}
