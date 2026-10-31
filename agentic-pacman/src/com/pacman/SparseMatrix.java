package com.pacman;

/**
 * Phase 6 data structure: a sparse matrix in COO (Coordinate List) format.
 *
 * <p>Instead of a full {@code char[31][28]} grid that is 95 % empty walls, only
 * the tiles that still hold a pellet are kept, each as a (row, col, value)
 * triple stored in three parallel arrays:
 *
 * <pre>
 *   rows[i]  = row of the i-th remaining pellet
 *   cols[i]  = column of the i-th remaining pellet
 *   vals[i]  = 1 for a normal pellet, 2 for a power pellet
 * </pre>
 *
 * <p>The triples are kept sorted by the packed key {@code row * Maze.COLS + col},
 * which is exactly the order a row-major scan of the dense grid produces, so
 * lookups are a plain binary search - O(log n) instead of O(1), for a fraction
 * of the memory.
 */
public final class SparseMatrix {

    public static final byte PELLET = 1;
    public static final byte POWER = 2;

    private final int[] rows;
    private final int[] cols;
    private final byte[] vals;

    /** Live entry count; shrinks as pellets are eaten (array capacity does not). */
    private int n;

    public SparseMatrix(int[] rows, int[] cols, byte[] vals) {
        if (rows.length != cols.length || rows.length != vals.length) {
            throw new IllegalArgumentException("COO arrays must have equal length");
        }
        this.rows = rows;
        this.cols = cols;
        this.vals = vals;
        this.n = rows.length;
    }

    public int size() {
        return n;
    }

    public int row(int i) {
        return rows[i];
    }

    public int col(int i) {
        return cols[i];
    }

    public byte value(int i) {
        return vals[i];
    }

    /** Packed sort key of entry i, i.e. its position in a row-major scan. */
    private int keyAt(int i) {
        return rows[i] * Maze.COLS + cols[i];
    }

    /** Value at (r, c): 0 when there is nothing there, else 1 or 2. */
    public byte get(int r, int c) {
        int i = indexOf(r, c);
        return i < 0 ? 0 : vals[i];
    }

    /** Removes the pellet at (r, c) and returns its type (0 = nothing there). */
    public byte eat(int r, int c) {
        int i = indexOf(r, c);
        if (i < 0) {
            return 0;
        }
        byte v = vals[i];
        int tail = n - i - 1;
        if (tail > 0) {
            System.arraycopy(rows, i + 1, rows, i, tail);
            System.arraycopy(cols, i + 1, cols, i, tail);
            System.arraycopy(vals, i + 1, vals, i, tail);
        }
        n--;
        vals[n] = 0;
        return v;
    }

    /** Binary search over the packed keys.  O(log n). */
    private int indexOf(int r, int c) {
        int key = r * Maze.COLS + c;
        int lo = 0;
        int hi = n - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int k = keyAt(mid);
            if (k == key) {
                return mid;
            }
            if (k < key) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Estimated heap footprint in bytes using the usual 64-bit JVM layout
     * (16-byte object header, fields padded to 8 bytes):
     *
     * <pre>
     *   int[]  = 16 header + align8(4 * length)
     *   byte[] = 16 header + align8(1 * length)
     * </pre>
     */
    public long estimatedBytes() {
        long r = 16 + align8(4L * rows.length);
        long c = 16 + align8(4L * cols.length);
        long v = 16 + align8(1L * vals.length);
        return r + c + v;
    }

    static long align8(long x) {
        return (x + 7L) & ~7L;
    }

    /** One-line dump used by the demo / console report. */
    public String describe() {
        StringBuilder sb = new StringBuilder("COO[");
        for (int i = 0; i < Math.min(n, 8); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('(').append(rows[i]).append(',').append(cols[i])
                    .append('=').append(vals[i]).append(')');
        }
        if (n > 8) {
            sb.append(", ... ").append(n - 8).append(" more");
        }
        sb.append(']');
        return sb.toString();
    }
}
