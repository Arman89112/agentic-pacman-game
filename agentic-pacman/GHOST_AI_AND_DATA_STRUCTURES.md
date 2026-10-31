# Ghost AI & Data Structures — Presentation Notes

> **Scope of this document:** *only* the data structures and the AI algorithms
> that turn the ghosts of `pacman-game` into **autonomous agents**. It explains
> *what each structure is*, *how it is coded*, *which algorithm runs on it*,
> *where it is used in the game*, and *why that makes the ghost "agentic"*.
> Nothing about JavaFX, audio, rendering or the UI lives here.
>
> Every claim below maps to a real file in `src/com/pacman/`. This is a
> presentation aid, so each section ends with a **"say this"** one-liner.

---

## 0. The 30-second version

A ghost is *agentic* when it **perceives** the world, **keeps state**, **plans
ahead**, and **acts on the plan**. This project gives each of the four ghosts a
different brain:

| Ghost | `Brain` | Data structure | AI algorithm | Personality it produces |
|-------|---------|----------------|--------------|-------------------------|
| **Blinky** (red) | `BOSS` | `MazeGraph` + `SearchTree` | **Minimax with alpha–beta pruning** | Looks several moves ahead, tries to corner Pac-Man |
| **Pinky** (pink) | `A_STAR` | `MazeGraph` adjacency list | **A\* search** (heuristic-guided) | Fastest-route interceptor, aims 4 tiles ahead |
| **Inky** (cyan) | `TRACKER` | `RingBuffer<Direction>` | **Memory-based trajectory prediction** | Reads Pac-Man's recent turns, cuts him off |
| **Clyde** (orange) | `DIJKSTRA` | `MazeGraph` adjacency matrix | **Dijkstra's shortest path** | Uninformed shortest-path hunter |
| **Solo hunter** | `HUNTER` | `MazeGraph` + `SearchTree` | **Minimax** (no scatter) | Relentless boss in the 1-vs-1 mode |

**Say this:** *"The maze is a graph, each ghost runs a different search
algorithm on that graph, and one ghost additionally predicts the player using a
ring-buffer memory."*

---

## 1. The perception → plan → act loop

Every ghost decision happens at a **tile centre** in `Ghost.gridMove(...)`,
which calls `Ghost.chooseDirection(...)` (`Ghost.java`). The agentic pipeline is:

```
        perception                    reasoning                     action
 ┌────────────────────┐   ┌──────────────────────────────┐   ┌──────────────┐
 │ maze graph nodes   │   │ A* / Dijkstra / Minimax      │   │ dirBetween() │
 │ pacman ring buffer │──▶│ returns a PATH (list of      │──▶│ turn one     │
 │ ghost & pacman pos │   │ node ids) or a single move   │   │ tile         │
 └────────────────────┘   └──────────────────────────────┘   └──────────────┘
```

The **state kept across frames** (what makes it more than a reflex) is:

* `Ghost.plannedPath` – the route currently being followed,
* `Ghost.targetNode` / `bossPick` / `interceptNode` – the current goal,
* `Pacman.history` (ring buffer) – the player's recent moves.

**Say this:** *"Perception feeds a planner, the planner produces an explicit
path, and the ghost commits to it — that is the agentic loop."*

---

## 2. `MazeGraph` — the central data structure

**File:** `MazeGraph.java`

### 2.1 The idea

The raw maze is a `28 × 31` grid (`Maze.java`). Searching that grid directly
would be wasteful, so it is **compressed into a graph**:

* **Node = a decision point** — a walkable tile that is an intersection (3–4 open
  sides), a corner (2 open sides that turn), or a dead end.
* **Edge = a corridor** between two nodes, weighted by its **length in tiles**.

Plain straight corridor tiles are *not* nodes — they are absorbed into edge
weights. On level 1 this shrinks ~700 walkable tiles down to **130 nodes / 354
edges**.

