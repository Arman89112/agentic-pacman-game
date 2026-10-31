package com.pacman;

import java.util.List;

/**
 * Phase 4 data structure: a depth-limited search tree (minimax with alpha-beta
 * pruning) used by the "boss" ghost (Blinky).
 *
 * <p>Every ply of the tree is one move on the maze graph: the boss walks from
 * its current node to a neighbouring node, then the simulated Pac-Man answers,
 * then the boss plans one more hop.  With a branching factor of ~3 and
 * {@link #DEPTH} = 3 the tree has at most a few dozen leaves, so it can be
 * re-evaluated at every single intersection without any noticeable cost.
 *
 * <p>The leaf states are scored by {@link #evaluate}, a heuristic that rewards
 * being close to the *real* Pac-Man while also considering the simulated future
 * position, how many exits Pac-Man still has, and whether every one of those
 * exits leads straight back into the ghost's arms.
 */
public final class SearchTree {

    /** Search depth: boss -> Pac-Man -> boss -> Pac-Man (4 plies). */
    public static final int DEPTH = 4;

    /** Weight of "how close am I to the real Pac-Man tile". */
    private static final double W_REAL = 3.0;
    /** Weight of "how close am I to the simulated future Pac-Man". */
    private static final double W_MODEL = 1.5;
    /** Reward for Pac-Man having few free intersections around him. */
    private static final double W_CORNER = 1.5;
    /** Bonus when Pac-Man's simulated self is completely cut off. */
    private static final double BONUS_TRAPPED = 12.0;
    /** Bonus when capture is one corridor-step away. */
    private static final double BONUS_CLOSE = 10.0;
    /** Score when the root has no legal move at all. */
    public static final double NO_MOVE = -1.0e9;

    private final MazeGraph graph;

    /** Nodes expanded during the last call - handy for the debug overlay. */
    public int lastNodesVisited;

    public SearchTree(MazeGraph graph) {
        this.graph = graph;
    }

    public MazeGraph graph() {
        return graph;
    }

    /**
     * Picks the boss's next node.
     *
     * @param ghostNode where the boss currently stands
     * @param pacmanNode the node Pac-Man is on / heading towards
     * @param reverse the direction the boss must NOT take (ghosts never reverse)
     * @return the chosen neighbour node, or -1 when nothing legal exists
     */
    public int chooseMove(int ghostNode, int pacmanNode, Direction reverse) {
        if (ghostNode < 0 || pacmanNode < 0) {
            return -1;
        }
        lastNodesVisited = 0;
        int best = -1;
        double bestScore = NO_MOVE;
        double alpha = NO_MOVE;

        for (int next : graph.neighbors(ghostNode)) {
            if (reverse != null && graph.dirBetween(ghostNode, next) == reverse) {
                continue;   // classic Pac-Man rule: no instant reversal
            }
            lastNodesVisited++;
            double score = minimax(next, pacmanNode, DEPTH - 1, false,
                    alpha, Double.POSITIVE_INFINITY, pacmanNode);
            if (score > bestScore) {
                bestScore = score;
                best = next;
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return best;
    }

    /**
     * Depth-first minimax with alpha-beta pruning.
     *
     * @param gNode boss position (node id)
     * @param pNode simulated Pac-Man position (node id)
     * @param depth plies still to be explored
     * @param maxing true when the boss is choosing (maximise), false for Pac-Man
     * @param alpha best score the maximiser can already guarantee
     * @param beta best score the minimiser can already guarantee
     * @param realPacman node of the *actual* Pac-Man, never moved by the tree
     */
    private double minimax(int gNode, int pNode, int depth, boolean maxing,
                           double alpha, double beta, int realPacman) {
        if (depth == 0) {
            return evaluate(gNode, pNode, realPacman);
        }
        if (maxing) {
            double best = NO_MOVE;
            for (int next : graph.neighbors(gNode)) {
                lastNodesVisited++;
                double s = minimax(next, pNode, depth - 1, false,
                        alpha, beta, realPacman);
                if (s > best) {
                    best = s;
                }
                if (best > alpha) {
                    alpha = best;
                }
                if (alpha >= beta) {
                    break;   // branch can be abandoned: Pac-Man would avoid it
                }
            }
            return best;
        }
        double best = -NO_MOVE;
        for (int next : graph.neighbors(pNode)) {
            lastNodesVisited++;
            double s = minimax(gNode, next, depth - 1, true,
                    alpha, beta, realPacman);
            if (s < best) {
                best = s;
            }
            if (best < beta) {
                beta = best;
            }
            if (alpha >= beta) {
                break;
            }
        }
        return best;
    }

    /**
     * Heuristic evaluation of a leaf state from the ghost's point of view
     * (bigger = better for the ghost).
     */
    private double evaluate(int gNode, int pNode, int realPacman) {
        int dReal = graph.distance(gNode, realPacman);
        int dModel = graph.distance(gNode, pNode);
        if (dReal >= MazeGraph.INF) {
            return NO_MOVE;
        }
        double score = -W_REAL * dReal - W_MODEL * dModel;

        int exits = graph.degree(pNode);
        score += W_CORNER * (4 - exits);

        if (dReal <= 1) {
            score += BONUS_CLOSE;
        }
        if (exits > 0) {
            boolean cutOff = true;
            for (int exit : graph.neighbors(pNode)) {
                if (graph.distance(exit, gNode) > dModel) {
                    cutOff = false;
                    break;
                }
            }
            if (cutOff) {
                score += BONUS_TRAPPED;
            }
        }
        return score;
    }

    /** Pretty summary of a chosen move, used by the debug overlay. */
    public String describeMove(List<Integer> path) {
        if (path == null || path.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                sb.append("->");
            }
            sb.append('(').append(graph.col(path.get(i)))
                    .append(',').append(graph.row(path.get(i))).append(')');
        }
        return sb.toString();
    }
}
