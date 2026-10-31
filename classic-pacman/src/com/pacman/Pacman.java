package com.pacman;

public class Pacman extends Entity {

    /** Buffered direction requested by the player (applied at the next corner). */
    public Direction desired = Direction.NONE;
    public double animTime;

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
    }

    public boolean moving() {
        return dir != Direction.NONE;
    }
}