```java
// MazeGraph.java — a tile is a node unless it is a straight corridor
private boolean isDecisionPoint(int c, int r) {
    int open = 0;
    boolean hasVertical = false, hasHorizontal = false;
    for (Direction d : CARDINAL) {
        if (walkable(c + d.dx, r + d.dy)) {
            open++;
            if (d == Direction.UP || d == Direction.DOWN) hasVertical = true;
            else hasHorizontal = true;
        }
    }
    if (open != 2) return true;            // 0,1,3,4 open sides => node
    return hasVertical && hasHorizontal;   // a bend is a node, a straight isn't
}
```

The ghost house and its door are **excluded** from the graph because house
behaviour is scripted separately (`walkable(...)`), so all pathfinding stays on
the open maze.

### 2.2 Three views of the same graph (the "data structure" part)

| View | Field | Purpose | Cost |
|------|-------|---------|------|
| **Adjacency matrix** | `int[][] edgeW` | corridor length between u and v (0 = no edge) | `O(V²)` memory |
| **Adjacency list** | `int[][] adj` | iterate a node's neighbours in `O(degree)` | `O(E)` memory |
| **All-pairs distances** | `int[][] allPairs` | `O(1)` "how far is A from B", used by minimax | `O(V²)` memory |

```java
// MazeGraph.java — building an edge by walking the corridor tile by tile
for (Direction d : CARDINAL) {
    int c = ncol[u], r = nrow[u], steps = 0;
    while (walkable(c + d.dx, r + d.dy)) {
        c = Maze.wrapCol(c + d.dx);
        r += d.dy;
        steps++;
        int v = tileToNode[r * Maze.COLS + c];
        if (v >= 0) { edgeW[u][v] = steps; /* + direction */ break; }
    }
}
```

### 2.3 Why this matters for agents

The graph is built **once per level** (in `Game.rebuildGraph()`). After that,
*every* query a ghost makes — "*what is the next hop?*", "*how far am I?*" —
is a cheap array lookup or a small priority-queue search instead of walking the
grid. This is what makes per-frame planning affordable.

**Say this:** *"`MazeGraph` is the shared world-model: a weighted graph whose
nodes are junctions and whose edges are corridor lengths, stored simultaneously
as a matrix, a list, and an all-pairs distance table."*

---

## 3. Search algorithms on the graph

All path searches live in `MazeGraph.search(...)` and return a `List<Integer>`
of node ids from start to goal.

### 3.1 A\* search (the agentic favourite)

Used by **Pinky** (`Brain.A_STAR`) and to fetch the tail of the boss's plan.

A\* expands the most promising node first using `f = g + h`:

* `g` = real corridor length travelled so far,
* `h` = heuristic estimate to the goal.

```java
// MazeGraph.java — admissible heuristic on a *wrapping* board
public int heuristic(int u, int v) {
    return colDelta(ncol[u], ncol[v]) + Math.abs(nrow[u] - nrow[v]);
}
// colDelta respects the tunnel: distance("left edge","right edge") is short
public int colDelta(int a, int b) {
    int d = Math.abs(a - b);
    return Math.min(d, Maze.COLS - d);
}
```

```java
// MazeGraph.java — the priority queue keyed on f = g + h
PriorityQueue<Entry> open = new PriorityQueue<>(Comparator.comparingDouble(e -> e.f));
open.add(new Entry(useHeuristic ? heuristic(start, goal) : 0, start));
while (!open.isEmpty()) {
    Entry e = open.poll();
    if (closed[e.node]) continue;          // stale duplicate, skip
    closed[e.node] = true;
    if (e.node == goal) return reconstruct(prev, start, goal);
    for (int v : adj[e.node]) {
        double tentative = gScore[e.node] + edgeW[e.node][v];
        if (tentative < gScore[v]) {
            gScore[v] = tentative; prev[v] = e.node;
            open.add(new Entry(tentative + heuristic(v, goal), v));
        }
    }
}
```

Because the heuristic **never over-estimates** (one step changes the wrap-aware
Manhattan distance by at most 1), A\* is guaranteed to return a shortest path,
while expanding far fewer nodes than Dijkstra.

