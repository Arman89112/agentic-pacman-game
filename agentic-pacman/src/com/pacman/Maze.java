package com.pacman;

/**
 * Holds the static level layout and the mutable pellet state.
 * Legend: '#' wall, '.' pellet, 'o' power pellet, '-' ghost-house door, ' ' empty.
 *
 * <p>Five levels are shipped.  Every layout keeps the identical ghost-house
 * block (rows 12-16), the horizontal warp tunnel (row 14) and Pac-Man's spawn
 * tile, so the ghost/AI code only ever needs to reason about one graph shape at
 * a time.  Each successive level packs the outer thirds with more walls and
 * longer, loopier routes, which makes pathing harder as the level rises.
 *
 * <p>Phase 6 lives here: at the start the pellets are stored inside a dense
 * {@code char[31][28]} grid.  Once 80 % of them have been eaten, {@link
 * #maybeCompress()} serialises the handful of survivors into a {@link
 * SparseMatrix} (COO) and throws the big grid away.
 */
public class Maze {

    public static final int COLS = 28;
    public static final int ROWS = 31;

    public static final char WALL = '#';
    public static final char PELLET = '.';
    public static final char POWER = 'o';
    public static final char DOOR = '-';
    public static final char EMPTY = ' ';

    // ------------------------------------------------------------------
    // level layouts (28 columns x 31 rows each)
    // ------------------------------------------------------------------

    private static final String[] LEVEL_1 = {
        "############################",
        "#..........................#",
        "#.####.######..######.####.#",
        "#o#  #.##          ##.#  #o#",
        "#.####.##.########.##.####.#",
        "#.........##....##.........#",
        "#.#######.##.##.##.#######.#",
        "#.#######.##.##.##.#######.#",
        "#............##............#",
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
        "#.##########.##.##########.#",
        "#.#........#.##.#........#.#",
        "#o..##.......  .......##..o#",
        "###.##.#####.##.#####.##.###",
        "###.##.#####.##.#####.##.###",
        "#......##..........##......#",
        "#.#######.########.#######.#",
        "#.#.......#......#.......#.#",
        "#..........................#",
        "############################"
    };

    private static final String[] LEVEL_2 = {
        "############################",
        "#..#......#.#..#.#......#..#",
        "#..#....................#..#",
        "#o...###..#......#..###...o#",
        "#.#...#..##.####.##..#...#.#",
        "#..##....#........#....##..#",
        "#.......#....##....#.......#",
        "#.#.#....#........#....#.#.#",
        "#............##............#",
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
        "#.##########.##.##########.#",
        "#.#..#..#..#....#..#..#..#.#",
        "#..#...##.... .....##...#..#",
        "#.#.#..#.####..####.#..#.#.#",
        "#.#...#..###....###..#...#.#",
        "#.#..##..............##..#.#",
        "#o#....#............#....#o#",
        "#......#...##..##...#......#",
        "##.#.....##......##.....#.##",
        "############################"
    };

    private static final String[] LEVEL_3 = {
        "############################",
        "#.....###..........###.....#",
        "##.###..#.#.#..#.#.#..###.##",
        "#o.#....#.##....##.#....#.o#",
        "#..#..#..##..##..##..#..#..#",
        "#....###.#.#....#.#.###....#",
        "#.##.......##..##.......##.#",
        "#..#.#.#.#........#.#.#.#..#",
        "#............##............#",
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
        "#.##########.##.##########.#",
        "#.##.#..##.#....#.##..#.##.#",
        "#..#..#.#...# .#...#.#..#..#",
        "#...#..................#...#",
        "#....#.#..#..##..#..#.#....#",
        "##...#.....######.....#...##",
        "#o.#......#......#......#.o#",
        "##.###.#....#..#....#.###.##",
        "##.####..####..####..####.##",
        "############################"
    };

