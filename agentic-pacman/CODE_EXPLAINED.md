# Pac-Man in Java — Complete Line-by-Line Guide for a Python Programmer

> This single file explains **every file, every class and essentially every line**
> of the `pacman-game` project.  It assumes you know Python well and **know nothing
> about Java**.  Every time Java does something Python does differently, we stop and
> compare the two.  Read it top to bottom once and you will be able to open any
> `.java` file in this project and understand it — and explain it to your professor.

The project implements a classic Pac-Man arcade clone and then layers the six
phases of the assignment on top of it, turning the ghosts from "wandering sprites"
into **agents** that plan on a graph, remember the player's habits, reason ahead
with a search tree and react to secret cheat codes.

---

## Table of contents

1. [How to run the project](#1-how- -run-the-project)
2. [Java for Python programmers (crash course)](#2-java-for-python-programmers-crash-course)
3. [The map: which file contains which phase](#3-the-map-which-file-contains-which-phase)
4. [Data structures used, and the Python equivalent](#4-data-structures-used-and-the-python-equivalent)
5. File-by-file walkthrough
   - [Direction.java](#51-directionjava)
   - [Entity.java](#52-entityjava)
   - [Maze.java](#53-mazejava)
   - [SparseMatrix.java](#54-sparsematrixjava-phase-6)
   - [RingBuffer.java](#55-ringbufferjava-phase-3)
   - [MazeGraph.java](#56-mazegraphjava-phase-2)
   - [SearchTree.java](#57-searchtreejava-phase-4)
   - [Trie.java](#58-triejava-phase-5)
   - [CommandEngine.java](#59-commandenginejava-phase-5)
   - [Pacman.java](#510-pacmanjava)
   - [Ghost.java](#511-ghostjava-the-agentic-core)
   - [Game.java](#512-gamejava)
   - [Renderer.java](#513-rendererjava)
   - [PacmanApp.java](#514-pacmanappjava)
   - [SoundManager.java](#515-soundmanagerjava)
   - [build.sh and run.sh](#516-buildsh-and-runsh)
   - [The tests](#517-the-tests-folder)
6. [Cheat codes and debug keys](#6-cheat-codes-and-debug-keys)
7. [Complexity summary](#7-complexity-summary)

---

## 1. How to run the project

The project lives in `pacman-game/`.  There is no Maven, no Gradle, no `pip`.
Compilation is done directly with the JDK compiler `javac`.

```bash
cd pacman-game
./build.sh      # compiles every .java file under src/ into out/
./run.sh        # launches the JavaFX window (builds first if needed)
```

`build.sh` is a four-line bash script.  `run.sh` starts the JVM (the Java Virtual
Machine).  We explain both line by line in section 5.16.

To run the automated tests (no window needed):

```bash
javac -cp out -d test test/*.java      # compile the tests against out/
java  -cp out:test TestLogic           # run one test class
java  -cp out:test TestAgentic         # run the new agent/data-structure tests
```

In Python you would say `python -m pytest`.  In Java the compiler and the runtime
are two separate programs: `javac` turns `.java` source into `.class` bytecode,
and `java` runs the bytecode.

---

## 2. Java for Python programmers (crash course)

Everything below appears in this project.  Skim this section now; come back to it
whenever a keyword in the code surprises you.

### 2.1 Static typing and declared variables

Python:

```python
speed = 10
name = "Pinky"
```

Java:

```java
int speed = 10;
String name = "Pinky";
```

- `int`, `double`, `char`, `boolean` are **primitive types** (fast, stored inline).
  They map to Python's `int`, `float`, `str`, `bool` for most purposes.
- **Every local variable and every field must declare its type** and end its
  statement with a semicolon `;`.
- Python's dynamic typing (`x = anything`) does not exist for primitives.
- `double` is the normal floating point type (Python `float` is also a double).
- `char` is a single character written in **single quotes**: `'a'`.  A string uses
  double quotes: `"abc"`.

### 2.2 Classes, methods, fields, `this`

Python:

```python
class Ghost:
    def __init__(self, kind):
        self.kind = kind
    def greet(self):
        return "I am " + self.kind
```

Java:

```java
public class Ghost {
    public final String kind;          // field
    public Ghost(String kind) {        // constructor
        this.kind = kind;
    }
    public String greet() {            // method
        return "I am " + this.kind;
    }
}
```

- `this` is Python's `self`, but it is **implicit** in Java: you do not list it in
  the parameter list.  You only write `this.kind` when a parameter/field name
  clashes.
- A constructor is a method with the same name as the class and no return type.
  It replaces `__init__`.
- **Fields** are declared at class level with a type.

### 2.3 Access modifiers: `public`, `private`, `protected`

- `public` — visible to everyone (like Python's "public by convention").
- `private` — visible only inside the same class (Java *enforces* it; Python only
  uses the `_name` convention).
- `protected` — visible inside the class, its subclasses and its package.
- No modifier = "package-private" (visible to files in the same `package`).

### 2.4 `final`, `static`

- `final` on a variable = "cannot be reassigned".  This is Python's constant
  convention (`MAX = 10`), but enforced by the compiler.  `final` on a class means
  "cannot be subclassed"; on a method "cannot be overridden".
- `static` = "belongs to the class, not to an instance".  Python's `@staticmethod`
  and `@classmethod`, plus module-level constants.  A `static` field exists once
  for the whole program, e.g. `Maze.COLS`.

### 2.5 Inheritance and abstract classes

Python:

```python
class Entity:
    def update(self, dt): ...
class Pacman(Entity):
    def update(self, dt): ...
```

Java:

```java
public abstract class Entity {           // cannot be instantiated directly
    public abstract void update(double dt);   // no body -> subclass must provide it
}
public class Pacman extends Entity {
    @Override
    public void update(double dt) { ... }
}
```

- `extends` is Python's `(Base)`.
- `abstract` is Python's `abc.ABC` + `@abstractmethod`.
- `@Override` is optional but good practice; it lets the compiler check you really
  are overriding.
- `super(...)` in a constructor calls the parent constructor; it is Python's
  `super().__init__(...)`.

### 2.6 `enum` (enumeration)

Python's `enum.Enum`:

```python
from enum import Enum
class Direction(Enum):
    UP = 1
```

Java's enum is a full class and is the idiomatic way to model "one of a fixed set
of values".  This project uses enums for directions, ghost kinds, states.

```java
public enum Direction { UP, LEFT, DOWN, RIGHT, NONE }
```

Each constant also has an integer **ordinal** (`UP`=0, `LEFT`=1, ...), available
through `.ordinal()`, and `Direction.values()` returns the array of all constants.

### 2.7 Collections: `List`, `Map`, arrays

Python: everything is a `list`/`dict`/`set`/`tuple`.

Java has interfaces and concrete classes, and you pick types:

| Python | Java typical | Notes |
|---|---|---|
| `list` | `ArrayList<T>` (interface `List<T>`) | ordered, indexed, resizable |
| `tuple` | array `T[]` or a record/class | fixed size |
| `dict` | `HashMap<K,V>` (interface `Map<K,V>`) | key → value |
| `set` | `HashSet<T>` | unique items |
| `deque` | `ArrayDeque<T>` | double ended queue |
| `heapq` | `PriorityQueue<T>` | a min-heap |

So `List<Ghost> ghosts = new ArrayList<>();` is Python
`ghosts: list[Ghost] = []`.  The `<...>` part is **generics**: the type carried
inside the container.  (Python uses `list[Ghost]` type hints; Java enforces it.)

Arrays are the fixed-size primitive structure: `int[] a = new int[10];` is Python
`a = [0]*10` but with a fixed length and element type.

### 2.8 `for`, `while`, `if`, `switch`

Python:

```python
for i in range(10):
    if i % 2 == 0:
        print(i)
```

Java:

```java
for (int i = 0; i < 10; i++) {   // init; condition; step
    if (i % 2 == 0) {
        System.out.println(i);   // System.out.println == print()
    }
}
```

Java also has the "enhanced for" (Python's `for x in items`):

```java
for (Direction d : ORDER) { ... }   // for d in ORDER
```

`switch` is Python's `match`/`case` (older style, uses `break` or `->`):

```java
switch (state) {
    case PLAYING: run(); break;
    default: break;
}
```

### 2.9 Exceptions & `null`

- Java uses `null` (Python's `None`).
- Exceptions are like Python's, but you must declare *checked* ones or catch them.
  This project only throws `IllegalArgumentException` (unchecked) and catches
  exceptions in the audio code.
- `try { ... } catch (Exception e) { ... }` is `try/except`.

### 2.10 Strings

- `+` concatenates, same as Python.
- `String.format` / `System.out.printf` is Python's `%` or f-string formatting:
  `%d` int, `%.1f` float with one decimal, `%n` newline (portable `\n`).
- `StringBuilder` is used to build strings in a loop efficiently (Python often just
  does `"".join(...)`; `StringBuilder` is the Java equivalent).

### 2.11 Packages and imports

- The first line of every file is `package com.pacman;`.  A package is a namespace
  and a folder: `src/com/pacman/*.java`.  Python's `package` is the folder +
  `__init__.py`; here it is explicit in every file.
- `import java.util.List;` is Python's `from java.util import List`.
- Classes in the same package do **not** need imports.

### 2.12 Lambdas and functional interfaces

Java has lambdas too:

```java
scene.setOnKeyPressed(e -> onKeyPressed(e.getCode()));  // lambda: e -> ...
```

This is Python `scene.on_key_pressed(lambda e: on_key_pressed(e.code))`.
`Runnable` (used in `SoundManager`) is basically a zero-argument function.

### 2.13 Threads

`SoundManager` runs audio on a background thread so the game loop never blocks.
In Python you would use `threading.Thread`.  Java uses `ExecutorService` +
`Runnable`.

### 2.14 The JavaFX GUI library

The window, canvas, key events and the per-frame game loop come from **JavaFX**.
`javafx.scene.canvas.GraphicsContext` is the "pen" you draw with, analogous to
Python's `tkinter.Canvas` or `pygame.surface`.  We cover the API as it appears.

---
## 3. The map: which file contains which phase

| Assignment phase | Goal | Where it lives |
|---|---|---|
| Phase 1 – Foundation & UI | maze, entities, keyboard, collision | `Maze`, `Entity`, `Pacman`, `Renderer`, `PacmanApp`, `Game` |
| Phase 2 – Graph & pathfinding | turn the maze into a graph, A*/Dijkstra chase | `MazeGraph`, `Ghost` (brains `A_STAR`, `DIJKSTRA`) |
| Phase 3 – Ring buffer memory | remember the player's last moves, flank him | `RingBuffer`, `Pacman.history`, `Ghost` (brain `TRACKER`) |
| Phase 4 – Search tree | minimax look-ahead for the boss ghost | `SearchTree`, `Ghost` (brain `BOSS`) |
| Phase 5 – Trie command engine | hidden cheat codes | `Trie`, `CommandEngine`, `Game.feedCheat` |
| Phase 6 – Sparse matrix | drop the dense grid when 80 % eaten | `SparseMatrix`, `Maze.maybeCompress` |

The **main purpose** is the middle column: making the ghosts *agentic*.  Each ghost
now owns a `Brain`:

| Ghost | Colour | Brain | What it does |
|---|---|---|---|
| Blinky | red | `BOSS` | minimax search tree (phase 4) |
| Pinky | pink | `A_STAR` | A* shortest path on the graph (phase 2) |
| Inky | cyan | `TRACKER` | reads Pac-Man's ring buffer and intercepts (phase 3) |
| Clyde | orange | `DIJKSTRA` | Dijkstra shortest path (phase 2) |

---

## 4. Data structures used, and the Python equivalent

This is the short version used by the professor's rubric.

1. **Graph** (`MazeGraph`) — adjacency matrix + adjacency list + Floyd-Warshall.
   Nodes = intersections/corners, edges = corridors.  Drives A*/Dijkstra pathfinding.
   *Python:* a `dict[int, list[int]]` plus a `list[list[int]]` matrix.
2. **Ring Buffer** (`RingBuffer<T>`) — fixed-size circular queue, O(1) push, used as
   Pac-Man's memory / context window.
   *Python:* a `collections.deque(maxlen=10)`.
3. **Search Tree** (`SearchTree`) — depth-3 minimax tree with alpha-beta pruning and
   a heuristic evaluation function.
   *Python:* recursive `minimax()` over a game tree.
4. **Trie** (`Trie`) — prefix tree for O(L) cheat-code matching.
   *Python:* nested `dict` of characters.
5. **Sparse Matrix** (`SparseMatrix`) — COO (Coordinate List) storage of the few
   remaining pellets, replacing the dense 2-D grid.
   *Python:* three parallel lists `rows, cols, vals` (like `scipy.sparse.coo_matrix`).

---

## 5. File-by-file walkthrough

### 5.1 Direction.java

Path: `src/com/pacman/Direction.java`

```java
package com.pacman;            // this file belongs to the "com.pacman" namespace
```

Everything in the folder `src/com/pacman/` uses this package, so the classes can
see each other without imports.

```java
public enum Direction {
    UP(0, -1),
    LEFT(-1, 0),
    DOWN(0, 1),
    RIGHT(1, 0),
    NONE(0, 0);
```

- `public enum Direction` declares a type with exactly five possible values.
  This is Python's `enum.Enum`, but unlike Python each constant can **carry data**
  and have methods.
- Each constant is followed by `(dx, dy)` — the grid step for that direction.  In
  a screen coordinate system `y` grows **downwards**, so `UP` is `(0, -1)` and
  `DOWN` is `(0, +1)`.  `LEFT` is `(-1, 0)`, `RIGHT` is `(+1, 0)`, and `NONE`
  (standing still) is `(0, 0)`.
- This is a Python `@dataclass`-like enum:

  ```python
  class Direction(Enum):
      UP    = (0, -1)
      LEFT  = (-1, 0)
      DOWN  = (0, 1)
      RIGHT = (1, 0)
      NONE  = (0, 0)
  ```

```java
    public final int dx;
    public final int dy;
```

- `public final int` fields: the step deltas, readable by everyone, never changed.
- This is why `dir.dx` works anywhere in the code, e.g. `x += dir.dx * step;`.

```java
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
```

- An enum constructor.  It is called once per constant at load time, with the
  values from `UP(0, -1)`, etc.  It is implicitly `private`, so you cannot create
  new directions — there are exactly five, forever.
- `this.dx = dx;` copies the parameter into the field.  Python: `self.dx = dx`.

```java
    public Direction opposite() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
            default: return NONE;
        }
    }
}
```

- A method returning the reverse direction.  Ghosts use it to know which way is
  "back", and Pac-Man uses it to allow an instant U-turn.
- `switch (this)` is Python's `match self: case Direction.UP: ...`.  The `default`
  branch catches `NONE`.
- Java `switch` on an enum requires `break` or `return`; here every case `return`s.

---

### 5.2 Entity.java

Path: `src/com/pacman/Entity.java`

`Entity` is the **abstract base class** for anything that moves (Pac-Man and the
ghosts).  It stores position in pixels and provides a small toolkit for
converting between pixels and grid tiles.

```java
package com.pacman;

public abstract class Entity {
```

- `abstract class`: you cannot write `new Entity(...)`.  Python's `abc.ABC`.
  It exists only to be inherited.

```java
    public double x;
    public double y;
    public Direction dir = Direction.NONE;
    public double speed;          // pixels per second
```

- `x, y`: the **centre** of the entity in pixels (not tiles).
- `dir`: current movement direction, initialised to `NONE`.
- `speed`: pixels per second, so movement per frame is `speed * dt`.
- `public` because the renderer reads them directly (`p.x`, `p.y`).

```java
    protected final Maze maze;
    protected final double tile;
```

- `maze`: every entity needs to ask the maze whether a move is legal.
- `tile`: the size of one tile in pixels (20).  Storing it here avoids recomputing.
- `protected` = subclasses may use them; `final` = set once in the constructor.
- Python: `self._maze = maze`, `self._tile = tile`.

```java
    protected Entity(Maze maze, double tile) {
        this.maze = maze;
        this.tile = tile;
    }
```

- Base constructor.  Subclasses call it through `super(maze, tile)`.
  Python: `super().__init__(maze, tile)`.

```java
    public int tileX() {
        return (int) Math.floor(x / tile);
    }

    public int tileY() {
        return (int) Math.floor(y / tile);
    }
```

- Convert a pixel coordinate to a **column / row** index.
  `Math.floor` rounds down; `(int)` casts the `double` to `int`.
- Python: `int(math.floor(self.x / self.tile))`.
- Example: `x = 30`, `tile = 20` → column `1` (tiles 0,1,2 span x = 0..19, 20..39, ...).

```java
    protected double centerX(int c) {
        return c * tile + tile / 2.0;
    }

    protected double centerY(int r) {
        return r * tile + tile / 2.0;
    }
```

- The pixel position of the **centre** of tile column `c` / row `r`.
  `tile / 2.0` is `10`, so tile 0's centre is at `x = 10`.
- Note `2.0` (a double) so the division is floating point, not integer division.

```java
    protected double boardWidth() {
        return Maze.COLS * tile;
    }
```

- The playfield width in pixels: 28 columns × 20 = 560.  Used for screen wrapping.

```java
    protected void wrapX() {
        double w = boardWidth();
        if (x < 0) {
            x += w;
        } else if (x >= w) {
            x -= w;
        }
    }
```

- The left/right tunnel: if the entity walks off the left edge, teleport it to the
  right edge (and vice versa).  `x += w` / `x -= w` instead of `x = ...` preserves
  the fractional progress, which keeps movement smooth.

```java
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
```

- `atCenter`: are we close enough to the centre of the current tile to turn a
  corner?  The `+ 0.01` tolerance prevents floating-point misses.  `Math.abs` is
  Python's `abs`.
- `snapToCenter`: hard-set the position to the exact tile centre.  Used after a
  decision so the grid logic stays aligned.
- (These two helpers exist for completeness; `Pacman` and `Ghost` compute the same
  test inline because they also need the centre coordinates.)

---

### 5.3 Maze.java

Path: `src/com/pacman/Maze.java`

Stores the level and, after the 80 % threshold, runs the phase-6 conversion.

```java
public class Maze {

    public static final int COLS = 28;
    public static final int ROWS = 31;

    public static final char WALL = '#';
    public static final char PELLET = '.';
    public static final char POWER = 'o';
    public static final char DOOR = '-';
    public static final char EMPTY = ' ';
```

- The board is 28 columns wide and 31 rows tall.
- These `static final char` constants are the legend used in the map.
  `static` = one shared copy for the whole program; `final` = constant.
  Python: module-level `COLS = 28`, `WALL = '#'`.

```java
    private static final String[] LAYOUT = {
        "############################",
        "#............##............#",
        ...
        "############################",
    };
```

- The level itself, an array of 31 strings of 28 characters each.
  This is exactly Python's `LAYOUT = [ "...", ... ]`.
- `.` is a pellet, `o` a power pellet, `#` a wall, `-` the ghost-house door and a
  space is empty floor.
- This is the raw data the whole game is built from; the graph and the walls are
  extracted from it.

```java
    private static final int WALL_WORDS = (ROWS * COLS + 63) / 64;

    private char[][] tiles = new char[ROWS][COLS];
    private SparseMatrix sparse;
    private boolean compressed;
    private static long[] wallBits;
    private static final int[] DOOR_KEYS = findDoorKeys();

    private int pelletsLeft;
    private int totalPellets;
```

- `WALL_WORDS`: how many 64-bit `long` words are needed to store one bit per tile
  (868 bits → 14 words).  This is used **after** compression to remember the walls
  cheaply.  The expression `(n + 63) / 64` is integer ceiling division.
- `tiles`: the **dense** `char[31][28]` grid — the "big array" the plan tells us to
  destroy.  In Python this is a list of lists: `[[...], [...], ...]`.
- `sparse`: after compression, the surviving pellets as a `SparseMatrix`.  `null`
  while we are still dense.
- `compressed`: a flag telling every query which representation to use.
- `wallBits`: the packed wall bitmask, built lazily (only when we compress).
- `DOOR_KEYS`: the positions of the two door tiles as packed integers, computed
  once at class-load by `findDoorKeys()`.
- `pelletsLeft` / `totalPellets`: pellets remaining and the level total.

```java
    public Maze() {
        reset();
    }
```

- Constructor: build the level.  Python's `__init__`.

```java
    public void reset() {
        compressed = false;
        sparse = null;
        if (tiles == null) {
            tiles = new char[ROWS][COLS];
        }
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
```

- `reset()` rebuilds the level for a new game/level.
- Sets us back to **dense mode** (`compressed = false`, `sparse = null`).
- If the dense grid was dropped, allocate it again (`tiles = new char[ROWS][COLS]`).
- The double loop copies each character from the `LAYOUT` strings into the 2-D array.
  `LAYOUT[r].charAt(c)` is Python's `LAYOUT[r][c]` — in Java a `String` is not
  directly indexable, you call `.charAt(c)`.
- Every `.` or `o` increments `pelletsLeft`.  At the end `totalPellets` remembers
  the starting count so the 80 % rule can be computed later.

```java
    public static int wrapCol(int c) {
        int m = c % COLS;
        return m < 0 ? m + COLS : m;
    }
```

- Wraps a column index into `0..27`.  Java's `%` can be negative for negative
  inputs (`-1 % 28 == -1`), so we shift it up.  Python's `%` is already
  non-negative, but writing it this way is portable.
- `m < 0 ? m + COLS : m` is Python's `m + COLS if m < 0 else m`.

```java
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
```

- The universal "what is on this tile?" query, used by physics, rendering and tests.
- Rows do **not** wrap (walking off the top/bottom is blocked → treated as `WALL`),
  which is why out-of-range rows immediately return `WALL`.
- **Dense mode** (bottom line): a single array lookup `tiles[r][c]`.
- **Compressed mode**: first ask the sparse matrix whether a pellet survived here;
  if not, check the two door tiles, then the packed wall bits, otherwise floor.
  This reconstructs the same answer as the dense array without keeping the array.

```java
    public boolean isWall(int c, int r) {
        return tileAt(c, r) == WALL;
    }

    public boolean isDoor(int c, int r) {
        return tileAt(c, r) == DOOR;
    }
```

- Convenience predicates.  Because they route through `tileAt`, they automatically
  work in both dense and compressed mode.

```java
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
```

- "Can an entity standing on tile `(c, r)` legally step one tile in `dir`?"
- `NONE` never moves.  Vertical bounds are checked first (no wrap up/down).
- `tileAt` handles the horizontal wrap, so `c + dir.dx` may be `-1` or `28` safely.
- A wall blocks everyone.  A door blocks Pac-Man and normal ghosts but lets "eaten"
  eyes through — that is what the `canUseDoor` flag controls.

```java
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
```

- Consumes whatever is under `(c, r)`.
- Returns `0` nothing, `1` a normal pellet, `2` a power pellet — the caller adds the
  right score and triggers (or not) a fright.
- Dense mode erases the character and decrements the counter.
- Compressed mode delegates to `SparseMatrix.eat` (binary search + removal) and
  decrements the same counter.
- `pelletsLeft--` is Python's `pellets_left -= 1`.

```java
    public int pelletsLeft() { return pelletsLeft; }
    public int totalPellets() { return totalPellets; }
```

- Getters (Java has no `@property`; you write tiny methods).

```java
    public boolean isCompressed() { return compressed; }
    public SparseMatrix sparse() { return sparse; }
```

- Exposed for the renderer (sparse drawing) and the tests.

```java
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
```

- The phase-6 **trigger**.  Called by `Game` after every pellet.
- Bail out if we already compressed or the level had no pellets.
- `eaten * 5 < totalPellets * 4` is the integer form of `eaten / total < 0.8`
  (avoid floating-point comparisons).  Once it is false we are at ≥ 80 %.
- `compress()` does the actual work; returns `true` so the caller can react/log.

```java
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
        tiles = null;
        compressed = true;
        if (wallBits == null) {
            wallBits = buildWallBits();
        }
        long after = afterSparse + wallBytes();
        System.out.println(...);
    }
```

- First pass: count the survivors (to size the arrays exactly).
- Allocate three parallel primitive arrays `rows`, `cols`, `vals` — the COO format.
- Second pass: fill them.  Because we scan row-major, the triples end up sorted,
  which is exactly what `SparseMatrix`'s binary search needs.
- `(condition) ? a : b` is Python's `a if condition else b`.
- `before`, `afterSparse`, `after`: byte estimates for the console report.
- `tiles = null;` — this is the crucial line: we drop the reference to the big
  array so the garbage collector can reclaim it.  Python's `del tiles` equivalent.
- `wallBits` is built lazily here, because only now do the walls need a separate
  representation.
- The `System.out.println` block prints the memory report (see the sample in the
  console output section).

```java
    public static long denseBytes() {
        long outer = 16 + SparseMatrix.align8(8L * ROWS);
        long inner = ROWS * (16 + SparseMatrix.align8(2L * COLS));
        return outer + inner;
    }

    public static long wallBytes() {
        return 16 + SparseMatrix.align8(8L * WALL_WORDS);
    }
```

- `denseBytes()`: estimated heap size of `char[31][28]` under the standard 64-bit JVM
  layout.  `16` bytes is an object header, `8L * ROWS` is the array of row
  references, and each row is a `char[]` whose data is `2 * COLS` bytes padded to
  8 bytes.  (These numbers are the conventional HotSpot estimates.)
- `wallBytes()`: size of the packed bitmask (`long[]` of 14 words).
- These two feed the "bytes saved" number.  They are estimates, which is normal.

```java
    private static long[] buildWallBits() {
        long[] w = new long[WALL_WORDS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (LAYOUT[r].charAt(c) == WALL) {
                    int k = r * COLS + c;
                    w[k >>> 6] |= 1L << (k & 63);
                }
            }
        }
        return w;
    }

    private static boolean wallBit(int c, int r) {
        if (wallBits == null) {
            wallBits = buildWallBits();
        }
        int k = r * COLS + c;
        return ((wallBits[k >>> 6] >>> (k & 63)) & 1L) != 0;
    }
```

- Bit packing in action.  Each tile gets a flat index `k = r*28 + c`.
- `w[k >>> 6]` selects the 64-bit word (`>>> 6` is a fast divide by 64, and `>>>`
  is unsigned shift).  `1L << (k & 63)` builds a mask for the bit inside that word
  (`& 63` is a fast modulo 64).  `|=` sets the bit (Python `w[i] |= mask`).
- `wallBit` reads it back: shift the word right by the bit position, `& 1` isolates
  the bit.  Python: `(w[k >> 6] >> (k & 63)) & 1`.
- This is "quantization / compression" — 868 booleans in 14 machine words.

```java
    private static int[] findDoorKeys() {
        ...counts DOOR tiles, then fills their packed keys...
    }

    private static boolean isDoorKey(int c, int r) {
        int key = r * COLS + c;
        for (int k : DOOR_KEYS) {
            if (k == key) {
                return true;
            }
        }
        return false;
    }
```

- `findDoorKeys`, called once when the class loads, scans `LAYOUT` for the two `-`
  tiles and returns their packed keys.  `isDoorKey` is a linear membership test
  (only two elements, so no need for anything fancier).

---

### 5.4 SparseMatrix.java (phase 6)

Path: `src/com/pacman/SparseMatrix.java`

The COO sparse matrix that replaces the dense grid.

```java
public final class SparseMatrix {

    public static final byte PELLET = 1;
    public static final byte POWER = 2;

    private final int[] rows;
    private final int[] cols;
    private final byte[] vals;
    private int n;
```

- `byte` is an 8-bit integer, perfect for storing `1`/`2`.
- The three arrays are the COO triples.  `n` is the number of **live** entries; the
  arrays keep their capacity but entries at/after `n` are considered deleted.
- `final` arrays: the reference does not change (we mutate the contents, that is
  allowed — same as a Python list in a `final` field).

```java
    public SparseMatrix(int[] rows, int[] cols, byte[] vals) {
        if (rows.length != cols.length || rows.length != vals.length) {
            throw new IllegalArgumentException("COO arrays must have equal length");
        }
        this.rows = rows;
        this.cols = cols;
        this.vals = vals;
        this.n = rows.length;
    }
```

- Constructor: validate the invariant (equal lengths) and store the arrays.
- `throw new IllegalArgumentException(...)` is Python's `raise ValueError(...)`.

```java
    public int size() { return n; }
    public int row(int i) { return rows[i]; }
    public int col(int i) { return cols[i]; }
    public byte value(int i) { return vals[i]; }
```

- Byte-sized accessors for the renderer and the tests.

```java
    private int keyAt(int i) {
        return rows[i] * Maze.COLS + cols[i];
    }
```

- The packed sort key of entry `i`, `row * 28 + col`.  Since we inserted entries in
  row-major order, `keyAt(i)` is strictly increasing, which is what lets us binary
  search.

```java
    public byte get(int r, int c) {
        int i = indexOf(r, c);
        return i < 0 ? 0 : vals[i];
    }
```

- Value at `(r, c)` or `0` if absent.

```java
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
```

- Removes a pellet and returns its type.
- `indexOf` finds the slot; then the three parallel arrays are shifted left by one
  to close the hole.  `System.arraycopy(src, srcPos, dst, dstPos, len)` is the fast,
  native bulk copy — Python would use a slice assignment `rows[i:] = rows[i+1:]`.
- `n--` shrinks the live region; `vals[n] = 0` cleans the stale trailing slot.

```java
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
```

- Mohit's binary search over the packed keys.  O(log n).
- `(lo + hi) >>> 1` is an overflow-safe `(lo + hi) // 2`.
- Returns `-1` when not found (Java uses `-1` where Python might raise or return
  `None`).

```java
    public long estimatedBytes() {
        long r = 16 + align8(4L * rows.length);
        long c = 16 + align8(4L * cols.length);
        long v = 16 + align8(1L * vals.length);
        return r + c + v;
    }

    static long align8(long x) {
        return (x + 7L) & ~7L;
    }
```

- Estimated heap footprint: each array is a 16-byte header plus its data rounded up
  to an 8-byte boundary.  `int[]` data = 4 bytes per element; `byte[]` = 1 byte.
- `align8(x)` rounds up to a multiple of 8: `(x + 7) & ~7`.  Python:
  `(x + 7) & ~7` works identically on integers.

```java
    public String describe() {
        ... prints up to 8 triples, then "... N more" ...
    }
```

- A human-readable summary used in the console report.

---
### 5.5 RingBuffer.java (phase 3)

Path: `src/com/pacman/RingBuffer.java`

A fixed-capacity circular queue.  This is Pac-Man's **memory / context window**.

```java
public final class RingBuffer<T> {
```

- `class RingBuffer<T>` is a **generic** class.  `T` is a type parameter, exactly
  Python's `TypeVar`: `class RingBuffer(Generic[T])`.  Here `T` ends up being
  `Direction`.

```java
    private final Object[] data;
    private final int capacity;
    private int head;
    private int size;
```

- Java cannot allocate `new T[capacity]` (generics are erased at runtime), so the
  backing store is an `Object[]` and reads are cast back to `T`.
- `head` = the slot where the next `push` writes.  `size` = how many live elements.
- Python's `deque(maxlen=capacity)` hides all of this; here we build it explicitly.

```java
    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("RingBuffer capacity must be > 0");
        }
        this.capacity = capacity;
        this.data = new Object[capacity];
    }
```

- Validate the capacity and allocate the array.  `head` and `size` default to `0`.

```java
    public void push(T value) {
        data[head] = value;
        head = (head + 1) % capacity;   // wrap around -> "ring"
        if (size < capacity) {
            size++;
        }
    }
```

- Write at `head`, then advance `head` modulo `capacity`.  When full, the next push
  overwrites the oldest slot — no shifting, so **O(1)** time and space.
- `size` stops growing once the buffer is full.

```java
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(...);
        }
        int start = (head - size + capacity) % capacity;
        return (T) data[(start + index) % capacity];
    }
```

- Logical indexing: index 0 is the **oldest** element still alive.
- `start` computes the physical slot of the oldest (`head - size`, wrapped).
- `@SuppressWarnings("unchecked")` silences the compiler warning about the
  `(T)` cast — the programmer promises it is safe.
- Python analogy:

  ```python
  def __getitem__(self, i):
      start = (self.head - self.size) % self.capacity
      return self.data[(start + i) % self.capacity]
  ```

```java
    public T newest() { return get(size - 1); }
    public T oldest() { return get(0); }
    public int size() { return size; }
    public int capacity() { return capacity; }
    public boolean isFull() { return size == capacity; }
    public boolean isEmpty() { return size == 0; }
```

- Small accessors.  `newest()` is used by the tracker indirectly through `dominant()`.

```java
    public void clear() {
        Arrays.fill(data, null);
        head = 0;
        size = 0;
    }
```

- `Arrays.fill(array, value)` is Python's `data[:] = [value]*len(data)`.
  `clear()` is called on every respawn.

```java
    public int count(T value) {
        int n = 0;
        for (int i = 0; i < size; i++) {
            if (same(get(i), value)) {
                n++;
            }
        }
        return n;
    }
```

- Counts occurrences.  Java `==` on objects compares references, so we use
  `same()` below which calls `.equals` (the object comparison) — Python's `==`.

```java
    public T dominant() {
        if (size == 0) {
            return null;
        }
        T best = null;
        int bestCount = 0;
        for (int i = size - 1; i >= 0; i--) {
            T candidate = get(i);
            int c = 0;
            for (int j = 0; j < size; j++) {
                if (same(get(j), candidate)) {
                    c++;
                }
            }
            if (c > bestCount) {
                bestCount = c;
                best = candidate;
            }
        }
        return best;
    }
```

- The "which direction is Pac-Man favouring?" analysis.
- Scans from **newest to oldest** and only replaces the champion on a *strictly*
  larger count, so ties go to the more recent value.  This matters: if the last
  three moves were `UP` after a long run of `RIGHT`, the trend flips quickly.
- O(size²) with size ≤ 10, totally fine; if it were large you would use a `HashMap`
  tally like Python's `collections.Counter`.

```java
    public String join(String separator) { ... }

    private static boolean same(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
```

- `join` is Python's `separator.join(...)`; `same` is a null-safe `==`.

---

### 5.6 MazeGraph.java (phase 2)

Path: `src/com/pacman/MazeGraph.java`

The centrepiece of the "agentic" upgrade: the maze compressed into a graph.

```java
public final class MazeGraph {

    public static final int INF = 1_000_000_000;

    private static final Direction[] CARDINAL = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };

    private final Maze maze;
    private final int nodeCount;
    private int edgeCount;
```

- `INF` is our "infinity" for un-reachable distances (large but no overflow when
  added).
- `CARDINAL` is the fixed array of the four movement directions; iterating it is
  Python's `for d in (UP, LEFT, DOWN, RIGHT)`.
- `maze` is kept because a few helpers ask it for walkability.

```java
    private final int[] ncol;
    private final int[] nrow;
    private final int[] tileToNode;
    private final int[][] edgeW;
    private final byte[][] edgeDir;
    private final int[][] adj;
    private final int[][] allPairs;
```

- `ncol[node]`, `nrow[node]`: pixel-independent tile coordinates of each node.
- `tileToNode[r*COLS + c]`: the flat lookup "is this tile a node, and which one?".
- `edgeW[u][v]`: the **adjacency matrix** — corridor length in tiles, `0` = no
  direct edge.  A 2-D array in Java is an array of arrays (Python list of lists).
- `edgeDir[u][v]`: which `Direction` to walk to go from `u` to `v`.  We store this
  because inferring it from coordinates is ambiguous at the tunnel.
- `adj[u]`: the **adjacency list** for O(degree) iteration.
- `allPairs[u][v]`: Floyd-Warshall shortest path distances between all node pairs.

```java
    public MazeGraph(Maze maze) {
        this.maze = maze;
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
        ...
```

**Pass 1 — discover the nodes.**  We scan every tile; if it is walkable *and* a
decision point it becomes a node.  `tileToNode` is initialised to `-1` ("not a
node") and then stamped with the node id.  Because we do not know the final count
up front we collect into oversized temporary arrays and then trim with
`Arrays.copyOf(arr, count)` (Python's `arr[:count]`).

```java
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
```

**Pass 2 — walk every corridor.**  For each node `u` and each of the four
directions, we step tile by tile.  Every intermediate tile is a plain corridor
tile (by construction), so we just keep going until we land on another node `v`.
The number of steps is the corridor length (the edge **weight**), and the direction
we were walking is the edge's direction.

- `(byte) d.ordinal()` stores the enum's ordinal (0..3) in one byte.
- The `if (steps > max) break;` is a paranoia guard against an accidental infinite
  loop; with this maze it never fires.
- This is the crucial difference from a coordinate-based weight: the tunnel edge
  from `(9,14)` to `(18,14)` is 19 tiles long (through the wrap), not 9, and the
  loop naturally counts 19.

```java
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
```

**Pass 3 — build adjacency lists** from the matrix: count the neighbours, allocate
exactly that many slots, fill.  This is a "ragged" 2-D array (`int[][] adj` where
each row can have a different length) — Python's `list[list[int]]`.

```java
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
```

**Pass 4 — Floyd-Warshall.**  Initialise the distance matrix from the adjacency
matrix (`INF` for no edge, `0` on the diagonal), then run the classic triple loop:
"is going `i → k → j` shorter than the best `i → j` so far?".  With ~64 nodes that
is 64³ ≈ 260 000 operations, done **once** at startup.  The payoff: any later
"distance between two nodes" query is a single O(1) array read — perfect for the
minimax evaluation function that does thousands of them.

- Python:

  ```python
  for k in range(n):
      for i in range(n):
          for j in range(n):
              if dist[i][k] + dist[k][j] < dist[i][j]:
                  dist[i][j] = dist[i][k] + dist[k][j]
  ```

  The `== INF` guards are just a small optimisation.

```java
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
```

- What counts as a tile a ghost may path through.
- Excludes walls, the door and the **ghost house interior** (rows 13–15, columns
  11–16).  The house is handled by the scripted `HOUSE/LEAVING/ENTERING` states, so
  it is intentionally absent from the graph.

```java
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
```

- The node rule.
- Count how many of the four sides are open and whether they are vertical/horizontal.
- `open != 2` → a junction (3+), a dead end (1) or an isolated tile (0) → node.
- `open == 2` → a node **only** if the two sides turn a corner (one vertical + one
  horizontal).  Two opposite sides (a straight corridor) is not a node.

```java
    public int nodeCount() { return nodeCount; }
    public int edgeCount() { return edgeCount; }
    public int col(int node) { return ncol[node]; }
    public int row(int node) { return nrow[node]; }
    public int nodeAt(int c, int r) {
        if (r < 0 || r >= Maze.ROWS) {
            return -1;
        }
        return tileToNode[r * Maze.COLS + Maze.wrapCol(c)];
    }
    public int[] neighbors(int node) { return adj[node]; }
    public int degree(int node) { return adj[node].length; }
    public int weight(int u, int v) { return edgeW[u][v]; }
    public int distance(int u, int v) { return allPairs[u][v]; }
```

- Straightforward accessors used by the ghosts, the search tree and the renderer.

```java
    public Direction dirBetween(int u, int v) {
        if (u < 0 || v < 0 || edgeW[u][v] == 0) {
            return Direction.NONE;
        }
        return Direction.values()[edgeDir[u][v]];
    }
```

- Converts the stored ordinal back into a `Direction`.
  `Direction.values()` is the array of all enum constants.

```java
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
```

- Linear scan for the closest node to a coordinate, using the wrap-aware column
  distance.  Used to convert "the classic target tile" (often a wall, e.g. a
  scatter corner) into a real node the ghost can path to.

```java
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
```

- "The node an entity is heading towards."  If it is already on a node, that node.
  If it is mid-corridor and moving, walk forward until the next node.  Otherwise
  fall back to the nearest node.
- This is how the boss ghost gets its target: the node Pac-Man is about to reach —
  the basis of an interception, not just a follow.

```java
    public int colDelta(int a, int b) {
        int d = Math.abs(a - b);
        return Math.min(d, Maze.COLS - d);
    }
```

- Wrap-aware column distance: the tunnel makes columns 0 and 27 neighbours, so
  `min(d, 28 - d)`.

```java
    public int heuristic(int u, int v) {
        return colDelta(ncol[u], ncol[v]) + Math.abs(nrow[u] - nrow[v]);
    }
```

- The A* heuristic: wrap-aware Manhattan distance between two nodes.
- This is **admissible**: one tile step changes it by at most 1, so it never
  overestimates the true path length — A* therefore stays optimal even through the
  tunnel.

```java
    public List<Integer> aStar(int start, int goal) {
        return search(start, goal, true);
    }

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
```

- One implementation serves **both** algorithms; the only difference is the
  heuristic (`h = 0` turns A* into Dijkstra).
- `gScore[v]` = cheapest known cost from `start` to `v`.  `prev[v]` = the node we
  came from (for path reconstruction).  `closed[v]` = already finalised.
- The **priority queue** always pops the node with the smallest `f = g + h`.
  `PriorityQueue` is a min-heap, Python's `heapq`.  `Comparator.comparingDouble(e -> e.f)`
  is a lambda telling it to compare by the `f` field.
- `Double.POSITIVE_INFINITY` is Python's `float('inf')`.
- When we pop the goal we reconstruct and return.  Stale (already-closed) entries
  are skipped.
- Returns an empty `List` (`List.of()`, Python `[]`) when unreachable.

```java
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
```

- Walks `prev` backwards from the goal, then reverses — Python's
  `path.reverse()` after following parent pointers.

```java
    private static final class Entry {
        final double f;
        final int node;
        Entry(double f, int node) { this.f = f; this.node = node; }
    }
}
```

- A tiny private value class for queue entries (Python's `namedtuple`).

---

### 5.7 SearchTree.java (phase 4)

Path: `src/com/pacman/SearchTree.java`

The boss ghost's minimax search tree.

```java
public final class SearchTree {

    public static final int DEPTH = 3;

    private static final double W_REAL = 3.0;
    private static final double W_MODEL = 1.5;
    private static final double W_CORNER = 1.5;
    private static final double BONUS_TRAPPED = 12.0;
    private static final double BONUS_CLOSE = 10.0;
    public static final double NO_MOVE = -1.0e9;

    private final MazeGraph graph;
    public int lastNodesVisited;
```

- `DEPTH = 3`: the tree looks three plies ahead: boss move → Pac-Man move → boss
  move.
- The `W_*` and `BONUS_*` constants are the weights of the heuristic evaluation
  function (explained below).
- `lastNodesVisited` is a diagnostic counter shown by the debug overlay/tests.

```java
    public SearchTree(MazeGraph graph) {
        this.graph = graph;
    }
```

- The tree searches over a graph it is given (the maze graph).

```java
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
```

- **Root of the tree.**  For each legal neighbour of the boss (excluding the exact
  reverse, like the arcade), evaluate the resulting state with `minimax` and keep
  the best.
- `alpha` accumulates the best score found so far across root children, which lets
  the deeper search prune.
- `best` is the neighbour node to move to; `-1` means "no legal move".

```java
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
```

- Classic **minimax with alpha-beta pruning**, adapted so each ply is a graph move.
- `maxing == true`: the **boss** chooses, so it maximises the score.
- `maxing == false`: **simulated Pac-Man** chooses, so it minimises the boss's
  score (i.e. it runs away as cleverly as it can).
- `depth == 0` means we reached a leaf and call `evaluate`.
- **Alpha-beta pruning**: `alpha` is the best the maximiser can already guarantee,
  `beta` the best the minimiser can guarantee.  When `alpha >= beta` the current
  branch cannot affect the result and is abandoned (`break`).  This turns the tree
  from "explore everything" into "explore only what matters".
- Note the recursion flips `maxing` and swaps which side moves; the boss's position
  stays fixed while Pac-Man moves, and vice versa.

```java
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
```

- The **heuristic evaluation function** — this is where the "intelligence" is.
  Bigger score = better for the boss.  Terms, in order:
  1. `-W_REAL * dReal`: strongly prefer being close to the **real** Pac-Man.  This
     is what guarantees the boss actually closes in and does not wander.
  2. `-W_MODEL * dModel`: also care about the simulated (future) Pac-Man position,
     so the boss anticipates instead of just tailing.
  3. `+W_CORNER * (4 - exits)`: reward Pac-Man having few exits (`degree`).
  4. `+BONUS_CLOSE` when the real Pac-Man is one edge away (imminent capture).
  5. `+BONUS_TRAPPED` when **every** escape from the simulated Pac-Man leads
     closer to the boss — i.e. there is no reverse-escape path.  This is exactly
     the "closer and no escape" idea from the plan.

  Python version of the same expression:

  ```python
  score = -3.0*d_real - 1.5*d_model + 1.5*(4 - exits)
  ```

- The constants were chosen so distance dominates over long ranges but the tactical
  bonuses decide between moves that are equally far.

```java
    public String describeMove(List<Integer> path) { ... }
}
```

- Debug helper that formats a node path as `(col,row)->(col,row)`.

---

### 5.8 Trie.java (phase 5)

Path: `src/com/pacman/Trie.java`

The prefix tree for the cheat codes.

```java
public final class Trie {

    public static final class Node {
        public final Node[] next = new Node[26];
        public String word;
    }

    private final Node root = new Node();

    public Node root() {
        return root;
    }
```

- `Node` is a **static nested class**: a helper class that does not need an outer
  `Trie` instance (Python's nested class, but Java lets it be `static`).
- `next` is 26 slots for `'a'..'z'`; each slot is either `null` (no such letter
  branch here) or another `Node`.  This is the "dictionary of children" that a
  Python implementation would write as `dict[str, Node]`, but as a fixed array so
  indexing is O(1) and allocation-light.
- `word` is `null` for normal vertices, and holds the full command string at the
  vertex that ends a command ("the leaf is marked valid").

```java
    public void insert(String word) {
        Node at = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            int k = c - 'a';
            if (k < 0 || k >= 26) {
                throw new IllegalArgumentException("Trie only stores a-z, got '" + c + "'");
            }
            if (at.next[k] == null) {
                at.next[k] = new Node();
            }
            at = at.next[k];
        }
        at.word = word;
    }
```

- Walk down the word's letters, creating missing child nodes, then mark the final
  node with the command.  `c - 'a'` converts a letter to 0..25 (Python's
  `ord(c) - ord('a')`).
- Python equivalent:

  ```python
  at = self.root
  for c in word:
      at = at.children.setdefault(c, Node())
  at.word = word
  ```

```java
    public static Node step(Node from, char c) {
        if (from == null) {
            return null;
        }
        int k = c - 'a';
        if (k < 0 || k >= 26) {
            return null;
        }
        return from.next[k];
    }
}
```

- Follows one character from a node; returns `null` when there is no such branch.

---

### 5.9 CommandEngine.java (phase 5)

Path: `src/com/pacman/CommandEngine.java`

The matcher that sits on top of the trie and remembers what has been typed.

```java
public final class CommandEngine {

    private final Trie trie = new Trie();
    private Trie.Node cursor;

    public CommandEngine() {
        trie.insert("speedup");
        trie.insert("freeze");
        trie.insert("godmode");
        reset();
    }

    public void reset() {
        cursor = trie.root();
    }
```

- The three known commands are loaded once at construction.  `cursor` is the trie
  vertex matching the partially-typed word; `reset()` puts it back at the root.
- The **cursor is the memory**: no matter what was typed before, recognising the
  next command is O(L) where L is the command length.

```java
    public String feed(char ch) {
        char c = Character.toLowerCase(ch);
        if (c < 'a' || c > 'z') {
            return null;
        }
        Trie.Node next = Trie.step(cursor, c);
        if (next == null) {
            reset();
            next = Trie.step(cursor, c);
            if (next == null) {
                return null;
            }
        }
        cursor = next;
        if (cursor.word != null) {
            String done = cursor.word;
            reset();
            return done;
        }
        return null;
    }
```

- Called for **every** keystroke.  `Character.toLowerCase` normalises `'S'` to `'s'`.
- Non-letters are ignored.
- If the current cursor has no child for this letter, the partial word was garbage:
  restart from the root and re-test *this* letter, so a command embedded after junk
  (e.g. `...xgodmode`) is still caught.
- If we land on a node with a `word`, a command completed: return it and reset so
  the next command starts fresh.  This is the O(L) tokenization the plan asks for.
- Otherwise return `null` and keep accumulating.

```java
    public Trie.Node cursor() { return cursor; }
}
```

- Exposed for tests / an optional debug display.

---
### 5.10 Pacman.java

Path: `src/com/pacman/Pacman.java`

The player character.  Phase 3 adds the ring-buffer memory here.

```java
public class Pacman extends Entity {

    public static final int MEMORY = 10;
```

- Inherits position/movement from `Entity` (`extends` = Python `(Entity)`).
- `MEMORY = 10`: how many recent moves the context window keeps.

```java
    public Direction desired = Direction.NONE;
    public double animTime;
    public final RingBuffer<Direction> history = new RingBuffer<>(MEMORY);
    private int lastTileX = Integer.MIN_VALUE;
    private int lastTileY = Integer.MIN_VALUE;
```

- `desired`: the direction the player has *asked* for; it is applied when Pac-Man
  reaches a tile centre (input buffering — pressing a key slightly early still
  works).
- `animTime`: seconds since start, used to animate the mouth.
- `history`: the ring buffer.  `final` means the buffer object never changes, but we
  mutate it by pushing.
- `lastTileX/Y`: the tile we last pushed a direction for, so we push exactly once
  per tile.  `Integer.MIN_VALUE` is a sentinel that can't be a real tile.

```java
    public Pacman(Maze maze, double tile) {
        super(maze, tile);
        speed = 128;
    }
```

- Constructor: pass the maze/tile size to `Entity`, set the speed to 128 pixels/s.

```java
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
```

- Respawn: reset position/direction/timer, **clear the memory** and initialise the
  "last tile" trackers to where we start.

```java
    public void update(double dt) {
        double step = speed * dt;
        animTime += dt;

        int col = tileX();
        int row = tileY();
        double ccx = centerX(col);
        double ccy = centerY(row);
        boolean centered = Math.abs(x - ccx) <= step + 0.01
                && Math.abs(y - ccy) <= step + 0.01;
```

- `update` is called every frame.  `dt` is the seconds since the previous frame, so
  `step = speed * dt` is how many pixels we may move this frame.
- `centered` is true when we are essentially on a tile centre, which is the only
  moment a 90° turn is allowed.  The `+0.01` guards against floating-point error.

```java
        // Instant reversal is always allowed.
        if (desired != Direction.NONE && desired != dir && dir != Direction.NONE
                && desired == dir.opposite()) {
            dir = desired;
            desired = Direction.NONE;
        }
```

- If the player asks for the opposite of the current direction, we reverse
  immediately without waiting for a tile centre.  `dir.opposite()` is the method
  from `Direction`.

```java
        if (desired != Direction.NONE && desired != dir && centered) {
            x = ccx;
            y = ccy;
            if (maze.canMove(col, row, desired, false)) {
                dir = desired;
                desired = Direction.NONE;
            }
        }
```

- At a tile centre, try to honour the buffered turn.  First snap to the exact
  centre, then ask the maze if the turn is legal.  The `desired != dir` guard
  prevents re-snapping every frame when the player holds a key (which used to freeze
  the character).
- `canMove(..., false)` means Pac-Man may not use the ghost-house door.

```java
        if (dir == Direction.NONE) {
            return;
        }

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
```

- If we are standing still, nothing else to do.
- If we are at a centre and a wall blocks us, stay put but keep facing the wall
  (so a later key press can turn us).
- Otherwise move `step` pixels along `dir` and wrap through the tunnel.
- Finally `recordMove()` feeds the memory.

```java
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
```

- Called after every move.  When the tile index actually changes (i.e. a new tile
  was entered), push the current direction into the ring buffer.  Because the ring
  buffer overwrites the oldest entry, memory is automatically capped at 10.

```java
    public boolean hasMemory(int n) {
        return history.size() >= n;
    }

    public Direction dominantDirection() {
        return history.dominant();
    }

    public boolean moving() {
        return dir != Direction.NONE;
    }
}
```

- `hasMemory(n)`: does the buffer already have at least `n` moves?  The tracker
  requires a minimum before trusting the trend.
- `dominantDirection()`: forwards to the ring buffer's analysis.
- `moving()`: used by the renderer to decide whether to animate the mouth.

---

### 5.11 Ghost.java — the agentic core

Path: `src/com/pacman/Ghost.java`

The biggest change: the ghosts now carry a `Brain` and use the graph, the ring
buffer and the search tree.

```java
public class Ghost extends Entity {

    public enum Kind { BLINKY, PINKY, INKY, CLYDE }
    public enum State { HOUSE, LEAVING, NORMAL, FRIGHTENED, EATEN, ENTERING }

    public enum Brain { BOSS, A_STAR, TRACKER, DIJKSTRA }
```

- `Kind`: the four classic ghosts.
- `State`: the ghost state machine.  `HOUSE` bobbing inside the pen, `LEAVING`
  walking out, `NORMAL` hunting, `FRIGHTENED` running away after a power pellet,
  `EATEN` flying home as eyes, `ENTERING` dropping back into the pen.
- `Brain`: **the new agentic tag**.  `BOSS` = minimax; `A_STAR` = A*; `TRACKER` =
  ring-buffer interception; `DIJKSTRA` = Dijkstra.

```java
    private static final Direction[] ORDER = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };

    private static final int TRACK_LOOKAHEAD = 10;

    public final Kind kind;
    public final Brain brain;
    public State state = State.NORMAL;

    public double houseTimer;
    public double baseSpeed = 108;

    public int scatterX;
    public int scatterY;

    public double homeX;
    public double homeY;

    public List<Integer> plannedPath = Collections.emptyList();
    public int targetNode = -1;
    public int bossPick = -1;
    public int interceptNode = -1;

    private double bobDir = 1;
    private boolean justDecided;
    private final Random rng = new Random();
```

- `ORDER` is the classic arcade tie-break order (up, left, down, right) used when
  several directions are equally good.
- `TRACK_LOOKAHEAD`: how many tiles the tracker projects forward.
- `kind`/`brain` are `final`: set once in the constructor.
- `state`, `houseTimer`, `baseSpeed`, `scatterX/Y`, `homeX/Y`: the usual ghost
  bookkeeping (scatter corner, respawn home, release timer).
- The four "debug" fields are read by the renderer to draw the graph overlay:
  the planned path, the target node, the boss's chosen node and the tracker's
  predicted interception node.
- `bobDir`, `justDecided`, `rng`: internal helpers.  `rng` is Python's
  `random.Random()` instance.

```java
    public Ghost(Maze maze, double tile, Kind kind) {
        super(maze, tile);
        this.kind = kind;
        this.brain = brainFor(kind);
    }

    private static Brain brainFor(Kind kind) {
        switch (kind) {
            case BLINKY: return Brain.BOSS;
            case PINKY:  return Brain.A_STAR;
            case INKY:   return Brain.TRACKER;
            case CLYDE:
            default:     return Brain.DIJKSTRA;
        }
    }
```

- The constructor assigns each classic ghost its brain.  `brainFor` is a `static`
  method (it does not need `this`), like a Python `@staticmethod`.

```java
    public void reset(double startX, double startY, State startState, double release) {
        x = startX;
        y = startY;
        homeX = startX;
        homeY = startY;
        state = startState;
        houseTimer = release;
        dir = Direction.LEFT;
        bobDir = 1;
        plannedPath = Collections.emptyList();
        targetNode = -1;
        bossPick = -1;
        interceptNode = -1;
    }
```

- Respawn/reset a ghost: position, home, state, release countdown, and clear the
  debug/planning fields.

```java
    public void update(double dt, Game game) {
        applySpeed();
        switch (state) {
            case HOUSE:      updateInHouse(dt);            break;
            case LEAVING:    updateLeaving(dt);            break;
            case ENTERING:   updateEntering(dt);           break;
            case EATEN:      gridMove(dt, game, true);     break;
            default:         gridMove(dt, game, false);    break;
        }
    }
```

- Route behaviour by state.  `applySpeed()` first sets the current speed for the
  state (frightened slower, eaten faster, house slow).  `EATEN` calls `gridMove`
  with `canUseDoor = true` so the eyes can enter the pen.

```java
    private void applySpeed() {
        switch (state) {
            case FRIGHTENED: speed = baseSpeed * 0.58; break;
            case EATEN:      speed = baseSpeed * 1.9;  break;
            case HOUSE:
            case LEAVING:
            case ENTERING:   speed = baseSpeed * 0.55; break;
            default:         speed = baseSpeed;
        }
    }
```

- Per-state speed multipliers.  Note the fall-through style: `case HOUSE:` with no
  body shares the next case's body (Java's switch falls through like C).

```java
    private void updateInHouse(double dt) {
        houseTimer -= dt;
        if (houseTimer <= 0) {
            state = State.LEAVING;
            return;
        }
        y += bobDir * 22 * dt;
        if (y > homeY + tile * 0.28) {
            y = homeY + tile * 0.28;
            bobDir = -1;
        } else if (y < homeY - tile * 0.28) {
            y = homeY - tile * 0.28;
            bobDir = 1;
        }
    }
```

- While waiting in the pen the ghost bobs up and down around its home position.
  `bobDir` flips at the top/bottom of the bob.

```java
    private void updateLeaving(double dt) {
        double step = speed * dt;
        double exitX = centerX(13);
        if (Math.abs(x - exitX) > step) {
            x += Math.signum(exitX - x) * step;
            return;
        }
        x = exitX;
        double exitY = centerY(11);
        if (y > exitY) {
            y -= step;
            if (y < exitY) {
                y = exitY;
            }
            return;
        }
        y = exitY;
        state = State.NORMAL;
        justDecided = false;
        dir = rng.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
    }
```

- Scripted exit: move horizontally to column 13, then up to row 11, then switch to
  `NORMAL` facing a random horizontal direction.  `Math.signum` is Python's
  `sign()`; `rng.nextBoolean()` returns True/False like `random.choice([True,False])`.

```java
    private void updateEntering(double dt) {
        double step = speed * dt;
        double exitX = centerX(13);
        if (Math.abs(x - exitX) > step) {
            x += Math.signum(exitX - x) * step;
            return;
        }
        x = exitX;
        double targetY = centerY(14);
        if (y < targetY) {
            y += step;
            if (y > targetY) {
                y = targetY;
            }
            return;
        }
        y = targetY;
        state = State.HOUSE;
        houseTimer = 0.9;
        bobDir = 1;
        dir = Direction.UP;
    }
```

- The mirror image: the eaten eyes descend back to row 14 and become a normal
  `HOUSE` ghost after a 0.9 s pause.

```java
    private void gridMove(double dt, Game game, boolean canUseDoor) {
        double step = speed * dt;
        int col = tileX();
        int row = tileY();
        double ccx = centerX(col);
        double ccy = centerY(row);
        boolean centered = Math.abs(x - ccx) <= step + 0.01
                && Math.abs(y - ccy) <= step + 0.01;

        if (centered) {
            if (!justDecided) {
                snapToCenter();
                col = tileX();
                row = tileY();
                if (state == State.EATEN && col == 13 && row == 11) {
                    state = State.ENTERING;
                    justDecided = false;
                    return;
                }
                chooseDirection(game, col, row, canUseDoor);
                justDecided = true;
            }
        } else {
            justDecided = false;
        }

        if (dir == Direction.NONE) {
            return;
        }
        x += dir.dx * step;
        y += dir.dy * step;
        wrapX();
    }
```

- The tile-by-tile mover shared by all "out in the maze" states.
- At each tile centre it asks `chooseDirection` **once** (`justDecided` guards
  against deciding repeatedly on several frames inside the centre tolerance, which
  caused jitter).  Between centres it just keeps moving.
- Special case: an `EATEN` ghost that reached (13,11) — just above the door —
  switches to `ENTERING` to descend.

```java
    private void chooseDirection(Game game, int col, int row, boolean canUseDoor) {
        Direction reverse = dir.opposite();
        List<Direction> options = new ArrayList<>(4);
        for (Direction d : ORDER) {
            if (d == reverse) {
                continue;
            }
            if (maze.canMove(col, row, d, canUseDoor)) {
                options.add(d);
            }
        }
        if (options.isEmpty()) {
            dir = reverse;
            return;
        }

        if (state == State.FRIGHTENED) {
            dir = options.get(rng.nextInt(options.size()));
            return;
        }

        if (canUseDoor) {
            dir = classicStep(game, col, row, options);
            return;
        }

        MazeGraph graph = game.graph;
        int here = graph.nodeAt(col, row);
        int target = chooseTarget(game, graph, col, row);
        targetNode = target;

        if (here < 0) {
            dir = options.size() == 1 ? options.get(0)
                                      : bestToward(options, game, col, row, target);
            bossPick = -1;
            return;
        }

        if (target < 0 || target == here) {
            plannedPath = Collections.emptyList();
            bossPick = -1;
            dir = chaseStep(game, col, row, options);
            return;
        }
        ...
```

- **The brain switchboard.**  Steps:
  1. Build the list of legal directions, excluding the reverse (classic rule).
  2. If none, reverse.
  3. `FRIGHTENED` → random pick (`rng.nextInt(n)` is Python's `random.randrange(n)`).
  4. `canUseDoor` (the eaten eyes) → classic greedy target chase.
  5. Otherwise we are an agent: get the current node `here` and the brain's chosen
     target node `target`.
  6. If `here < 0` we are mid-corridor; the only sensible non-reverse move is
     straight ahead (one option).  The previously planned path is intentionally kept
     so the debug overlay keeps drawing it.
  7. If we are already on the target node (`target == here`) we stop planning and
     shadow Pac-Man tile by tile with `chaseStep`.

```java
        if (brain == Brain.BOSS && game.globalMode == Game.Mode.CHASE) {
            int next = game.searchTree.chooseMove(here, target, reverse);
            bossPick = next;
            if (next >= 0) {
                List<Integer> tiny = new ArrayList<>(2);
                tiny.add(here);
                tiny.add(next);
                plannedPath = tiny;
                dir = graph.dirBetween(here, next);
                return;
            }
        }

        List<Integer> path = (brain == Brain.DIJKSTRA)
                ? graph.dijkstra(here, target)
                : graph.aStar(here, target);

        if (path.size() < 2) {
            plannedPath = Collections.emptyList();
            bossPick = -1;
            dir = classicStep(game, col, row, options);
            return;
        }
        plannedPath = path;
        bossPick = -1;
        dir = graph.dirBetween(here, path.get(1));
    }
```

- **Boss + CHASE mode** → ask the minimax tree for `next`; store it for the overlay
  and head there.  If it returns `-1` we fall through to a normal shortest-path
  search (a safety net).
- Otherwise compute the shortest path with Dijkstra (Clyde) or A* (Pinky/Inky/the
  boss in scatter mode).  The direction to take is the direction of the edge toward
  `path.get(1)`, the second node on the path (the first is where we stand).
- If the path is degenerate (already at the goal, or unreachable) fall back to the
  classic greedy step.

```java
    private int chooseTarget(Game game, MazeGraph graph, int col, int row) {
        switch (brain) {
            case BOSS:
                if (game.globalMode == Game.Mode.SCATTER) {
                    return graph.nearestNode(scatterX, scatterY);
                }
                return graph.nodeAhead(game.pacman.tileX(), game.pacman.tileY(),
                        game.pacman.dir);
            case TRACKER:
                if (game.globalMode == Game.Mode.SCATTER) {
                    interceptNode = -1;
                    return graph.nearestNode(scatterX, scatterY);
                }
                interceptNode = trackerIntercept(game, graph);
                return interceptNode;
            case A_STAR:
            case DIJKSTRA:
            default:
                interceptNode = -1;
                int[] tile = game.targetFor(this, col, row);
                return graph.nearestNode(tile[0], tile[1]);
        }
    }
```

- **What node does each brain aim for?**
  - **BOSS**: in scatter mode, the corner; in chase mode, the node Pac-Man is
    heading towards (`nodeAhead`) — that is the boss's "real Pac-Man" reference for
    the minimax evaluation.
  - **TRACKER**: corner in scatter; otherwise the ring-buffer prediction.
  - **A_STAR / DIJKSTRA**: fall back to the classic per-ghost personality target
    from `Game.targetFor` (Blinky = Pac-Man, Pinky = 4 ahead, Clyde = distance rule)
    and snap it to a real node.

```java
    public int trackerIntercept(Game game, MazeGraph graph) {
        Pacman p = game.pacman;
        int px = p.tileX();
        int py = p.tileY();
        if (!p.hasMemory(3)) {
            return graph.nodeAhead(px, py, p.dir);
        }
        Direction trend = p.dominantDirection();
        if (trend == null || trend == Direction.NONE) {
            return graph.nodeAhead(px, py, p.dir);
        }
        int c = px;
        int r = py;
        for (int i = 0; i < TRACK_LOOKAHEAD; i++) {
            if (!maze.canMove(c, r, trend, false)) {
                break;
            }
            c = Maze.wrapCol(c + trend.dx);
            r += trend.dy;
            int node = graph.nodeAt(c, r);
            if (node >= 0) {
                return node;
            }
        }
        return graph.nearestNode(c, r);
    }
```

- **Phase 3 in action.**  If Pac-Man's memory is too short (< 3 moves) or has no
  clear direction, just use the node ahead.
- Otherwise read the dominant direction from the ring buffer and walk forward up to
  `TRACK_LOOKAHEAD` tiles in that direction, stopping early at a wall.  The first
  node encountered is the intersection Pac-Man is rolling towards — the ghost will
  path there to **flank** him.  If no node was found within the look-ahead, aim for
  the nearest node to the projected point.

```java
    private Direction classicStep(Game game, int col, int row, List<Direction> options) {
        int[] target = game.targetFor(this, col, row);
        Direction best = options.get(0);
        double bestDist = Double.MAX_VALUE;
        for (Direction d : options) {
            int nc = Maze.wrapCol(col + d.dx);
            int nr = row + d.dy;
            double ddx = nc - target[0];
            double ddy = nr - target[1];
            double dist = ddx * ddx + ddy * ddy;
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }
        return best;
    }
```

- The original arcade targeting: among the legal non-reverse directions, pick the
  one whose tile centre is squared-closest to the ghost's target (squared distance
  avoids a `sqrt`).  Used for the eaten eyes and as a fallback.

```java
    private Direction chaseStep(Game game, int col, int row, List<Direction> options) {
        int px = game.pacman.tileX();
        int py = game.pacman.tileY();
        Direction pacDir = game.pacman.dir;
        Direction best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction d : ORDER) {
            if (!maze.canMove(col, row, d, false)) {
                continue;
            }
            int nc = Maze.wrapCol(col + d.dx);
            int nr = row + d.dy;
            double dist = (nc - px) * (nc - px) + (nr - py) * (nr - py);
            if (dist < bestDist - 1e-9) {
                bestDist = dist;
                best = d;
            }
        }
        if (best == null) {
            return options.isEmpty() ? dir.opposite() : options.get(0);
        }
        if (pacDir != Direction.NONE && maze.canMove(col, row, pacDir, false)) {
            int nc = Maze.wrapCol(col + pacDir.dx);
            int nr = row + pacDir.dy;
            double dist = (nc - px) * (nc - px) + (nr - py) * (nr - py);
            if (Math.abs(dist - bestDist) < 1e-9) {
                return pacDir;
            }
        }
        return best;
    }
```

- Shadowing Pac-Man once the target node is reached.  Unlike `classicStep`, this
  **includes the reverse direction**, which is how a ghost that overshot turns
  around.  Ties are broken in favour of following Pac-Man's own heading.
- The `1e-9` epsilon makes floating-point comparisons robust.

```java
    private Direction bestToward(List<Direction> options, Game game, int col, int row, int target) {
        if (target < 0) {
            return options.get(0);
        }
        int tc = game.graph.col(target);
        int tr = game.graph.row(target);
        ...
    }

    public void reverse() {
        if (dir != Direction.NONE) {
            dir = dir.opposite();
        }
        justDecided = false;
    }
}
```

- `bestToward`: mid-corridor fallback when several options somehow exist.
- `reverse()`: called by `Game` when scatter/chase mode flips, so ghosts turn
  around exactly like the arcade original.

---
### 5.12 Game.java

Path: `src/com/pacman/Game.java`

The central controller: game states, scoring, collisions, and now the graph, the
search tree and the cheat engine.

```java
public class Game {

    public static final double TILE = 20;

    public enum State { TITLE, READY, PLAYING, PAUSED, DYING, LEVEL_COMPLETE, GAME_OVER }
    public enum Mode { SCATTER, CHASE }
```

- `TILE = 20` pixels is the single source of truth for tile size (the renderer
  re-uses it).
- `State`: the overall screen/simulation state machine.
- `Mode`: the global ghost behaviour mode, alternating scatter (go to corners) and
  chase (hunt Pac-Man), exactly like the arcade.

```java
    public final Maze maze = new Maze();
    public final Pacman pacman;
    public final List<Ghost> ghosts = new ArrayList<>();
    public final SoundManager sound = new SoundManager();

    public final MazeGraph graph;
    public final SearchTree searchTree;
    public final CommandEngine commands = new CommandEngine();

    public boolean showGraph;
```

- `maze`, `pacman`, `ghosts`, `sound`: the core objects.  `final` = created once.
- `graph`: the maze graph shared by every ghost.  `searchTree`: the minimax tree.
  `commands`: the cheat tokenizer.
- `showGraph`: toggled by the G key; the renderer draws the overlay when true.

```java
    public State state = State.TITLE;
    public Mode globalMode = Mode.SCATTER;

    public int score;
    public int highScore;
    public int lives = 3;
    public int level = 1;

    public double time;
    public double stateTimer;
    public double frightTimer;
    public int ghostCombo;

    public double cheatSpeedTimer;
    public double cheatFreezeTimer;
    public double cheatGodTimer;

    private double pacmanBaseSpeed = 128;

    private double modeTimer;
    private int modeIndex;
```

- Public game statistics (score, lives, level, timers) and the three cheat timers.
- `pacmanBaseSpeed`: Pac-Man's un-boosted speed, so the `speedup` cheat can be
  applied and removed cleanly.
- `modeTimer`/`modeIndex`: track the scatter/chase schedule.

```java
    private static final Mode[] MODE_KIND = {
        Mode.SCATTER, Mode.CHASE, Mode.SCATTER, Mode.CHASE,
        Mode.SCATTER, Mode.CHASE, Mode.SCATTER, Mode.CHASE
    };
    private static final double[] MODE_DURATION = {
        7, 20, 7, 20, 5, 20, 5, Double.MAX_VALUE
    };
```

- The classic alternating schedule and how long each phase lasts.  The final chase
  lasts "forever" (`Double.MAX_VALUE`).

```java
    public Game() {
        pacman = new Pacman(maze, TILE);
        graph = new MazeGraph(maze);
        searchTree = new SearchTree(graph);
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.BLINKY));
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.PINKY));
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.INKY));
        ghosts.add(new Ghost(maze, TILE, Ghost.Kind.CLYDE));
        ghosts.get(0).scatterX = 25; ghosts.get(0).scatterY = 0;
        ghosts.get(1).scatterX = 2;  ghosts.get(1).scatterY = 0;
        ghosts.get(2).scatterX = 27; ghosts.get(2).scatterY = 30;
        ghosts.get(3).scatterX = 0;  ghosts.get(3).scatterY = 30;
        resetActors();
    }
```

- Build the graph **before** the ghosts (they need it to move), then the search
  tree, then the four ghosts with their scatter corners.  `ghosts.add` is Python's
  `list.append`; `ghosts.get(i)` is `list[i]`.
- `resetActors()` places everyone at their starting positions.

```java
    private static double center(int i) {
        return i * TILE + TILE / 2.0;
    }
```

- Tile index → pixel centre, a local convenience.

```java
    public void startNewGame() {
        score = 0;
        lives = 3;
        level = 1;
        maze.reset();
        resetActors();
        state = State.READY;
        stateTimer = 2.2;
    }

    public void nextLevel() {
        level++;
        maze.reset();
        resetActors();
        state = State.READY;
        stateTimer = 2.0;
    }
```

- New game / next level: reset counters, rebuild the maze (dense again — important
  for phase 6!) and actors, then a short `READY` countdown.

```java
    private void resetActors() {
        pacman.reset(center(13), center(23));
        pacmanBaseSpeed = 128 + Math.min(level - 1, 3) * 2;
        pacman.speed = pacmanBaseSpeed;

        ghosts.get(0).reset(center(13), center(11), Ghost.State.NORMAL, 0);
        ghosts.get(1).reset(center(13), center(14), Ghost.State.HOUSE, 2.0);
        ghosts.get(2).reset(center(11), center(14), Ghost.State.HOUSE, 6.0);
        ghosts.get(3).reset(center(15), center(14), Ghost.State.HOUSE, 10.0);

        double ghostSpeed = 104 + Math.min(level - 1, 8) * 3;
        for (Ghost g : ghosts) {
            g.baseSpeed = ghostSpeed;
        }

        globalMode = Mode.SCATTER;
        modeIndex = 0;
        modeTimer = MODE_DURATION[0];
        frightTimer = 0;
        ghostCombo = 0;
        cheatSpeedTimer = 0;
        cheatFreezeTimer = 0;
        cheatGodTimer = 0;
    }
```

- Place Pac-Man and the ghosts.  Blinky starts outside the pen in `NORMAL`, the
  other three start in `HOUSE` with staggered release timers (2/6/10 s).
- Pac-Man and ghost speeds grow slightly with the level.  `Math.min(a,b)` is
  Python's `min(a,b)`.
- Reset the mode schedule and **clear all cheat effects** on respawn.
- The enhanced `for` loop `for (Ghost g : ghosts)` is Python's `for g in ghosts`.

```java
    private void die() {
        lives--;
        state = State.DYING;
        stateTimer = 1.8;
        sound.death();
    }
```

- Lose a life, enter the death animation state, play the death jingle.

```java
    public void requestDirection(Direction d) {
        if (state == State.PLAYING || state == State.READY) {
            pacman.desired = d;
        }
    }
```

- Called from the key handler.  Buffers the requested direction (or ignores it
  unless we are actually playing).

```java
    public void primaryAction() {
        switch (state) {
            case TITLE:
            case GAME_OVER:
                startNewGame();
                break;
            case PAUSED:
                state = State.PLAYING;
                break;
            case PLAYING:
                state = State.PAUSED;
                break;
            default:
                break;
        }
    }

    public void togglePause() { ... }
    public void toggleMute() { ... }
```

- Space/Enter starts a game or unpauses; P toggles pause; M toggles sound.

```java
    public void feedCheat(char ch) {
        String command = commands.feed(ch);
        if (command != null) {
            activateCheat(command);
        }
    }

    private void activateCheat(String command) {
        double seconds;
        switch (command) {
            case "speedup": cheatSpeedTimer = 8;  seconds = 8;  break;
            case "freeze":  cheatFreezeTimer = 6; seconds = 6;  break;
            case "godmode": cheatGodTimer = 10;   seconds = 10; break;
            default: return;
        }
        sound.cheat();
        System.out.println("[CHEAT] '" + command + "' activated for " + seconds + "s");
    }
```

- **Phase 5 wiring.**  Feed the character to the trie; when a command completes,
  start its timer and give feedback (sound + console).  This is the only place the
  cheat layer touches the game.

```java
    public boolean ghostsFrozen() { return cheatFreezeTimer > 0; }
    public boolean godMode()      { return cheatGodTimer > 0; }
    public boolean speedBoost()   { return cheatSpeedTimer > 0; }
```

- Predicates used by the update loop and the HUD.

```java
    public void update(double dt) {
        time += dt;
        switch (state) {
            case READY:
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    state = State.PLAYING;
                }
                break;
            case PLAYING:
                updatePlaying(dt);
                break;
            case DYING:
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    if (lives <= 0) {
                        state = State.GAME_OVER;
                        if (score > highScore) {
                            highScore = score;
                        }
                    } else {
                        resetActors();
                        state = State.READY;
                        stateTimer = 1.5;
                    }
                }
                break;
            case LEVEL_COMPLETE:
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    nextLevel();
                }
                break;
            default:
                break;
        }
    }
```

- The top-level per-frame update, dispatching on `state`.  This is the master
  state machine: countdown → play → die/respawn or level-complete → next level or
  game over.

```java
    private void updatePlaying(double dt) {
        tickCheats(dt);

        if (frightTimer > 0) {
            frightTimer -= dt;
            if (frightTimer <= 0) {
                frightTimer = 0;
                for (Ghost g : ghosts) {
                    if (g.state == Ghost.State.FRIGHTENED) {
                        g.state = Ghost.State.NORMAL;
                    }
                }
            }
        } else {
            modeTimer -= dt;
            if (modeTimer <= 0) {
                advanceMode();
            }
        }

        pacman.update(dt);
        eatAtPacman();

        if (!ghostsFrozen()) {
            for (Ghost g : ghosts) {
                g.update(dt, this);
            }
        }

        checkCollisions();

        if (maze.pelletsLeft() == 0) {
            state = State.LEVEL_COMPLETE;
            stateTimer = 2.4;
            sound.levelUp();
        }
    }
```

- The live gameplay update:
  1. Tick cheat timers (and apply the speed multiplier).
  2. Count down the fright timer, restoring `FRIGHTENED` ghosts to `NORMAL`.
  3. Otherwise count down the scatter/chase timer and switch modes.
  4. Move Pac-Man and eat pellets.
  5. Update the ghosts — **only if the freeze cheat is off**.
  6. Check collisions.
  7. If no pellets remain, level complete.

```java
    private void tickCheats(double dt) {
        if (cheatSpeedTimer > 0)  cheatSpeedTimer  = Math.max(0, cheatSpeedTimer - dt);
        if (cheatFreezeTimer > 0) cheatFreezeTimer = Math.max(0, cheatFreezeTimer - dt);
        if (cheatGodTimer > 0)    cheatGodTimer    = Math.max(0, cheatGodTimer - dt);
        pacman.speed = pacmanBaseSpeed * (cheatSpeedTimer > 0 ? 1.75 : 1.0);
    }
```

- Count the timers down (clamped at 0) and set Pac-Man's speed to 175 % while the
  boost is active.

```java
    private void eatAtPacman() {
        int e = maze.eat(pacman.tileX(), pacman.tileY());
        if (e == 1) {
            score += 10;
            sound.chomp();
        } else if (e == 2) {
            score += 50;
            startFright();
            sound.power();
        }
        if (e != 0) {
            maze.maybeCompress();   // phase 6 trigger
        }
    }
```

- Eat whatever is under Pac-Man: `1` → +10 and a chomp; `2` → +50 and a fright.
- After any successful eat, ask the maze to check the 80 % compression threshold
  (**this is where phase 6 fires**).

```java
    private void startFright() {
        ghostCombo = 0;
        frightTimer = Math.max(2.0, 8.0 - (level - 1) * 0.5);
        for (Ghost g : ghosts) {
            if (g.state == Ghost.State.NORMAL) {
                g.state = Ghost.State.FRIGHTENED;
                g.reverse();
            }
        }
    }

    private void advanceMode() { ... }

    private void checkCollisions() {
        double hit = TILE * 0.65;
        double hit2 = hit * hit;
        for (Ghost g : ghosts) {
            if (g.state == Ghost.State.EATEN
                    || g.state == Ghost.State.ENTERING
                    || g.state == Ghost.State.HOUSE) {
                continue;
            }
            double dx = pacman.x - g.x;
            double dy = pacman.y - g.y;
            if (dx * dx + dy * dy < hit2) {
                if (g.state == Ghost.State.FRIGHTENED) {
                    g.state = Ghost.State.EATEN;
                    ghostCombo++;
                    int pts = 200 * (1 << (ghostCombo - 1));
                    score += pts;
                    sound.eatGhost();
                } else if (g.state == Ghost.State.NORMAL) {
                    if (!godMode()) {
                        die();
                        return;
                    }
                }
            }
        }
    }
```

- `startFright`: power pellet turns normal ghosts frightened and reverses them;
  the fright duration shrinks as levels go up.
- `checkCollisions`: distance² test (avoids `sqrt`) between Pac-Man and each ghost.
  - Frightened ghost → become `EATEN` and add `200 * 2^(combo-1)` points.
    `1 << (n-1)` is Python's `1 << (n-1)`.
  - Normal ghost → Pac-Man dies **unless god mode is active** (phase-5 cheat).
  - Eaten/entering/house ghosts are ignored.

```java
    public int[] targetFor(Ghost g, int col, int row) {
        if (g.state == Ghost.State.EATEN) {
            return new int[]{13, 11};
        }
        if (globalMode == Mode.SCATTER) {
            return new int[]{g.scatterX, g.scatterY};
        }
        int px = pacman.tileX();
        int py = pacman.tileY();
        int pdx = pacman.dir.dx;
        int pdy = pacman.dir.dy;
        switch (g.kind) {
            case BLINKY: return new int[]{px, py};
            case PINKY:  return new int[]{px + pdx * 4, py + pdy * 4};
            case INKY: {
                Ghost blinky = ghosts.get(0);
                int ax = px + pdx * 2;
                int ay = py + pdy * 2;
                return new int[]{ax * 2 - blinky.tileX(), ay * 2 - blinky.tileY()};
            }
            case CLYDE:
            default: {
                double dist = Math.hypot(px - col, py - row);
                if (dist > 8) {
                    return new int[]{px, py};
                }
                return new int[]{g.scatterX, g.scatterY};
            }
        }
    }
```

- The classic per-ghost **personality targets** in tile coordinates:
  - Eaten eyes → the tile above the pen.
  - Scatter → the ghost's own corner.
  - Chase: Blinky → Pac-Man; Pinky → 4 tiles ahead; Inky → a doubled vector from
    Blinky through 2-ahead of Pac-Man; Clyde → Pac-Man when farther than 8 tiles,
    otherwise his corner.
- These feeds the `A_STAR`/`DIJKSTRA` brains (via `nearestNode`) and the eaten-step,
  so the classic personalities survive while the pathfinding is upgraded.
- `new int[]{...}` returns an integer array (Python `(x, y)` tuple).

```java
    public boolean isFrightBlink() {
        return frightTimer > 0 && frightTimer < 2.0 && ((int) (frightTimer * 6)) % 2 == 0;
    }
}
```

- Used by the renderer to make frightened ghosts blink white near the end.

---

### 5.13 Renderer.java

Path: `src/com/pacman/Renderer.java`

All drawing.  We focus on the structure plus the new overlay; the drawing math is
standard JavaFX.

```java
public class Renderer {

    public static final double TILE = Game.TILE;
    public static final double HUD = 48;
    public static final double BOARD_W = Maze.COLS * TILE;
    public static final double BOARD_H = Maze.ROWS * TILE;
    public static final double CANVAS_W = BOARD_W;
    public static final double CANVAS_H = BOARD_H + HUD;
```

- Geometry constants: the board is `28*20 = 560` × `31*20 = 620`, plus a 48-pixel
  HUD strip at the bottom, so the canvas is 560 × 668.

```java
    private static final Color WALL = Color.web("#2121DE");
    private static final Color DOOR = Color.web("#FFB8FF");
    private static final Color PELLET = Color.web("#FFC4A8");
    private static final Color PAC = Color.web("#FFE100");
    private static final Color TEXT = Color.web("#FFE100");
    private static final Color RED = Color.web("#FF3B3B");
```

- `Color.web("#RRGGBB")` builds a colour from a hex string, like Python's
  `#rrggbb` in Tkinter/`pygame.Color`.

```java
    public void render(GraphicsContext gc, Game game) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        drawWalls(gc, game.maze);
        drawDoor(gc, game.maze);
        drawPellets(gc, game);
        if (game.showGraph) {
            drawGraph(gc, game);
        }
        drawPacman(gc, game);
        drawGhosts(gc, game);
        drawHud(gc, game);
        drawCheatTags(gc, game);
        drawOverlay(gc, game);
    }
```

- `gc` is the "pen" (`GraphicsContext`).  Every frame starts by clearing to black
  and then painting layers **back to front**: walls, door, pellets, the optional
  graph overlay, the characters, the HUD, the cheat tags, and the modal overlays.
- Draw order matters: later calls paint over earlier ones.  The cheat tags come
  after the HUD so they are not painted over.

```java
    private void drawWalls(GraphicsContext gc, Maze maze) {
        gc.setStroke(WALL);
        gc.setLineWidth(2.4);
        gc.setLineCap(StrokeLineCap.ROUND);
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                if (!maze.isWall(c, r)) {
                    continue;
                }
                double x0 = c * TILE;
                double y0 = r * TILE;
                double x1 = x0 + TILE;
                double y1 = y0 + TILE;
                if (!maze.isWall(c, r - 1)) { line(gc, x0 + 1, y0 + 1, x1 - 1, y0 + 1); }
                if (!maze.isWall(c, r + 1)) { line(gc, x0 + 1, y1 - 1, x1 - 1, y1 - 1); }
                if (!maze.isWall(c - 1, r)) { line(gc, x0 + 1, y0 + 1, x0 + 1, y1 - 1); }
                if (!maze.isWall(c + 1, r)) { line(gc, x1 - 1, y0 + 1, x1 - 1, y1 - 1); }
            }
        }
    }
```

- Walls are not filled; only the **edges facing an open tile** are stroked, which
  produces the classic thin blue outline.  For each wall tile we test the four
  neighbours and draw the relevant side.
- `continue` is Python's `continue`; `line` is a small helper calling
  `gc.strokeLine`.

```java
    private void drawPellets(GraphicsContext gc, Game game) {
        boolean powerVisible = ((int) (game.time * 4)) % 2 == 0;
        gc.setFill(PELLET);
        if (game.maze.isCompressed()) {
            SparseMatrix s = game.maze.sparse();
            for (int i = 0; i < s.size(); i++) {
                double cx = s.col(i) * TILE + TILE / 2.0;
                double cy = s.row(i) * TILE + TILE / 2.0;
                if (s.value(i) == SparseMatrix.PELLET) {
                    gc.fillOval(cx - 2.2, cy - 2.2, 4.4, 4.4);
                } else if (powerVisible) {
                    gc.fillOval(cx - 5.5, cy - 5.5, 11, 11);
                }
            }
            return;
        }
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                char ch = game.maze.tileAt(c, r);
                double cx = c * TILE + TILE / 2.0;
                double cy = r * TILE + TILE / 2.0;
                if (ch == Maze.PELLET) {
                    gc.fillOval(cx - 2.2, cy - 2.2, 4.4, 4.4);
                } else if (ch == Maze.POWER && powerVisible) {
                    gc.fillOval(cx - 5.5, cy - 5.5, 11, 11);
                }
            }
        }
    }
```

- Power pellets blink: `powerVisible` is true on even quarter-seconds.
- **Dense mode** scans all 868 tiles and draws a dot where `tileAt` says pellet/power.
- **Compressed mode** iterates only the survivors in the sparse matrix — far fewer
  iterations, which is exactly the performance win of sparse storage.
- `fillOval(x, y, w, h)` draws an ellipse with its **top-left** corner at `(x, y)`,
  so we subtract the radius to centre it.

```java
    private void drawGraph(GraphicsContext gc, Game game) {
        MazeGraph g = game.graph;

        gc.setStroke(Color.web("#00E5FF", 0.22));
        gc.setLineWidth(1.1);
        for (int u = 0; u < g.nodeCount(); u++) {
            for (int v : g.neighbors(u)) {
                if (v > u) {
                    line(gc, nodeX(g, u), nodeY(g, u), nodeX(g, v), nodeY(g, v));
                }
            }
        }

        for (Ghost gh : game.ghosts) {
            List<Integer> path = gh.plannedPath;
            gc.setStroke(ghostColor(gh.kind));
            gc.setLineWidth(2.4);
            for (int i = 0; i + 1 < path.size(); i++) {
                int a = path.get(i);
                int b = path.get(i + 1);
                line(gc, nodeX(g, a), nodeY(g, a), nodeX(g, b), nodeY(g, b));
            }
            if (gh.bossPick >= 0) { ...white ring at the boss's choice... }
            if (gh.interceptNode >= 0) { ...magenta X at the tracker's target... }
        }

        gc.setFill(Color.web("#FFFFFF", 0.8));
        for (int u = 0; u < g.nodeCount(); u++) {
            gc.fillOval(nodeX(g, u) - 2.2, nodeY(g, u) - 2.2, 4.4, 4.4);
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#00E5FF"));
        gc.fillText("GRAPH " + g.nodeCount() + " nodes / " + g.edgeCount()
                + " edges   [G to hide]", 8, 16);
        gc.setFill(Color.web("#FFD700"));
        gc.fillText("MEMORY(10): " + arrowHistory(game.pacman), 8, 30);
    }
```

- The **debug overlay** (toggled with G).  It draws:
  - every graph edge as a faint cyan line (`v > u` avoids drawing each edge twice);
  - each ghost's planned path in the ghost's own colour;
  - a white ring at the boss's minimax pick and a magenta X at the tracker's
    predicted interception node;
  - a white dot at every node;
  - a legend plus Pac-Man's ring-buffer history rendered as arrows.
- `Color.web(hex, opacity)` adds transparency, so the graph sits under the sprites
  without hiding them.

```java
    private static String arrowHistory(Pacman p) {
        if (p.history.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p.history.size(); i++) {
            switch (p.history.get(i)) {
                case UP: sb.append('\u2191'); break;
                case DOWN: sb.append('\u2193'); break;
                case LEFT: sb.append('\u2190'); break;
                case RIGHT: sb.append('\u2192'); break;
                default: sb.append('.'); break;
            }
            sb.append(' ');
        }
        return sb.toString();
    }
```

- Turns the ring buffer into "↑ → → ↓ …" using Unicode arrow characters
  (`\u2191` = ↑).  Great for showing the professor the context window live.

```java
    private static double nodeX(MazeGraph g, int node) {
        return g.col(node) * TILE + TILE / 2.0;
    }
    private static double nodeY(MazeGraph g, int node) {
        return g.row(node) * TILE + TILE / 2.0;
    }
```

- Node id → pixel centre.

```java
    private void drawCheatTags(GraphicsContext gc, Game game) {
        StringBuilder sb = new StringBuilder();
        if (game.speedBoost())  sb.append("SPEED ");
        if (game.ghostsFrozen()) sb.append("FREEZE ");
        if (game.godMode())     sb.append("GOD ");
        if (sb.length() == 0) {
            return;
        }
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#8AE234"));
        gc.fillText("CHEAT " + sb.toString().trim(), 12, BOARD_H + 46);
    }
```

- Shows which cheats are currently active (phase 5 feedback).
- `.trim()` strips the trailing space; `StringBuilder` accumulates the tags.

```java
    private void drawPacman(GraphicsContext gc, Game game) { ... }
    private void drawGhosts(GraphicsContext gc, Game game) { ... }
    private void drawGhost(GraphicsContext gc, Game game, Ghost g) { ... }
    private void drawGhostBody(...) { ... }
    private void drawEyes(...) { ... }
    private static Color ghostColor(Ghost.Kind kind) { ... }
    private void drawHud(GraphicsContext gc, Game game) { ... }
    private void drawLifeIcon(...) { ... }
    private void drawOverlay(GraphicsContext gc, Game game) { ... }
    private void title(...) { ... }
    private void line(...) { ... }
}
```

- These are the original (phase 1) drawing routines: Pac-Man's animated mouth
  (a filled arc whose wedge angle depends on `dir`), the ghost body (a semicircle
  plus a wavy skirt), the eyes (which look in the ghost's direction), the HUD
  (score/level/sound/lives), and the TITLE/PAUSED/GAME OVER overlays.
- `ghostColor` maps a `Kind` to its arcade colour with a `switch`.
- They were unchanged by this assignment except that the eyes use `g.dir.dx/dy`,
  which now reflects the agentic directions too.

---

### 5.14 PacmanApp.java

Path: `src/com/pacman/PacmanApp.java`

> **Updated architecture:** the window is no longer assembled with raw JavaFX
> code.  The scene graph now lives in `pacman.fxml` (section 5.14b) and
> `PacmanApp` simply loads it with `FXMLLoader` and delegates the game loop to
> `GameController`.  The code below is kept for reference; the explanatory notes
> about `AnimationTimer`, `dt` and the key handler still apply, but those pieces
> now live in `GameController`.

The JavaFX entry point and game loop.

```java
public class PacmanApp extends Application {

    private Game game;
    private Renderer renderer;
    private long lastNano;

    @Override
    public void start(Stage stage) {
        game = new Game();
        renderer = new Renderer();

        Canvas canvas = new Canvas(Renderer.CANVAS_W, Renderer.CANVAS_H);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, Renderer.CANVAS_W, Renderer.CANVAS_H, Color.BLACK);
        scene.setOnKeyPressed(e -> {
            onKeyPressed(e.getCode());
            String text = e.getText();
            if (text != null && text.length() == 1) {
                game.feedCheat(text.charAt(0));
            }
        });
```

- `extends Application` is JavaFX's required entry-point class; `start(Stage)` is
  the equivalent of `main()` for the GUI.
- Create the game and renderer, a `Canvas` (drawing surface) and its
  `GraphicsContext` (the pen).
- Put the canvas in a `Pane` (a simple layout container), wrap it in a `Scene`
  (the window content), and register a key-press handler.
- The handler is a **lambda**: it first does the normal key action, then feeds the
  typed character to the cheat engine.  `e.getCode()` is the physical key
  (`KeyCode.UP`, ...) while `e.getText()` is the printable character; we only feed
  single-character text.  This one line implements phase 5's "listen to the key
  stream" requirement.

```java
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNano == 0) {
                    lastNano = now;
                    return;
                }
                double dt = (now - lastNano) / 1_000_000_000.0;
                lastNano = now;
                if (dt > 0.05) {
                    dt = 0.05;
                }
                game.update(dt);
                renderer.render(gc, game);
            }
        };
        timer.start();

        stage.setTitle("Pac-Man");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        root.requestFocus();
    }
```

- `AnimationTimer` is JavaFX's per-frame callback; `handle(long now)` receives the
  current time in **nanoseconds** (a monotonic clock).
- `dt` is computed from the difference and converted to seconds
  (`/ 1_000_000_000.0`).  The first frame is skipped (`lastNano == 0`).
- `dt` is clamped to 50 ms so a hiccup does not teleport the actors through walls
  (a classic fixed-step-safety trick).
- Each frame: `game.update(dt)` advances the simulation, `renderer.render` paints.
- `An anonymous inner class` (`new AnimationTimer() { ... }`) is Python's
  one-off subclass.  `timer.start()` begins the loop.
- `stage` is the OS window; we set its title, content, fixed size and show it.
  `requestFocus()` makes sure the canvas receives key events.

```java
    private void onKeyPressed(KeyCode code) {
        switch (code) {
            case UP:
            case W:
                game.requestDirection(Direction.UP);
                break;
            case DOWN:
            case S:
                game.requestDirection(Direction.DOWN);
                break;
            case LEFT:
            case A:
                game.requestDirection(Direction.LEFT);
                break;
            case RIGHT:
            case D:
                game.requestDirection(Direction.RIGHT);
                break;
            case SPACE:
            case ENTER:
                game.primaryAction();
                break;
            case P:
                game.togglePause();
                break;
            case M:
                game.toggleMute();
                break;
            case G:
                game.showGraph = !game.showGraph;
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

- Maps physical keys to game actions: arrows/WASD to move, Space/Enter to
  start/pause, P to pause, M to mute, and the new **G to toggle the graph debug
  overlay**.
- The fall-through `case UP: case W:` means both keys do the same thing.
- `main` calls `launch(args)`, which hands control to JavaFX (which then calls
  `start`).  This is the Python `if __name__ == "__main__":` of JavaFX apps.

---

### 5.14b pacman.fxml + GameController.java

Paths: `src/com/pacman/pacman.fxml`, `src/com/pacman/GameController.java`

The UI is declarative: `pacman.fxml` describes the drawing `Canvas` and the
start menu (title, the two ghost-mode radio buttons and the START button),
while `GameController` is the `fx:controller` that receives those nodes with
`@FXML` and drives the game.

```xml
<AnchorPane xmlns="http://javafx.com/javafx" xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.pacman.GameController"
            prefWidth="560.0" prefHeight="668.0"
            style="-fx-background-color: black;">
    <Canvas fx:id="canvas" width="560.0" height="668.0"/>
    <VBox fx:id="menu" alignment="CENTER" ...>
        <RadioButton fx:id="soloGhost" text="1 Ghost - HUNTER (hard, calculated)"/>
        <RadioButton fx:id="allGhosts" text="4 Ghosts - CLASSIC" selected="true"
                     toggleGroup="$ghostGroup"/>
        <Button fx:id="startButton" text="START" onAction="#onStart"/>
    </VBox>
</AnchorPane>
```

- `fx:controller` names the Java class that will own the file.  Every element
  with an `fx:id` is injected into a matching `@FXML` field of that class.
- `onAction="#onStart"` binds the button to the `onStart()` method.
- The list of `RadioButton`s sharing one `ToggleGroup` is mutually exclusive, so
  the player can pick exactly one opponent.

`PacmanApp` now just loads it:

```java
FXMLLoader loader = new FXMLLoader(
        PacmanApp.class.getResource("/com/pacman/pacman.fxml"));
Parent root = loader.load();                 // builds the scene graph
GameController controller = loader.getController();
Scene scene = new Scene(root, Renderer.CANVAS_W, Renderer.CANVAS_H, Color.BLACK);
controller.attach(scene);                    // give the controller the key source
stage.setScene(scene);
stage.show();
controller.start();                          // start the AnimationTimer loop
```

- `getResource("/com/pacman/pacman.fxml")` finds the file on the **classpath**,
  which is why `build.sh` copies the `.fxml` next to the compiled classes.
- `loader.getController()` returns the already-constructed `GameController`, so
  we can hand it the `Scene` (for key events) and start its loop.

The controller implements the same `AnimationTimer` loop as before, plus the
menu logic:

```java
@FXML private void onStart() {
    game.setGhostMode(soloGhost.isSelected()
            ? Game.GhostMode.SOLO : Game.GhostMode.ALL);
    game.startNewGame();
    menu.setVisible(false);       // hide the FXML overlay and play
    menu.setManaged(false);
    running = true;
}
```

- When the game reaches `GAME_OVER` the loop flips `running` back to false and
  re-shows the FXML menu, so a new ghost mode can be chosen without restarting
  the application.

**Ghost modes (`Game.GhostMode`).**  `SOLO` keeps only Blinky active and swaps
his brain to `Ghost.Brain.HUNTER`; `ALL` restores the classic four ghosts
(`A_STAR`, `TRACKER`, `DIJKSTRA`, `BOSS`).  Inactive ghosts are skipped by
`Game.updatePlaying`, `Game.checkCollisions`, the renderer and the debug
overlay, so a solo game really has a single opponent on screen.

**Why the hunter feels calculated.**  Unlike the classic brains, the hunter:

1. never enters scatter mode - it always targets Pac-Man;
2. always runs the phase-4 minimax search tree (depth 4) on Pac-Man's
   *predicted* node (`nodeAhead` of the player's current heading);
3. stamps the full shortest-path continuation into `plannedPath`, which the
   renderer draws when `G` is pressed, so every decision is visible.

---

### 5.15 SoundManager.java

Path: `src/com/pacman/SoundManager.java`

Procedural sound with no audio files.  Only the new `cheat()` method is new; the
rest is unchanged.

```java
public class SoundManager {

    private static final float RATE = 44100f;

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "pacman-sound");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean enabled = true;
    private long lastChomp;
```

- `RATE` is the sample rate (44.1 kHz).
- `pool` is a **single-thread executor**: audio is generated/played on one
  background thread so the game loop never blocks.  `r -> {...}` is a lambda that
  builds a **daemon** thread (daemon threads die with the program).
  Python: `threading.Thread(target=..., daemon=True)`.
- `enabled` is `volatile` so the game and audio threads see updates reliably.
- `lastChomp` throttles the chomp sound.

```java
    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; }

    public void chomp() { ... throttle then async(...) ... }
    public void power()  { async(() -> play(170, 240, 0.30)); }
    public void eatGhost() { async(() -> play(760, 160, 0.34)); }
    public void death() { ... a descending note sequence ... }
    public void levelUp() { ... an ascending note sequence ... }

    public void cheat() {
        async(() -> {
            int[] notes = {660, 990, 1320};
            for (int n : notes) {
                play(n, 70, 0.28);
            }
        });
    }
```

- `cheat()` is the new feedback sound: a three-note rising blip when a cheat
  activates.  `async(...)` submits a `Runnable` (a zero-arg function) to the pool.

```java
    private void async(Runnable r) {
        if (enabled) {
            pool.submit(r);
        }
    }

    private void play(int freq, int ms, double vol) {
        if (!enabled) {
            return;
        }
        try {
            int len = (int) (RATE * ms / 1000.0);
            byte[] buf = new byte[len];
            for (int i = 0; i < len; i++) {
                double t = i / RATE;
                double env = Math.min(1.0, Math.min(i / (RATE * 0.008), (len - i) / (RATE * 0.03)));
                double wobble = Math.sin(2 * Math.PI * freq * t);
                buf[i] = (byte) (wobble * env * vol * 127);
            }
            AudioFormat fmt = new AudioFormat(RATE, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(info)) {
                return;
            }
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt);
            line.start();
            line.write(buf, 0, len);
            line.drain();
            line.close();
        } catch (Exception ignored) {
            // audio is optional
        }
    }
}
```

- `play` **synthesises** a sine tone: for each sample index `i`, it computes the
  sine value, multiplies by an amplitude **envelope** (a short fade-in and fade-out
  to avoid clicks) and the volume, and stores it as a signed byte.
- `AudioFormat(RATE, 8, 1, true, false)` = 44.1 kHz, 8-bit, mono, signed,
  little-endian.
- If the machine has no audio line (`AudioSystem.isLineSupported` false) we simply
  return; audio is best-effort.
- The whole block is wrapped in `try { } catch (Exception ignored) { }` — audio must
  never crash the game.  Python: `try/except Exception: pass`.

---

### 5.16 build.sh and run.sh

Path: `build.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

FX=/usr/lib/jvm/java-27-openjfx/lib
OUT=out

rm -rf "$OUT"
mkdir -p "$OUT"

javac --module-path "$FX" --add-modules javafx.controls,javafx.fxml \
      -d "$OUT" $(find src -name '*.java')

# Copy the FXML next to the compiled classes so FXMLLoader finds it on the classpath.
find src -name '*.fxml' -print0 | while IFS= read -r -d '' f; do
    rel="${f#src/}"
    mkdir -p "$OUT/$(dirname "$rel")"
    cp "$f" "$OUT/$rel"
done

echo "Build OK -> $OUT"
```

- The module list now includes `javafx.fxml` (needed by `FXMLLoader`).
- `javac` compiles `.java` files only, so the `.fxml` resources are **copied** into
  `out/` afterwards with a tiny shell loop, preserving the `com/pacman/` folder.
  `"${f#src/}"` strips the leading `src/` prefix (bash parameter expansion).

- `#!/usr/bin/env bash` chooses the interpreter.
- `set -euo pipefail` makes the script stop on errors / undefined variables / failed
  pipes — the bash equivalent of strict mode.
- `cd "$(dirname "$0")"` makes the script run from its own directory regardless of
  where you called it.
- `FX` points at the JavaFX modules shipped with the JDK; `OUT` is the build folder.
- `rm -rf` + `mkdir -p` wipe and recreate the output folder.
- `javac --module-path "$FX" --add-modules javafx.controls -d "$OUT" $(find src -name '*.java')`
  compiles every `.java` under `src/` into `out/`, with the JavaFX `controls` module
  on the module path.  `$(find ...)` is command substitution — Python's
  `glob.glob("src/**/*.java")` fed to the compiler.
- The trailing `\` continues the command on the next line.

Path: `run.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

FX=/usr/lib/jvm/java-27-openjfx/lib
OUT=out

if [ ! -d "$OUT" ]; then
    ./build.sh
fi

exec java --module-path "$FX" --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     -Djava.library.path="$FX" \
     -cp "$OUT" com.pacman.PacmanApp "$@"
```

- Build first if `out/` is missing.
- `java` launches the JVM running `com.pacman.PacmanApp` (the fully-qualified class
  name, like Python's `module.Class`).
- `--module-path`/`--add-modules` provide JavaFX; `-cp "$OUT"` is the classpath where
  our `.class` files live; `--enable-native-access` and `-Djava.library.path` point
  the native JavaFX libraries at the same folder.
- `exec` replaces the shell with the JVM (cleaner process handling).
- `"$@"` forwards any extra arguments you passed to the script.

---

### 5.17 The `test/` folder

Path: `test/*.java` (compiled with `javac -cp out -d test test/*.java`)

The tests are plain classes with a `main` method — no JUnit needed.  They are the
"does it still work?" safety net and a great demo for the professor.

- `TestLogic.java` — phase-1 rules: death by a normal ghost, eating a frightened
  ghost, power-pellet fright, level completion, tunnel wrapping.
- `TestCycle.java` — house/enter/leave lifecycle and eventual game over.
- `TestSim.java` — runs a crude Pac-Man bot for thousands of frames and asserts
  invariants (nobody ever stands inside a wall), then reports score/progress.
- `TestGhost.java`, `TestDebug.java`, `TraceReturn.java` — small trace tools that
  print ghost positions over time for manual inspection.
- **`TestAgentic.java`** (new) — validates every assignment data structure:
  - graph: node/edge counts, symmetric adjacency matrix, connectivity (BFS);
  - pathfinding: A* and Dijkstra are cross-checked against Floyd-Warshall on
    hundreds of node pairs;
  - ring buffer: capacity, overwrite, dominant-direction tie-breaking;
  - tracker: produces a valid interception node ahead of Pac-Man;
  - search tree: returns a legal neighbour and visits nodes;
  - trie/command engine: `speedup`/`freeze`/`godmode` match, junk does not;
  - cheats: all three actually flip the game flags and the speed boost raises
    Pac-Man's speed;
  - sparse matrix: compression fires at 80 %, survivors remain visible/edible, walls
    survive after the dense grid is dropped;
  - a full run with all agents checking that no ghost ever enters a wall and every
    planned path is a legal chain of edges.

---
## 6. Cheat codes and debug keys

### Cheat codes (phase 5)

Type these **letters** while playing.  Each keystroke is fed to the `Trie`; when a
whole word is matched the effect starts, a rising three-note blip plays and a line
is printed to the console.  Effects stack and each has its own timer.

| Type in order | Command | Effect | Duration |
|---------------|---------|--------|----------|
| `s p e e d u p` | `speedup` | Pac-Man moves at ×1.75 speed | 8 s |
| `f r e e z e` | `freeze` | Ghosts stop updating entirely | 6 s |
| `g o d m o d e` | `godmode` | Pac-Man cannot die on contact | 10 s |

While any cheat is active the bottom HUD shows `CHEAT SPEED FREEZE GOD`
(whichever apply).

How it works end-to-end:

1. `PacmanApp` key handler calls `game.feedCheat(text.charAt(0))` for every typed
   character.
2. `Game.feedCheat` passes it to `CommandEngine.feed`, which walks the `Trie`.
3. When a terminal node is reached, `feed` returns the command string; `Game`
   starts the matching timer and calls `sound.cheat()`.
4. `Game.tickCheats(dt)` counts the timers down each frame and applies the speed
   multiplier; `Game.ghostsFrozen()` and `Game.godMode()` gate ghost updates and
   death.

### Debug keys

| Key | Action |
|-----|--------|
| Arrow keys / `W` `A` `S` `D` | Move Pac-Man |
| `Space` / `Enter` | Start game / unpause |
| `P` | Pause / resume |
| `M` | Mute / unmute sound |
| `G` | Toggle the graph debug overlay |

The **G overlay** draws:

- all graph nodes (white dots) and edges (faint cyan lines);
- each ghost's current planned path in that ghost's colour;
- a white ring at the `BOSS` ghost's minimax-chosen node;
- a magenta `X` at the `TRACKER` ghost's predicted interception node;
- a legend (`GRAPH n nodes / m edges`) and `MEMORY(10)` showing Pac-Man's ring
  buffer as a series of arrows (↑ → → ↓ …).

---

## 7. Complexity summary

Notation: **V** = number of graph nodes, **E** = number of graph edges,
**N** = number of cells in the maze (28 × 31 = 868), **K** = ring-buffer capacity
(10), **P** = pellets remaining, **b** = branching factor, **d** = minimax depth.

| Component | File | Time | Space |
|-----------|------|------|-------|
| Graph build | `MazeGraph` | O(N) scan + O(V + E) refinement | O(V + E) |
| A* / Dijkstra query | `MazeGraph` | O((V + E) log V) per query (E ≈ 3V, so ≈ O(V log V)) | O(V) per query |
| Floyd–Warshall all-pairs | `MazeGraph` | O(V³ ≈ 64³ ≈ 262 144 ops, once) | O(V²) = 4096 ints |
| Minimax (boss) | `SearchTree` | O(b^d), d = 3 | O(b·d) recursion |
| Ring buffer push | `RingBuffer` | O(1) | O(K) |
| Dominant direction | `RingBuffer` | O(K) (K = 10) | O(1) extra |
| Trie insert / longest match | `Trie` | O(len) | O(total characters) |
| Sparse conversion | `Maze` | O(N) once | O(P) survivors + O(H·W/64) wall bitmask |
| Render (sparse pellets) | `Renderer` | O(P) not O(N) | — |
| Whole game (per-query work) | `MazeGraph` | one-time queries per decision, not per frame | — |

Key observations to mention to the professor:

- **Graph beats grid per frame.**  Ghosts never search the 868-tile grid each
  frame; they query the small graph (64 nodes / 168 edges).  Every distance the
  minimax evaluation needs is a **single array lookup** thanks to Floyd–Warshall —
  O(1) per evaluation node.
- **The minimax cost is bounded** because `d = 3` and the evaluation uses the
  O(1) distance matrix.  Keeping a dominant "distance to the real Pac-Man" term
  guarantees convergence (no oscillating boss).
- **The ring buffer is O(1)** per recorded move and only K=10 entries, so the
  tracker's memory is effectively free.
- **Sparse storage wins when the maze empties**: once ≥ 80 % of pellets are gone,
  drawing and iterating become O(P) instead of O(N); the dense grid is dropped so
  memory falls from ~2496 B to ~720 B (a 71 % saving in the report).

---

*End of guide.*