*Complexity:* `O(E log V)` with the binary heap.

### 3.2 Dijkstra's algorithm

Used by **Clyde** (`Brain.DIJKSTRA`). It is literally the same method with
`useHeuristic = false`, i.e. `h = 0`. It explores in rings of increasing `g`.

**Say this:** *"A\* and Dijkstra are the same code; A\* just adds an admissible
heuristic so it explores fewer nodes but still finds the optimal route."*

### 3.3 Floyd–Warshall (all-pairs shortest paths)

Run **once at level build time** in the `MazeGraph` constructor. It fills
`allPairs[u][v]` with the shortest tile distance between every pair of nodes.

```java
// MazeGraph.java — classic triple loop
for (int k = 0; k < nodeCount; k++)
    for (int i = 0; i < nodeCount; i++) {
        if (allPairs[i][k] == INF) continue;
        for (int j = 0; j < nodeCount; j++)
            allPairs[i][j] = Math.min(allPairs[i][j],
                                      allPairs[i][k] + allPairs[k][j]);
    }
```

*Complexity:* `O(V³)` — cheap enough because `V ≈ 100–300` and it runs once.
Its pay-off: the **minimax evaluation function** can ask "how far is the ghost
from Pac-Man?" in `O(1)` via `distance(u, v)` instead of re-searching.

### 3.4 Translating a path into motion

```java
// Ghost.java — follow the plan: turn toward the next node
dir = graph.dirBetween(here, path.get(1));
```

`dirBetween` uses `edgeDir[u][v]`, stored while building edges, so turning the
plan into a direction is an array read.

---

## 4. `SearchTree` + Minimax (the boss brain)

**File:** `SearchTree.java` — this is the most "AI" part of the project.

### 4.1 The idea

Blinky (`Brain.BOSS`) and the solo `HUNTER` play a **two-player game** against
Pac-Man. Each ply is one move *on the graph*:

```
            ghost to move            Pac-Man to move
   ┌────────────────────┐   ┌──────────────────────────┐
   │ ghost picks a node │──▶│ simulated Pac-Man answers │──▶ ... depth 4
   └────────────────────┘   └──────────────────────────┘
```

`DEPTH = 4` → ghost → Pac-Man → ghost → Pac-Man. With a branching factor of
~3 that is only a few dozen leaves, so the tree can be re-evaluated at **every
intersection**.

### 4.2 The decision function

```java
// SearchTree.java — choose the neighbour with the best minimax value
public int chooseMove(int ghostNode, int pacmanNode, Direction reverse) {
    int best = -1; double bestScore = NO_MOVE, alpha = NO_MOVE;
    for (int next : graph.neighbors(ghostNode)) {
        if (graph.dirBetween(ghostNode, next) == reverse) continue; // no reversing
        double score = minimax(next, pacmanNode, DEPTH - 1, false,
                               alpha, Double.POSITIVE_INFINITY, pacmanNode);
        if (score > bestScore) { bestScore = score; best = next; }
        if (score > alpha) alpha = score;
    }
    return best;
}
```

### 4.3 Minimax with alpha–beta pruning

```java
// SearchTree.java — boss maximises, Pac-Man minimises
private double minimax(int gNode, int pNode, int depth, boolean maxing,
                       double alpha, double beta, int realPacman) {
    if (depth == 0) return evaluate(gNode, pNode, realPacman);
    if (maxing) {
        double best = NO_MOVE;
        for (int next : graph.neighbors(gNode)) {
            best = Math.max(best, minimax(next, pNode, depth-1, false, alpha, beta, realPacman));
            alpha = Math.max(alpha, best);
            if (alpha >= beta) break;      // prune: Pac-Man would never allow this
        }
        return best;
    }
    double best = -NO_MOVE;
    for (int next : graph.neighbors(pNode)) {
        best = Math.min(best, minimax(gNode, next, depth-1, true, alpha, beta, realPacman));
        beta = Math.min(beta, best);
        if (alpha >= beta) break;          // prune
    }
    return best;
}
```