    private static final String[] LEVEL_4 = {
        "############################",
        "#...###..###.##.###..###...#",
        "##.##..#.....##.....#..##.##",
        "#o....#..#.#....#.#..#....o#",
        "#...#.#...##....##...#.#...#",
        "#.##....................##.#",
        "#...#..#...##..##...#..#...#",
        "#.##...#....#..#....#...##.#",
        "#............##............#",
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
        "#.##########.##.##########.#",
        "#...#.......#..#.......#...#",
        "##..##.###... ....###.##..##",
        "###.....#.###..###.#.....###",
        "#.###.#....#....#....#.###.#",
        "#.#..##....#....#....##..#.#",
        "#...##..###......###..##...#",
        "#.#....#....####....#....#.#",
        "####...#.#........#.#...####",
        "############################"
    };

    private static final String[] LEVEL_5 = {
        "############################",
        "#.......##.######.##.......#",
        "###...####.#....#.####...###",
        "#o..#.#.#..........#.#.#..o#",
        "#...#.....#......#.....#...#",
        "#.#.#....#.#.##.#.#....#.#.#",
        "#.#.#.##.#.#.##.#.#.##.#.#.#",
        "#..##.##....####....##.##..#",
        "#............##............#",
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
        "#.##########.##.##########.#",
        "#.###..#####....#####..###.#",
        "#.#...###.### .###.###...#.#",
        "#.....#...#......#...#.....#",
        "#.##.#.##...#..#...##.#.##.#",
        "#..#.....#.######.#.....#..#",
        "#..#.####..#.##.#..####.#..#",
        "###..........##..........###",
        "#######..###....###..#######",
        "############################"
    };

    /** All playable layouts, in increasing order of difficulty. */
    private static final String[][] LEVELS = {
        LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5
    };

    public static final int LEVEL_COUNT = LEVELS.length;

    /** Number of 64-bit words needed to hold ROWS*COLS wall bits. */
    private static final int WALL_WORDS = (ROWS * COLS + 63) / 64;

    /** Dense storage used until the compression threshold is crossed. */
    private char[][] tiles = new char[ROWS][COLS];

    /** Compact storage used after compression; null while still dense. */
    private SparseMatrix sparse;
    private boolean compressed;

    /**
     * Static walls packed one bit per tile.  Rebuilt per level, so that the
     * phantom-pellet logic of the compressed mode still knows where the walls
     * are for the layout that is currently loaded.
     */
    private long[] wallBits;

    /** Packed keys of the two ghost-house door tiles for the active layout. */
    private int[] doorKeys;

    /** Zero-based index of the active layout. */
    private int levelIndex;

    private int pelletsLeft;
    private int totalPellets;

    public Maze() {
        reset();
    }

    /** Loads level 1 (kept for backwards compatibility). */
    public void reset() {
        reset(1);
    }

