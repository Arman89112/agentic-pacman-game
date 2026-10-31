package com.pacman;

/**
 * A movable object positioned by the pixel coordinates of its centre.
 * Movement is axis-aligned and always snapped to tile centres on the
 * perpendicular axis, which keeps the grid navigation simple and robust.
 */
public abstract class Entity {

    public double x;
    public double y;
    public Direction dir = Direction.NONE;
    public double speed;          // pixels per second

    protected final Maze maze;
    protected final double tile;

    protected Entity(Maze maze, double tile) {
        this.maze = maze;
        this.tile = tile;
    }

    public int tileX() {
        return (int) Math.floor(x / tile);
    }

    public int tileY() {
        return (int) Math.floor(y / tile);
    }

    protected double centerX(int c) {
        return c * tile + tile / 2.0;
    }

    protected double centerY(int r) {
        return r * tile + tile / 2.0;
    }

    protected double boardWidth() {
        return Maze.COLS * tile;
    }

    protected void wrapX() {
        double w = boardWidth();
        if (x < 0) {
            x += w;
        } else if (x >= w) {
            x -= w;
        }
    }

    /** True when the entity is close enough to a tile centre to change direction. */
    protected boolean atCenter(double step) {
        int c = tileX();
        int r = tileY();
        return Math.abs(x - centerX(c)) <= step + 0.01
                && Math.abs(y - centerY(r)) <= step + 0.01;
    }

    protected void snapToCenter() {
        x = centerX(tileX());
        y = centerY(tileY());
    }
}