*Alpha* = best score the **ghost** can already guarantee; *beta* = best score
**Pac-Man** can guarantee. When `alpha >= beta` the branch is hopeless and is
cut, roughly halving the effective branching factor.

### 4.4 The evaluation function (the "intelligence")

Leaves are scored from the ghost's point of view. Bigger = better for the ghost:

```java
// SearchTree.java (paraphrased weights)
double score = -W_REAL * dReal     // reward being close to the REAL Pac-Man
             - W_MODEL * dModel    // reward being close to the simulated Pac-Man
             + W_CORNER * (4 - exits);   // reward Pac-Man having few exits
if (dReal <= 1)             score += BONUS_CLOSE;    // capture next step
if (pacman is cornered)     score += BONUS_TRAPPED;  // trap detected
```

This is why the boss *feels* smart: it does not just shorten distance, it
actively prefers game states where Pac-Man's escape options collapse.

*Complexity:* `O(b^d)` worst case, `≈ O(b^(d/2))` with good move ordering /
pruning (`b ≈ 3`, `d = 4`).

**Say this:** *"The boss runs a depth-4 minimax game tree with alpha–beta
pruning; its leaf heuristic rewards closing distance **and** removing the
player's escape routes."*

---

## 5. `RingBuffer` + trajectory prediction (the tracker brain)

**File:** `RingBuffer.java` (data structure) + `Ghost.trackerIntercept(...)` (AI)

### 5.1 The data structure: a fixed-capacity circular buffer

Pac-Man's short-term memory is a **ring buffer of capacity 10**. Writing is
`O(1)`: a cursor walks forward and, once full, overwrites the oldest slot
instead of shifting the array (`list.pop(0)` in a naive implementation).

```java
// RingBuffer.java — O(1) append with wraparound
public void push(T value) {
    data[head] = value;
    head = (head + 1) % capacity;   // wrap -> "ring"
    if (size < capacity) size++;
}
```

`Pacman.java` pushes his direction once per new tile:

```java
// Pacman.java — O(1) memory update
private void recordMove() {
    int tx = tileX(), ty = tileY();
    if (tx != lastTileX || ty != lastTileY) {
        if (dir != Direction.NONE) history.push(dir);
        lastTileX = tx; lastTileY = ty;
    }
}
```

### 5.2 The AI: predict where Pac-Man is going

`RingBuffer.dominant()` finds his **most frequent recent direction**, with ties
broken toward the newest move.

```java
// RingBuffer.java — mode of the window, newest wins ties
public T dominant() {
    T best = null; int bestCount = 0;
    for (int i = size - 1; i >= 0; i--) {          // newest -> oldest
        int c = 0;
        for (int j = 0; j < size; j++) if (same(get(j), get(i))) c++;
        if (c > bestCount) { bestCount = c; best = get(i); }
    }
    return best;
}
```

Inky then walks that trend forward until the next intersection:

```java
// Ghost.java — read the memory, race to the junction Pac-Man is rolling toward
public int trackerIntercept(Game game, MazeGraph graph) {
    Direction trend = p.dominantDirection();
    int c = p.tileX(), r = p.tileY();
    for (int i = 0; i < TRACK_LOOKAHEAD; i++) {
        if (!maze.canMove(c, r, trend, false)) break;
        c = Maze.wrapCol(c + trend.dx); r += trend.dy;
        int node = graph.nodeAt(c, r);
        if (node >= 0) return node;      // the junction to ambush
    }
    return graph.nearestNode(c, r);
}
```

Inky then paths to that node with A\* — a **flanking** move instead of a chase.

*Complexity:* `push` `O(1)`; `dominant` `O(n²)` with `n = 10` (trivial);
prediction `O(lookahead)`.

**Say this:** *"Inky keeps the player's last 10 moves in a ring buffer and
attacks the junction that the player's dominant direction leads to — that is
online opponent modelling."*

