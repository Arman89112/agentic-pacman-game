package com.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Phase 2 data structure: the maze turned into a mathematical graph.
 *
 * <p>Nodes are the "decision points" of the maze - every walkable tile that is
 * an intersection (3+ open sides), a corner (2 perpendicular open sides) or a
 * dead end.  Plain corridor tiles are NOT nodes: they are abstracted away into
 * weighted edges, so the graph stays small (~100 nodes instead of ~700 tiles).
 *
 * <p>Three views of the same graph are maintained:
 * <ul>
 *   <li><b>adjacency matrix</b> {@code edgeW[u][v]} = length in tiles of the
 *       corridor joining node u with node v (0 = no direct corridor).  This is
 *       the linear-algebra style matrix from the plan;</li>
 *   <li><b>adjacency list</b> {@code adj[u]} = node indices reachable from u,
 *       for iterating in O(degree);</li>
 *   <li><b>all-pairs distance matrix</b> {@code allPairs}, produced once with
 *       Floyd-Warshall, which gives O(1) "how far is A from B" queries that the
 *       minimax evaluation function needs.</li>
 * </ul>
 */
public final class MazeGraph {

    /** Distance value used for "no path exists". */
    public static final int INF = 1_000_000_000;

    private static final Direction[] CARDINAL = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };

    private final Maze maze;
    private final int nodeCount;
    private int edgeCount;

    /** Column / row of every node, indexed by node id. */
    private final int[] ncol;
    private final int[] nrow;
    /** tileToNode[row * COLS + col] = node id, or -1 when the tile is not a node. */
    private final int[] tileToNode;

    /** Adjacency matrix of corridor lengths (0 = no corridor). */
    private final int[][] edgeW;
    /** edgeDir[u][v] = Direction.ordinal() to walk from u to v. */
    private final byte[][] edgeDir;
    /** Adjacency list, only real corridors. */
    private final int[][] adj;

    /** Floyd-Warshall all-pairs shortest path lengths. */
    private final int[][] allPairs;

    public MazeGraph(Maze maze) {
        this.maze = maze;

        // ------------------------------------------------------------------
        // pass 1 - discover the nodes (decision points)
        // ------------------------------------------------------------------
        int max = Maze.ROWS * Maze.COLS;
        tileToNode = new int[max];
        Arrays.fill(tileToNode, -1);

        int[] tmpCol = new int[max];
        int[] tmpRow = new int[max];
        int count = 0;
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                if (walkable(c, r) && isDecisionPoint(c, r)) {
                    tileToNode[r * Maze.COLS + c] = count;
                    tmpCol[count] = c;
                    tmpRow[count] = r;
                    count++;
                }
            }
        }
        nodeCount = count;
        ncol = Arrays.copyOf(tmpCol, count);
        nrow = Arrays.copyOf(tmpRow, count);

        // ------------------------------------------------------------------
        // pass 2 - walk every corridor and fill the adjacency matrix
        // ------------------------------------------------------------------
        edgeW = new int[nodeCount][nodeCount];
        edgeDir = new byte[nodeCount][nodeCount];
        for (int u = 0; u < nodeCount; u++) {
            for (Direction d : CARDINAL) {
                int c = ncol[u];
                int r = nrow[u];
                int steps = 0;
                while (walkable(c + d.dx, r + d.dy)) {
                    c = Maze.wrapCol(c + d.dx);
                    r += d.dy;
                    steps++;
                    int v = tileToNode[r * Maze.COLS + c];
                    if (v >= 0) {
                        if (edgeW[u][v] == 0) {
                            edgeW[u][v] = steps;
                            edgeDir[u][v] = (byte) d.ordinal();
                            edgeCount++;
                        }
                        break;
                    }
                    if (steps > max) {
                        break;   // safety net, should never trigger
                    }
                }
            }
        }

        // ------------------------------------------------------------------
        // pass 3 - adjacency lists (cheap iteration for the search tree)
        // ------------------------------------------------------------------
        adj = new int[nodeCount][];
        for (int u = 0; u < nodeCount; u++) {
            int deg = 0;
            for (int v = 0; v < nodeCount; v++) {
                if (edgeW[u][v] > 0) {
                    deg++;
                }
            }
            int[] list = new int[deg];
            int i = 0;
            for (int v = 0; v < nodeCount; v++) {
                if (edgeW[u][v] > 0) {
                    list[i++] = v;
                }
            }
            adj[u] = list;
        }

        // ------------------------------------------------------------------
        // pass 4 - Floyd-Warshall: every node to every node, once, at build time
        // ------------------------------------------------------------------
        allPairs = new int[nodeCount][nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            Arrays.fill(allPairs[i], INF);
            allPairs[i][i] = 0;
        }
        for (int u = 0; u < nodeCount; u++) {
            for (int v = 0; v < nodeCount; v++) {
                if (edgeW[u][v] > 0) {
                    allPairs[u][v] = edgeW[u][v];
                }
            }
        }
        for (int k = 0; k < nodeCount; k++) {
            for (int i = 0; i < nodeCount; i++) {
                if (allPairs[i][k] == INF) {
                    continue;
                }
                for (int j = 0; j < nodeCount; j++) {
                    if (allPairs[k][j] == INF) {
                        continue;
                    }
                    int via = allPairs[i][k] + allPairs[k][j];
                    if (via < allPairs[i][j]) {
                        allPairs[i][j] = via;
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // geometry helpers
    // ------------------------------------------------------------------

    /**
     * A tile a ghost may path through: not a wall, not the ghost-house door and
     * not inside the ghost house.  Ghosts that are still inside the house are
     * driven by their own scripted states, so the house is excluded from the
     * graph entirely.
     */
    public boolean walkable(int c, int r) {
        if (r < 0 || r >= Maze.ROWS) {
            return false;
        }
        c = Maze.wrapCol(c);
        if (maze.isWall(c, r) || maze.isDoor(c, r)) {
            return false;
        }
        if (r >= 13 && r <= 15 && c >= 11 && c <= 16) {
            return false;   // ghost house interior
        }
        return true;
    }

    /**
     * A decision point: 0, 1, 3 or 4 open sides are always a node; exactly two
     * open sides only count when they turn a corner (one vertical + one
     * horizontal).  Two opposite open sides are a plain corridor tile.
     */
    private boolean isDecisionPoint(int c, int r) {
        int open = 0;
        boolean hasVertical = false;
        boolean hasHorizontal = false;
        for (Direction d : CARDINAL) {
            if (walkable(c + d.dx, r + d.dy)) {
                open++;
                if (d == Direction.UP || d == Direction.DOWN) {
                    hasVertical = true;
                } else {
                    hasHorizontal = true;
                }
            }
        }
        if (open != 2) {
            return true;
        }
        return hasVertical && hasHorizontal;
    }

    // ------------------------------------------------------------------
    // lookups
    // ------------------------------------------------------------------

    public int nodeCount() {
        return nodeCount;
    }

    public int edgeCount() {
        return edgeCount;
    }

    public int col(int node) {
        return ncol[node];
    }

    public int row(int node) {
        return nrow[node];
    }

    public int nodeAt(int c, int r) {
        if (r < 0 || r >= Maze.ROWS) {
            return -1;
        }
        return tileToNode[r * Maze.COLS + Maze.wrapCol(c)];
    }

    public int[] neighbors(int node) {
        return adj[node];
    }

    public int degree(int node) {
        return adj[node].length;
    }

    /** Corridor length in tiles between two directly connected nodes (0 = none). */
    public int weight(int u, int v) {
        return edgeW[u][v];
    }

    /** Shortest path length in tiles between any two nodes (Floyd-Warshall). */
    public int distance(int u, int v) {
        return allPairs[u][v];
    }

    /** Direction to walk to go from node u to the directly connected node v. */
    public Direction dirBetween(int u, int v) {
        if (u < 0 || v < 0 || edgeW[u][v] == 0) {
            return Direction.NONE;
        }
        return Direction.values()[edgeDir[u][v]];
    }

    /**
     * Nearest graph node to a tile, using the cylinder metric of the wrapping
     * board (columns wrap around, so the left and right edges are neighbours).
     */
    public int nearestNode(int c, int r) {
        int best = -1;
        int bestD = Integer.MAX_VALUE;
        for (int i = 0; i < nodeCount; i++) {
            int d = colDelta(c, ncol[i]) + Math.abs(r - nrow[i]);
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }

    /**
     * The node an entity standing on tile (c, r) is heading towards: itself when
     * it is already on a node, otherwise the first node met while walking in
     * {@code dir}, otherwise simply the nearest node.
     */
    public int nodeAhead(int c, int r, Direction dir) {
        int here = nodeAt(c, r);
        if (here >= 0) {
            return here;
        }
        if (dir != null && dir != Direction.NONE) {
            int cc = c;
            int rr = r;
            for (int i = 0; i < Maze.COLS; i++) {
                if (!maze.canMove(cc, rr, dir, false)) {
                    break;
                }
                cc = Maze.wrapCol(cc + dir.dx);
                rr += dir.dy;
                int n = nodeAt(cc, rr);
                if (n >= 0) {
                    return n;
                }
            }
        }
        return nearestNode(c, r);
    }

    /** Column distance that respects the horizontal tunnel wrap. */
    public int colDelta(int a, int b) {
        int d = Math.abs(a - b);
        return Math.min(d, Maze.COLS - d);
    }

    /**
     * Admissible A* heuristic: because one tile step can only change the
     * wrap-aware Manhattan distance by one, this is never larger than the true
     * shortest path, so A* stays optimal even across the tunnel.
     */
    public int heuristic(int u, int v) {
        return colDelta(ncol[u], ncol[v]) + Math.abs(nrow[u] - nrow[v]);
    }

    // ------------------------------------------------------------------
    // path finding
    // ------------------------------------------------------------------

    /** A* shortest path; returns node ids [start .. goal], or empty if unreachable. */
    public List<Integer> aStar(int start, int goal) {
        return search(start, goal, true);
    }

    /** Dijkstra shortest path; identical to A* with a zero heuristic. */
    public List<Integer> dijkstra(int start, int goal) {
        return search(start, goal, false);
    }

    private List<Integer> search(int start, int goal, boolean useHeuristic) {
        if (start < 0 || goal < 0) {
            return List.of();
        }
        if (start == goal) {
            List<Integer> only = new ArrayList<>(1);
            only.add(start);
            return only;
        }
        double[] gScore = new double[nodeCount];
        int[] prev = new int[nodeCount];
        boolean[] closed = new boolean[nodeCount];
        Arrays.fill(gScore, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        gScore[start] = 0;

        PriorityQueue<Entry> open = new PriorityQueue<>(Comparator.comparingDouble(e -> e.f));
        open.add(new Entry(useHeuristic ? heuristic(start, goal) : 0, start));

        while (!open.isEmpty()) {
            Entry e = open.poll();
            int u = e.node;
            if (closed[u]) {
                continue;   // stale queue entry
            }
            closed[u] = true;
            if (u == goal) {
                return reconstruct(prev, start, goal);
            }
            for (int v : adj[u]) {
                double tentative = gScore[u] + edgeW[u][v];
                if (tentative < gScore[v]) {
                    gScore[v] = tentative;
                    prev[v] = u;
                    double h = useHeuristic ? heuristic(v, goal) : 0;
                    open.add(new Entry(tentative + h, v));
                }
            }
        }
        return List.of();   // goal unreachable
    }

    private static List<Integer> reconstruct(int[] prev, int start, int goal) {
        List<Integer> path = new ArrayList<>();
        for (int at = goal; at != -1; at = prev[at]) {
            path.add(at);
            if (at == start) {
                break;
            }
        }
        List<Integer> forward = new ArrayList<>(path.size());
        for (int i = path.size() - 1; i >= 0; i--) {
            forward.add(path.get(i));
        }
        return forward;
    }

    /** Priority-queue payload for A* / Dijkstra (f = g + h). */
    private static final class Entry {
        final double f;
        final int node;

        Entry(double f, int node) {
            this.f = f;
            this.node = node;
        }
    }
}
