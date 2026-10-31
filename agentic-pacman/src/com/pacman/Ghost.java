package com.pacman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ghost extends Entity {

    public enum Kind { BLINKY, PINKY, INKY, CLYDE }

    public enum State { HOUSE, LEAVING, NORMAL, FRIGHTENED, EATEN, ENTERING }

    /**
     * Which agentic brain drives this ghost once it is out of the house.
     * <ul>
     *   <li>{@code BOSS} - Blinky, minimax search tree (phase 4)</li>
     *   <li>{@code A_STAR} - Pinky, A* shortest path on the maze graph (phase 2)</li>
     *   <li>{@code TRACKER} - Inky, reads Pac-Man's ring buffer and intercepts
     *       him ahead of an intersection (phase 3)</li>
     *   <li>{@code DIJKSTRA} - Clyde, Dijkstra shortest path (phase 2)</li>
     *   <li>{@code HUNTER} - the solo opponent: never scatters, always runs the
     *       minimax search on Pac-Man's predicted node, then commits to the
     *       full shortest path it computed.</li>
     * </ul>
     */
    public enum Brain { BOSS, A_STAR, TRACKER, DIJKSTRA, HUNTER }

    private static final Direction[] ORDER = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };

    /** How far ahead the tracker looks along Pac-Man's dominant direction. */
    private static final int TRACK_LOOKAHEAD = 10;

    public final Kind kind;
    public Brain brain;
    /** False when this ghost is parked because a different roster is active. */
    public boolean active = true;
    public State state = State.NORMAL;

    public double houseTimer;
    public double baseSpeed = 108;

    public int scatterX;
    public int scatterY;

    public double homeX;
    public double homeY;

    // ----- agentic bookkeeping, read by the debug overlay -----
    /** Current planned path of graph nodes (empty when not pathing). */
    public List<Integer> plannedPath = Collections.emptyList();
    /** Node the brain is steering towards. */
    public int targetNode = -1;
    /** Node the minimax tree picked for this decision (boss only). */
    public int bossPick = -1;
    /** Predicted interception node (tracker only). */
    public int interceptNode = -1;

    private double bobDir = 1;
    private boolean justDecided;
    private final Random rng = new Random();

    public Ghost(Maze maze, double tile, Kind kind) {
        super(maze, tile);
        this.kind = kind;
        this.brain = brainFor(kind);
    }

    /** Swaps the brain at runtime (used to promote Blinky to {@code HUNTER}). */
    public void setBrain(Brain brain) {
        this.brain = brain;
    }

    /** Each classic ghost gets a different artificial-intelligence method. */
    private static Brain brainFor(Kind kind) {
        switch (kind) {
            case BLINKY: return Brain.BOSS;
            case PINKY:  return Brain.A_STAR;
            case INKY:   return Brain.TRACKER;
            case CLYDE:
            default:     return Brain.DIJKSTRA;
        }
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
        plannedPath = Collections.emptyList();
        targetNode = -1;
        bossPick = -1;
        interceptNode = -1;
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

    // ------------------------------------------------------------------
    // decision making
    // ------------------------------------------------------------------

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

        // Frightened ghosts stumble around at random (classic behaviour).
        if (state == State.FRIGHTENED) {
            dir = options.get(rng.nextInt(options.size()));
            return;
        }

        // "Eaten" eyes may pass the door and fly home with the classic chase.
        if (canUseDoor) {
            dir = classicStep(game, col, row, options);
            return;
        }

        // -------- agentic brains --------
        MazeGraph graph = game.graph;
        int here = graph.nodeAt(col, row);
        int target = chooseTarget(game, graph, col, row);
        targetNode = target;

        if (here < 0) {
            // Middle of a corridor: the only non-reverse move is straight ahead.
            // (The previously planned path is kept for the debug overlay.)
            dir = options.size() == 1 ? options.get(0)
                                      : bestToward(options, game, col, row, target);
            bossPick = -1;
            return;
        }

        if (target < 0 || target == here) {
            // Already standing on the goal node: shadow Pac-Man directly.
            plannedPath = Collections.emptyList();
            bossPick = -1;
            dir = chaseStep(game, col, row, options);
            return;
        }

        // The boss ghost (and the solo hunter, which never scatters) reasons a
        // few plies ahead with the minimax search tree and then commits to the
        // full shortest-path continuation, so its motion reads as a plan rather
        // than a greedy twitch.
        boolean bossSearch = (brain == Brain.BOSS && game.globalMode == Game.Mode.CHASE)
                || brain == Brain.HUNTER;
        if (bossSearch) {
            int next = game.searchTree.chooseMove(here, target, reverse);
            bossPick = next;
            if (next >= 0) {
                List<Integer> route = new ArrayList<>();
                route.add(here);
                List<Integer> tail = graph.aStar(next, target);
                if (tail.size() >= 2) {
                    route.addAll(tail);
                } else {
                    route.add(next);
                }
                plannedPath = route;
                dir = graph.dirBetween(here, next);
                return;
            }
            // otherwise fall through to a normal shortest-path search
        }

        List<Integer> path = (brain == Brain.DIJKSTRA)
                ? graph.dijkstra(here, target)
                : graph.aStar(here, target);

        if (path.size() < 2) {
            plannedPath = Collections.emptyList();
            bossPick = -1;
            dir = classicStep(game, col, row, options);
            return;
        }
        plannedPath = path;
        bossPick = -1;
        dir = graph.dirBetween(here, path.get(1));
    }

    /**
     * Chooses the node this ghost wants to reach, according to its brain.
     * In scatter mode every ghost heads for its own corner, exactly like the
     * arcade original.
     */
    private int chooseTarget(Game game, MazeGraph graph, int col, int row) {
        switch (brain) {
            case BOSS:
            case HUNTER:
                if (game.globalMode == Game.Mode.SCATTER && brain != Brain.HUNTER) {
                    return graph.nearestNode(scatterX, scatterY);
                }
                return graph.nodeAhead(game.pacman.tileX(), game.pacman.tileY(),
                        game.pacman.dir);
            case TRACKER:
                if (game.globalMode == Game.Mode.SCATTER) {
                    interceptNode = -1;
                    return graph.nearestNode(scatterX, scatterY);
                }
                interceptNode = trackerIntercept(game, graph);
                return interceptNode;
            case A_STAR:
            case DIJKSTRA:
            default:
                interceptNode = -1;
                int[] tile = game.targetFor(this, col, row);
                return graph.nearestNode(tile[0], tile[1]);
        }
    }

    /**
     * Phase 3: read Pac-Man's ring buffer, find the direction he has favoured
     * lately and predict which intersection he is rolling towards.  The ghost
     * then paths to that intersection (a flanking move) instead of following.
     */
    public int trackerIntercept(Game game, MazeGraph graph) {
        Pacman p = game.pacman;
        int px = p.tileX();
        int py = p.tileY();
        if (!p.hasMemory(3)) {
            return graph.nodeAhead(px, py, p.dir);
        }
        Direction trend = p.dominantDirection();
        if (trend == null || trend == Direction.NONE) {
            return graph.nodeAhead(px, py, p.dir);
        }
        int c = px;
        int r = py;
        for (int i = 0; i < TRACK_LOOKAHEAD; i++) {
            if (!maze.canMove(c, r, trend, false)) {
                break;
            }
            c = Maze.wrapCol(c + trend.dx);
            r += trend.dy;
            int node = graph.nodeAt(c, r);
            if (node >= 0) {
                return node;
            }
        }
        return graph.nearestNode(c, r);
    }

    // ------------------------------------------------------------------
    // movement primitives
    // ------------------------------------------------------------------

    /**
     * Classic greedy targeting: pick the legal non-reverse direction whose tile
     * centre is closest to the ghost's arcade target.  Used for frightened
     * randomness' sibling (eaten eyes) and as a fallback.
     */
    private Direction classicStep(Game game, int col, int row, List<Direction> options) {
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
        return best;
    }

    /**
     * Once a ghost has reached the node it was heading for, it stops planning
     * and simply shadows Pac-Man tile-by-tile.  Reversal is allowed here (that
     * is how the ghost turns around when it overshoots), with Pac-Man's own
     * heading used as a tie-break.
     */
    private Direction chaseStep(Game game, int col, int row, List<Direction> options) {
        int px = game.pacman.tileX();
        int py = game.pacman.tileY();
        Direction pacDir = game.pacman.dir;
        Direction best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction d : ORDER) {          // ORDER includes the reverse direction
            if (!maze.canMove(col, row, d, false)) {
                continue;
            }
            int nc = Maze.wrapCol(col + d.dx);
            int nr = row + d.dy;
            double dist = (nc - px) * (nc - px) + (nr - py) * (nr - py);
            if (dist < bestDist - 1e-9) {
                bestDist = dist;
                best = d;
            }
        }
        if (best == null) {
            return options.isEmpty() ? dir.opposite() : options.get(0);
        }
        // Tie-break: if following Pac-Man's own heading is just as short, do that.
        if (pacDir != Direction.NONE && maze.canMove(col, row, pacDir, false)) {
            int nc = Maze.wrapCol(col + pacDir.dx);
            int nr = row + pacDir.dy;
            double dist = (nc - px) * (nc - px) + (nr - py) * (nr - py);
            if (Math.abs(dist - bestDist) < 1e-9) {
                return pacDir;
            }
        }
        return best;
    }

    /** Best direction towards a target node when standing mid-corridor. */
    private Direction bestToward(List<Direction> options, Game game, int col, int row, int target) {
        if (target < 0) {
            return options.get(0);
        }
        int tc = game.graph.col(target);
        int tr = game.graph.row(target);
        Direction best = options.get(0);
        double bestDist = Double.MAX_VALUE;
        for (Direction d : options) {
            int nc = Maze.wrapCol(col + d.dx);
            int nr = row + d.dy;
            double dist = (nc - tc) * (nc - tc) + (nr - tr) * (nr - tr);
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }
        return best;
    }

    public void reverse() {
        if (dir != Direction.NONE) {
            dir = dir.opposite();
        }
        justDecided = false;
    }
}