---

## 6. The classic 1980 targeting layer (baseline AI)

**File:** `Game.targetFor(...)` — this is authentic arcade behaviour, and every
agentic brain ultimately steers toward *these* tiles (or its own variant).

| Ghost | Chase target | Formula |
|-------|--------------|---------|
| Blinky | Pac-Man's tile | `(px, py)` |
| Pinky | 4 tiles ahead | `(px + 4·dx, py + 4·dy)` |
| Inky | vector through Blinky | `2·(px+2·dx, py+2·dy) − Blinky` |
| Clyde | Pac-Man if far, else corner | scatter target when `dist ≤ 8` |

Scatter mode sends each ghost to a fixed corner; the mode timer alternates
SCATTER/CHASE exactly like the arcade (`MODE_KIND`/`MODE_DURATION`).

`Ghost.classicStep(...)` implements the literal 1980 rule (no reversal, pick the
non-reverse direction whose tile is closest to the target, ties broken **UP,
LEFT, DOWN, RIGHT**). It is used for the "eaten eyes" flight home and as a
safety fallback when a search returns nothing.

**Say this:** *"The arcade targeting is the baseline; the graph searches are
layered on top so the ghosts keep the classic feel but plan smarter routes."*

---

## 7. `SparseMatrix` + wall bitmask (memory optimisation)

**File:** `SparseMatrix.java` (used by `Maze.java`)

Not an AI structure, but it is a data-structure talking point and it directly
affects how the (agentic) game runs as the level empties.

* The board starts as a dense `char[31][28]`.
* When **80 % of pellets are eaten**, the survivors are serialised into a
  **COO sparse matrix** — three parallel arrays `rows[], cols[], vals[]`, kept
  sorted by the packed key `row·28 + col`, so lookups are a **binary search**.
* The static walls are packed into a **one-bit-per-tile bitmask** (`long[]`),
  so the big grid can be handed to the garbage collector.

```java
// SparseMatrix.java — O(log n) lookup over packed keys
private int indexOf(int r, int c) {
    int key = r * COLS + c, lo = 0, hi = n - 1;
    while (lo <= hi) {
        int mid = (lo + hi) >>> 1, k = keyAt(mid);
        if (k == key) return mid;
        if (k < key) lo = mid + 1; else hi = mid - 1;
    }
    return -1;
}
```

The console prints the exact byte saving on each conversion (≈ 68 % in the
shipped level). **Say this:** *"The board degrades gracefully from a dense grid
to a sparse COO matrix plus a wall bitmask, saving memory without changing the
gameplay."*

---

## 8. `Trie` + `CommandEngine` (the other data structure)

**Files:** `Trie.java`, `CommandEngine.java`

A **trie (prefix tree)** recognises hidden cheat codes. Each node has 26 children
(one per `a–z`); a word is inserted by walking/creating the chain and marking the
last node.

```java
// Trie.java — O(L) insertion for an L-letter code
public void insert(String word) {
    Node at = root;
    for (int i = 0; i < word.length(); i++) {
        int k = word.charAt(i) - 'a';
        if (at.next[k] == null) at.next[k] = new Node();
        at = at.next[k];
    }
    at.word = word;
}
```

`CommandEngine` keeps a single **cursor** inside the trie that *is* the memory
of the partially typed command; matching `speedup` / `freeze` / `godmode` is
`O(L)` and never blocks the frame loop.

**Say this:** *"A trie turns a stream of keystrokes into O(length) command
recognition, with the cursor acting as built-in memory."*

---

## 9. How the pieces make the ghosts *agentic*

1. **World model** — `MazeGraph` (adjacency matrix + list + Floyd–Warshall
   distances) gives every ghost the same map abstraction.
2. **Goal selection** — `Game.targetFor` (classic arcade) *plus* the graph's
   `nearestNode` / `nodeAhead` turn a pixel target into a graph node.
