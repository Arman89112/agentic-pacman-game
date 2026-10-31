import com.pacman.*;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Validates every new data structure added by the six-phase plan.
 * Run with:  java -cp out:test TestAgentic
 */
public class TestAgentic {

    static int failures = 0;

    public static void main(String[] args) {
        testGraphStructure();
        testPathfinding();
        testRingBuffer();
        testTracker();
        testSearchTree();
        testTrieCommandEngine();
        testCheatsThroughGame();
        testSparseCompression();
        testAgentsNeverEnterWalls();
        if (failures == 0) {
            System.out.println("ALL AGENTIC TESTS PASSED");
        } else {
            System.out.println(failures + " AGENTIC TEST(S) FAILED");
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------

    static void testGraphStructure() {
        Game g = new Game();
        MazeGraph graph = g.graph;
        check(graph.nodeCount() > 20, "graph has a useful number of nodes");
        check(graph.edgeCount() > graph.nodeCount(), "graph has more edges than nodes");

        // adjacency matrix must be symmetric and each edge recorded once
        for (int u = 0; u < graph.nodeCount(); u++) {
            check(graph.weight(u, u) == 0, "no self loops");
            for (int v : graph.neighbors(u)) {
                check(graph.weight(u, v) > 0, "adjacency list matches matrix");
                check(graph.weight(v, u) == graph.weight(u, v), "matrix is symmetric");
                check(graph.dirBetween(u, v) != Direction.NONE, "edge has a direction");

                int c = graph.col(u);
                int r = graph.row(u);
                Direction d = graph.dirBetween(u, v);
                check(g.maze.canMove(c, r, d, false), "first step of edge is walkable");
            }
        }

        // connectivity: BFS over the adjacency list from node 0
        boolean[] seen = new boolean[graph.nodeCount()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        seen[0] = true;
        int reached = 1;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : graph.neighbors(u)) {
                if (!seen[v]) {
                    seen[v] = true;
                    reached++;
                    queue.add(v);
                }
            }
        }
        check(reached == graph.nodeCount(),
                "graph is connected (" + reached + "/" + graph.nodeCount() + ")");

        System.out.println("  graph OK: " + graph.nodeCount() + " nodes, "
                + graph.edgeCount() + " edges, connected");
    }

    static void testPathfinding() {
        Game g = new Game();
        MazeGraph graph = g.graph;
        int probes = 0;
        for (int s = 0; s < graph.nodeCount(); s += 3) {
            for (int t = 0; t < graph.nodeCount(); t += 5) {
                int expected = graph.distance(s, t);
                if (expected >= MazeGraph.INF) {
                    continue;
                }
                List<Integer> astar = graph.aStar(s, t);
                List<Integer> dij = graph.dijkstra(s, t);
                check(pathCost(graph, astar) == expected,
                        "A* matches Floyd-Warshall " + s + "->" + t);
                check(pathCost(graph, dij) == expected,
                        "Dijkstra matches Floyd-Warshall " + s + "->" + t);
                check(astar.get(0) == s && astar.get(astar.size() - 1) == t,
                        "path has correct endpoints");
                probes++;
            }
        }
        check(probes > 100, "pathfinding probed enough pairs");
        System.out.println("  pathfinding OK: A* and Dijkstra agree on " + probes + " pairs");
    }

    static int pathCost(MazeGraph graph, List<Integer> path) {
        if (path.isEmpty()) {
            return MazeGraph.INF;
        }
        int sum = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            int w = graph.weight(path.get(i), path.get(i + 1));
            if (w <= 0) {
                return -1;   // illegal hop
            }
            sum += w;
        }
        return sum;
    }

