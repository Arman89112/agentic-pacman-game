package com.pacman;

/**
 * Holds the static level layout and the mutable pellet state.
 * Legend: '#' wall, '.' pellet, 'o' power pellet, '-' ghost-house door, ' ' empty.
 */
public class Maze {

    public static final int COLS = 28;
    public static final int ROWS = 31;

    public static final char WALL = '#';
    public static final char PELLET = '.';
    public static final char POWER = 'o';
    public static final char DOOR = '-';
    public static final char EMPTY = ' ';

    private static final String[] LAYOUT = {
        "############################",
        "#............##............#",
        "#.####.#####.##.#####.####.#",
        "#o####.#####.##.#####.####o#",
        "#.####.#####.##.#####.####.#",
        "#..........................#",
        "#.####.##.########.##.####.#",
        "#.####.##.########.##.####.#",
        "#......##....##....##......#",
        "######.#####.##.#####.######",
        "######.#####.##.#####.######",
        "######.##..........##.######",
        "######.##.###--###.##.######",
        "######.##.#      #.##.######",
        "..........#      #..........",
        "######.##.#      #.##.######",
        "######.##.########.##.######",
        "######.##..........##.######",
        "######.##.########.##.######",
        "######.##.########.##.######",
        "#............##............#",
        "#.####.#####.##.#####.####.#",
        "#.####.#####.##.#####.####.#",
        "#o..##.......  .......##..o#",
        "###.##.##.########.##.##.###",
        "###.##.##.########.##.##.###",
        "#......##....##....##......#",
        "#.##########.##.##########.#",
        "#.##########.##.##########.#",
        "#..........................#",
        "############################",
    };

    private final char[][] tiles = new char[ROWS][COLS];
    private int pelletsLeft;
    private int totalPellets;

    public Maze() {
        reset();
    }

    public void reset() {
        pelletsLeft = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = LAYOUT[r].charAt(c);
                tiles[r][c] = ch;
                if (ch == PELLET || ch == POWER) {
                    pelletsLeft++;
                }
            }
        }
        totalPellets = pelletsLeft;
    }

    public static int wrapCol(int c) {
        int m = c % COLS;
        return m < 0 ? m + COLS : m;
    }

    public char tileAt(int c, int r) {
        if (r < 0 || r >= ROWS) {
            return WALL;
        }
        return tiles[r][wrapCol(c)];
    }

    public boolean isWall(int c, int r) {
        return tileAt(c, r) == WALL;
    }

    public boolean isDoor(int c, int r) {
        return tileAt(c, r) == DOOR;
    }

    /**
     * Can an entity on tile (c,r) move one tile in {@code dir}?
     * Doors block Pac-Man but let ghosts pass when {@code canUseDoor} is true.
     */
    public boolean canMove(int c, int r, Direction dir, boolean canUseDoor) {
        if (dir == Direction.NONE) {
            return false;
        }
        int tc = wrapCol(c + dir.dx);
        int tr = r + dir.dy;
        if (tr < 0 || tr >= ROWS) {
            return false;
        }
        char ch = tiles[tr][tc];
        if (ch == WALL) {
            return false;
        }
        if (ch == DOOR && !canUseDoor) {
            return false;
        }
        return true;
    }

    /** Consumes whatever is on the tile; returns 0 none, 1 pellet, 2 power pellet. */
    public int eat(int c, int r) {
        if (r < 0 || r >= ROWS) {
            return 0;
        }
        c = wrapCol(c);
        char ch = tiles[r][c];
        if (ch == PELLET) {
            tiles[r][c] = EMPTY;
            pelletsLeft--;
            return 1;
        }
        if (ch == POWER) {
            tiles[r][c] = EMPTY;
            pelletsLeft--;
            return 2;
        }
        return 0;
    }

    public int pelletsLeft() {
        return pelletsLeft;
    }

    public int totalPellets() {
        return totalPellets;
    }
}