3. **Planning** — A\* (Pinky), Dijkstra (Clyde), minimax (Blinky/Hunter),
   interception (Inky) each produce an explicit plan (`plannedPath`).
4. **Memory & prediction** — the `RingBuffer` lets Inky model the player.
5. **Action** — `dirBetween` + grid movement execute the plan one tile at a time.
6. **Readable reasoning** — press **G** and the debug overlay draws the graph,
   every ghost's `plannedPath`, the boss's chosen move, and Inky's intercept
   node (`Renderer.drawGraph`), so the planning is *visible*.

**Say this:** *"A graph for the world model, A\*/Dijkstra/minimax for planning,
a ring buffer for memory, and a trie for input — together they form the
perceive–plan–act loop that makes the ghosts agents rather than random movers."*

---

## 10. Complexity cheat-sheet (memorise this table)

| Structure / algorithm | Operation | Complexity | Used by |
|-----------------------|-----------|------------|---------|
| `MazeGraph` build | construct | `O(V·E)` edges + `O(V³)` Floyd–Warshall | once per level |
| A\* | shortest path | `O(E log V)` | Pinky, boss tail |
| Dijkstra | shortest path | `O(E log V)` | Clyde |
| Floyd–Warshall | all pairs | `O(V³)` (once) | minimax evaluation |
| Minimax + α–β | decision | `O(b^d)` → `≈O(b^(d/2))` | Blinky, Hunter |
| `RingBuffer.push` | append | `O(1)` | Pacman memory |
| `RingBuffer.dominant` | mode | `O(n²)`, `n=10` | Inky prediction |
| `SparseMatrix.get/eat` | lookup | `O(log n)` / `O(n)` | Maze memory |
| `Trie.insert` / match | command | `O(L)` | cheat codes |

Where `V` = graph nodes, `E` = graph edges, `b` = branching factor (~3),
`d` = minimax depth (4), `L` = command length.

---

## 11. Which ghost proves which topic (great for Q&A)

* **"Show me a data structure."** → `MazeGraph`: matrix **and** list **and**
  all-pairs table for the same graph.
* **"Show me an informed search."** → A\* (`MazeGraph.search`, Pinky).
* **"Show me memory."** → `RingBuffer` (Inky intercepting ahead of an
  intersection).
* **"Show me adversarial AI."** → `SearchTree` minimax with alpha–beta
  (Blinky cornering Pac-Man).
* **"Show me a memory optimisation."** → `SparseMatrix` COO + wall bitmask.
* **"Show me the input structure."** → `Trie` / `CommandEngine`.

---

## Appendix A — Levels and the graph

The game now ships **five levels** (`Maze.LEVELS`). Each layout keeps the same
ghost-house, tunnel and spawn tiles (so the actor logic is unchanged) but packs
the outer thirds with more walls and junctions. Because more junctions mean a
bigger graph, later levels are genuinely harder for both the player and the
search algorithms:

| Level | Nodes | Edges | Notes |
|-------|-------|-------|-------|
| 1 | 130 | 354 | classic, sparse junctions |
| 2 | 288 | 816 | dense loops |
| 3 | 242 | 630 | corridor lattice |
| 4 | 256 | 670 | many dead ends |
| 5 | 208 | 530 | narrowest routes |

`Game.rebuildGraph()` rebuilds `MazeGraph` and `SearchTree` on every level
change, so the AI always reasons on the correct maze.

## Appendix B — The "saved one" (classic 1980) version

The separate `saved one/` project is a **plain arcade Pac-Man**: the ghosts use
only the classic 1980 rule set (`Ghost.chooseDirection` — no reversal, nearest
tile to the arcade target, ties **UP, LEFT, DOWN, RIGHT**). No graph, no A\*,
no minimax. The one addition is the **G overlay**, which *simulates* the same
greedy rule a few tiles ahead (`Ghost.computePlannedPath`) and draws each
ghost's upcoming path plus its target tile. The simulation only visualises the
classic movement; it never changes it.