    static void testRingBuffer() {
        RingBuffer<String> ring = new RingBuffer<>(3);
        check(ring.isEmpty(), "new ring empty");
        ring.push("a");
        ring.push("b");
        ring.push("c");
        check(ring.isFull(), "ring full after capacity pushes");
        check(ring.size() == 3, "size capped at capacity");
        ring.push("d");                     // overwrites "a"
        check(ring.get(0).equals("b"), "oldest is now b");
        check(ring.newest().equals("d"), "newest is d");
        check(ring.count("b") == 1, "count works");

        RingBuffer<Direction> moves = new RingBuffer<>(10);
        for (int i = 0; i < 6; i++) {
            moves.push(Direction.RIGHT);
        }
        moves.push(Direction.UP);
        moves.push(Direction.UP);
        moves.push(Direction.LEFT);
        check(moves.dominant() == Direction.RIGHT, "dominant direction is RIGHT");
        System.out.println("  ring buffer OK: capacity=" + moves.capacity()
                + " dominant=" + moves.dominant());
    }

    static void testTracker() {
        Game g = new Game();
        g.globalMode = Game.Mode.CHASE;
        // Pac-Man heads right for a while so the memory becomes dominated by RIGHT
        g.pacman.reset(center(6), center(1));
        g.pacman.dir = Direction.RIGHT;
        for (int i = 0; i < 6; i++) {
            g.pacman.history.push(Direction.RIGHT);
        }
        Ghost inky = g.ghosts.get(2);
        int target = inky.trackerIntercept(g, g.graph);
        check(target >= 0 && target < g.graph.nodeCount(),
                "tracker produces a valid interception node");
        int pacNode = g.graph.nodeAt(g.pacman.tileX(), g.pacman.tileY());
        check(target != pacNode || pacNode < 0,
                "interception node is ahead of Pac-Man, not his current tile");
        System.out.println("  tracker OK: intercept node (" + g.graph.col(target)
                + "," + g.graph.row(target) + ") while Pac-Man is at ("
                + g.pacman.tileX() + "," + g.pacman.tileY() + ")");
    }

    static void testSearchTree() {
        Game g = new Game();
        g.globalMode = Game.Mode.CHASE;
        SearchTree tree = g.searchTree;
        int ghostNode = g.graph.nodeAhead(13, 11, Direction.NONE);
        int pacNode = g.graph.nodeAhead(13, 23, Direction.NONE);
        check(ghostNode >= 0 && pacNode >= 0, "start nodes exist");
        int pick = tree.chooseMove(ghostNode, pacNode, Direction.NONE);
        check(pick >= 0, "minimax returns a move");
        boolean legal = false;
        for (int v : g.graph.neighbors(ghostNode)) {
            if (v == pick) {
                legal = true;
            }
        }
        check(legal, "minimax move is a legal neighbour");
        check(tree.lastNodesVisited > 0, "search expanded nodes");
        System.out.println("  search tree OK: boss chose (" + g.graph.col(pick)
                + "," + g.graph.row(pick) + "), " + tree.lastNodesVisited + " nodes visited");
    }

    static void testTrieCommandEngine() {
        CommandEngine engine = new CommandEngine();
        for (char c : "xxgodmode".toCharArray()) {   // garbage prefix then the key
            String hit = engine.feed(c);
            if (c == 'e' && "godmode".endsWith("e")) {
                check("godmode".equals(hit), "trie matched godmode after garbage");
            }
        }
        check("speedup".equals(feedAll(engine, "speedup")), "speedup matched");
        check("freeze".equals(feedAll(engine, "freeze")), "freeze matched");
        check(feedAll(engine, "speedxx") == null, "non-command returns null");
        System.out.println("  trie command engine OK");
    }

    static String feedAll(CommandEngine engine, String s) {
        String result = null;
        for (char c : s.toCharArray()) {
            String r = engine.feed(c);
            if (r != null) {
                result = r;
            }
        }
        return result;
    }