    /**
     * Loads the given 1-based level.  Levels past the last layout re-use the
     * hardest one, so an endless game keeps getting the level-5 maze.
     */
    public void reset(int level) {
        levelIndex = Math.max(0, Math.min(level - 1, LEVELS.length - 1));
        String[] layout = LEVELS[levelIndex];

        compressed = false;
        sparse = null;
        wallBits = null;
        doorKeys = findDoorKeys(layout);

        if (tiles == null) {
            tiles = new char[ROWS][COLS];
        }
        pelletsLeft = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = layout[r].charAt(c);
                tiles[r][c] = ch;
                if (ch == PELLET || ch == POWER) {
                    pelletsLeft++;
                }
            }
        }
        totalPellets = pelletsLeft;
    }

    /** Zero-based active level index. */
    public int levelIndex() {
        return levelIndex;
    }

    public static int levelCount() {
        return LEVEL_COUNT;
    }

    public static int wrapCol(int c) {
        int m = c % COLS;
        return m < 0 ? m + COLS : m;
    }

    public char tileAt(int c, int r) {
        if (r < 0 || r >= ROWS) {
            return WALL;
        }
        c = wrapCol(c);
        if (compressed) {
            byte v = sparse.get(r, c);
            if (v == SparseMatrix.PELLET) {
                return PELLET;
            }
            if (v == SparseMatrix.POWER) {
                return POWER;
            }
            if (isDoorKey(c, r)) {
                return DOOR;
            }
            return wallBit(c, r) ? WALL : EMPTY;
        }
        return tiles[r][c];
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
        if (r + dir.dy < 0 || r + dir.dy >= ROWS) {
            return false;
        }
        char ch = tileAt(c + dir.dx, r + dir.dy);
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
        if (compressed) {
            int type = sparse.eat(r, c);
            if (type != 0) {
                pelletsLeft--;
            }
            return type;
        }
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

    // ------------------------------------------------------------------
    // phase 6: dense -> sparse conversion
    // ------------------------------------------------------------------

    public boolean isCompressed() {
        return compressed;
    }

    public SparseMatrix sparse() {
        return sparse;
    }

    /**
     * Checks the memory-optimisation trigger: once at least 80 % of the pellets
     * have been eaten the dense grid is replaced by a sparse one.
     *
     * @return true when a conversion happened during this call
     */
    public boolean maybeCompress() {
        if (compressed || totalPellets == 0) {
            return false;
        }
        int eaten = totalPellets - pelletsLeft;
        if (eaten * 5 < totalPellets * 4) {   // eaten / total < 0.8
            return false;
        }
        compress();
        return true;
    }

    private void compress() {
        int n = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = tiles[r][c];
                if (ch == PELLET || ch == POWER) {
                    n++;
                }
            }
        }
        int[] rows = new int[n];
        int[] cols = new int[n];
        byte[] vals = new byte[n];
        int i = 0;
        // A row-major scan already visits tiles in ascending key order, so the
        // resulting COO lists are sorted and binary search works directly.
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = tiles[r][c];
                if (ch == PELLET || ch == POWER) {
                    rows[i] = r;
                    cols[i] = c;
                    vals[i] = (ch == PELLET) ? SparseMatrix.PELLET : SparseMatrix.POWER;
                    i++;
                }
            }
        }

        long before = denseBytes();
        long afterSparse = 0;
        sparse = new SparseMatrix(rows, cols, vals);
        afterSparse = sparse.estimatedBytes();
        tiles = null;                 // hand the big grid to the garbage collector
        compressed = true;
        if (wallBits == null) {
            wallBits = buildWallBits();
        }
        long after = afterSparse + wallBytes();

        System.out.println("==================================================");
        System.out.println("[MEMORY] 80% of pellets eaten -> sparse conversion");
        System.out.printf("[MEMORY] dense  char[%d][%d]        : %d bytes%n",
                ROWS, COLS, before);
        System.out.printf("[MEMORY] sparse %s%n", sparse.describe());
        System.out.printf("[MEMORY] sparse COO + wall bits : %d bytes%n", after);
        System.out.printf("[MEMORY] saved                  : %d bytes (%.1f%%)%n",
                before - after, 100.0 * (before - after) / before);
        System.out.println("[MEMORY] dense grid released to the garbage collector");
        System.out.println("==================================================");
    }

    /** Estimated size of the dense {@code char[31][28]} (64-bit JVM, aligned). */
    public static long denseBytes() {
        long outer = 16 + SparseMatrix.align8(8L * ROWS);        // header + row refs
        long inner = ROWS * (16 + SparseMatrix.align8(2L * COLS)); // char[] per row
        return outer + inner;
    }

    /** Estimated size of the packed wall bitmask. */
    public static long wallBytes() {
        return 16 + SparseMatrix.align8(8L * WALL_WORDS);
    }

    private long[] buildWallBits() {
        long[] w = new long[WALL_WORDS];
        String[] layout = LEVELS[levelIndex];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (layout[r].charAt(c) == WALL) {
                    int k = r * COLS + c;
                    w[k >>> 6] |= 1L << (k & 63);
                }
            }
        }
        return w;
    }

    private boolean wallBit(int c, int r) {
        if (wallBits == null) {
            wallBits = buildWallBits();
        }
        int k = r * COLS + c;
        return ((wallBits[k >>> 6] >>> (k & 63)) & 1L) != 0;
    }

    private static int[] findDoorKeys(String[] layout) {
        int count = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (layout[r].charAt(c) == DOOR) {
                    count++;
                }
            }
        }
        int[] keys = new int[count];
        int i = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (layout[r].charAt(c) == DOOR) {
                    keys[i++] = r * COLS + c;
                }
            }
        }
        return keys;
    }

    private boolean isDoorKey(int c, int r) {
        int key = r * COLS + c;
        for (int k : doorKeys) {
            if (k == key) {
                return true;
            }
        }
        return false;
    }
}
