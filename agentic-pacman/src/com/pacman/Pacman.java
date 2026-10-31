package com.pacman;

public class Pacman extends Entity {

    /** How many recent moves the memory keeps (the "context window"). */
    public static final int MEMORY = 10;

    /** Buffered direction requested by the player (applied at the next corner). */
    public Direction desired = Direction.NONE;
    public double animTime;

    /**
     * Phase 3: Pac-Man's short-term memory.  Every time he steps onto a new
     * tile the direction he used is pushed in; the ring buffer keeps only the
     * last {@link #MEMORY} moves, in O(1) time and space.
     */
    public final RingBuffer<Direction> history = new RingBuffer<>(MEMORY);

    /** Tile the memory last recorded, so we push exactly once per tile. */
    private int lastTileX = Integer.MIN_VALUE;
    private int lastTileY = Integer.MIN_VALUE;

    public Pacman(Maze maze, double tile) {
        super(maze, tile);
        speed = 128;
    }

    public void reset(double startX, double startY) {
        x = startX;
        y = startY;
        dir = Direction.NONE;
        desired = Direction.NONE;
        animTime = 0;
        history.clear();
        lastTileX = tileX();
        lastTileY = tileY();
    }

    public void update(double dt) {
        double step = speed * dt;
        animTime += dt;

        int col = tileX();
        int row = tileY();
        double ccx = centerX(col);
        double ccy = centerY(row);
        boolean centered = Math.abs(x - ccx) <= step + 0.01
                && Math.abs(y - ccy) <= step + 0.01;

        // Instant reversal is always allowed.
        if (desired != Direction.NONE && desired != dir && dir != Direction.NONE
                && desired == dir.opposite()) {
            dir = desired;
            desired = Direction.NONE;
        }

        // Apply a buffered turn once we are aligned with a tile centre.
        // Ignore a direction we are already moving in, otherwise holding a key
        // (or re-sending the same input each frame) would re-snap us to the
        // tile centre and freeze the character.
        if (desired != Direction.NONE && desired != dir && centered) {
            x = ccx;
            y = ccy;
            if (maze.canMove(col, row, desired, false)) {
                dir = desired;
                desired = Direction.NONE;
            }
        }

        if (dir == Direction.NONE) {
            return;
        }

        // Blocked by a wall: stay put but keep facing the wall.
        if (centered && !maze.canMove(col, row, dir, false)) {
            x = ccx;
            y = ccy;
            return;
        }

        x += dir.dx * step;
        y += dir.dy * step;
        wrapX();
        recordMove();
    }

    /** Pushes the current direction into the ring buffer when a new tile is entered. */
    private void recordMove() {
        int tx = tileX();
        int ty = tileY();
        if (tx != lastTileX || ty != lastTileY) {
            if (dir != Direction.NONE) {
                history.push(dir);
            }
            lastTileX = tx;
            lastTileY = ty;
        }
    }

    /** True when at least {@code n} moves are remembered. */
    public boolean hasMemory(int n) {
        return history.size() >= n;
    }

    /** The direction Pac-Man used most often recently (null when empty). */
    public Direction dominantDirection() {
        return history.dominant();
    }

    public boolean moving() {
        return dir != Direction.NONE;
    }
}