    static void testCheatsThroughGame() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);

        for (char c : "freeze".toCharArray()) {
            g.feedCheat(c);
        }
        check(g.ghostsFrozen(), "freeze cheat active");
        for (char c : "godmode".toCharArray()) {
            g.feedCheat(c);
        }
        check(g.godMode(), "godmode cheat active");
        for (char c : "speedup".toCharArray()) {
            g.feedCheat(c);
        }
        check(g.speedBoost(), "speedup cheat active");

        double before = g.pacman.speed;
        g.update(1.0 / 60.0);
        check(g.pacman.speed > before, "speedup raises Pac-Man speed");
        System.out.println("  cheats OK: freeze/godmode/speedup all trigger");
    }

    static void testSparseCompression() {
        Game g = new Game();
        g.startNewGame();
        play(g, 3);
        Maze maze = g.maze;
        int total = maze.totalPellets();
        int guard = 0;
        while (!maze.isCompressed() && guard++ < 5000) {
            outer:
            for (int r = 0; r < Maze.ROWS; r++) {
                for (int c = 0; c < Maze.COLS; c++) {
                    if (maze.eat(c, r) != 0 && !maze.isCompressed()) {
                        maze.maybeCompress();
                        break outer;
                    }
                }
            }
        }
        check(maze.isCompressed(), "maze compressed after 80% eaten");

        SparseMatrix s = maze.sparse();
        check(s != null && s.size() == maze.pelletsLeft(), "sparse holds every survivor");

        // every survivor must still be visible and edible through the maze API
        for (int i = 0; i < s.size(); i++) {
            int r = s.row(i);
            int c = s.col(i);
            char ch = maze.tileAt(c, r);
            boolean edible = ch == Maze.PELLET || ch == Maze.POWER;
            check(edible, "survivor visible at (" + r + "," + c + ")");
        }
        // eat one survivor, verify count drops and it disappears
        int r0 = s.row(0);
        int c0 = s.col(0);
        int left = maze.pelletsLeft();
        check(maze.eat(c0, r0) != 0, "survivor can be eaten in sparse mode");
        check(maze.pelletsLeft() == left - 1, "pellet counter decremented");
        check(maze.tileAt(c0, r0) != Maze.PELLET && maze.tileAt(c0, r0) != Maze.POWER,
                "eaten survivor is gone");
        // walls must still be walls after the dense grid is destroyed
        check(maze.isWall(0, 0), "corner is still a wall");
        check(!maze.isWall(6, 1), "pellet row is not a wall");
        System.out.println("  sparse compression OK: " + total + " pellets -> "
                + s.size() + " survivors kept");
    }

    static void testAgentsNeverEnterWalls() {
        Game g = new Game();
        g.startNewGame();
        double dt = 1.0 / 60.0;
        int frames = 0;
        while (frames < 60 * 90 && g.state != Game.State.GAME_OVER) {
            frames++;
            if (g.state == Game.State.PLAYING) {
                g.requestDirection(TestSim.aim(g));
            }
            g.update(dt);
            if (g.state == Game.State.PLAYING || g.state == Game.State.READY) {
                for (Ghost gh : g.ghosts) {
                    if (g.maze.isWall(gh.tileX(), gh.tileY())) {
                        check(false, gh.kind + " entered a wall at ("
                                + gh.tileX() + "," + gh.tileY() + ")");
                        return;
                    }
                    // any planned path must be a legal chain of edges
                    List<Integer> path = gh.plannedPath;
                    for (int i = 0; i + 1 < path.size(); i++) {
                        if (g.graph.weight(path.get(i), path.get(i + 1)) <= 0) {
                            check(false, gh.kind + " planned an illegal edge");
                            return;
                        }
                    }
                }
            }
        }
        System.out.println("  agents OK: " + frames + " frames with no wall violations");
    }

    // ------------------------------------------------------------------

    static final double T = Game.TILE;

    static double center(int i) {
        return i * T + T / 2.0;
    }

    static void play(Game g, double seconds) {
        for (int i = 0; i < (int) (seconds / (1.0 / 60.0)); i++) {
            g.update(1.0 / 60.0);
        }
    }

    static void check(boolean condition, String msg) {
        if (!condition) {
            failures++;
            System.out.println("FAIL: " + msg);
        }
    }
}
