# Pac-Man — The Absolutely Complete, From-Zero Deep Dive

> **Who this is for.** You have *never* programmed in Java. Words like `public`,
> `void`, `static`, `final`, `new`, `extends`, `<T>` and `@Override` make you
> physically unwell. You *do* know Python reasonably well. You read
> `CODE_EXPLAINED.md` and liked it, but you want to go **even deeper** — down to
> the raw meaning of every keyword, every brace, every semicolon, and down to the
> full logic of every AI algorithm and every data structure in this game.
>
> That is exactly what this file is. Nothing here assumes Java knowledge.
> Everything is compared to Python. No question is "too basic".
>
> **This file is brand new and does not touch `CODE_EXPLAINED.md`.** That older
> file is a friendly tour; this one is the deep technical manual. You can read
> them side by side.

---

## Table of contents

**Part 0 — How to use this document**
- 0.1 What the game is
- 0.2 The folder map
- 0.3 How to build and run (and what `javac`/`java` actually do)

**Part 1 — Java from absolute zero (the "vomit cure")**
- 1.1 The shape of a Java file
- 1.2 `package` and `import`
- 1.3 Statements, semicolons, braces, whitespace
- 1.4 Comments
- 1.5 Types: primitives vs references
- 1.6 Variables and assignment
- 1.7 Operators
- 1.8 Control flow: `if`, `for`, `while`, `switch`, ternary
- 1.9 Methods: signatures, `void`, `return`, overloading
- 1.10 Classes, objects, `new`, `this`, constructors
- 1.11 Access modifiers: `public`, `private`, `protected`, package-private
- 1.12 `static` — what it really means
- 1.13 `final` — what it really means
- 1.14 `abstract`, `extends`, `@Override`, `super`
- 1.15 Enums (and why `Direction` is one)
- 1.16 Generics: the `<T>` thing
- 1.17 Interfaces and polymorphism
- 1.18 Exceptions: `try`, `catch`, `throw`, `throws`
- 1.19 Collections: `List`, `ArrayList`, `Map`, `Queue`, `PriorityQueue`, `Deque`
- 1.20 Lambdas, functional interfaces, `Comparator`
- 1.21 Nested classes: static nested, inner, anonymous
- 1.22 Bitwise operators (used by the sparse matrix)
- 1.23 `Math` and number formatting
- 1.24 `null`, references, stack vs heap
- 1.25 Modules and the classpath

**Part 2 — The game's architecture (the big picture)**
- 2.1 Coordinates: pixels vs tiles
- 2.2 The game loop and `dt`
- 2.3 Model / View / Controller split
- 2.4 A data-flow diagram

**Part 3 — Every file, every line**
- 3.1 `Direction.java`
- 3.2 `Entity.java`
- 3.3 `Maze.java`
- 3.4 `SparseMatrix.java`
- 3.5 `RingBuffer.java`
- 3.6 `MazeGraph.java`
- 3.7 `Trie.java`
- 3.8 `SearchTree.java`
- 3.9 `Pacman.java`
- 3.10 `Ghost.java`
- 3.11 `Game.java`
- 3.12 `Renderer.java`
- 3.13 `CommandEngine.java`
- 3.14 `SoundManager.java`
- 3.15 `PacmanApp.java`
- 3.16 `GameController.java` and `pacman.fxml`
- 3.17 `build.sh` and `run.sh`

**Part 4 — The AI, algorithm by algorithm**
- 4.1 The ghost finite-state machine
- 4.2 Classic arcade targeting (Blinky/Pinky/Inky/Clyde)
- 4.3 Pac-Man prediction via the ring buffer
- 4.4 The maze graph: turning a grid into a graph
- 4.5 Dijkstra's algorithm
- 4.6 A\* search
- 4.7 Floyd–Warshall all-pairs shortest paths
- 4.8 Minimax with alpha–beta pruning
- 4.9 The evaluation function (where "intelligence" lives)
- 4.10 The HUNTER brain (the solo opponent)
- 4.11 Scatter / chase scheduling
- 4.12 Difficulty: SOLO vs ALL

**Part 5 — The data structures, in depth**
- 5.1 Dense grid (`char[][]`)
- 5.2 Sparse matrix (COO) + bit packing
- 5.3 Ring buffer (circular queue)
- 5.4 Adjacency matrix vs adjacency list
- 5.5 Binary heap / priority queue
- 5.6 Trie (prefix tree)
- 5.7 The search tree and the call stack

**Part 6 — The math toolkit**
**Part 7 — Rendering deep dive**
**Part 8 — FXML & JavaFX deep dive**
**Part 9 — Cheats, debug keys, and the tests**
**Part 10 — Speed/space cheat sheet and glossary**

---

# Part 0 — How to use this document

## 0.1 What the game is

It is a faithful **Pac-Man arcade clone** written in Java, drawn with **JavaFX**,
with **procedurally synthesised sound** (no audio files) and — most importantly —
**ghosts driven by real AI algorithms and data structures**:

- a **graph** of the maze, with **A\*** and **Dijkstra** pathfinding;
- a **ring buffer** that remembers the player's last moves;
- a **minimax search tree** with **alpha–beta pruning**;
- a **trie** that recognises secret cheat codes;
- a **sparse matrix** with **bit packing** that replaces the dense grid once the
  level is mostly eaten.

Each classic ghost uses a *different* algorithm, and there is also a single
"boss" hunter you can choose to fight alone.

## 0.2 The folder map

```
pacman-game/
├── build.sh                 # compiles everything
├── run.sh                   # launches the game
├── CODE_EXPLAINED.md        # the older, friendlier guide (kept!)
├── JAVA_DEEP_DIVE.md        # <- you are here
├── pacman-game.iml          # IntelliJ project descriptor
├── src/com/pacman/          # ALL the source code
│   ├── Direction.java
│   ├── Entity.java
│   ├── Maze.java
│   ├── SparseMatrix.java
│   ├── RingBuffer.java
│   ├── MazeGraph.java
│   ├── SearchTree.java
│   ├── Trie.java
│   ├── CommandEngine.java
│   ├── Pacman.java
│   ├── Ghost.java
│   ├── Game.java
│   ├── Renderer.java
│   ├── SoundManager.java
│   ├── GameController.java
│   ├── PacmanApp.java
│   ├── pacman.fxml
│   └── Readme.md
└── test/                    # plain-Java test programs (no JUnit)
    ├── TestLogic.java
    ├── TestCycle.java
    ├── TestSim.java
    ├── TestGhost.java
    ├── TestDebug.java
    ├── TraceReturn.java
    └── TestAgentic.java
```

Why `src/com/pacman/` and not just `src/`? Because Java ties the **folder** to the
**package name** (`com.pacman`), and packages are how Java namespaces code. More
on that in §1.2.

## 0.3 How to build and run

```bash
cd pacman-game
./build.sh     # compile every .java into out/  (uses the javac compiler)
./run.sh       # launch the JavaFX window     (uses the java runtime)
```

There is **no** Maven, Gradle, `pip`, or `requirements.txt`. Java separates the
**compiler** and the **runtime** into two different programs:

- `javac` = compile source `.java` → bytecode `.class` (like Python compiling to
  `.pyc`, but mandatory and explicit).
- `java` = run the bytecode (like `python main.py`, but on the pre-compiled
  classes).

`build.sh` (explained fully in §3.18):

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

find src -name '*.fxml' -print0 | while IFS= read -r -d '' f; do
    rel="${f#src/}"
    mkdir -p "$OUT/$(dirname "$rel")"
    cp "$f" "$OUT/$rel"
done

echo "Build OK -> $OUT"
```

Two important ideas:

1. **`javac` only compiles `.java`.** The `pacman.fxml` layout is *not* Java, so
   we must **copy** it into `out/` ourselves so the program can find it at runtime.
2. **JavaFX is a separate module.** Modern Java removed JavaFX from the JDK; it
   ships as modules (`javafx.controls`, `javafx.fxml`, ...) that you put on the
   *module path* with `--module-path ... --add-modules ...`.

---

# Part 1 — Java from absolute zero (the "vomit cure")

Read this whole part once. You do not need to memorise it; every term is used
again later with a pointer back here.

## 1.1 The shape of a Java file

Almost every Java file looks like this:

```java
package com.pacman;              // 1. where this file lives

import java.util.List;           // 2. things we borrow from the library

public class Ghost {             // 3. a class named Ghost (file Ghost.java)

    private int lives;           // 4. fields (data)
    public double speed;

    public Ghost(int lives) {    // 5. constructor (how to build one)
        this.lives = lives;
    }

    public int getLives() {      // 6. a method (behaviour)
        return lives;
    }
}
```

Compare to Python:

```python
# no package line (folders handle it)
from java_util import List  # imports

class Ghost:                # a class named Ghost
    def __init__(self, lives):
        self.lives = lives
        self.speed = 0.0

    def get_lives(self):
        return self.lives
```

Key differences a Python programmer feels immediately:

| Java | Python |
|---|---|
| One **public** class per file, file name must match | Any number of classes, any file name |
| `package` declared in the file | implied by folder + `__init__.py` |
| Types written for **every** variable and field | dynamic typing |
| Statements end with `;` | newline ends a statement |
| Blocks use `{ }` | indentation |
| `self` is implicit (`this` only when needed) | `self` is explicit |

The outer `{ }` starting right after the class name must be closed at the very
end of the file. If a file "doesn't compile and you can't see why", it is almost
always a missing or extra brace.

## 1.2 `package` and `import`

**`package com.pacman;`** — the first non-comment line. It declares the *package*
this file belongs to. It must match the folders after `src/`:

```
src/com/pacman/Ghost.java   ->   package com.pacman;
```

Two classes in the **same** package can use each other with **no import** — that
is why `Ghost` can freely say `Maze`, `Direction`, `Game`, etc.

**`import java.util.List;`** — brings in a class from the standard library. It is
Python's `from java.util import List`. You can also write `import java.util.*;` to
grab a whole package.

- The standard library package `java.lang` (contains `String`, `Math`, `System`,
  `Integer`, ...) is imported **automatically** — that's why `Math.abs(...)` and
  `System.out.println(...)` work with no import.

## 1.3 Statements, semicolons, braces, whitespace

- A **statement** is one instruction, ending in `;`. Forgetting the `;` is the
  single most common beginner error.
- A **block** is a group of statements inside `{ }`. A block is *not* ended by a
  `;`.
- **Whitespace is free.** Java does not care about indentation — only about `{`
  and `;`. Indentation exists purely for humans (unlike Python where it *is* the
  syntax).

```java
if (x > 0) { y = 1; }        // one legal line
if (x > 0)                   // the same thing spread out
{
    y = 1;
}
```

## 1.4 Comments

```java
// one-line comment, exactly like Python's #

/* multi-line
   comment */

/**
 * Javadoc comment.  Special: tools read these to build documentation.
 * @param dt seconds since last frame
 * @return true if something happened
 */
public boolean step(double dt) { ... }
```

Javadoc tags (`@param`, `@return`) are just text; the compiler ignores comments.

## 1.5 Types: primitives vs references

Java is **statically typed**: every variable and field is declared with a type,
and that type cannot change.

### The eight primitive types

| Type | Size | Holds | Python analogue |
|---|---|---|---|
| `byte` | 8 bits | integer −128..127 | `int` (small) |
| `short` | 16 bits | integer ±32k | `int` |
| `int` | 32 bits | integer ±2.1 billion | `int` |
| `long` | 64 bits | huge integer (write `L` suffix: `10L`) | `int` |
| `float` | 32 bits | decimal | `float` (less precise) |
| `double` | 64 bits | decimal (the default) | `float` |
| `char` | 16 bits | one character: `'a'`, `'\n'` | `str` of length 1 |
| `boolean` | 1 bit | `true` / `false` | `bool` |

Primitives are **values**: `int a = 5;` stores the number 5 directly.

- `double` is the normal floating-point type in this project (as in Python, where
  `float` is double-precision).
- `char` uses **single quotes**: `'a'`. A `String` uses **double quotes**: `"a"`.
  This trips up Python programmers, who use quotes interchangeably.

### Reference types

Anything that is a **class** (`String`, `Maze`, `ArrayList<>`, arrays, your own
types) is a **reference**. A variable of a reference type stores *where the object
lives in memory*, not the object itself — like a Python variable pointing to an
object.

```java
String name = "Pinky";   // name points at a String object
Ghost g = new Ghost(...); // g points at a Ghost object
```

> **This is the single most important concept** for reading the code. When you
> pass an object to a method, you are passing the *reference* — so the method can
> mutate the object (just like Python). But when you reassign the variable
> (`g = null;`) the caller does not see it. Java is always "pass by value", but
> the value of an object variable is a reference.

### Arrays

An array is a fixed-length, indexed container of one type:

```java
int[] a = new int[10];        // ten ints, all 0
char[][] grid = new char[31][28];  // a 2-D array (array of arrays)
double[] speeds = { 1.0, 1.5, 2.0 };  // literal form
a[0] = 42;                    // index like Python: a[0]
int len = a.length;           // NO parentheses: length is a field
```

- `a.length` is a **field** (no `()`), unlike Python's `len(a)`.
- Arrays are **0-indexed**, fixed-size, and will throw
  `ArrayIndexOutOfBoundsException` if you go past the end.
- `char[][] grid` is literally an array whose elements are `char[]` arrays —
  Python's list-of-lists.
- A 2-D array is **not** necessarily rectangular in Java (`int[][] adj` in
  `MazeGraph` has rows of different lengths — a "ragged" array).

## 1.6 Variables and assignment

```java
int score = 0;            // declare type + name, initialise
double x;                 // declaration only; primitives default to 0.0
Maze maze = new Maze();   // object
final int MAX = 100;      // a constant (see §1.13)
int a = 1, b = 2, c = 3;  // multiple in one line
```

- `=` is **assignment**, `==` is **equality**. `if (x = 5)` does **not** compile
  (this is a JavaScript trap, not a Java one).
- Constants for the whole class are written `static final` (see §1.12–1.13), e.g.
  `public static final int COLS = 28;`.

## 1.7 Operators

Same birthday-to-birthday as Python:

```java
+  -  *  /  %            // arithmetic; NOTE integer division truncates
==  !=  <  >  <=  >=     // comparison -> boolean
&&  ||  !                // logical AND / OR / NOT  (Python: and/or/not)
+                        // ALSO string concatenation
+=  -=  *=  /=  ++  --   // shorthand
? :                      // ternary, same as Python's a if c else b
```

Two gotchas:

1. **Integer division:** `7 / 2 == 3` (not 3.5). Write `7 / 2.0` to get `3.5`.
2. **`==` on objects compares references**, not contents. For contents use
   `.equals(...)`:

```java
String a = new String("hi");
String b = new String("hi");
a == b        // false  (different objects)
a.equals(b)   // true   (same text)
```

For **enums** (§1.15) and primitives, `==` is correct. That is why the codebase
writes `dir == Direction.UP` but `name.equals("Pinky")`.

## 1.8 Control flow

```java
if (x > 0) {
    ...
} else if (x == 0) {
    ...
} else {
    ...
}
```

The classic **for** loop has three parts: `init; condition; step`.

```java
for (int i = 0; i < 10; i++) {   // Python: for i in range(10)
    System.out.println(i);
}
```

The **enhanced for** (a.k.a. for-each) is Python's `for x in items`:

```java
for (Direction d : ORDER) { ... }     // for d in ORDER
for (Ghost g : ghosts) { ... }
```

`while` and `do/while`:

```java
while (cond) { ... }
do { ... } while (cond);   // runs the body at least once
```

`switch` picks one branch of many. **Beware:** without `break` (or `return`) it
"falls through" into the next case, exactly like C:

```java
switch (state) {
    case HOUSE:
    case LEAVING:          // shares the next body (falls through)!
        slowDown();
        break;
    case EATEN:
        rushHome();
        break;
    default:
        chase();
}
```

Modern Java (used for the `switch` in `Ghost`) can also use arrow labels, but
this project uses the classic `break`/`return` style.

`break` exits the nearest loop/switch; `continue` jumps to the next iteration.

**Ternary** (Python's conditional expression):

```java
int m = (a > b) ? a : b;     // a if a > b else b
```

## 1.9 Methods: signatures, `void`, `return`

A method is a function attached to a class:

```java
public int add(int a, int b) {
    return a + b;
}
```

Decode left to right:

- `public` — who may call it (access modifier, §1.11).
- `int` — the **return type** (the kind of value handed back).
- `add` — the method **name**.
- `(int a, int b)` — the **parameters**, each with an explicit type.
- `{ ... }` — the **body**.

`void` means "returns nothing":

```java
public void toggle() {
    enabled = !enabled;      // no return statement needed
}
```

A `void` method may use a bare `return;` to exit early. A non-`void` method **must**
return a value on every path, or the compiler refuses to compile.

**Overloading**: two methods may share a name if their parameters differ:

```java
public Direction opposite() { ... }          // no parameters
public void move(int dx) { ... }             // one int
public void move(int dx, int dy) { ... }     // two ints
```

**`static` methods** (§1.12) belong to the class, not an object, and are called as
`ClassName.method(...)`, e.g. `Maze.wrapCol(c)`.

## 1.10 Classes, objects, `new`, `this`, constructors

A **class** is a blueprint; an **object** is an instance built from it.

```java
public class Ghost {
    private int lives;              // field

    public Ghost(int lives) {       // constructor: same name as class, no return type
        this.lives = lives;         // "this" = the object being built
    }
}

Ghost g = new Ghost(3);             // "new" allocates an object; constructor runs
g.getLives();
```

- `new Ghost(3)` is Python's `Ghost(3)`.
- The **constructor** is named exactly like the class and has **no return type**
  (not even `void`). Python's `__init__`.
- `this` is Python's `self`, but it is **implicit**: you never list it as a
  parameter. You write `this.lives` only to disambiguate from a parameter of the
  same name.
- Java has **no default parameter values** and **no keyword arguments** (except
  annotations). That is why you often see several overloaded constructors/methods.

Calling the parent constructor in a subclass uses `super(...)` (§1.14).

## 1.11 Access modifiers

| Modifier | Visible to | Python analogue |
|---|---|---|
| `public` | everyone | "public by convention" |
| `private` | only the same class | `_name` convention, but **enforced** |
| `protected` | same class, subclasses, same package | `_name` for subclasses |
| *(none)* | same package only ("package-private") | `_name` within a module |

Java **enforces** these at compile time. `Entity` keeps `protected final Maze maze`
so subclasses (`Pacman`, `Ghost`) can read it but nobody outside the package can.

## 1.12 `static` — what it really means

`static` = "belongs to the **class itself**, not to any particular object."

```java
public static final int COLS = 28;     // one shared constant
public static int wrapCol(int c) { ... } // callable without an instance
```

- Call a static member as `Maze.COLS` / `Maze.wrapCol(c)` — no object needed.
- Inside a static method there is **no `this`**: it cannot touch instance fields.
  That is why `Maze.wrapCol` is static (it needs no maze) but `maze.isWall` is not
  (it reads the maze's tiles).
- `Maze.WALL_WORDS` is computed once at class-load with a static initialiser:
  `private static final int WALL_WORDS = (ROWS * COLS + 63) / 64;`
- **Static initialisation order**: static fields/initialisers run top to bottom the
  first time the class is touched. `Maze.DOOR_KEYS = findDoorKeys();` runs once,
  before any object exists.

Python analogue: module-level variables/functions, or `@staticmethod`.1

## 1.13 `final` — what it really means

`final` = "assign once; never reassign."

- **On a variable/field:** the reference/primitive can't change.
  ```java
  public final Kind kind;   // set in the constructor, then frozen
  ```
  But note: `final` on an **object reference** does **not** make the object
  immutable. `public final List<Ghost> ghosts` means you can't point `ghosts`
  elsewhere, but you *can* `ghosts.add(...)` (just like a Python list bound to a
  module constant).
- **On a class:** cannot be subclassed: `public final class MazeGraph`.
- **On a method:** cannot be overridden. (Not used much here.)

## 1.14 `abstract`, `extends`, `@Override`, `super`

Inheritance:

```java
public abstract class Entity {            // cannot be instantiated directly
    protected final Maze maze;

    protected Entity(Maze maze, double tile) {
        this.maze = maze;                 // parent constructor
    }

    public abstract void update(double dt);   // no body: subclass MUST provide it
}

public class Pacman extends Entity {      // "extends" = Python's (Entity)
    public Pacman(Maze maze, double tile) {
        super(maze, tile);                // call the parent constructor
    }

    @Override
    public void update(double dt) { ... } // provide the body
}
```

- `abstract` on a class = Python's `abc.ABC`; on a method = `@abstractmethod`.
- `extends` = `class Child(Parent)`.
- `@Override` is an **annotation**: the compiler checks you really are overriding a
  parent method. It is optional but catches typos.
- `super(...)` in a constructor calls the parent constructor; `super.method()`
  calls a parent method.
- A subclass **inherits** all non-private fields/methods, so `Pacman` and `Ghost`
  get `x`, `y`, `dir`, `speed`, `tileX()`, `wrapX()`, etc. for free.

There is no multiple inheritance in Java (single class parent), but a class can
implement many **interfaces** (§1.17).

## 1.15 Enums (and why `Direction` is one)

An **enum** is a type with a fixed set of named values:

```java
public enum Direction {
    UP(0, -1), LEFT(-1, 0), DOWN(0, 1), RIGHT(1, 0), NONE(0, 0);
    ...
}
```

Unlike Python's `enum.Enum` (which is just constants), a Java enum is a **full
class**: each constant can carry **data** and have **methods**, and there are
exactly as many instances as constants — forever. `Direction.UP` is a singleton.

Key facts:

- `Direction.UP.dx` reads the `dx` field of the `UP` constant.
- `Direction.values()` returns the array `{UP, LEFT, DOWN, RIGHT, NONE}`.
- `d.ordinal()` returns the position (`UP`=0, `LEFT`=1, ...). `MazeGraph` stores
  edge directions as `byte` ordinals and converts back with `Direction.values()[i]`.
- `switch (this)` over enum constants is idiomatic.
- Comparison is with `==`, never `.equals` (each constant is one object).

Python perspective: an enum whose members are dataclass-like objects with methods.
The constructor `Direction(int dx, int dy)` runs **once per constant at class load**.

## 1.16 Generics: the `<T>` thing

Generics let a class/method work for many element types while staying type-safe:

```java
public final class RingBuffer<T> {   // T is a placeholder type
    private final Object[] data;
    public void push(T value) { ... }
    public T get(int index) { ... }
}

RingBuffer<Direction> history = new RingBuffer<>(10);
history.push(Direction.RIGHT);
Direction d = history.get(0);        // no cast needed: T is Direction here
```

- `<T>` is Python's `TypeVar` / `Generic[T]` / `list[int]` type hint, but Java
  **enforces** it at compile time and **erases** it at runtime ("type erasure").
- Because of erasure you **cannot** write `new T[capacity]`. That is why
  `RingBuffer` stores an `Object[]` and casts on read:
  `return (T) data[...]` — with `@SuppressWarnings("unchecked")` to silence the
  warning.
- `List<Ghost>` reads "a List of Ghost". The diamond `new ArrayList<>()` infers
  the type argument from context.

## 1.17 Interfaces and polymorphism

An interface is a pure contract (method signatures, no implementation):

```java
public interface Movable {
    void move(double dt);            // implicitly public + abstract
}
```

A class **implements** one or more interfaces:

```java
public class Pacman extends Entity implements Movable {
    @Override public void move(double dt) { ... }
}
```

Polymorphism: you can call interface methods on any implementation without knowing
which concrete class it is. This project uses interfaces heavily from the standard
library (`Comparable`, `Comparator`, `Runnable`).

## 1.18 Exceptions

```java
throw new IllegalArgumentException("capacity must be > 0");

try {
    risky();
} catch (Exception e) {
    // handle it
} finally {
    // always runs
}
```

- `throw` = Python's `raise`.
- **Checked** exceptions must be declared (`throws IOException`) or caught;
  **unchecked** ones (`RuntimeException` and friends like
  `IllegalArgumentException`, `NullPointerException`) need not be.
- This project mostly throws `IllegalArgumentException` and catches broad
  `Exception` in the audio code so sound never crashes the game.

## 1.19 Collections

Everything is an object with a **type parameter**:

```java
List<Ghost> ghosts = new ArrayList<>();        // Python: list
ghosts.add(new Ghost(...));                    // append
Ghost g = ghosts.get(0);                        // indexing
int n = ghosts.size();                          // len

Map<String, Integer> scores = new HashMap<>(); // Python: dict
scores.put("a", 1);
int v = scores.get("a");

PriorityQueue<Entry> open = new PriorityQueue<>(Comparator.comparingDouble(e -> e.f));
Deque<Integer> q = new ArrayDeque<>();         // Python: collections.deque
```

| Python | Java |
|---|---|
| `list` | `ArrayList<T>` (interface `List<T>`) |
| `dict` | `HashMap<K,V>` (interface `Map<K,V>`) |
| `set` | `HashSet<T>` |
| `deque` | `ArrayDeque<T>` |
| `heapq` | `PriorityQueue<T>` (a min-heap) |
| `arr[i]` | `list.get(i)` (lists) but `arr[i]` (arrays) |
| `len(x)` | `x.size()` (collections) / `x.length` (arrays) |

Note the **indentation on loop bodies** in a `for (int i=0; ...)` when the body is
a single statement without braces is discouraged; the codebase always uses braces.

## 1.20 Lambdas, functional interfaces, `Comparator`

A **lambda** is an inline function:

```java
scene.setOnKeyPressed(e -> onKeyPressed(e.getCode()));
```

This is Python `scene.on_key_pressed(lambda e: on_key_pressed(e.code))`. The type
`e` is inferred. A lambda can only be used where a **functional interface** (an
interface with a single abstract method) is expected — here, JavaFX's
`EventHandler<KeyEvent>`.

`Comparator.comparingDouble(e -> e.f)` builds a comparator that orders objects by
their `f` field (ascending). The priority queue then pops the smallest `f` first —
exactly a min-heap.

## 1.21 Nested classes

- **Static nested class** — a helper that does not need an outer instance:
  ```java
  public final class Trie {
      public static final class Node { ... }   // Trie.Node, no outer needed
  }
  ```
  Access it as `Trie.Node`.

- **Anonymous inner class** — an inline subclass used once:
  ```java
  AnimationTimer timer = new AnimationTimer() {
      @Override public void handle(long now) { ... }
  };
  ```
  Python's one-off `class X(AnimationTimer): ...` assigned to a variable.

- **Inner (non-static) class** — has access to the outer instance's fields. Not
  used here.

## 1.22 Bitwise operators (used by the sparse matrix)

Java has the usual bit operators:

```java
&   // AND
|   // OR
^   // XOR
~   // NOT (flip all bits)
<<  // shift left
>>  // shift right (sign-preserving)
>>> // shift right, unsigned (fills with 0)
```

Two idioms appear in `Maze` and `SparseMatrix`:

- **Pack a 2-D index into one integer:** `key = r * COLS + c`.
- **Ceiling division:** `(n + 63) / 64` = how many 64-bit words hold `n` bits.
- **Bit position tricks:** `word = k >>> 6` (fast `k / 64`), `bit = k & 63` (fast
  `k % 64`), set with `w[word] |= 1L << bit`, read with `(w[word] >>> bit) & 1L`.
- **Overflow-safe midpoint:** `(lo + hi) >>> 1` = `(lo + hi) // 2` without
  overflow.
- **Align to 8:** `(x + 7L) & ~7L`.

## 1.23 `Math` and number formatting

`java.lang.Math` (auto-imported) provides static maths functions:

```java
Math.abs(x);            // abs()
Math.floor(x);          // floor()
Math.min(a, b);         // min()
Math.max(a, b);         // max()
Math.hypot(dx, dy);     // sqrt(dx*dx + dy*dy)
Math.sin(a); Math.cos(a); Math.toRadians(deg); Math.signum(x);
```

`System.out.println(x)` = `print()`. `System.out.printf("%.1f%n", x)` =
`print("%.1f" % x)` (`%n` is a portable newline).

## 1.24 `null`, references, stack vs heap

- `null` is Python's `None`. It means "points at nothing".
- Calling a method on `null` throws `NullPointerException` (the famous "NPE").
  The code guards against it, e.g. `if (trend == null || trend == NONE)`.
- **Stack vs heap:** local primitive variables live on the call stack (fast,
  auto-freed when the method returns). Objects created with `new` live on the
  **heap**; the stack variable holds a *reference* to them. When nothing
  references an object, the **garbage collector** reclaims it — like Python, you
  never `free()` manually.
- That "nothing references it" idea is central to Phase 6: `tiles = null;` drops
  the dense grid so the GC can reclaim thousands of bytes.

## 1.25 Modules and the classpath

- **Classpath (`-cp`)** = the list of folders/JARs where `java` looks for
  `.class` files. `-cp out` means "look in `out/`".
- **Module path (`--module-path`)** + `--add-modules` = the modern way to get
  JavaFX. JavaFX classes live in modules like `javafx.controls` and `javafx.fxml`.
- `-Djava.library.path=...` tells the JVM where the **native** `.so` libraries
  (graphics, fonts) are.
- `--enable-native-access=javafx.graphics` silences Java 27's strict warning
  about JavaFX using native code.

That's the whole Java primer. Everything below references these sections.

---

# Part 2 — The game's architecture (the big picture)

## 2.1 Coordinates: pixels vs tiles

The board is a **grid**: `Maze.COLS = 28` columns and `Maze.ROWS = 31` rows.
Every tile is `Game.TILE = 20` pixels wide/tall. So the playfield is

```
28 * 20 = 560 pixels wide
31 * 20 = 620 pixels tall
```

plus a 48-pixel HUD bar underneath → canvas is `560 x 668`.

Every moving thing (`Entity`) stores its position as **pixel coordinates of its
centre** (`double x, y`), not tile indices. Two conversions are used constantly:

```java
public int tileX() { return (int) Math.floor(x / tile); }   // pixel -> column
protected double centerX(int c) { return c * tile + tile / 2.0; } // column -> centre pixel
```

Example: column 0 spans x = 0..19, its centre is x = 10. Column 1 spans 20..39,
centre x = 30. `tileX` of x=30 is `floor(30/20)=1`. `centerX(1)` is `1*20+10=30`.
Rounding down (`floor`) means "which tile am I overlapping".

Because x is a `double`, movement is smooth; the grid is only consulted at tile
centres to make turning/first-decision legal.

## 2.2 The game loop and `dt`

The game is **frame-based**. JavaFX calls one method every screen refresh. We
measure the seconds since the previous frame (`dt`, "delta time") and advance the
simulation by exactly that much:

```java
double dt = (now - lastNano) / 1_000_000_000.0;
if (dt > 0.05) dt = 0.05;      // clamp so a hiccup can't teleport anyone
game.update(dt);
renderer.render(gc, game);
```

Why this matters:

- `speed` is in **pixels per second**. Each frame you move `speed * dt` pixels. On
  a 60 Hz monitor `dt ≈ 0.0167 s`, so a 128 px/s Pac-Man moves ≈ 2.1 px/frame.
- `dt` is **clamped** to 50 ms. If the OS stalls (e.g. you drag the window), a huge
  `dt` would make entities jump more than a tile and skip collisions. Clamping
  keeps physics safe.
- All logic is written to be **dt-correct** (timer fields are decremented by `dt`),
  so the game runs at the same *speed* regardless of frame rate.

## 2.3 Model / View / Controller split

```
                 +-------------------+
   keyboard  --> |  GameController   |  (Controller + FXML)
   (JavaFX)      |  pacman.fxml      |
                 +---------+---------+
                           | game.update(dt)
                           v
                 +-------------------+        reads state
                 |       Game        | -----------------------+
                 |  Maze, Pacman,    |                        v
                 |  Ghosts, graph,   |              +------------------+
                 |  SearchTree...    |              |    Renderer      |
                 +-------------------+              |  (draws Canvas)  |
                     (Model)                        +------------------+
```

- **Model** = `Game`, `Maze`, `Pacman`, `Ghost`, `MazeGraph`, `SearchTree`,
  `RingBuffer`, `Trie`, `SparseMatrix`. It holds *all* the state and rules and
  knows nothing about drawing or JavaFX.
- **View** = `Renderer`. It reads the model and paints pixels. It never changes
  game state.
- **Controller** = `GameController` + `pacman.fxml`. It reads input, ticks the
  model (`game.update`), calls the view, and owns the start menu.
- **Bootstrap** = `PacmanApp`, which loads the FXML and shows the window.

This separation is why you can run the tests (`TestAgentic`, `TestSim`) with **no
window at all**: they drive the Model directly, and JavaFX is never started.

## 2.4 A data-flow diagram (one frame)

```
KeyEvent ──► GameController.onKeyPressed ──► Game.requestDirection / primaryAction
                                                     │
tick ─────────► Game.update(dt) ─┬─► Pacman.update(dt) ──► maze.canMove / history.push
                                 ├─► Ghost.update(dt)  ──► chooseDirection ──► MazeGraph / SearchTree / RingBuffer
                                 ├─► eatAtPacman()     ──► maze.eat / maybeCompress (SparseMatrix)
                                 └─► checkCollisions() ──► die() / EATEN
                                                     │
render ─────────► Renderer.render(gc, game) ──► drawWalls / drawPellets / drawPacman / drawGhosts / HUD
```

---

# Part 3 — Every file, every line

We now walk each file top to bottom. Terms like "field", "method", "enum" refer
back to Part 1.

## 3.1 `Direction.java`

```java
package com.pacman;

public enum Direction {
    UP(0, -1),
    LEFT(-1, 0),
    DOWN(0, 1),
    RIGHT(1, 0),
    NONE(0, 0);

    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

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

Line by line:

- `package com.pacman;` — namespace declaration (§1.2).
- `public enum Direction {` — a type with five singleton values (§1.15).
- `UP(0, -1),` … — each constant is constructed with the two constructor
  arguments. Screen coordinates put `y` growing **downwards**, so `UP` is
  `dy = -1`. `DOWN` is `+1`. There is no `dx`/`dy` for vertical in `LEFT`... it's
  `(-1, 0)`, meaning "one column left, no row change".
- `public final int dx;` / `dy;` — each constant carries its step. `final` =
  never changed after construction.
- `Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }` — the enum
  constructor. It is **implicitly private**: only the five constants above can use
  it. It runs once per constant when the class loads.
- `opposite()` — returns the reverse. Used by ghosts (they may not reverse
  mid-corridor) and by Pac-Man (instant U-turn). `switch (this)` matches the
  current constant; `default` catches `NONE` (and any future constant).

Python equivalent:

```python
from enum import Enum
class Direction(Enum):
    UP    = (0, -1)
    LEFT  = (-1, 0)
    DOWN  = (0, 1)
    RIGHT = (1, 0)
    NONE  = (0, 0)
    @property
    def dx(self): return self.value[0]
    @property
    def dy(self): return self.value[1]
    def opposite(self):
        return {Direction.UP: Direction.DOWN, ...}[self]
```

## 3.2 `Entity.java`

The abstract base class for anything that moves. `abstract` (§1.14) means you
cannot write `new Entity(...)`; it exists only to be extended.

```java
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
```

- `x, y` — centre position in pixels. `public` so the renderer reads them directly.
- `dir` — current movement direction; starts as `NONE` (standing still).
- `speed` — pixels/second. No default here; subclasses set it.
- `maze` — every entity needs to ask "can I move there?". `final` = set once.
- `tile` — tile size (20), cached to avoid recomputing.
- `protected Entity(...)` — the base constructor, called with `super(maze, tile)`.

```java
    public int tileX() {
        return (int) Math.floor(x / tile);
    }
    public int tileY() {
        return (int) Math.floor(y / tile);
    }
```

- Convert pixel → tile index. `Math.floor` rounds down; `(int)` casts the
  resulting `double` to `int` (§1.5). Example: x=30, tile=20 → 1.

```java
    protected double centerX(int c) {
        return c * tile + tile / 2.0;
    }
    protected double centerY(int r) {
        return r * tile + tile / 2.0;
    }
```

- Tile index → centre pixel. `tile / 2.0` is `10.0` (the `2.0` forces floating
  division; `tile / 2` would still be `10` here, but `2.0` documents intent and is
  safe if tile were odd-ish).

```java
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
```

- The left/right **tunnel**. Walking off the left edge teleports you to the right
  and vice versa. Using `+= w`/`-= w` (not `x = w`) preserves the fractional part
  so the motion stays smooth across the wrap. Rows do **not** wrap.

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
}
```

- `atCenter`: are we within one frame's travel of a tile centre? The `+0.01`
  tolerance guards floating-point error. (Pacman/Ghost compute this inline because
  they also need the centre values.)
- `snapToCenter`: hard-set the exact centre after deciding, so grid logic stays
  aligned.

> Python note: `Math.abs` is `abs`; `(int)` is `int(...)`; `protected` is the
> `_`-prefix convention but enforced.

## 3.3 `Maze.java` — the level and Phase 6

`Maze` owns two things: the **static layout** (walls/pellets) and the **mutable
pellet state**. It also performs the Phase-6 dense→sparse conversion.

### The constants and layout

```java
public class Maze {
    public static final int COLS = 28;
    public static final int ROWS = 31;

    public static final char WALL   = '#';
    public static final char PELLET = '.';
    public static final char POWER  = 'o';
    public static final char DOOR   = '-';
    public static final char EMPTY  = ' ';
```

- `static final` = class-level constant (§1.12–1.13). `COLS`/`ROWS` are used
  everywhere. The `char` legend: `#` wall, `.` pellet, `o` power pellet, `-` ghost
  door, ` ` empty.

```java
    private static final String[] LAYOUT = {
        "############################",
        "#............##............#",
        ...
    };
```

- An array of 31 strings, each 28 characters: the level drawn as text. `LAYOUT[r]`
  is row `r`; `LAYOUT[r].charAt(c)` is the character at (row r, col c) — Python's
  `LAYOUT[r][c]` (Java `String` has no `[]` indexing; you call `.charAt`).

### Fields

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

- `WALL_WORDS` — number of 64-bit words to store one bit per tile (868 bits → 14
  words). Ceiling division `(n+63)/64` (§1.22). Used only after compression.
- `tiles` — the **dense** `char[31][28]` grid. This is the "big array" Phase 6
  destroys.
- `sparse` — after compression, the survivors as a COO matrix; `null` while dense.
- `compressed` — which representation is live.
- `wallBits` — a packed wall bitmask built lazily at compression time.
- `DOOR_KEYS` — the packed keys of the two door tiles, computed once at class load
  (`findDoorKeys()`), so the compressed path can recognise doors without the dense
  grid.
- `pelletsLeft` / `totalPellets` — remaining and initial counts.

### `reset()`

```java
    public Maze() {
        reset();
    }

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

- The constructor calls `reset()` (a common Java pattern).
- Reset goes back to dense mode, re-allocates the grid if Phase 6 had thrown it
  away, copies `LAYOUT` into `tiles` with a nested loop, and counts pellets.
- `LAYOUT[r].charAt(c)` is Python's `LAYOUT[r][c]`.

### `wrapCol`

```java
    public static int wrapCol(int c) {
        int m = c % COLS;
        return m < 0 ? m + COLS : m;
    }
```

- Wrap a column into 0..27. Java's `%` can be negative for negative inputs
  (`-1 % 28 == -1`), so we add `COLS` when negative. Python's `%` is already
  non-negative, but Java does not. This is a static helper used all over.

### `tileAt` — the universal query

```java
    public char tileAt(int c, int r) {
        if (r < 0 || r >= ROWS) {
            return WALL;
        }
        c = wrapCol(c);
        if (compressed) {
            byte v = sparse.get(r, c);
            if (v == SparseMatrix.PELLET) return PELLET;
            if (v == SparseMatrix.POWER)  return POWER;
            if (isDoorKey(c, r))          return DOOR;
            return wallBit(c, r) ? WALL : EMPTY;
        }
        return tiles[r][c];
    }
```

- Rows do not wrap: off the top/bottom is `WALL`.
- Columns wrap.
- **Dense:** one array read.
- **Compressed:** ask the sparse matrix for a pellet; else a door; else the packed
  wall bit; else empty. It reconstructs the same answers as the dense grid without
  keeping it.

### Predicates, movement, eating

```java
    public boolean isWall(int c, int r) { return tileAt(c, r) == WALL; }
    public boolean isDoor(int c, int r) { return tileAt(c, r) == DOOR; }

    public boolean canMove(int c, int r, Direction dir, boolean canUseDoor) {
        if (dir == Direction.NONE) return false;
        if (r + dir.dy < 0 || r + dir.dy >= ROWS) return false;
        char ch = tileAt(c + dir.dx, r + dir.dy);
        if (ch == WALL) return false;
        if (ch == DOOR && !canUseDoor) return false;
        return true;
    }
```

- `canMove` is THE legal-move test. `NONE` never moves. Vertical bounds are
  checked. `tileAt` handles horizontal wrap. A wall blocks everyone; a door blocks
  Pac-Man and normal ghosts but lets eating-eyes through (`canUseDoor = true`).

```java
    public int eat(int c, int r) {
        if (r < 0 || r >= ROWS) return 0;
        c = wrapCol(c);
        if (compressed) {
            int type = sparse.eat(r, c);
            if (type != 0) pelletsLeft--;
            return type;
        }
        char ch = tiles[r][c];
        if (ch == PELLET) { tiles[r][c] = EMPTY; pelletsLeft--; return 1; }
        if (ch == POWER)  { tiles[r][c] = EMPTY; pelletsLeft--; return 2; }
        return 0;
    }
```

- Consume whatever is here: returns `0` nothing, `1` pellet, `2` power pellet. The
  caller decides score and fright. Dense erases the char; compressed delegates to
  the sparse matrix.

### Phase 6 trigger

```java
    public boolean maybeCompress() {
        if (compressed || totalPellets == 0) return false;
        int eaten = totalPellets - pelletsLeft;
        if (eaten * 5 < totalPellets * 4) return false;   // eaten/total < 0.8
        compress();
        return true;
    }
```

- Called after every pellet. `eaten * 5 < totalPellets * 4` is the integer form of
  `eaten/total < 0.8`, avoiding floating point. When false, we are at ≥80% and
  compress.

```java
    private void compress() {
        // 1) count survivors
        int n = 0;
        for (...) if (tiles[r][c] == PELLET || tiles[r][c] == POWER) n++;
        // 2) fill three parallel arrays (row-major => sorted keys)
        int[] rows = new int[n]; int[] cols = new int[n]; byte[] vals = new byte[n];
        int i = 0;
        for (...) { rows[i]=r; cols[i]=c; vals[i]=...; i++; }
        // 3) build sparse + wall bits, drop the dense grid
        long before = denseBytes();
        sparse = new SparseMatrix(rows, cols, vals);
        tiles = null;            // <-- the key line: release the big array to the GC
        compressed = true;
        if (wallBits == null) wallBits = buildWallBits();
        ...
    }
```

- First pass counts (to size arrays exactly), second fills them. A row-major scan
  visits tiles in ascending `row*28+col` order, which is exactly the order the
  sparse matrix's binary search needs — no sorting required.
- `tiles = null;` is the heart of Phase 6: drop the reference so the **garbage
  collector** frees ~2.5 KB (see §5.2 for the exact memory math).
- `wallBits` is built here because before this the dense grid already held the
  walls.

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

- Byte estimates for the console report (the `[MEMORY]` lines). `16` = object
  header; `8L * ROWS` = the array of row references; each row's `char[]` data is
  `2 * COLS` bytes padded to 8. These are standard 64-bit HotSpot estimates, not
  exact.

```java
    private static long[] buildWallBits() {
        long[] w = new long[WALL_WORDS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (LAYOUT[r].charAt(c) == WALL) {
                    int k = r * COLS + c;
                    w[k >>> 6] |= 1L << (k & 63);
                }
        return w;
    }
    private static boolean wallBit(int c, int r) {
        if (wallBits == null) wallBits = buildWallBits();
        int k = r * COLS + c;
        return ((wallBits[k >>> 6] >>> (k & 63)) & 1L) != 0;
    }
```

- Flat index `k = r*28 + c`. `k >>> 6` picks the 64-bit word; `1L << (k & 63)`
  builds the bit mask; `|=` sets it. `wallBit` reads it back. This packs 868
  booleans into 14 machine words (§5.2).

```java
    private static int[] findDoorKeys() { ... }
    private static boolean isDoorKey(int c, int r) {
        int key = r * COLS + c;
        for (int k : DOOR_KEYS) if (k == key) return true;
        return false;
    }
}
```

- `findDoorKeys` (run once at class load) scans `LAYOUT` for `-` and returns their
  packed keys. `isDoorKey` is a tiny linear search (only two doors).

## 3.4 `SparseMatrix.java`

COO = "Coordinate List". Only the tiles that still hold a pellet are stored, as
three parallel arrays (§5.2).

```java
public final class SparseMatrix {
    public static final byte PELLET = 1;
    public static final byte POWER  = 2;

    private final int[] rows;
    private final int[] cols;
    private final byte[] vals;
    private int n;                 // live count; capacity never shrinks

    public SparseMatrix(int[] rows, int[] cols, byte[] vals) {
        if (rows.length != cols.length || rows.length != vals.length)
            throw new IllegalArgumentException("COO arrays must have equal length");
        this.rows = rows; this.cols = cols; this.vals = vals;
        this.n = rows.length;
    }
```

- `byte` = 8-bit int, perfect for `1`/`2` (§1.5).
- The constructor validates the invariant with `throw ...` (Python `raise
  ValueError`).

```java
    public int size()  { return n; }
    public int row(int i)   { return rows[i]; }
    public int col(int i)   { return cols[i]; }
    public byte value(int i){ return vals[i]; }

    private int keyAt(int i) { return rows[i] * Maze.COLS + cols[i]; }
```

- Accessors for the renderer/tests. `keyAt` is the packed sort key.

```java
    public byte get(int r, int c) {
        int i = indexOf(r, c);
        return i < 0 ? 0 : vals[i];
    }
```

- Value at (r,c) or 0 if absent. Note the `(cond) ? a : b` ternary (§1.8).

```java
    public byte eat(int r, int c) {
        int i = indexOf(r, c);
        if (i < 0) return 0;
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

- Remove a pellet and return its type. The three arrays are shifted left by one to
  close the hole: `System.arraycopy(src, srcPos, dst, dstPos, len)` is a fast
  native bulk copy (Python slice assignment `rows[i:] = rows[i+1:]`).
- `n--` shrinks the live region; `vals[n] = 0` cleans the stale trailing slot.

```java
    private int indexOf(int r, int c) {
        int key = r * Maze.COLS + c;
        int lo = 0, hi = n - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;        // overflow-safe midpoint
            int k = keyAt(mid);
            if (k == key) return mid;
            if (k < key) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }
```

- Standard **binary search** over the packed keys, O(log n) (§5.2). `>>> 1` is
  `//2` without overflow.

```java
    public long estimatedBytes() {
        long r = 16 + align8(4L * rows.length);
        long c = 16 + align8(4L * cols.length);
        long v = 16 + align8(1L * vals.length);
        return r + c + v;
    }
    static long align8(long x) { return (x + 7L) & ~7L; }
```

- Heap-size estimate: each array is a 16-byte header plus its data rounded up to 8
  bytes (`align8`). `int[]` = 4 bytes/element, `byte[]` = 1.

```java
    public String describe() { ... }
```

- Human-readable dump (first 8 triples + "... N more") used in the console report.

## 3.5 `RingBuffer.java` — Phase 3 memory, generically

A fixed-capacity **circular queue**. Writing is O(1): the write cursor wraps around
and overwrites the oldest entry instead of shifting (Python's `list.pop(0)` is
O(n), this is O(1)).

```java
public final class RingBuffer<T> {

    private final Object[] data;   // can't write "new T[capacity]" (type erasure)
    private final int capacity;
    private int head;              // where the NEXT push writes
    private int size;              // live count (<= capacity)

    public RingBuffer(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("RingBuffer capacity must be > 0");
        this.capacity = capacity;
        this.data = new Object[capacity];
    }
```

- `<T>` generic (§1.16). The backing store is `Object[]` because Java cannot
  allocate `new T[]` (generics are erased at runtime). Reads cast back to `T`.

```java
    public void push(T value) {
        data[head] = value;
        head = (head + 1) % capacity;   // wrap -> "ring"
        if (size < capacity) size++;
    }
```

- Write at `head`, advance modulo `capacity`, grow `size` until full. After that,
  every push silently overwrites the oldest slot — no shifting.

```java
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(...);
        int start = (head - size + capacity) % capacity;   // physical slot of oldest
        return (T) data[(start + index) % capacity];
    }
```

- Logical index 0 = oldest, `size-1` = newest. `start` computes the oldest's
  physical slot. `@SuppressWarnings("unchecked")` silences the compiler warning
  about the `(T)` cast, which the programmer promises is safe.

```java
    public T newest() { return get(size - 1); }
    public T oldest() { return get(0); }
    public int size() { return size; }
    public int capacity() { return capacity; }
    public boolean isFull() { return size == capacity; }
    public boolean isEmpty() { return size == 0; }

    public void clear() {
        Arrays.fill(data, null);
        head = 0;
        size = 0;
    }
```

- `Arrays.fill(array, value)` = Python `data[:] = [value]*len(data)`. `clear()` is
  called on every respawn.

```java
    public int count(T value) {
        int n = 0;
        for (int i = 0; i < size; i++)
            if (same(get(i), value)) n++;
        return n;
    }
```

- Count occurrences. `same` (below) uses `.equals`, so it behaves like Python `==`
  for objects.

```java
    public T dominant() {
        if (size == 0) return null;
        T best = null;
        int bestCount = 0;
        for (int i = size - 1; i >= 0; i--) {     // newest -> oldest
            T candidate = get(i);
            int c = 0;
            for (int j = 0; j < size; j++)
                if (same(get(j), candidate)) c++;
            if (c > bestCount) { bestCount = c; best = candidate; }
        }
        return best;
    }
```

- The "which direction does Pac-Man favour?" analysis. Scanning **newest to
  oldest** and only replacing the champion on a *strictly* larger count means
  ties go to the **more recent** value — so a trend flips quickly. O(size²) with
  size ≤ 10 is fine (a `HashMap` tally would be needed if size were large).

```java
    public String join(String separator) { ... }

    private static boolean same(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
```

- `join` = Python `separator.join(...)`; `same` = null-safe `==`.

## 3.6 `MazeGraph.java` — Phase 2, the maze as a graph

This is the biggest algorithmic file. It turns the 868-tile maze into a small
**graph** (~100 nodes) and precomputes distances so the ghosts can pathfind cheaply.

### The idea (read this first)

A corridor tile with two opposite open sides is boring: you enter one end and must
leave the other. So we **contract** all corridors and keep only the interesting
tiles as graph **nodes**:

- intersections (3 or 4 open sides),
- corners (2 open sides that turn: one vertical + one horizontal),
- dead ends (1 open side).

The weight of an edge between two nodes is the number of tiles you walk to get
between them. Python analogy:

```python
# nodes = "interesting" cells; edges = corridors between them
```

```java
public final class MazeGraph {

    public static final int INF = 1_000_000_000;

    private static final Direction[] CARDINAL = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };
```

- `INF` — sentinel "no path". `1e9` is big but `2e9` still fits in an `int`, so
  adding two can't overflow (§1.5).
- `CARDINAL` — the four move directions, in the arcade ghost tie-break order
  (up, left, down, right).

```java
    private final Maze maze;
    private final int nodeCount;
    private int edgeCount;

    private final int[] ncol;
    private final int[] nrow;
    private final int[] tileToNode;

    private final int[][] edgeW;
    private final byte[][] edgeDir;
    private final int[][] adj;

    private final int[][] allPairs;
```

- `ncol`/`nrow` — column/row of each node (node id → tile).
- `tileToNode` — reverse map (flat tile index → node id, or `-1`). This is a
  **dense array** of size 868; it is cheap (a few KB) and gives O(1) lookup.
- `edgeW` — the **adjacency matrix**: `edgeW[u][v]` = corridor length u→v, `0` = no direct corridor.
- `edgeDir` — direction to walk from u to v, stored as a `byte` (the
  `Direction.ordinal()`, §1.15).
- `adj` — the **adjacency list**: `adj[u]` = array of neighbours, for O(degree)
  iteration.
- `allPairs` — **Floyd–Warshall** all-pairs shortest distances.

### Constructor pass 1: discover nodes

```java
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
```

- `Arrays.fill(arr, -1)` sets every entry (Python `arr[:] = [-1]*len(arr)`).
- Temporary arrays are sized `max` (worst case) then **trimmed** with
  `Arrays.copyOf(tmpCol, count)` to the exact node count. We don't know the count
  until we finish scanning, hence the two-step.

### Pass 2: walk corridors, fill the matrix

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
                        break;
                    }
                }
            }
        }
```

- For each node `u`, from each of the four directions, **march** one tile at a
  time while the tile is walkable. Count `steps`. Stop when you hit another node
  `v` → record an edge of length `steps`. This is the corridor contraction.
- `(byte) d.ordinal()` — cast the enum's index to a byte.
- `if (steps > max) break;` — a safety net against an infinite loop (should never
  happen because there are only `max` tiles).

> Note: a node's "open tile grid" is the special `walkable` below, which excludes
> walls, the ghost door and the ghost house.

### Pass 3: adjacency lists

```java
        adj = new int[nodeCount][];
        for (int u = 0; u < nodeCount; u++) {
            int deg = 0;
            for (int v = 0; v < nodeCount; v++) {
                if (edgeW[u][v] > 0) deg++;
            }
            int[] list = new int[deg];
            int i = 0;
            for (int v = 0; v < nodeCount; v++) {
                if (edgeW[u][v] > 0) list[i++] = v;
            }
            adj[u] = list;
        }
```

- Convert the matrix row into a compact list so iterating neighbours is
  O(degree) instead of O(nodeCount). Same "count then fill" pattern.

### Pass 4: Floyd–Warshall

```java
        allPairs = new int[nodeCount][nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            Arrays.fill(allPairs[i], INF);
            allPairs[i][i] = 0;
        }
        for (int u = 0; u < nodeCount; u++) {
            for (int v = 0; v < nodeCount; v++) {
                if (edgeW[u][v] > 0) allPairs[u][v] = edgeW[u][v];
            }
        }
        for (int k = 0; k < nodeCount; k++) {
            for (int i = 0; i < nodeCount; i++) {
                if (allPairs[i][k] == INF) continue;
                for (int j = 0; j < nodeCount; j++) {
                    if (allPairs[k][j] == INF) continue;
                    int via = allPairs[i][k] + allPairs[k][j];
                    if (via < allPairs[i][j]) allPairs[i][j] = via;
                }
            }
        }
```

- Classic **Floyd–Warshall** triple loop: "is going `i → k → j` shorter than
  `i → j`?" After it runs, `allPairs[i][j]` is the shortest path length between any
  two nodes. O(N³) one time at startup; with N≈100 it's ~1M ops — instant.
- The result powers `distance(u,v)` which the minimax **evaluation** calls a lot,
  turning "how far is the ghost from Pac-Man" into an O(1) array read.

### Geometry helpers

```java
    public boolean walkable(int c, int r) {
        if (r < 0 || r >= Maze.ROWS) return false;
        c = Maze.wrapCol(c);
        if (maze.isWall(c, r) || maze.isDoor(c, r)) return false;
        if (r >= 13 && r <= 15 && c >= 11 && c <= 16) return false;   // ghost house
        return true;
    }
```

- Ghost graph tiles: not out of bounds, not wall, not door, not inside the ghost
  house. The house is excluded because ghosts there follow a scripted exit, not
  pathfinding.

```java
    private boolean isDecisionPoint(int c, int r) {
        int open = 0;
        boolean hasVertical = false;
        boolean hasHorizontal = false;
        for (Direction d : CARDINAL) {
            if (walkable(c + d.dx, r + d.dy)) {
                open++;
                if (d == Direction.UP || d == Direction.DOWN) hasVertical = true;
                else hasHorizontal = true;
            }
        }
        if (open != 2) return true;
        return hasVertical && hasHorizontal;
    }
```

- Count open sides. `open != 2` (i.e. 1, 3, 4) is always a node (dead end or
  intersection). Exactly 2 is a node only if they **turn a corner**; two opposite
  sides is a plain corridor tile (not a node).

### Lookups

```java
    public int nodeAt(int c, int r) {
        if (r < 0 || r >= Maze.ROWS) return -1;
        return tileToNode[r * Maze.COLS + Maze.wrapCol(c)];
    }
    public int[] neighbors(int node) { return adj[node]; }
    public int degree(int node)     { return adj[node].length; }
    public int weight(int u, int v) { return edgeW[u][v]; }
    public int distance(int u, int v) { return allPairs[u][v]; }
```

- O(1) accessors. `degree` = number of neighbours (used by the evaluation
  function as "how many exits does Pac-Man have?").

```java
    public Direction dirBetween(int u, int v) {
        if (u < 0 || v < 0 || edgeW[u][v] == 0) return Direction.NONE;
        return Direction.values()[edgeDir[u][v]];
    }
```

- `Direction.values()` returns the array of constants; indexing by the stored
  `ordinal` gives the enum back. This is how a byte in a matrix becomes a
  `Direction`.

```java
    public int nearestNode(int c, int r) {
        int best = -1;
        int bestD = Integer.MAX_VALUE;
        for (int i = 0; i < nodeCount; i++) {
            int d = colDelta(c, ncol[i]) + Math.abs(r - nrow[i]);
            if (d < bestD) { bestD = d; best = i; }
        }
        return best;
    }
```

- Linear scan for the closest node under the **cylinder metric** (columns wrap).
  O(nodeCount) ≈ 100 — fine.

```java
    public int nodeAhead(int c, int r, Direction dir) {
        int here = nodeAt(c, r);
        if (here >= 0) return here;
        if (dir != null && dir != Direction.NONE) {
            int cc = c, rr = r;
            for (int i = 0; i < Maze.COLS; i++) {
                if (!maze.canMove(cc, rr, dir, false)) break;
                cc = Maze.wrapCol(cc + dir.dx);
                rr += dir.dy;
                int n = nodeAt(cc, rr);
                if (n >= 0) return n;
            }
        }
        return nearestNode(c, r);
    }
```

- If Pac-Man is **on** a node, that's the target. Otherwise march in his current
  direction until the next node — the intersection he is rolling toward. Fallback:
  nearest node.

```java
    public int colDelta(int a, int b) {
        int d = Math.abs(a - b);
        return Math.min(d, Maze.COLS - d);
    }

    public int heuristic(int u, int v) {
        return colDelta(ncol[u], ncol[v]) + Math.abs(nrow[u] - nrow[v]);
    }
```

- `colDelta` — wrap-aware column distance (going left from column 0 wraps to 27).
- `heuristic` — the A* heuristic. Never overestimates the true distance (one step
  changes it by at most one), so A* stays **optimal** even across the tunnel.

### A* and Dijkstra

```java
    public List<Integer> aStar(int start, int goal) { return search(start, goal, true); }
    public List<Integer> dijkstra(int start, int goal) { return search(start, goal, false); }
```

- Both call one routine; A* passes `useHeuristic = true`, Dijkstra `false`.
  Dijkstra is literally "A* with a zero heuristic".

```java
    private List<Integer> search(int start, int goal, boolean useHeuristic) {
        if (start < 0 || goal < 0) return List.of();
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
            if (closed[u]) continue;          // stale entry
            closed[u] = true;
            if (u == goal) return reconstruct(prev, start, goal);
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
        return List.of();
    }
```

- `gScore[u]` — best known cost from `start` to `u`. `prev[u]` — the node we came
  from (to rebuild the path). `closed[u]` — already finalised.
- `PriorityQueue<Entry>` — a **min-heap** (§5.2). Its comparator orders by `f`.
  The `lambda` `e -> e.f` (§1.17) is the Python `key=lambda e: e.f`.
- Loop: pop the lowest-`f` node; if it's the goal, rebuild the path. Otherwise
  relax every neighbour: if going through `u` is cheaper, update and push.
- **Stale entries**: we may push a node several times with decreasing `f`; when we
  pop an already-`closed` one we `continue`. This is the standard lazy-deletion
  heap trick (avoids an O(n) "decrease-key").

```java
    private static List<Integer> reconstruct(int[] prev, int start, int goal) {
        List<Integer> path = new ArrayList<>();
        for (int at = goal; at != -1; at = prev[at]) {
            path.add(at);
            if (at == start) break;
        }
        // reverse into start..goal order
        List<Integer> forward = new ArrayList<>(path.size());
        for (int i = path.size() - 1; i >= 0; i--) forward.add(path.get(i));
        return forward;
    }
```

- Walk `prev` backwards from `goal` to `start`, then reverse. Python:
  `path.reverse()`.

```java
    private static final class Entry {
        final double f;
        final int node;
        Entry(double f, int node) { this.f = f; this.node = node; }
    }
```

- A tiny immutable record-like class. `final` fields = constructed-and-frozen.

## 3.7 `Trie.java` — Phase 5, the prefix tree

```java
public final class Trie {

    public static final class Node {
        public final Node[] next = new Node[26];
        public String word;
    }

    private final Node root = new Node();
    public Node root() { return root; }
```

- Each `Node` has 26 child slots (`a`–`z`) plus `word` (non-null iff a command
  ends here). `next[k]` is `null` when no word uses that letter. The `word` field
  is the "is this a terminal?" flag.

```java
    public void insert(String word) {
        Node at = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            int k = c - 'a';
            if (k < 0 || k >= 26)
                throw new IllegalArgumentException("Trie only stores a-z, got '" + c + "'");
            if (at.next[k] == null) at.next[k] = new Node();
            at = at.next[k];
        }
        at.word = word;
    }
```

- Walk down the letters, creating nodes as needed, and mark the last node. In
  Python you'd do `node = node.children.setdefault(c, Node())`.
- `c - 'a'` maps `'a'`→0 … `'z'`→25 (§1.4).

```java
    public static Node step(Node from, char c) {
        if (from == null) return null;
        int k = c - 'a';
        if (k < 0 || k >= 26) return null;
        return from.next[k];
    }
```

- Follow one character. `static` because it needs no instance; callable as
  `Trie.step(node, c)`.

## 3.8 `SearchTree.java` — Phase 4, minimax with alpha-beta

```java
public final class SearchTree {

    public static final int DEPTH = 4;

    private static final double W_REAL = 3.0;
    private static final double W_MODEL = 1.5;
    private static final double W_CORNER = 1.5;
    private static final double BONUS_TRAPPED = 12.0;
    private static final double BONUS_CLOSE = 10.0;
    public static final double NO_MOVE = -1.0e9;

    private final MazeGraph graph;
    public int lastNodesVisited;
```

- `DEPTH = 4` plies (was 3; raised to 4 for a more decisive hunter): boss → Pac-Man
  → boss → Pac-Man. The comment says "boss -> Pac-Man -> boss -> Pac-Man (4 plies)".
- The `W_*` and `BONUS_*` are the tunable weights of the evaluation function.
- `lastNodesVisited` — instrumentation for the debug overlay.
- `NO_MOVE = -1e9` — a very bad score, used as the "Pac-Man unreachable" sentinel.

```java
    public int chooseMove(int ghostNode, int pacmanNode, Direction reverse) {
        if (ghostNode < 0 || pacmanNode < 0) return -1;
        lastNodesVisited = 0;
        int best = -1;
        double bestScore = NO_MOVE;
        double alpha = NO_MOVE;

        for (int next : graph.neighbors(ghostNode)) {
            if (reverse != null && graph.dirBetween(ghostNode, next) == reverse) continue;
            lastNodesVisited++;
            double score = minimax(next, pacmanNode, DEPTH - 1, false,
                    alpha, Double.POSITIVE_INFINITY, pacmanNode);
            if (score > bestScore) { bestScore = score; best = next; }
            if (score > alpha) alpha = score;
        }
        return best;
    }
```

- The boss tries each legal first move (never reversing). For each, it runs
  minimax with Pac-Man to move (`maxing = false`). `alpha` remembers the best the
  boss already guarantees; `beta` starts at `+inf`.
- `pacmanNode` is both the simulated Pac-Man's start AND the `realPacman` fixed
  reference.

```java
    private double minimax(int gNode, int pNode, int depth, boolean maxing,
                           double alpha, double beta, int realPacman) {
        if (depth == 0) return evaluate(gNode, pNode, realPacman);
        if (maxing) {
            double best = NO_MOVE;
            for (int next : graph.neighbors(gNode)) {
                lastNodesVisited++;
                double s = minimax(next, pNode, depth - 1, false, alpha, beta, realPacman);
                if (s > best) best = s;
                if (best > alpha) alpha = best;
                if (alpha >= beta) break;      // beta cut-off
            }
            return best;
        }
        double best = -NO_MOVE;
        for (int next : graph.neighbors(pNode)) {
            lastNodesVisited++;
            double s = minimax(gNode, next, depth - 1, true, alpha, beta, realPacman);
            if (s < best) best = s;
            if (best < beta) beta = best;
            if (alpha >= beta) break;          // alpha cut-off
        }
        return best;
    }
```

- **Two-player minimax**: `maxing` = the boss's turn (maximise score), else
  Pac-Man's turn (minimise score). Both explore every neighbour.
- `alpha`/`beta` pruning: once the maximiser has a result `>= beta`, the minimiser
  above would never allow this branch, so we `break`.
- At `depth == 0` we score the leaf with `evaluate`.
- This is ordinary recursion; the call stack **is** the search tree (§1.18, §5.2).

```java
    private double evaluate(int gNode, int pNode, int realPacman) {
        int dReal = graph.distance(gNode, realPacman);
        int dModel = graph.distance(gNode, pNode);
        if (dReal >= MazeGraph.INF) return NO_MOVE;
        double score = -W_REAL * dReal - W_MODEL * dModel;

        int exits = graph.degree(pNode);
        score += W_CORNER * (4 - exits);

        if (dReal <= 1) score += BONUS_CLOSE;
        if (exits > 0) {
            boolean cutOff = true;
            for (int exit : graph.neighbors(pNode)) {
                if (graph.distance(exit, gNode) > dModel) { cutOff = false; break; }
            }
            if (cutOff) score += BONUS_TRAPPED;
        }
        return score;
    }
```

- Score is from the **ghost's** point of view (bigger = better for the ghost), so
  distances enter **negated**.
- `dReal` — distance to the *real* Pac-Man (weight 3). `dModel` — distance to the
  *simulated* Pac-Man (weight 1.5).
- `exits = degree(pNode)`; fewer exits is better (cornering), hence `W_CORNER *
  (4 - exits)`.
- `BONUS_CLOSE` if already adjacent (`dReal <= 1`).
- `BONUS_TRAPPED` if **every** exit from Pac-Man's node leads to a node that is
  not farther from the ghost than the current `dModel` — i.e. all doors lead into
  the ghost's reach.

```java
    public String describeMove(List<Integer> path) { ... }
```

- Pretty-prints a path as `(col,row)->(col,row)` for the debug overlay.

## 3.9 `Pacman.java`

```java
public class Pacman extends Entity {
    public static final int MEMORY = 10;
    public Direction desired = Direction.NONE;
    public double animTime;
    public final RingBuffer<Direction> history = new RingBuffer<>(MEMORY);
    private int lastTileX = Integer.MIN_VALUE;
    private int lastTileY = Integer.MIN_VALUE;
```

- `desired` — the buffered turn the player just pressed. It is stored separately
  from `dir` so a press slightly before a corner still registers when you arrive.
- `history` — the Phase 3 ring buffer, memory of the last 10 tile-directions used.
- `lastTileX/Y` — remember the last tile we recorded, so the history gets one entry
  per tile, not per frame.

```java
    public Pacman(Maze maze, double tile) {
        super(maze, tile);
        speed = 128;
    }

    public void reset(double startX, double startY) {
        x = startX; y = startY;
        dir = Direction.NONE;
        desired = Direction.NONE;
        animTime = 0;
        history.clear();
        lastTileX = tileX();
        lastTileY = tileY();
    }
```

- Pac-Man moves at 128 px/s (vs ghosts ~104). `reset` re-centres him, clears the
  buffer and snapping bookkeeping.

### `update(dt)` — the heart of Pac-Man's movement

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

        // Instant reversal is always allowed.
        if (desired != Direction.NONE && desired != dir && dir != Direction.NONE
                && desired == dir.opposite()) {
            dir = desired;
            desired = Direction.NONE;
        }
```

- `step` = this frame's travel in pixels. `animTime` drives the mouth animation.
- `centered` = are we within one `step` of the tile centre (with 0.01 slack)?
- **Instant reversal** is handled first and unconditionally: if you press the
  opposite of the current direction, turn at once without snapping (Pac-Man can
  always about-face).

```java
        if (desired != Direction.NONE && desired != dir && centered) {
            x = ccx;
            y = ccy;
            if (maze.canMove(col, row, desired, false)) {
                dir = desired;
                desired = Direction.NONE;
            }
        }

        if (dir == Direction.NONE) return;
```

- A non-reverse turn is applied only **at a centre**, and only if the turn is
  legal. We snap to the centre first so the turn is exact.
- If still standing (no direction), stop.

```java
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

- Facing a wall at a centre → snap and stay, but keep facing it (classic
  behaviour). Otherwise advance by `step`, wrap through the tunnel, and record the
  move.

```java
    private void recordMove() {
        int tx = tileX(), ty = tileY();
        if (tx != lastTileX || ty != lastTileY) {
            if (dir != Direction.NONE) history.push(dir);
            lastTileX = tx; lastTileY = ty;
        }
    }
```

- Whenever the tile index changes, push the current direction into the ring
  buffer. One push per tile entered.

```java
    public boolean hasMemory(int n) { return history.size() >= n; }
    public Direction dominantDirection() { return history.dominant(); }
    public boolean moving() { return dir != Direction.NONE; }
```

- `dominantDirection` is the ring buffer's "which way has Pac-Man favoured lately"
  query — read by the TRACKER ghost.

## 3.10 `Ghost.java`

```java
public class Ghost extends Entity {

    public enum Kind { BLINKY, PINKY, INKY, CLYDE }
    public enum State { HOUSE, LEAVING, NORMAL, FRIGHTENED, EATEN, ENTERING }
    public enum Brain { BOSS, A_STAR, TRACKER, DIJKSTRA, HUNTER }
```

- `Kind` — which of the four classic ghosts.
- `State` — the ghost **state machine** (in the house waiting → leaving → normal →
  frightened → eaten → entering home).
- `Brain` — **which AI drives the ghost**: Blinky = `BOSS` (minimax), Pinky =
  `A_STAR`, Inky = `TRACKER` (ring-buffer prediction), Clyde = `DIJKSTRA`, and the
  special `HUNTER` (the solo opponent).

```java
    private static final Direction[] ORDER = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };
    private static final int TRACK_LOOKAHEAD = 10;

    public final Kind kind;
    public Brain brain;
    public boolean active = true;
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

- `ORDER` — the classic arcade tie-break order for choosing among equal distances.
- `kind` — `final`; `brain` — **not** final, because the solo mode swaps it at
  runtime.
- `active` — starts `true`; set `false` for ghosts parked in solo mode. Read by
  `Game` (update/collision) and `Renderer` (draw).
- `baseSpeed` — the per-ghost baseline (set by `Game` per level; hunter gets a
  boost).
- `scatterX/Y` — the ghost's home corner for scatter mode.
- `homeX/Y` — spawn centre (used by the bobbing-in-house animation).
- Bookkeeping fields for the debug overlay: `plannedPath`, `targetNode`,
  `bossPick`, `interceptNode`.
- `rng` — a per-ghost `Random` for the frightened random walk.
- `justDecided` — guard so a ghost decides once per tile-centre, not every frame
  (prevents jitter).

```java
    public Ghost(Maze maze, double tile, Kind kind) {
        super(maze, tile);
        this.kind = kind;
        this.brain = brainFor(kind);
    }
    public void setBrain(Brain brain) { this.brain = brain; }

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

- Construction assigns each classic ghost a different brain. `setBrain` is the
  runtime swap (used to promote Blinky to `HUNTER`).

### `reset` and `update`

```java
    public void reset(double startX, double startY, State startState, double release) {
        x = startX; y = startY;
        homeX = startX; homeY = startY;
        state = startState;
        houseTimer = release;
        dir = Direction.LEFT;
        bobDir = 1;
        plannedPath = Collections.emptyList();
        targetNode = -1; bossPick = -1; interceptNode = -1;
    }

    public void update(double dt, Game game) {
        applySpeed();
        switch (state) {
            case HOUSE:   updateInHouse(dt);      break;
            case LEAVING: updateLeaving(dt);      break;
            case ENTERING:updateEntering(dt);     break;
            case EATEN:   gridMove(dt, game, true);  break;
            default:      gridMove(dt, game, false); break;
        }
    }
```

- `update` picks a movement routine by state. `EATEN` eyes travel through the door
  (`canUseDoor = true`); everything else does not.

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

- Per-state speed multipliers: frightened ghosts are slow, eaten eyes are fast,
  house ghosts crawl.

```java
    private void updateInHouse(double dt) {
        houseTimer -= dt;
        if (houseTimer <= 0) { state = State.LEAVING; return; }
        y += bobDir * 22 * dt;
        if (y > homeY + tile * 0.28) { y = homeY + tile * 0.28; bobDir = -1; }
        else if (y < homeY - tile * 0.28) { y = homeY - tile * 0.28; bobDir = 1; }
    }
```

- The "release timer" counts down; when it hits zero the ghost starts leaving.
  Meanwhile it bobs up and down between `±0.28` tiles.

```java
    private void updateLeaving(double dt) {
        double step = speed * dt;
        double exitX = centerX(13);
        if (Math.abs(x - exitX) > step) { x += Math.signum(exitX - x) * step; return; }
        x = exitX;
        double exitY = centerY(11);
        if (y > exitY) { y -= step; if (y < exitY) y = exitY; return; }
        y = exitY;
        state = State.NORMAL;
        justDecided = false;
        dir = rng.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
    }
```

- Scripted exit: slide horizontally to column 13, then rise to row 11 (the door),
  then enter `NORMAL` with a random left/right heading. `Math.signum` returns
  `-1`/`0`/`1` (Python's `math.copysign`/signum).

```java
    private void updateEntering(double dt) {
        // same idea, but move DOWN into the house at column 13, row 14
        ...
        y = targetY;
        state = State.HOUSE;
        houseTimer = 0.9;
        bobDir = 1;
        dir = Direction.UP;
    }
```

- Mirror of leaving: an eaten ghost descends into the house, then waits 0.9 s
  before it leaves again.

### The grid core: `gridMove`

```java
    private void gridMove(double dt, Game game, boolean canUseDoor) {
        double step = speed * dt;
        int col = tileX(), row = tileY();
        double ccx = centerX(col), ccy = centerY(row);
        boolean centered = Math.abs(x - ccx) <= step + 0.01
                && Math.abs(y - ccy) <= step + 0.01;

        if (centered) {
            if (!justDecided) {
                snapToCenter();
                col = tileX(); row = tileY();
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

        if (dir == Direction.NONE) return;
        x += dir.dx * step;
        y += dir.dy * step;
        wrapX();
    }
```

- Same centering logic as Pac-Man, but: (a) a ghost decides only **once** at each
  centre (`justDecided`), and (b) an eaten ghost that reaches the door tile
  (col 13, row 11) switches to `ENTERING`.
- `snapToCenter()` then `tileX()` again: snapping can change the tile index at a
  boundary, so it is re-read.

### `chooseDirection` — the AI dispatch

```java
    private void chooseDirection(Game game, int col, int row, boolean canUseDoor) {
        Direction reverse = dir.opposite();
        List<Direction> options = new ArrayList<>(4);
        for (Direction d : ORDER) {
            if (d == reverse) continue;
            if (maze.canMove(col, row, d, canUseDoor)) options.add(d);
        }
        if (options.isEmpty()) { dir = reverse; return; }

        if (state == State.FRIGHTENED) {
            dir = options.get(rng.nextInt(options.size()));
            return;
        }
        if (canUseDoor) { dir = classicStep(game, col, row, options); return; }

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
```

- Build `options`: legal, non-reverse directions (classic no-U-turn rule).
- Frightened → random choice (`rng.nextInt(size)` picks an index).
- Eaten (`canUseDoor`) → classic greedy step home.
- Mid-corridor (`here < 0`): if only one option, take it; else head toward the
  target node.
- Already at the target node: drop planning and shadow Pac-Man directly
  (`chaseStep`).

```java
        boolean bossSearch = (brain == Brain.BOSS && game.globalMode == Game.Mode.CHASE)
                || brain == Brain.HUNTER;
        if (bossSearch) {
            int next = game.searchTree.chooseMove(here, target, reverse);
            bossPick = next;
            if (next >= 0) {
                List<Integer> route = new ArrayList<>();
                route.add(here);
                List<Integer> tail = graph.aStar(next, target);
                if (tail.size() >= 2) route.addAll(tail);
                else route.add(next);
                plannedPath = route;
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

- **BOSS** (only during global CHASE) or **HUNTER** (always): ask the search tree
  for the best next node, then commit to the full A* continuation from that node
  to the target and store it in `plannedPath`. That is why the boss reads as
  "moving with a plan" rather than twitching.
- Everyone else: plain shortest path — Dijkstra for Clyde, A* for Pinky.
- `path.size() < 2` means unreachable → fall back to a classic greedy step.

```java
    private int chooseTarget(Game game, MazeGraph graph, int col, int row) {
        switch (brain) {
            case BOSS:
            case HUNTER:
                if (game.globalMode == Game.Mode.SCATTER && brain != Brain.HUNTER)
                    return graph.nearestNode(scatterX, scatterY);
                return graph.nodeAhead(game.pacman.tileX(), game.pacman.tileY(), game.pacman.dir);
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

- `BOSS` targets the node Pac-Man is heading toward. In SCATTER it heads to its
  corner — but `HUNTER` is explicitly excluded (`brain != Brain.HUNTER`), so the
  hunter **never scatters** and always chases.
- `TRACKER` uses `trackerIntercept` (ring-buffer prediction) except in scatter.
- `A_STAR`/`DIJKSTRA` use the classic `Game.targetFor` (Blinky=direct,
  Pinky=4 ahead, Inky=mirror, Clyde=distance-dependent).

```java
    public int trackerIntercept(Game game, MazeGraph graph) {
        Pacman p = game.pacman;
        int px = p.tileX(), py = p.tileY();
        if (!p.hasMemory(3)) return graph.nodeAhead(px, py, p.dir);
        Direction trend = p.dominantDirection();
        if (trend == null || trend == Direction.NONE) return graph.nodeAhead(px, py, p.dir);
        int c = px, r = py;
        for (int i = 0; i < TRACK_LOOKAHEAD; i++) {
            if (!maze.canMove(c, r, trend, false)) break;
            c = Maze.wrapCol(c + trend.dx);
            r += trend.dy;
            int node = graph.nodeAt(c, r);
            if (node >= 0) return node;
        }
        return graph.nearestNode(c, r);
    }
```

- Phase 3 in action: without at least 3 remembered moves, just aim ahead of
  Pac-Man's current direction. Otherwise take his `dominantDirection()` (ring
  buffer) and march up to `TRACK_LOOKAHEAD = 10` tiles that way, returning the
  first node — a **flanking** target rather than a tail chase.

### Movement primitives

```java
    private Direction classicStep(Game game, int col, int row, List<Direction> options) {
        int[] target = game.targetFor(this, col, row);
        Direction best = options.get(0);
        double bestDist = Double.MAX_VALUE;
        for (Direction d : options) {
            int nc = Maze.wrapCol(col + d.dx);
            int nr = row + d.dy;
            double ddx = nc - target[0], ddy = nr - target[1];
            double dist = ddx * ddx + ddy * ddy;
            if (dist < bestDist) { bestDist = dist; best = d; }
        }
        return best;
    }
```

- Arcade-style greedy targeting: among the legal non-reverse directions, pick the
  one whose next tile is closest (squared Euclidean) to the ghost's arcade target.

```java
    private Direction chaseStep(Game game, int col, int row, List<Direction> options) {
        int px = game.pacman.tileX(), py = game.pacman.tileY();
        Direction pacDir = game.pacman.dir;
        Direction best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction d : ORDER) {                 // includes reverse now
            if (!maze.canMove(col, row, d, false)) continue;
            int nc = Maze.wrapCol(col + d.dx);
            int nr = row + d.dy;
            double dist = (nc - px) * (nc - px) + (nr - py) * (nr - py);
            if (dist < bestDist - 1e-9) { bestDist = dist; best = d; }
        }
        if (best == null) return options.isEmpty() ? dir.opposite() : options.get(0);
        if (pacDir != Direction.NONE && maze.canMove(col, row, pacDir, false)) {
            int nc = Maze.wrapCol(col + pacDir.dx);
            int nr = row + pacDir.dy;
            double dist = (nc - px) * (nc - px) + (nr - py) * (nr - py);
            if (Math.abs(dist - bestDist) < 1e-9) return pacDir;
        }
        return best;
    }
```

- Direct tile-by-tile shadowing. Reversal is allowed here (that's how the ghost
  turns around when it overshoots). Ties are broken toward Pac-Man's own heading
  so the ghost mirrors his motion. The `1e-9` epsilon is a floating-point tie
  tolerance.

```java
    private Direction bestToward(List<Direction> options, Game game, int col, int row, int target) {
        // pick the option whose next tile is closest to node `target`
        ...
    }

    public void reverse() {
        if (dir != Direction.NONE) dir = dir.opposite();
        justDecided = false;
    }
```

- `bestToward` — mid-corridor aiming at a node. `reverse` — used when scatter/chase
  flips or fright begins; also re-arms `justDecided`.

## 3.11 `Game.java` — the model / rules

```java
public class Game {

    public static final double TILE = 20;

    public enum State { TITLE, READY, PLAYING, PAUSED, DYING, LEVEL_COMPLETE, GAME_OVER }
    public enum Mode { SCATTER, CHASE }
    public enum GhostMode { ALL, SOLO }
```

- `State` — the top-level game state machine.
- `Mode` — the classic scatter/chase alternation.
- `GhostMode` — the user's roster choice (all four vs the solo hunter).

```java
    public final Maze maze = new Maze();
    public final Pacman pacman;
    public final List<Ghost> ghosts = new ArrayList<>();
    public final SoundManager sound = new SoundManager();

    public final MazeGraph graph;
    public final SearchTree searchTree;
    public final CommandEngine commands = new CommandEngine();

    public boolean showGraph;

    public State state = State.TITLE;
    public Mode globalMode = Mode.SCATTER;
    public GhostMode ghostMode = GhostMode.ALL;

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

- The model owns one of everything. `graph` and `searchTree` are built once in the
  constructor and reused.
- `ghostCombo` — number of frightened ghosts eaten in the current power-pellet
  window (drives the 200/400/800/1600 score).
- Three cheat timers (seconds remaining).

```java
    private static final Mode[] MODE_KIND = {
        Mode.SCATTER, Mode.CHASE, Mode.SCATTER, Mode.CHASE,
        Mode.SCATTER, Mode.CHASE, Mode.SCATTER, Mode.CHASE
    };
    private static final double[] MODE_DURATION = {
        7, 20, 7, 20, 5, 20, 5, Double.MAX_VALUE
    };
```

- The scatter/chase schedule. The final chase is effectively forever
  (`Double.MAX_VALUE`). This is the classic "ghosts wander home then hunt hard"
  pattern.

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
        setGhostMode(GhostMode.ALL);
        resetActors();
    }
```

- Wire Pac-Man, graph, search tree, four ghosts and their scatter corners. Then
  apply the default roster and place everyone.

```java
    public void setGhostMode(GhostMode mode) {
        ghostMode = mode;
        boolean solo = mode == GhostMode.SOLO;
        for (Ghost g : ghosts) {
            g.active = !solo || g.kind == Ghost.Kind.BLINKY;
            if (g.kind == Ghost.Kind.BLINKY) {
                g.setBrain(solo ? Ghost.Brain.HUNTER : Ghost.Brain.BOSS);
            }
        }
    }
```

- **The central change for the ghost-count feature.** In `SOLO`, only Blinky is
  `active`; the other three are parked (skipped by update/collision/draw). Blinky's
  brain is swapped to `HUNTER`, which never scatters and always runs minimax.
  Switching back to `ALL` restores everyone and Blinky's `BOSS` brain.

```java
    public void startNewGame() {
        score = 0; lives = 3; level = 1;
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

- Reset score/lives/level, rebuild the maze, place actors, hand control to READY.

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
            g.baseSpeed = (g.brain == Ghost.Brain.HUNTER) ? ghostSpeed * 1.12 : ghostSpeed;
        }
        ...
    }
```

- Pac-Man starts below the house; Blinky starts OUTSIDE (NORMAL, release 0) and the
  other three wait in the house with staggered release timers (2/6/10 s).
- Ghost speed scales with level (capped at +8 levels). The **hunter gets a ×1.12
  speed boost** so it can actually close a gap — the second half of the solo
  balancing.
- `center(i)` is `i * TILE + TILE/2`, the pixel centre of a column/row.

```java
    private void die() {
        lives--;
        state = State.DYING;
        stateTimer = 1.8;
        sound.death();
    }
```

- Classic death: lose a life, play the animation for 1.8 s.

### Input & cheats

```java
    public void requestDirection(Direction d) {
        if (state == State.PLAYING || state == State.READY) pacman.desired = d;
    }
    public void primaryAction() {
        switch (state) {
            case TITLE:
            case GAME_OVER: startNewGame(); break;
            case PAUSED: state = State.PLAYING; break;
            case PLAYING: state = State.PAUSED; break;
            default: break;
        }
    }
    public void togglePause() { ... }
    public void toggleMute() { sound.toggle(); }
```

- Input is only accepted while playing/ready. `primaryAction` (space/enter) starts
  or toggles pause.

```java
    public void feedCheat(char ch) {
        String command = commands.feed(ch);
        if (command != null) activateCheat(command);
    }

    private void activateCheat(String command) {
        double seconds;
        switch (command) {
            case "speedup": cheatSpeedTimer = 8; seconds = 8; break;
            case "freeze":  cheatFreezeTimer = 6; seconds = 6; break;
            case "godmode": cheatGodTimer = 10; seconds = 10; break;
            default: return;
        }
        sound.cheat();
        System.out.println("[CHEAT] '" + command + "' activated for " + seconds + "s");
    }
    public boolean ghostsFrozen()  { return cheatFreezeTimer > 0; }
    public boolean godMode()       { return cheatGodTimer > 0; }
    public boolean speedBoost()    { return cheatSpeedTimer > 0; }
```

- Letters typed during play go into the trie engine; a completed command arms a
  timer and prints to the console.

### `update(dt)`

```java
    public void update(double dt) {
        time += dt;
        switch (state) {
            case READY:
                stateTimer -= dt;
                if (stateTimer <= 0) state = State.PLAYING;
                break;
            case PLAYING: updatePlaying(dt); break;
            case DYING:
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    if (lives <= 0) {
                        state = State.GAME_OVER;
                        if (score > highScore) highScore = score;
                    } else {
                        resetActors();
                        state = State.READY;
                        stateTimer = 1.5;
                    }
                }
                break;
            case LEVEL_COMPLETE:
                stateTimer -= dt;
                if (stateTimer <= 0) nextLevel();
                break;
            default: break;
        }
    }
```

- The top-level state machine. READY counts down to PLAYING; DYING either ends the
  game (update high score) or respawns; LEVEL_COMPLETE advances.

```java
    private void updatePlaying(double dt) {
        tickCheats(dt);

        if (frightTimer > 0) {
            frightTimer -= dt;
            if (frightTimer <= 0) {
                frightTimer = 0;
                for (Ghost g : ghosts)
                    if (g.state == Ghost.State.FRIGHTENED) g.state = Ghost.State.NORMAL;
            }
        } else {
            modeTimer -= dt;
            if (modeTimer <= 0) advanceMode();
        }

        pacman.update(dt);
        eatAtPacman();

        if (!ghostsFrozen()) {
            for (Ghost g : ghosts)
                if (g.active) g.update(dt, this);
        }

        checkCollisions();

        if (maze.pelletsLeft() == 0) {
            state = State.LEVEL_COMPLETE;
            stateTimer = 2.4;
            sound.levelUp();
        }
    }
```

- The main tick: cheat timers; fright timer OR scatter/chase timer (mutually
  exclusive — while frightened the mode clock is paused, arcade-accurate).
- Pac-Man moves, eats; **only active ghosts** update (the `if (g.active)` guard is
  where solo mode parks the other three); collisions; level-complete check.

```java
    private void tickCheats(double dt) {
        if (cheatSpeedTimer > 0) cheatSpeedTimer = Math.max(0, cheatSpeedTimer - dt);
        if (cheatFreezeTimer > 0) cheatFreezeTimer = Math.max(0, cheatFreezeTimer - dt);
        if (cheatGodTimer > 0) cheatGodTimer = Math.max(0, cheatGodTimer - dt);
        pacman.speed = pacmanBaseSpeed * (cheatSpeedTimer > 0 ? 1.75 : 1.0);
    }
```

- Count down each timer (never below 0) and apply the speed multiplier.

```java
    private void eatAtPacman() {
        int e = maze.eat(pacman.tileX(), pacman.tileY());
        if (e == 1) { score += 10; sound.chomp(); }
        else if (e == 2) { score += 50; startFright(); sound.power(); }
        if (e != 0) maze.maybeCompress();   // phase 6 trigger
    }
```

- Eat the tile under Pac-Man: `1` = pellet (+10), `2` = power (+50 and fright).
  Every eaten pellet tries the Phase 6 compression.

```java
    private void startFright() {
        ghostCombo = 0;
        frightTimer = Math.max(2.0, 8.0 - (level - 1) * 0.5);
        for (Ghost g : ghosts) {
            if (g.active && g.state == Ghost.State.NORMAL) {
                g.state = Ghost.State.FRIGHTENED;
                g.reverse();
            }
        }
    }
```

- Power pellet: reset the combo, set fright time (shrinks with level, min 2 s),
  and turn every **active** NORMAL ghost frightened and reverse it.

```java
    private void advanceMode() {
        if (modeIndex < MODE_KIND.length - 1) modeIndex++;
        globalMode = MODE_KIND[modeIndex];
        modeTimer = MODE_DURATION[modeIndex];
        for (Ghost g : ghosts) {
            if (g.active && g.state == Ghost.State.NORMAL) g.reverse();
        }
    }
```

- Step through the scatter/chase schedule and reverse the active ghosts (classic
  "they flip around" behaviour).

```java
    private void checkCollisions() {
        double hit = TILE * 0.65;
        double hit2 = hit * hit;
        for (Ghost g : ghosts) {
            if (!g.active) continue;
            if (g.state == Ghost.State.EATEN
                    || g.state == Ghost.State.ENTERING
                    || g.state == Ghost.State.HOUSE) continue;
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
                    if (!godMode()) { die(); return; }
                }
            }
        }
    }
```

- Circle overlap test using **squared** distance (avoids a square root). Radius
  `0.65` tiles. Inactive ghosts are skipped; eyes/entering/house ghosts can't
  collide.
- Frightened → eaten, combo scoring `200 * 2^(combo-1)` (`1 << k` is `2^k`, §1.4).
- NORMAL → death, unless god mode is on.

### Targeting

```java
    public int[] targetFor(Ghost g, int col, int row) {
        if (g.state == Ghost.State.EATEN) return new int[]{13, 11};
        if (globalMode == Mode.SCATTER) return new int[]{g.scatterX, g.scatterY};

        int px = pacman.tileX(), py = pacman.tileY();
        int pdx = pacman.dir.dx, pdy = pacman.dir.dy;

        switch (g.kind) {
            case BLINKY: return new int[]{px, py};
            case PINKY:  return new int[]{px + pdx * 4, py + pdy * 4};
            case INKY: {
                Ghost blinky = ghosts.get(0);
                int ax = px + pdx * 2, ay = py + pdy * 2;
                return new int[]{ax * 2 - blinky.tileX(), ay * 2 - blinky.tileY()};
            }
            case CLYDE:
            default: {
                double dist = Math.hypot(px - col, py - row);
                if (dist > 8) return new int[]{px, py};
                return new int[]{g.scatterX, g.scatterY};
            }
        }
    }
    public boolean isFrightBlink() {
        return frightTimer > 0 && frightTimer < 2.0 && ((int) (frightTimer * 6)) % 2 == 0;
    }
}
```

- The authentic arcade targeting: Blinky aims at Pac-Man; Pinky 4 tiles ahead;
  Inky uses the vector from Blinky through a point 2 ahead of Pac-Man, doubled
  (the classic mirror trick); Clyde chases when far (>8) but retreats to his
  corner when close. Scatter sends everyone home; eaten eyes head for the door.
- `isFrightBlink` — the HUD blink during the last 2 s of fright, toggling 6 times
  a second.

## 3.12 `Renderer.java` — the view

```java
public class Renderer {

    public static final double TILE = Game.TILE;
    public static final double HUD = 48;
    public static final double BOARD_W = Maze.COLS * TILE;
    public static final double BOARD_H = Maze.ROWS * TILE;
    public static final double CANVAS_W = BOARD_W;
    public static final double CANVAS_H = BOARD_H + HUD;

    private static final Color WALL = Color.web("#2121DE");
    private static final Color DOOR = Color.web("#FFB8FF");
    private static final Color PELLET = Color.web("#FFC4A8");
    private static final Color PAC = Color.web("#FFE100");
    private static final Color TEXT = Color.web("#FFE100");
    private static final Color RED = Color.web("#FF3B3B");
```

- Derived geometry: board `560 x 620`, plus a 48-pixel HUD → `560 x 668`.
- `Color.web("#RRGGBB")` parses a hex string into a JavaFX colour.

```java
    public void render(GraphicsContext gc, Game game) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        drawWalls(gc, game.maze);
        drawDoor(gc, game.maze);
        drawPellets(gc, game);
        if (game.showGraph) drawGraph(gc, game);
        drawPacman(gc, game);
        drawGhosts(gc, game);
        drawHud(gc, game);
        drawCheatTags(gc, game);
        drawOverlay(gc, game);
    }
```

- The draw order is a **painter's algorithm**: later calls paint on top. Clear →
  maze → pellets → (optional graph) → Pac-Man → ghosts → HUD → cheat tags →
  overlay text. `GraphicsContext` (`gc`) is the canvas paintbrush.

```java
    private void drawWalls(GraphicsContext gc, Maze maze) {
        gc.setStroke(WALL);
        gc.setLineWidth(2.4);
        gc.setLineCap(StrokeLineCap.ROUND);
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                if (!maze.isWall(c, r)) continue;
                double x0 = c * TILE, y0 = r * TILE;
                double x1 = x0 + TILE, y1 = y0 + TILE;
                if (!maze.isWall(c, r - 1)) line(gc, x0 + 1, y0 + 1, x1 - 1, y0 + 1);
                if (!maze.isWall(c, r + 1)) line(gc, x0 + 1, y1 - 1, x1 - 1, y1 - 1);
                if (!maze.isWall(c - 1, r)) line(gc, x0 + 1, y0 + 1, x0 + 1, y1 - 1);
                if (!maze.isWall(c + 1, r)) line(gc, x1 - 1, y0 + 1, x1 - 1, y1 - 1);
            }
        }
    }
```

- Instead of filling each wall tile, it draws a line on **every side of a wall tile
  that faces open space**. This yields the thin blue outline look of the arcade.
  The `+1`/`-1` insets keep the lines inside their tiles.

```java
    private void drawPellets(GraphicsContext gc, Game game) {
        boolean powerVisible = ((int) (game.time * 4)) % 2 == 0;
        gc.setFill(PELLET);
        if (game.maze.isCompressed()) {
            SparseMatrix s = game.maze.sparse();
            for (int i = 0; i < s.size(); i++) {
                double cx = s.col(i) * TILE + TILE / 2.0;
                double cy = s.row(i) * TILE + TILE / 2.0;
                if (s.value(i) == SparseMatrix.PELLET) gc.fillOval(cx - 2.2, cy - 2.2, 4.4, 4.4);
                else if (powerVisible) gc.fillOval(cx - 5.5, cy - 5.5, 11, 11);
            }
            return;
        }
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                char ch = game.maze.tileAt(c, r);
                double cx = c * TILE + TILE / 2.0;
                double cy = r * TILE + TILE / 2.0;
                if (ch == Maze.PELLET) gc.fillOval(cx - 2.2, cy - 2.2, 4.4, 4.4);
                else if (ch == Maze.POWER && powerVisible) gc.fillOval(cx - 5.5, cy - 5.5, 11, 11);
            }
        }
    }
```

- Phase 6-aware drawing: dense mode scans all 868 tiles; compressed mode iterates
  only the survivors in the sparse matrix. `powerVisible` pulses the power pellets
  4 times a second using `game.time`.
- `fillOval(x, y, w, h)` draws an ellipse with its top-left at `(x,y)`; we offset
  by half the diameter to centre it.

```java
    private void drawPacman(GraphicsContext gc, Game game) {
        Pacman p = game.pacman;
        double r = TILE * 0.47;
        double mouth, base;
        if (game.state == Game.State.DYING) {
            double progress = 1 - Math.max(0, game.stateTimer) / 1.8;
            mouth = 6 + 348 * Math.min(1, progress);
        } else {
            mouth = p.moving() ? 16 + 22 * Math.abs(Math.sin(p.animTime * 9)) : 14;
        }
        switch (p.dir) {
            case UP: base = 270; break;
            case DOWN: base = 90; break;
            case LEFT: base = 180; break;
            default: base = 0; break;
        }
        gc.setFill(PAC);
        gc.beginPath();
        gc.moveTo(p.x, p.y);
        double start = base + mouth / 2.0;
        double end = base + 360 - mouth / 2.0;
        int steps = 28;
        for (int i = 0; i <= steps; i++) {
            double a = Math.toRadians(start + (end - start) * i / steps);
            gc.lineTo(p.x + r * Math.cos(a), p.y + r * Math.sin(a));
        }
        gc.closePath();
        gc.fill();
    }
```

- Pac-Man is drawn as a **pac-shaped polygon**: a full circle minus a wedge
  ("mouth"). While moving, the mouth angle oscillates with `sin(animTime*9)`. The
  `base` angle orients the mouth to the travel direction (down = 90°, up = 270°).
- During `DYING`, the mouth opens all the way (`6 → 354°`) as the death animation.

```java
    private void drawGhosts(GraphicsContext gc, Game game) {
        for (Ghost g : game.ghosts) {
            if (g.active) drawGhost(gc, game, g);   // <-- solo mode skips the parked three
        }
    }
```

- Only active ghosts are drawn. This is the render-side half of the solo-mode
  parking (`Game` skips them in update; `Renderer` skips them here).

```java
    private void drawGhost(GraphicsContext gc, Game game, Ghost g) {
        double r = TILE * 0.46;
        double x = g.x, y = g.y;
        boolean frightened = g.state == Ghost.State.FRIGHTENED;
        boolean eaten = g.state == Ghost.State.EATEN || g.state == Ghost.State.ENTERING;

        if (!eaten) {
            Color body = frightened
                    ? (game.isFrightBlink() ? Color.web("#FFFFFF") : Color.web("#2121DE"))
                    : ghostColor(g.kind);
            drawGhostBody(gc, x, y, r, body);
        }
        drawEyes(gc, x, y, r, g, frightened, eaten);
    }
```

- Eaten ghosts draw only eyes. Frightened ghosts are blue, blinking white. Others
  use their identity colour.

```java
    private void drawGhostBody(GraphicsContext gc, double x, double y, double r, Color color) {
        double bottom = y + r * 0.55;
        gc.setFill(color);
        gc.beginPath();
        gc.moveTo(x - r, bottom);
        for (int i = 0; i <= 20; i++) {
            double a = Math.PI + Math.PI * i / 20.0;      // top half-circle
            gc.lineTo(x + r * Math.cos(a), y + r * Math.sin(a));
        }
        gc.lineTo(x + r, bottom);
        int segs = 3;
        double segW = (2 * r) / segs;
        double bump = r * 0.32;
        for (int i = 0; i < segs; i++) {
            double sx = x + r - i * segW;
            double ex = sx - segW;
            double mx = (sx + ex) / 2;
            gc.quadraticCurveTo(mx, bottom + bump, ex, bottom);
        }
        gc.closePath();
        gc.fill();
    }
```

- Dome: a half-circle from π to 2π (the top). Then three **quadratic Bézier**
  scallops along the bottom (`quadraticCurveTo`) form the classic wavy skirt.

```java
    private void drawEyes(GraphicsContext gc, double x, double y, double r,
                          Ghost g, boolean frightened, boolean eaten) {
        double eyeDX = r * 0.34;
        double eyeDY = -r * 0.12;
        double eyeR = r * 0.27;
        double pupilR = r * 0.14;
        for (int s = -1; s <= 1; s += 2) {
            double ex = x + s * eyeDX;
            double ey = y + eyeDY;
            gc.setFill(Color.WHITE);
            gc.fillOval(ex - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);
            double px = ex + g.dir.dx * r * 0.12;
            double py = ey + g.dir.dy * r * 0.12;
            gc.setFill(Color.web("#2121DE"));
            gc.fillOval(px - pupilR, py - pupilR, pupilR * 2, pupilR * 2);
        }
        if (frightened && !eaten) {
            // white zig-zag mouth
            ...
        }
    }
```

- The loop `for (int s = -1; s <= 1; s += 2)` runs for `s = -1` and `s = +1`,
  i.e. left and right eye. Pupils shift with `g.dir` so the eyes look where the
  ghost is heading.

```java
    private void drawGraph(GraphicsContext gc, Game game) {
        MazeGraph g = game.graph;
        gc.setStroke(Color.web("#00E5FF", 0.22));
        gc.setLineWidth(1.1);
        for (int u = 0; u < g.nodeCount(); u++)
            for (int v : g.neighbors(u))
                if (v > u) line(gc, nodeX(g, u), nodeY(g, u), nodeX(g, v), nodeY(g, v));

        for (Ghost gh : game.ghosts) {
            if (!gh.active) continue;
            List<Integer> path = gh.plannedPath;
            gc.setStroke(ghostColor(gh.kind));
            gc.setLineWidth(2.4);
            for (int i = 0; i + 1 < path.size(); i++)
                line(gc, nodeX(g, path.get(i)), nodeY(g, path.get(i)),
                         nodeX(g, path.get(i + 1)), nodeY(g, path.get(i + 1)));
            if (gh.bossPick >= 0) { ... white circle on the minimax pick ... }
            if (gh.interceptNode >= 0) { ... pink cross on the tracker target ... }
        }

        gc.setFill(Color.web("#FFFFFF", 0.8));
        for (int u = 0; u < g.nodeCount(); u++)
            gc.fillOval(nodeX(g, u) - 2.2, nodeY(g, u) - 2.2, 4.4, 4.4);

        gc.setFill(Color.web("#00E5FF"));
        gc.fillText("GRAPH " + g.nodeCount() + " nodes / " + g.edgeCount()
                + " edges   [G to hide]", 8, 16);
        gc.setFill(Color.web("#FFD700"));
        gc.fillText("MEMORY(10): " + arrowHistory(game.pacman), 8, 30);
    }
```

- The debug overlay (toggled with G): faint graph edges, each active ghost's
  planned path in its colour, the minimax choice as a white ring, the tracker's
  intercept as a pink X, white node dots, and a HUD line showing Pac-Man's ring
  buffer as arrow glyphs.

```java
    private void drawHud(GraphicsContext gc, Game game) {
        double top = BOARD_H;
        gc.setFill(Color.web("#05050A"));
        gc.fillRect(0, top, CANVAS_W, HUD);

        gc.setFill(TEXT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE " + game.score, 12, top + 30);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("LEVEL " + game.level, CANVAS_W - 12, top + 30);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(game.sound.isEnabled() ? Color.web("#8AE234") : Color.web("#666666"));
        gc.fillText(game.sound.isEnabled() ? "SOUND ON" : "MUTED", CANVAS_W / 2, top + 30);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(game.ghostMode == Game.GhostMode.SOLO
                ? Color.web("#FF3B3B") : Color.web("#8890BB"));
        gc.fillText(game.ghostMode == Game.GhostMode.SOLO ? "SOLO HUNTER" : "CLASSIC x4",
                CANVAS_W / 2, top + 44);

        for (int i = 0; i < game.lives; i++) drawLifeIcon(gc, 14 + i * 26, top + 10);
    }
```

- The bottom HUD bar: score (left), level (right), sound state (centre), and the
  **roster label** — `SOLO HUNTER` in red or `CLASSIC x4` in grey, reflecting
  `game.ghostMode`. Then one Pac-Man icon per remaining life.

```java
    private void drawOverlay(GraphicsContext gc, Game game) {
        double cx = BOARD_W / 2;
        double cy = BOARD_H / 2;
        gc.setTextAlign(TextAlignment.CENTER);
        switch (game.state) {
            case TITLE:           ... title card ...             break;
            case READY:           ... "READY!" ...               break;
            case PAUSED:          ... "PAUSED" ...               break;
            case LEVEL_COMPLETE:  ... "LEVEL n COMPLETE" ...     break;
            case GAME_OVER:       ... "GAME OVER" + score ...    break;
            default: break;
        }
    }
```

- Full-screen messages per game state, drawn last so they sit on top. The
  semi-transparent panels use the 8-digit `#RRGGBBAA` colour form.
- Note: the FXML start menu also covers the screen with its own overlay while the
  model is in `TITLE`; both exist, and the menu is what the player actually
  interacts with before pressing START.

## 3.13 `CommandEngine.java` — Phase 5 wiring

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
    public void reset() { cursor = trie.root(); }
```

- The engine owns a trie pre-loaded with the three cheat words and one **cursor**
  node that remembers the partial word typed so far.

```java
    public String feed(char ch) {
        char c = Character.toLowerCase(ch);
        if (c < 'a' || c > 'z') return null;
        Trie.Node next = Trie.step(cursor, c);
        if (next == null) {
            reset();
            next = Trie.step(cursor, c);
            if (next == null) return null;
        }
        cursor = next;
        if (cursor.word != null) {
            String done = cursor.word;
            reset();
            return done;
        }
        return null;
    }
    public Trie.Node cursor() { return cursor; }
}
```

- Lower-case the char; ignore non-letters. Try to advance the cursor. If that
  fails, **restart from the root** and retry — this is what lets `xxgodmode` still
  work; only the last `x` is dropped, not the whole buffer.
- On reaching a node with a word, return the command and reset the cursor.
- O(L) per keystroke, no buffering string to scan.

## 3.14 `SoundManager.java`

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

- No sound files: every effect is a **synthesised** waveform generated at play
  time.
- `ExecutorService` is a background thread pool (§1.19). `newSingleThreadExecutor`
  gives exactly one worker thread, named `pacman-sound`.
- A **daemon** thread does not keep the JVM alive when the main program ends.
- `volatile` guarantees the `enabled` flag is read fresh across threads (§1.19).
- `lastChomp` throttles the chomp sound.

```java
    public void chomp() {
        long now = System.currentTimeMillis();
        if (now - lastChomp < 110) return;      // at most ~9 chomps/sec
        lastChomp = now;
        async(() -> play(430 + (int) (System.nanoTime() % 90), 60, 0.22));
    }
    public void power()    { async(() -> play(170, 240, 0.30)); }
    public void eatGhost() { async(() -> play(760, 160, 0.34)); }
    public void death()    { async(() -> { int[] notes = {520,470,420,360,300,250,190,140};
                                            for (int n : notes) play(n, 110, 0.32); }); }
    // levelUp(), cheat() similar note sequences
```

- `() -> ...` is a **lambda** implementing `Runnable` (§1.17). Each sound runs on
  the worker thread so it never stalls the 60 fps loop.
- `death()`/`levelUp()`/`cheat()` play a short **sequence** of notes.

```java
    private void async(Runnable r) { if (enabled) pool.submit(r); }

    private void play(int freq, int ms, double vol) {
        if (!enabled) return;
        try {
            int len = (int) (RATE * ms / 1000.0);
            byte[] buf = new byte[len];
            for (int i = 0; i < len; i++) {
                double t = i / RATE;
                double env = Math.min(1.0, Math.min(i / (RATE * 0.008),
                                                     (len - i) / (RATE * 0.03)));
                double wobble = Math.sin(2 * Math.PI * freq * t);
                buf[i] = (byte) (wobble * env * vol * 127);
            }
            AudioFormat fmt = new AudioFormat(RATE, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(info)) return;
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt); line.start();
            line.write(buf, 0, len);
            line.drain(); line.close();
        } catch (Exception ignored) {
            // audio is optional
        }
    }
```

- Build a PCM buffer: a **sine wave** `sin(2πft)` scaled by an **envelope** `env`
  (quick 8 ms fade-in, 30 ms fade-out) to avoid clicks, times volume. 8-bit mono.
- `SourceDataLine` is the low-level audio output. Wrapped in try/catch so a machine
  with no audio device simply gets silence.
- `catch (Exception ignored)` — intentionally swallow errors; sound is optional.

## 3.15 `PacmanApp.java` — bootstrap

```java
public class PacmanApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                PacmanApp.class.getResource("/com/pacman/pacman.fxml"));
        Parent root = loader.load();
        GameController controller = loader.getController();

        Scene scene = new Scene(root, Renderer.CANVAS_W, Renderer.CANVAS_H, Color.BLACK);
        controller.attach(scene);

        stage.setTitle("Pac-Man");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        controller.start();
    }
    public static void main(String[] args) { launch(args); }
}
```

- `Application` is JavaFX's entry-point base class. `launch(args)` starts the
  toolkit and calls `start(stage)`.
- `getResource("/com/pacman/pacman.fxml")` finds the FXML on the **classpath**
  (leading `/` = from the root). The build copies it into `out/`.
- `loader.load()` parses the FXML, builds the scene graph, and instantiates the
  controller named in `fx:controller`. `loader.getController()` returns that same
  controller so we can call `attach`/`start`.
- Compare with the old hand-coded version: previously `PacmanApp` built the
  `Canvas` and wired keys itself. Now all that lives in FXML + `GameController`,
  and `PacmanApp` is trivial.

## 3.16 `GameController.java` and `pacman.fxml` — the controller & view layout

### The FXML (`pacman.fxml`)

```xml
<?import javafx.scene.canvas.Canvas?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.RadioButton?>
<?import javafx.scene.control.ToggleGroup?>
<?import javafx.scene.layout.AnchorPane?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.text.Font?>

<AnchorPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.pacman.GameController"
            fx:id="root"
            prefWidth="560.0" prefHeight="668.0"
            style="-fx-background-color: black;">

    <children>
        <Canvas fx:id="canvas" width="560.0" height="668.0"/>

        <VBox fx:id="menu" alignment="CENTER" spacing="16.0"
              AnchorPane.topAnchor="0.0" AnchorPane.bottomAnchor="0.0"
              AnchorPane.leftAnchor="0.0" AnchorPane.rightAnchor="0.0"
              style="-fx-background-color: rgba(0,0,0,0.84);">
            <children>
                <Label text="PAC-MAN" textFill="#FFE100">
                    <font><Font name="Monospaced" size="46.0"/></font>
                </Label>
                ...
                <RadioButton fx:id="soloGhost" text="1 Ghost  -  HUNTER  (hard, calculated)"
                             textFill="#FF3B3B"
                             style="-fx-font-family: Monospaced; -fx-font-size: 14px;">
                    <toggleGroup>
                        <ToggleGroup fx:id="ghostGroup"/>
                    </toggleGroup>
                </RadioButton>
                <RadioButton fx:id="allGhosts" text="4 Ghosts  -  CLASSIC  (...)"
                             textFill="#00E5FF" selected="true" toggleGroup="$ghostGroup"/>
                <Button fx:id="startButton" text="START" onAction="#onStart"/>
                ...
                <Label text="secret codes:  speedup   freeze   godmode" .../>
            </children>
        </VBox>
    </children>
</AnchorPane>
```

What each piece means:

- `<?import ...?>` — brings a Java class into the XML so tags can use short names.
- `fx:controller="com.pacman.GameController"` — the class whose `@FXML` fields are
  injected and whose `#onStart` method the button calls.
- `fx:id="..."` — names an element so the controller can receive it via a matching
  `@FXML` field of the same name.
- `AnchorPane` — a layout that pins children to its edges.
- `Canvas` — the drawing surface (managed by the `Renderer`, not by layout).
- `VBox` — a vertical stack; `spacing` = gap between children. It is anchored to
  all four edges so it fills the window — this is the dark **start menu overlay**.
- `ToggleGroup` — makes the two radios mutually exclusive (only one selectable).
  The first radio declares/creates the group; the second references it with
  `toggleGroup="$ghostGroup"`.
- `selected="true"` on `allGhosts` — classic four-ghost is the default.
- `onAction="#onStart"` — clicking START calls the controller's `onStart`.
- `style="-fx-..."` — inline CSS. `rgba(0,0,0,0.84)` is a translucent black panel.

### The controller (`GameController.java`)

```java
public class GameController {

    @FXML private Canvas canvas;
    @FXML private VBox menu;
    @FXML private RadioButton soloGhost;
    @FXML private RadioButton allGhosts;
    @FXML private Button startButton;
    @FXML private Label statusLabel;

    private Game game;
    private Renderer renderer;
    private GraphicsContext gc;
    private long lastNano;
    private boolean running;
```

- `@FXML` marks fields the loader fills by matching `fx:id`. The annotation is how
  the FXML mechanism finds them (reflection).
- `running` — `false` while the menu is showing, `true` during play.

```java
    @FXML
    private void initialize() {
        game = new Game();
        renderer = new Renderer();
        canvas.setWidth(Renderer.CANVAS_W);
        canvas.setHeight(Renderer.CANVAS_H);
        gc = canvas.getGraphicsContext2D();
    }
```

- `initialize()` is called by the loader **after** field injection. This is where
  the model, renderer and graphics context are created. We also force the canvas
  size from code so it always matches the renderer's constants.

```java
    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (!running) {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) onStart();
                return;
            }
            onKeyPressed(e.getCode());
            String text = e.getText();
            if (text != null && text.length() == 1) game.feedCheat(text.charAt(0));
        });
    }
```

- Register one key handler for the whole scene.
- While the menu is up (`!running`) only ENTER/SPACE start the game.
- Otherwise, route movement/commands through `onKeyPressed`, and feed each typed
  single character to the cheat engine.

```java
    public void start() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNano == 0) { lastNano = now; return; }
                double dt = (now - lastNano) / 1_000_000_000.0;
                lastNano = now;
                if (dt > 0.05) dt = 0.05;
                if (running) {
                    game.update(dt);
                    if (game.state == Game.State.GAME_OVER) {
                        running = false;
                        showMenu();
                    }
                }
                renderer.render(gc, game);
            }
        };
        timer.start();
    }
```

- `AnimationTimer` is JavaFX's per-frame callback; `now` is in **nanoseconds**.
  The first call only records the baseline.
- `dt` is computed and clamped (the same loop described in §2.2). When the game
  ends, `running` flips false and the menu reappears.
- Rendering happens **every** frame, even in the menu, so the board is visible
  behind the translucent overlay.

```java
    @FXML
    private void onStart() {
        game.setGhostMode(soloGhost.isSelected()
                ? Game.GhostMode.SOLO
                : Game.GhostMode.ALL);
        game.startNewGame();
        menu.setVisible(false);
        menu.setManaged(false);
        statusLabel.setText("");
        running = true;
        lastNano = 0;
        canvas.requestFocus();
    }

    private void showMenu() {
        menu.setVisible(true);
        menu.setManaged(true);
        statusLabel.setText("GAME OVER     SCORE " + game.score);
        startButton.requestFocus();
    }
```

- **The START handler**: read the radio selection, tell the `Game` which roster to
  use, start a new game, hide the menu.
- `setManaged(false)` removes the hidden `VBox` from layout so it doesn't capture
  input; `setVisible(false)` hides it visually. Both are needed.
- `lastNano = 0` tells the loop to re-baseline `dt` (avoids a huge first frame).
- `requestFocus()` sends keyboard focus to the canvas.
- `showMenu` restores the menu and shows the final score.

```java
    private void onKeyPressed(KeyCode code) {
        switch (code) {
            case UP: case W:    game.requestDirection(Direction.UP);    break;
            case DOWN: case S:  game.requestDirection(Direction.DOWN);  break;
            case LEFT: case A:  game.requestDirection(Direction.LEFT);  break;
            case RIGHT: case D: game.requestDirection(Direction.RIGHT); break;
            case SPACE: case ENTER: game.primaryAction(); break;
            case P: game.togglePause(); break;
            case M: game.toggleMute();  break;
            case G: game.showGraph = !game.showGraph; break;
            default: break;
        }
    }
}
```

- Arrows or WASD move; SPACE/ENTER acts; P pauses; M mutes; G toggles the debug
  graph. Every branch just delegates to the model — the controller holds no game
  rules itself.

## 3.17 `build.sh` and `run.sh`

### `build.sh`

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

# Copy FXML (and any other) resources so they are found on the classpath.
find src -name '*.fxml' -print0 | while IFS= read -r -d '' f; do
    rel="${f#src/}"
    mkdir -p "$OUT/$(dirname "$rel")"
    cp "$f" "$OUT/$rel"
done

echo "Build OK -> $OUT"
```

- `#!/usr/bin/env bash` — the shebang (same as any script).
- `set -euo pipefail` — `-e` exit on error, `-u` error on undefined variable,
  `-o pipefail` make a failing pipe stage fail the whole pipe. Good hygiene.
- `cd "$(dirname "$0")"` — run from the script's own directory regardless of where
  it was invoked.
- `FX=...` — path to the JavaFX jars; `--module-path`/`--add-modules` make the
  JavaFX modules available (JavaFX is **not** bundled with the JDK).
- `javac -d out $(find src -name '*.java')` — compile **every** `.java` under
  `src` into `out`. Python equivalent: compile all modules to a bytecode cache,
  but here it's an explicit two-step.
- The `find ... while read` loop copies each `.fxml` from `src/...` to the matching
  `out/...` path. This is essential: `getResource("/com/pacman/pacman.fxml")` looks
  on the classpath (`out`), and resources are **not** compiled, so they must be
  copied. This is the one-line addition the FXML rework required.

### `run.sh`

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

- Auto-builds if `out/` is missing.
- `java -cp out com.pacman.PacmanApp` runs the class whose `main` starts the app
  (the classpath tells the JVM where compiled classes live).
- `--add-modules javafx.controls,javafx.fxml` — both modules are needed now:
  `controls` for the buttons/radios/labels and `fxml` for `FXMLLoader`.
- `--enable-native-access=javafx.graphics` and `-Djava.library.path="$FX"` — allow
  the JavaFX native libraries to load on modern JDKs.
- `exec` replaces the shell with the JVM. `"$@"` forwards any extra CLI args.

---

# Part 4 — The AI, algorithm by algorithm

This is the part you asked for: how the ghosts actually think. We go from the
simplest (a state machine) to the most complex (minimax with alpha-beta).

## 4.1 The ghost finite-state machine

Each ghost is always in exactly one `State`:

```
        reset
          |
     +----v-----+   timer<=0    +---------+  reach exit  +--------+
     |  HOUSE   |-------------->| LEAVING |------------->| NORMAL |
     +----------+               +---------+              +---+----+
                                                             |
                            power pellet                     | head home (13,11)
        NORMAL -----------------------------> FRIGHTENED     v
          ^                                        |    +----------+
          |            fright timer ends           |    | ENTERING |
          +----------------------------------------+    +----+-----+
                     eaten by Pac-Man                     |  reach house
          +------+  collision (frightened)                |
          | EATEN|<----------------------------------------+  (HOUSE again)
          +---+--+
              |  eyes reach door (13,11)
              v
         ENTERING
```

The transitions in code:

- `HOUSE → LEAVING`: `houseTimer` hits 0 (`updateInHouse`).
- `LEAVING → NORMAL`: the ghost finishes the scripted exit (`updateLeaving`).
- `NORMAL → FRIGHTENED`: a power pellet is eaten (`Game.startFright`, only for
  **active** NORMAL ghosts).
- `FRIGHTENED → NORMAL`: `frightTimer` expires (`Game.updatePlaying`).
- `FRIGHTENED → EATEN`: Pac-Man collides with it (`Game.checkCollisions`).
- `EATEN → ENTERING`: the eyes reach tile `(13, 11)` (`Ghost.gridMove`).
- `ENTERING → HOUSE`: the eyes descend to `(13, 14)` (`updateEntering`).
- `NORMAL → (dying)` is implicit: Pac-Man colliding with a NORMAL ghost calls
  `Game.die()`.

The "AI brain" only runs in `NORMAL`. Frightened is a random walk; eaten is classic
targeting toward the door; house/leaving/entering are scripted. That is why the
intelligent pathfinding can be expensive without affecting most frames.

## 4.2 Classic arcade targeting (Blinky / Pinky / Inky / Clyde)

`Game.targetFor` reproduces the original target-tile formulas. Let Pac-Man be at
`(px, py)` moving `(pdx, pdy)`:

| Ghost | Target tile | Meaning |
|-------|-------------|---------|
| Blinky (BOSS) | `(px, py)` | direct chase |
| Pinky (A*) | `(px + 4·pdx, py + 4·pdy)` | ambush 4 tiles ahead |
| Inky (TRACKER) | `((px+2·pdx)·2 − bx, (py+2·pdy)·2 − by)` | mirror Blinky through a point 2 ahead |
| Clyde (DIJKSTRA) | if far (>8 tiles): `(px,py)`, else `(sx, sy)` | chase until close, then flee to corner |

Why these work:

- **Blinky** is the relentless one — the classic "shadow".
- **Pinky** cuts you off by aiming where you *will* be.
- **Inky** is the trickiest: take the tile 2 ahead of Pac-Man, draw a vector from
  Blinky to it, and double it. The result swings wildly depending on where Blinky
  is, producing the original's erratic feel.
- **Clyde** is timid: he closes in but loses his nerve inside 8 tiles and retreats
  to his corner (the "pokey" behaviour).

In the classic arcade these targets are then followed by greedy tile choice; here
Blinky/Pinky/Inky/Clyde instead feed their target into the graph pathfinders
(BOSS→minimax, A_STAR→A*, TRACKER→prediction, DIJKSTRA→Dijkstra), which is the
"agentic" twist.

## 4.3 Pac-Man prediction via the ring buffer

Inky's `TRACKER` brain predicts where Pac-Man is going using his **short-term
memory** (`Pacman.history`, a `RingBuffer<Direction>` of the last 10 tile moves).

Algorithm (`Ghost.trackerIntercept`):

1. If fewer than 3 moves are remembered, fall back to `nodeAhead` (the intersection
   in his current facing direction) — not enough signal yet.
2. `trend = history.dominant()` — the most frequent recent direction, ties going to
   the most *recent*. (See `RingBuffer.dominant`.)
3. If the trend is `NONE`/null, also fall back to `nodeAhead`.
4. March up to `TRACK_LOOKAHEAD = 10` tiles from Pac-Man in `trend`, stopping at a
   wall, and return the first graph node reached. If none, return the nearest node.

Why a ring buffer and not a list? Pac-Man pushes one entry per tile, forever. A
plain list would grow without bound; a ring buffer keeps exactly the last 10 in
O(1) time and O(1) memory, discarding the oldest automatically. The "dominant"
query then answers "which way is he leaning lately?". This is a genuine piece of
"reading the opponent" — a mini opponent model.

Worked example: Pac-Man's last 10 moves are `→ → → ↑ → → ↓ → → →`. Counts: RIGHT
appears 8 times → `dominant = RIGHT`. Inky then projects 10 tiles right and paths
to the first intersection there, i.e. he **flanks** rather than tailgates.

## 4.4 The maze graph — turning a grid into a graph

Formally: the maze is an undirected graph `G = (V, E)`.

- `V` = decision points (`isDecisionPoint`): dead ends, corners, intersections.
- `E` = corridors between adjacent decision points, weighted by length in tiles.

Steps (constructor of `MazeGraph`, detailed in §3.6):

1. **Discover vertices** by scanning all tiles; a tile is a vertex if walkable and
   `isDecisionPoint`.
2. **Walk corridors**: from each vertex, march each of the 4 directions tile by
   tile until hitting another vertex; that's an edge of length = number of steps.
3. **Adjacency list** for cheap neighbour iteration.
4. **Floyd–Warshall** for O(1) all-pairs distances.

Why bother? The raw maze has ~700 walkable tiles. Pathfinding tile-by-tile would
be ~700 nodes per search, repeatedly, for every intersection decision of every
ghost. Contraction yields ~100 nodes, shrinking every search dramatically and,
more importantly, making the minimax evaluation's "distance A to B" an O(1) table
lookup instead of a fresh Dijkstra.

The excluded tiles (walls, door, ghost-house interior) keep the graph clean: the
house has its own scripted movement and would otherwise poison the topology.

## 4.5 Dijkstra's algorithm

Used by Clyde (`DIJKSTRA`). Purpose: single-source shortest paths on a graph with
**non-negative** edge weights (corridor lengths ≥ 1).

Idea:

```
gScore[start] = 0, everything else = INF
open = min-heap ordered by gScore
while open not empty:
    u = pop smallest
    if u == goal: done
    for each neighbour v of u:
        if gScore[u] + weight(u,v) < gScore[v]:
            gScore[v] = that
            prev[v] = u
            push v
```

By always expanding the closest unvisited node, when the goal is popped its
distance is provably minimal. Complexity: O((V + E) log V) with a binary heap.

In this code Dijkstra and A* share `MazeGraph.search`; the only difference is
whether the priority is `g` (Dijkstra) or `g + h` (A*).

## 4.6 A* search

Used by Pinky (`A_STAR`) and to build the boss's committed route. A* is Dijkstra
plus a **heuristic**: priority = `f = g + h`, where `g` is the cost so far and `h`
is an estimate of the remaining cost.

The heuristic (`MazeGraph.heuristic`):

```
h(u, v) = wrapAwareColumnDistance(u, v) + |row(u) − row(v)|
```

This is the Manhattan distance adapted for the wraparound tunnel. It is
**admissible** (never overestimates) because a single tile step can change this
value by at most 1 — so A* returns a genuinely optimal path, tunnel included.

A* vs Dijkstra here:

- Dijkstra explores nodes in every direction equally.
- A* is pulled toward the goal by `h`, exploring fewer nodes for the same optimal
  path. With a good `h`, A* is strictly faster.

The search uses **lazy deletion**: when a better `g` is found for a node already in
the heap, we push a second entry rather than decrease its key; the stale entry is
skipped with `if (closed[u]) continue;`. This avoids implementing a decrease-key
operation.

## 4.7 Floyd–Warshall all-pairs shortest paths

Computed once at startup. It answers "shortest distance between *any* two nodes"
in O(1) afterward.

```
dist[i][j] = weight(i,j) if edge, INF otherwise; dist[i][i] = 0
for k in all nodes:
    for i in all nodes:
        for j in all nodes:
            dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
```

The meaning of the outer loop: "using nodes `0..k` as allowed intermediate stops,
is there a shorter `i→j`?". After all `k`, every intermediate is allowed, so
`dist[i][j]` is the true shortest path. Complexity O(V³) ≈ 10⁶ for V≈100 — trivial
once, at load time.

Why precompute? The minimax **evaluation function** calls `distance(g, p)` for the
real ghost position, the simulated positions, *and* every exit around Pac-Man, at
every leaf of every search, at every intersection. Doing a fresh Dijkstra each time
would be far too slow; a table lookup is free. This is the key performance enabler
for the search.

Trade-off: O(V²) memory (100² ints ≈ 40 KB) and O(V³) build time, in exchange for
O(1) queries forever.

## 4.8 Minimax with alpha–beta pruning

Used by Blinky when chasing (`BOSS`) and always by the solo `HUNTER`. This is where
the ghost reasons about the *future*.

Model: a two-player, turn-based, zero-sum game.

- **Maximiser** = the ghost (wants a high score).
- **Minimiser** = Pac-Man (wants a low score).
- A **ply** is one move by one player. `DEPTH = 4` plies: ghost, Pac-Man, ghost,
  Pac-Man.

The tree:

```
                ghost root
        /          |          \
    move A      move B      move C        <- maximiser (ghost) picks here
     |            |            |
   PacMan       PacMan       PacMan       <- minimiser (Pac-Man) picks worst-for-ghost
   / | \        / | \        / | \
 ... (depth 4 leaves)
```

`minimax` (a recursive function):

- Base case: `depth == 0` → return `evaluate(...)`.
- Maximising node: return the **max** over child scores.
- Minimising node: return the **min** over child scores.

Without pruning the leaf count is `b^depth` where `b` is the branching factor
(~2–3 on the maze graph), so ~3⁴ = 81 leaves — tiny, re-computable every
intersection.

**Alpha–beta pruning** cuts branches that cannot affect the result:

- `alpha` = best score the maximiser can already guarantee.
- `beta` = best score the minimiser can already guarantee.
- While maximising, if `best >= beta`, the minimiser above would never let us reach
  this branch → `break`.
- While minimising, if `best <= alpha`, the maximiser above would avoid it → `break`.

With good move ordering alpha–beta explores roughly `b^(depth/2)` leaves instead of
`b^depth` — the square root — i.e. it can search **twice as deep** for the same
cost. Here it mostly saves the small constant factor, but the mechanism is the
classic one.

`chooseMove` is the root: it tries each legal first step (never reversing), runs
the minimax subtree for each, keeps the best, and returns the chosen neighbour node.

## 4.9 The evaluation function (where "intelligence" lives)

At the leaves, `evaluate(gNode, pNode, realPacman)` scores the position from the
ghost's point of view. This single function is what makes the hunter feel smart.

```
dReal  = distance(ghost, realPacman)          // distance to the actual Pac-Man
dModel = distance(ghost, simulatedPacman)     // distance to the tree's Pac-Man
if dReal == INF: return NO_MOVE               // unreachable → terrible for ghost

score  = −W_REAL · dReal                      // reward closing on the real Pac-Man
       − W_MODEL · dModel                     // reward closing on the simulated one
       + W_CORNER · (4 − degree(pNode))       // reward Pac-Man having few exits
       + (dReal ≤ 1 ? BONUS_CLOSE : 0)        // capture is one step away
       + (all exits lead to the ghost ? BONUS_TRAPPED : 0)
```

Weights (`SearchTree`):

- `W_REAL = 3.0` — the real Pac-Man matters most; the ghost must not lose the plot.
- `W_MODEL = 1.5` — the simulated line matters too, so the ghost respects what the
  tree says Pac-Man will do.
- `W_CORNER = 1.5` — cornering is valuable (few exits = easy catch).
- `BONUS_CLOSE = 10.0` — heavily rewards being adjacent.
- `BONUS_TRAPPED = 12.0` — heavily rewards a fully cornered Pac-Man.

The `cutOff` test is the cleverest part: it counts as "trapped" only when **every**
exit from Pac-Man's node leads somewhere that is *not farther* from the ghost than
the current distance. In other words, whichever way he runs, the ghost is already
closer — a genuine pincer.

Why two distances (`dReal` and `dModel`)? The simulated Pac-Man is a *hypothesis*
that could be wrong; the real one is ground truth. Blending them means the ghost
exploits the predicted line **while still respecting reality** — the standard way
to make a game-theoretic search robust in a real-time game.

## 4.10 The HUNTER brain (the solo opponent)

`HUNTER` is a thin variant of `BOSS`, tuned to be a genuinely harder, single
opponent:

- **It never scatters.** In `chooseTarget`, `BOSS` returns a corner when
  `globalMode == SCATTER`, but the guard `brain != Brain.HUNTER` excludes the
  hunter, so it always targets Pac-Man's predicted node.
- **It always searches.** In `chooseDirection`, the `bossSearch` flag is true for
  `HUNTER` unconditionally (whereas `BOSS` only searches during CHASE, not SCATTER).
- **It commits to a plan.** After the search picks the next node, the hunter runs
  A* from that node to the target and stores the whole route in `plannedPath`, so
  its motion reads as deliberate rather than twitchy.
- **It is faster.** In `Game.resetActors`, `baseSpeed` for a `HUNTER` is
  `ghostSpeed * 1.12` — a 12% edge so it can actually close a gap.

Combined effect: one ghost that always hunts, always plans, and moves slightly
faster — a focused, predictable-but-dangerous solo challenge, versus the chaos of
four differently-behaving ghosts.

## 4.11 Scatter / chase scheduling

`Game` alternates global `Mode` on a timer (`MODE_KIND` / `MODE_DURATION`):

```
SCATTER 7s → CHASE 20s → SCATTER 7s → CHASE 20s → SCATTER 5s → CHASE 20s → SCATTER 5s → CHASE forever
```

- On each flip, `advanceMode` reverses all **active** NORMAL ghosts — the classic
  "they turn around" tell.
- While frightened, the mode clock is **paused** (arcade-accurate): the fright
  timer branch runs *instead of* the mode timer.
- `BOSS` uses scatter to go to its corner; `HUNTER` ignores it entirely.

## 4.12 Difficulty: SOLO vs ALL

The two rosters are balanced by three knobs:

| | ALL (classic) | SOLO (hunter) |
|---|---|---|
| Active ghosts | 4 | 1 (Blinky) |
| Brain | Blinky=BOSS, Pinky=A*, Inky=TRACKER, Clyde=DIJKSTRA | Blinky=HUNTER |
| Scatter | yes | never |
| Speed | vanilla | ×1.12 |
| Search | minimax only in CHASE | minimax always |

Four ghosts give *quantity* and varied, semi-predictable behaviour you can learn.
One hunter gives *quality*: fewer, but relentless and genuinely calculated
opponents. The `GhostMode` enum plus `setGhostMode` toggle everything, and the
parked ghosts are excluded everywhere (`active` checks in `Game.updatePlaying`,
`Game.checkCollisions`, `Game.startFright`, `Game.advanceMode`, and
`Renderer.drawGhosts`/`drawGraph`).

---

# Part 5 — The data structures, in depth

Each structure is introduced by *why it exists* and *what it costs*, with the
Python equivalent and the exact Java mechanics.

## 5.1 Dense grid (`char[][]`)

`Maze.tiles` is `new char[Maze.ROWS][Maze.COLS]` = `char[31][28]`. In Java a 2-D
array is an **array of arrays** (jagged-capable), not a rectangular block:

```
tiles ──► [ ref0, ref1, ..., ref30 ]      (an array of 31 row references)
              │
              v
           [ '#', '.', ... , '#' ]        (a char[28])
```

Access `tiles[r][c]` does two dereferences: `(tiles[r])[c]`.

Costs (64-bit HotSpot):

- outer array header 16 B + 31 refs × 8 B = 264 B;
- each row header 16 B + 28 chars × 2 B = 72 B, padded to 72 B;
- total ≈ 264 + 31 × 72 ≈ **2.5 KB**.

Plus the walls and pellets. Fast O(1) random access, but it stores *every* tile
even after most are empty. That waste is exactly what Phase 6 removes.

Python equivalent: a nested list `[[...], ...]`, but Java's `char` is a true 16-bit
value type (no per-char object overhead) — so this is compact compared to a Python
list of one-char strings.

## 5.2 Sparse matrix (COO) + bit packing

Two ideas fight the wasted space once ≥80% of pellets are gone.

**(a) COO (Coordinate List) `SparseMatrix`** keeps only the survivors as three
parallel arrays:

```
rows[] = [1, 1, 1, 2, ...]     // which row
cols[] = [1, 2, 3, 1, ...]     // which column
vals[] = [1, 1, 1, 2, ...]     // 1=pellet, 2=power
```

with `rows[i]*28 + cols[i]` as a sort key. Filled in **row-major scan order**, the
keys are automatically ascending, so lookup is a **binary search** (O(log n)):

```
indexOf(r, c):
    key = r*28 + c
    lo=0, hi=n-1
    while lo<=hi:
        mid = (lo+hi)/2
        if keyAt(mid) == key: return mid
        if keyAt(mid) <  key: lo = mid+1
        else:                 hi = mid-1
    return -1
```

Deletion (`eat`) shifts the three arrays left to close the hole
(`System.arraycopy`), which is O(n) but only happens on eating, with n shrinking
over time.

**(b) Bit packing `wallBits`** stores one bit per tile in `long` words:

```
WALL_WORDS = (ROWS*COLS + 63) / 64 = (868+63)/64 = 14 words
k = r*28 + c
set bit:  wallBits[k >>> 6] |= 1L << (k & 63)
read  bit: (wallBits[k >>> 6] >>> (k & 63)) & 1L
```

- `k >>> 6` = `k / 64` (unsigned shift) → which word.
- `k & 63` = `k % 64` → which bit in the word.
- `1L << b` builds a mask with bit `b` set; `|=` sets it; `& 1` tests it.

14 longs = 14 × 8 = **112 B** for the entire wall layout, versus ~2.5 KB for the
dense grid. 868 booleans in 112 bytes — about 1 bit each (§1.22).

**Memory math for the whole Phase 6 conversion:**

| Representation | Bytes (approx) |
|---|---|
| dense `char[][]` | ~2.5 KB |
| walls as bits | 112 B |
| sparse (only remaining pellets) | a few hundred B |

`Maze.compress()` builds the sparse matrix, builds `wallBits`, then sets
`tiles = null` — releasing the big array to the **garbage collector**. From then
on, `tileAt` reconstructs answers from sparse + bits, and the renderer iterates
only the survivors. The `[MEMORY]` lines the game prints come from
`Maze.denseBytes()`/`wallBytes()`/`SparseMatrix.estimatedBytes()`.

Important subtlety: the resident set can only **shrink** while eating, because the
allocated arrays keep their capacity (`n` shrinks but `rows.length` doesn't). So
the saving is the removal of the *dense* grid, which is the dominant term.

## 5.3 Ring buffer (circular queue)

Fixed capacity `C`; O(1) push that overwrites the oldest:

```
capacity=5
data:   [ _ _ _ _ _ ]
head ──┘              head = where the NEXT push writes
push A: [A _ _ _ _]  head=1 size=1
push B: [A B _ _ _]  head=2 size=2
...
push F: [F B C D E]  head=2 size=5   (A overwritten; index 0 now = B)
```

- `head = (head + 1) % capacity` wraps around — that's the "ring".
- Physical slot of the logical `i` (0=oldest): `start = (head - size + capacity) % capacity; slot = (start + i) % capacity`.

Costs: O(1) push, O(1) random access, O(size) for `count`/`dominant` (fine, size
≤ 10). Memory is fixed at `C` references — never grows.

Used by `Pacman.history` (last 10 moves) to feed the TRACKER brain.

`dominant()` scans **newest → oldest** and only replaces the champion on a strictly
larger count, so ties favour the **more recent** direction — the trend adapts
quickly. `same(a,b)` uses `a.equals(b)` (null-safe), which is Python `==` for
objects.

Python equivalent: `collections.deque(maxlen=C)` — but note `deque` discards on the
*far* end and `popleft` is O(1); this ring buffer is the manual O(1) version.

## 5.4 Adjacency matrix vs adjacency list

`MazeGraph` keeps **both**, because they answer different questions:

- **Adjacency matrix** `edgeW[u][v]` (int[nodeCount][nodeCount]): O(1) "is there a
  direct corridor u→v, and how long?". Ideal for weights and Floyd–Warshall.
  Memory O(V²) ≈ 100² ints ≈ 40 KB.
- **Adjacency list** `adj[u]` = array of neighbours: iterate a node's neighbours in
  O(degree) instead of O(V). Ideal for A*/Dijkstra/minimax expansion.

Rule of thumb: **dense graphs** favour the matrix; **sparse graphs** favour the
list. The maze graph is sparse (each node has ~2–4 neighbours), so the list is what
search uses, while the matrix is kept for weights and O(1) distance checks.

Python analogy: a matrix is a `list[list[int]]`; a list is a
`dict[int, list[int]]` (or `list[list[int]]`).

## 5.5 Binary heap / priority queue

`java.util.PriorityQueue` is a **binary min-heap**: a complete binary tree stored
in an array where every parent is ≤ its children. Push/pop are O(log n); peek is
O(1).

```
heap array: [1, 3, 2, 7, 4, 5]
tree:          1
              / \
             3   2
            / \ /
           7  4 5
```

Used in `MazeGraph.search` for the A*/Dijkstra open set. Ordering is provided by a
`Comparator`, here the lambda `Comparator.comparingDouble(e -> e.f)` — i.e. compare
entries by their `f` field.

Why not sort a list each time? That would be O(n log n) per step. The heap inserts
in O(log n) and always yields the current minimum — the natural fit for "expand
the lowest-f node".

Java doesn't have a built-in decrease-key, hence the lazy-deletion trick: push a
new entry instead, and skip stale popped entries with `if (closed[u]) continue;`.

Python equivalent: `heapq` (a min-heap over tuples) — same idea, same O(log n).

## 5.6 Trie (prefix tree)

Stores a set of words so that matching is O(length), independent of how many words
exist. Each node has 26 child slots (one per letter). Inserting `godmode`:

```
root
 └─ g ─ o ─ d ─ m ─ o ─ d ─ e[word="godmode"]
```

Matching a keystroke stream walks one edge per character. The **cursor** node *is*
the memory of what has been typed — no buffer String to re-scan, no re-tokenisation.

Used by `CommandEngine` for the cheat vocabulary (`speedup`, `freeze`, `godmode`).
The recovery trick (`reset()` then re-`step` on a miss) means `xxgodmode` still
triggers: only the last bad character is dropped, and it may start a fresh match.

Costs: insert O(L); match O(1) amortised per keystroke; memory O(total characters ×
branching) but shared prefixes save space.

Python analogy: a nested dict `{'g': {'o': {'d': ...}}}` with a terminal marker; or
`str.startswith` against a set, but the trie does it in one pass and streams
naturally.

## 5.7 The search tree and the call stack

There is **no explicit tree object**. `SearchTree.minimax` recurses; the recursion
**is** the tree, and the JVM's **call stack** holds the path from root to current
node. Each frame stores the arguments (`gNode`, `pNode`, `depth`, `maxing`,
`alpha`, `beta`, `realPacman`) and the loop state.

```
minimax(...)              <- frame 0 (depth 4)
  minimax(...)            <- frame 1 (depth 3)
    minimax(...)          <- frame 2 (depth 2)
      minimax(...)        <- frame 3 (depth 1)
        evaluate(...)     <- frame 4 (depth 0) returns a number
      ← returns max/min
    ← returns max/min
  ← returns max/min
← returns max/min (root picks)
```

- Depth is bounded by `DEPTH = 4`, so the stack is shallow (a handful of frames) —
  no risk of stack overflow.
- `lastNodesVisited` is incremented at each expansion purely for the debug overlay.

Python equivalent: plain recursive functions; Python's recursion limit
(`sys.setrecursionlimit`) has no analogue here because the depth is tiny.

---

# Part 6 — The math toolkit

Every number trick used in the game, in one place.

## 6.1 Pixels ↔ tiles

```
tileX(x)      = floor(x / TILE)          // pixel → column
centerX(c)    = c * TILE + TILE/2.0      // column → centre pixel
```

`Math.floor` rounds toward −∞. Because positions are `double` and the board is a
grid, "which tile am I on" is always a floor. `TILE = 20`.

## 6.2 Detecting a tile centre within one frame

```
centered = |x − centerX | <= step + 0.01
```

We are at a centre if the remaining distance is within this frame's travel
(`step = speed·dt`) plus a small epsilon. The `+0.01` guards floating-point drift
so a ghost never *just misses* a centre and overshoots forever.

## 6.3 The wraparound tunnel (modular arithmetic)

```
wrapX(): if x < 0: x += W; else if x >= W: x -= W
wrapCol(c): m = c % COLS; return m < 0 ? m + COLS : m
```

Java's `%` can be negative (`-1 % 28 == -1`), so `wrapCol` adds `COLS` when
negative to force 0..27. Python's `%` is already non-negative for positive
modulus, but Java is not — this is a common Java gotcha.

## 6.4 Wrap-aware distances

```
colDelta(a, b) = min(|a−b|, COLS − |a−b|)      // cylinder metric
heuristic(u,v) = colDelta + |row(u) − row(v)|  // admissible A* estimate
```

`colDelta` is "as the crow flies *through the tunnel*". The heuristic must never
overestimate; this one can't, because one tile step changes it by ≤ 1.

## 6.5 Squared distance (avoid the square root)

```
dist² = dx*dx + dy*dy
```

Collisions and "closest target" only need to *compare* distances, and comparing
squares gives the same order. So we skip `sqrt` — cheaper and exact.

```
collision:     dx² + dy² < (TILE·0.65)²
nearest tile:  pick direction minimising (nc−tx)² + (nr−ty)²
```

## 6.6 Euclidean distance via `Math.hypot`

Clyde uses `Math.hypot(px−col, py−row)` when he genuinely needs the real distance
(>8 tiles test). `hypot` is `sqrt(a²+b²)` but overflow-safe.

## 6.7 Trigonometry for drawing

```
angleRadians = Math.toRadians(degrees)
point = (cx + r·cos(a), cy + r·sin(a))
```

Java's `Math.sin`/`Math.cos` take **radians**. Angles are measured with 0° to the
right, growing clockwise on screen (because y grows down). Pac-Man's mouth is an
arc swept from `base + mouth/2` to `base + 360 − mouth/2`; the `base` angle orients
it: right 0°, down 90°, left 180°, up 270°.

The moving mouth uses `Math.abs(Math.sin(animTime * 9))` — a smooth open/close.
The power-pellet pulse uses `((int)(time * 4)) % 2 == 0` — a 4 Hz blink.

## 6.8 Sign and magnitude

```
Math.signum(v) → −1, 0, or +1
```

Used when a ghost slides horizontally toward the door column: move by
`signum(target − x) · step`, and clamp so it never overshoots.

## 6.9 Integration with `dt`

```
x += dir.dx * speed * dt
```

Velocity in px/s times seconds = pixels — correct at any frame rate. Timers count
down the same way (`t -= dt`). This is why the game speed is independent of FPS.

## 6.10 Clamping and epsilon comparisons

```
if (dt > 0.05) dt = 0.05;                      // clamp a frame spike
if (dist < bestDist − 1e-9) { ... }            // strict improvement wobble
if (Math.abs(dist − bestDist) < 1e-9) { ... }  // "equal" within 1e-9
```

`Math.min`/`Math.max` are the clamp primitives. `1e-9` tolerances absorb the tiny
errors of `double` arithmetic so ties behave deterministically.

## 6.11 Bit arithmetic (power-of-two scoring)

```
pts = 200 * (1 << (ghostCombo − 1))     // 200, 400, 800, 1600
```

`1 << k` = `2^k`. The chain of eaten frightened ghosts doubles each time — the
classic arcade rack-up.

---

# Part 7 — Rendering deep dive

## 7.1 The canvas model

JavaFX draws onto a `Canvas` through a `GraphicsContext` (`gc`) — an immediate-mode
2-D painter:

```
gc = canvas.getGraphicsContext2D();
```

State lives in the context between calls (`setFill`, `setStroke`, `setLineWidth`,
`setFont`, `setTextAlign`). Each frame the `Renderer` sets state then draws. There
is no retained scene graph for the game board — it is redrawn from scratch every
frame, like most games and unlike most UI toolkits.

## 7.2 The painter's algorithm

Order matters: later draws cover earlier ones. `render()` clears to black, then:
maze walls → door → pellets → optional debug graph → Pac-Man → ghosts → HUD →
cheat tags → overlay. So the HUD sits over everything and the overlay text sits on
top of the HUD.

## 7.3 Fills, strokes, paths

- `fillRect`/`fillOval`/`fillText` — filled primitives.
- `strokeLine` — a line; `setLineWidth`, `setLineCap(ROUND)` control look.
- A **path** is `beginPath() → moveTo/lineTo/quadraticCurveTo → closePath() → fill()`.
  Used for Pac-Man (polygon) and the ghost body (dome + Bézier skirt).

Watch the off-by-a-half: `fillOval(x, y, w, h)` takes the **top-left** corner, so
to centre a circle of radius `r` at `(cx,cy)` you pass `(cx−r, cy−r, 2r, 2r)`.

## 7.4 Wall outlines

Rather than filling wall tiles, the renderer draws a 2.4 px blue line along every
side of a wall tile that borders open space. This produces the thin arcade outline
without a texture. The `+1`/`−1` inset keeps lines within their tiles so adjacent
walls join cleanly.

## 7.5 Actors

- **Pac-Man**: a circle minus a wedge. Vertices are generated by stepping an angle
  from `start` to `end` and connecting `lineTo`. The wedge makes the mouth; when
  dying the mouth sweeps to nearly 360°.
- **Ghosts**: a top half-circle plus three quadratic-Bézier scallops. Frightened =
  blue (blinking white in the last 2 s), eaten = eyes only, else identity colour.
- **Eyes**: two white circles with dark pupils offset by `dir`, so the ghost looks
  where it is going.

## 7.6 Text and the HUD

`Font.font("Monospaced", FontWeight.BOLD, size)` and `setTextAlign(LEFT/CENTER/
RIGHT)` position HUD text; the code sets alignment before each `fillText`. The
overlay panels use 8-digit hex colours (`#RRGGBBAA`) for translucency. `title()`
draws a drop shadow by painting the text twice (black at +3, then the colour).

## 7.7 The debug overlay

With G, `drawGraph` sketches the graph (faint cyan edges, white node dots), each
active ghost's `plannedPath` in its colour, the minimax `bossPick` as a white ring,
and the tracker `interceptNode` as a pink X — plus a live dump of Pac-Man's ring
buffer rendered as arrow glyphs (`\u2190` etc.). This makes the AI *visible*,
which is invaluable when learning how the algorithms behave.

---

# Part 8 — FXML & JavaFX deep dive

## 8.1 What JavaFX is

JavaFX is Java's modern UI toolkit (the successor to Swing). Key concepts:

- **Stage** — the OS window (`stage.setTitle`, `stage.show`).
- **Scene** — the content inside a window; holds a **scene graph**.
- **Parent / Node** — the scene graph is a tree of nodes (layouts, controls,
  shapes, canvases).
- **Layouts** — containers that position children: `AnchorPane`, `VBox`, `HBox`,
  `StackPane`, `BorderPane`, `GridPane`.
- **Controls** — `Button`, `Label`, `RadioButton`, `ToggleGroup`, `TextField`, etc.
- **Canvas** — a low-level drawing surface (used for the game field).

## 8.2 Declarative UI: FXML

FXML is an XML file that describes the scene graph instead of building it in Java.
The old version of this game created the `Canvas` and wired keys in code; now the
layout lives in `pacman.fxml` and code only reacts.

Mapping between FXML and Java:

| FXML | Meaning |
|---|---|
| `<AnchorPane>` | instantiate `javafx.scene.layout.AnchorPane` |
| `fx:controller="com.pacman.GameController"` | controller class for this view |
| `fx:id="canvas"` | name injected into a controller field `@FXML Canvas canvas` |
| `onAction="#onStart"` | event handler method in the controller |
| `toggleGroup="$ghostGroup"` | reference to another element by id |
| `style="-fx-..."` | inline CSS |
| `<?import ...?>` | import a class so its short tag name resolves |

`FXMLLoader.load()` parses the XML, creates the nodes, sets the controller, injects
`@FXML` fields, and calls `initialize()` **after** injection. Then
`getController()` hands you the instance.

## 8.3 Injection and lifecycle

```
loader.load()
  → build nodes
  → new GameController()
  → inject @FXML fields by fx:id  (reflection)
  → controller.initialize()       (our setup)
  → return root
loader.getController()            (same instance)
```

Order matters: in `initialize`, `canvas` is already non-null, so we can call
`canvas.getGraphicsContext2D()`. That is why setup lives there, not in the
constructor.

## 8.4 The scene-graph layout here

```
AnchorPane (root)
├── Canvas (the game board, drawn by Renderer)
└── VBox  (fx:id="menu", fills the window, translucent black)
    ├── Label "PAC-MAN"
    ├── Label statusLabel
    ├── Label "CHOOSE YOUR OPPONENT"
    ├── RadioButton soloGhost  ┐ same
    ├── RadioButton allGhosts  ┘ ToggleGroup → mutually exclusive
    ├── Button startButton (onAction=#onStart)
    └── Labels (help + secret codes)
```

The `Canvas` is beneath the `VBox`; the `VBox` is the start-menu overlay. Hiding it
(`setVisible(false)` + `setManaged(false)`) reveals the live board. `setManaged` is
important: an invisible-but-managed node still occupies layout space and can grab
input.

## 8.5 Events and key handling

`scene.setOnKeyPressed(handler)` installs one handler for the whole scene. Events
**propagate** from the focused node up the graph, so a single scene-level handler
sees all keys. `e.getCode()` is a `KeyCode` enum (UP, W, SPACE, …); `e.getText()`
is the typed character (used for cheats — note arrow keys have empty text).

## 8.6 The animation loop

`AnimationTimer.handle(long now)` is called once per pulse (~60 Hz), `now` in
nanoseconds. It is the game's heartbeat; the controller computes `dt`, ticks the
model, and redraws. It needs no explicit render call because JavaFX repaints after
`handle` returns.

## 8.7 CSS styling

`style="-fx-font-family: Monospaced; -fx-font-size: 14px;"` is inline CSS. JavaFX
CSS supports a subset of web CSS with `-fx-` prefixed properties. Used here to keep
the retro monospace look without image assets.

## 8.8 Modules and the classpath

JavaFX ships as **modules**, separate from the JDK. `--module-path` points at them
and `--add-modules` enables them (`javafx.controls`, `javafx.fxml`). The game's own
classes are on the plain `-cp` classpath. Resources like the FXML are found via
`getResource` on the classpath, which is why `build.sh` copies `.fxml` into `out/`.

---

# Part 9 — Cheats, debug keys, and the tests

## 9.1 Cheat codes (Phase 5)

Three secret codes are typed as letters during play. Every letter — movement keys
included — is also fed to the trie engine, so the codes are just letter sequences:

| Code | Effect | Duration |
|---|---|---|
| `speedup` | Pac-Man moves at ×1.75 | 8 s |
| `freeze` | all ghosts stop moving | 6 s |
| `godmode` | ghosts can't kill you | 10 s |

Flow:

```
keypress → GameController.onKeyPressed (movement) + Game.feedCheat(char)
        → CommandEngine.feed(char)             (trie walk, O(1) per char)
        → returns e.g. "godmode"
        → Game.activateCheat sets a timer + plays sound
        → each frame tickCheats() counts it down and applies the effect
```

Because the engine restarts from the trie root on a mismatch, `xxgodmode` works:
only the trailing `x` is discarded, and it can begin a fresh match.

Effects are applied through `ghostsFrozen()` (skip ghost updates),
`godMode()` (skip death in collisions) and `speedBoost()` (multiply Pac-Man's
speed). The HUD shows active cheats in green (`Renderer.drawCheatTags`).

## 9.2 Debug keys

| Key | Action |
|---|---|
| Arrows / WASD | move |
| Space / Enter | start, or pause/resume |
| P | pause |
| M | mute/unmute |
| G | toggle the graph/AI debug overlay |

The G overlay draws the maze graph, every active ghost's planned path (in its
colour), the minimax choice (white ring), the tracker's intercept (pink X), and a
live rendering of Pac-Man's 10-move ring buffer as arrow glyphs.

## 9.3 The tests

The tests live in `test/` and drive the **model** directly — no window, no
JavaFX. They are plain classes with a `main`, run against the compiled `out/`:

```bash
javac -cp out -d test test/*.java
java  -cp out:test TestAgentic
```

(`-cp out:test` puts both the compiled game and the compiled tests on the
classpath; `:` is the Linux separator, `;` on Windows.)

| Test | What it checks |
|---|---|
| `TestAgentic` | the whole six-phase feature set |
| `TestLogic` | death, eating ghosts, power pellets, level complete, tunnel wrap |
| `TestCycle` | eaten ghost leaves→house→returns; game reaches GAME_OVER |
| `TestSim` | a bot plays a full session and no actor enters a wall |
| `TestGhost` | prints a running trace of a ghost's position/mode/state |
| `TestDebug` | prints Pac-Man's per-frame trace with the bot's aim |
| `TraceReturn` | prints a ghost's return-home state transitions |

`TestAgentic` in detail:

- **Graph structure**: >20 nodes, edges > nodes, no self-loops, symmetric matrix,
  adjacency list matches matrix, every edge's first step is walkable, and a BFS
  from node 0 reaches every node (connectivity).
- **Pathfinding**: for many (s,t) pairs, the cost of A*'s path and Dijkstra's path
  equals the Floyd–Warshall distance; endpoints are correct.
- **Ring buffer**: capacity, overwrite-oldest, `newest`, `count`, `dominant`.
- **Tracker**: Inky produces a valid intercept node *ahead* of Pac-Man.
- **Search tree**: minimax returns a legal neighbour and expands nodes.
- **Trie/commands**: `xxgodmode`, `speedup`, `freeze` match; `speedxx` returns null.
- **Cheats through the game**: each code activates; `speedup` raises Pac-Man speed.
- **Sparse compression**: after 80% eaten the maze compresses; every survivor is
  still visible and edible; eating decrements the count; walls remain walls after
  the dense grid is destroyed.
- **No wall violations**: 90 simulated seconds with a bot; no ghost (or Pac-Man)
  ever occupies a wall tile, and every planned path is a chain of legal edges.

`TestSim.aim(g)` is a crude pellet-seeking bot: find the nearest remaining pellet
and pick the legal direction that reduces the squared distance to it. Its shared
`aim` is reused by `TestDebug` and `TestAgentic`.

Why these tests matter for the two features we added:

- `TestAgentic.testAgentsNeverEnterWalls` catches regressions in the new
  `HUNTER`/`BOSS` planning and in the ghost-mode parking (a parked ghost must still
  never corrupt state during a level).
- `TestCycle` verifies the frightened→eaten→house→leaving→normal cycle, which the
  hunter also relies on.

---

# Part 10 — Speed/space cheat sheet and glossary

## 10.1 Complexity cheat sheet

Let `V` = graph nodes (~100), `E` = graph edges, `N` = tiles (868), `d` = degree,
`D` = minimax depth (4), `b` = branching (~2–3).

| Operation | Structure | Time | Space |
|---|---|---|---|
| tile lookup (dense) | `char[][]` | O(1) | O(N) ≈ 2.5 KB |
| tile lookup (sparse) | COO + binary search | O(log n) | O(n survivors) |
| wall test | bit array | O(1) | O(N/64) ≈ 112 B |
| graph build | scan + contract | O(N + E) | O(V²) |
| Floyd–Warshall | matrix | O(V³) once | O(V²) |
| distance(u,v) | table lookup | O(1) | — |
| Dijkstra | binary heap | O((V+E) log V) | O(V) |
| A* | binary heap | ≤ Dijkstra | O(V) |
| minimax (+αβ) | recursion | O(b^D) ~ O(b^(D/2)) | O(D) stack |
| ring push/get | circular queue | O(1) | O(capacity) |
| ring dominant | linear scan | O(capacity²) | O(1) |
| trie match | per character | O(1) amortised | O(total chars) |
| collision test | squared distance | O(ghosts) | O(1) |
| one full frame | — | O(V log V + b^D) at most, in practice tiny | O(1) extra |

## 10.2 Design decisions, in one line each

- **Graph contraction** → few pathfinding nodes, O(1) distances for minimax.
- **Floyd–Warshall at startup** → evaluation becomes array reads.
- **A* over Dijkstra** where a goal exists → fewer expansions, same optimal path.
- **Minimax + alpha–beta** → the boss/hunter plans a few plies ahead cheaply.
- **Ring buffer** → bounded opponent memory in O(1), powers the TRACKER.
- **Trie** → cheat codes in O(1)/keystroke with no buffering.
- **COO + bit packing** → reclaim memory once the level is mostly eaten.
- **Frame-rate-independent `dt`** → same physics at any FPS.
- **`active` flag** → solo mode parks three ghosts with one boolean, respected by
  update, collision, and draw.
- **`HUNTER` brain** → one focused, faster, always-calculating opponent.

## 10.3 Glossary (Python-anchored)

| Java term | Meaning | Python analogue |
|---|---|---|
| `class` | blueprint for objects | `class` |
| `object` / instance | a value of a class | instance |
| `method` | function inside a class | method |
| `static` | belongs to the class, not an instance | `@staticmethod`-ish/module-level |
| `final` | can't be reassigned/overridden | `const`-ish |
| `public`/`private`/`protected` | visibility | `_`/`__` but enforced |
| `abstract` | must be subclassed | `abc.ABC` |
| `interface` | contract of methods | `typing.Protocol`/ABC |
| `extends` | inherit | `class B(A)` |
| `@Override` | assert/allow overriding | `@override` |
| `super` | parent reference | `super()` |
| `enum` | fixed set of constants | `enum.Enum` |
| generics `<T>` | parameterised types | `typing.Generic[T]` |
| `new` | allocate an object | `A()` |
| `null` | "no object" | `None` |
| `package` | namespace/folder | package/module |
| `import` | bring a name in scope | `import` |
| `Exception`/`try`/`catch`/`throw` | errors | `Exception`/`try`/`except`/`raise` |
| `double`/`int`/`char`/`boolean` | primitives | `float`/`int`/`str`/`bool` |
| `List`/`ArrayList` | ordered collection | `list` |
| `Map`/`HashMap` | key-value | `dict` |
| `PriorityQueue` | heap | `heapq` |
| `Deque`/`ArrayDeque` | double-ended queue | `collections.deque` |
| `lambda` | inline function | `lambda` |
| `Comparator` | ordering callback | `key=`/`functools.cmp_to_key` |
| `Runnable` | no-arg task | callable taking no args |
| `Thread`/`ExecutorService` | concurrency | `threading`/`concurrent.futures` |
| `volatile` | cross-thread visibility | needs a lock in Python |
| `GraphicsContext` | 2-D painter | (no direct analogue) |
| `Stage`/`Scene`/`Node` | window/content/widget tree | (no direct analogue) |
| `@FXML` | inject from FXML | (no analogue) |
| `AnimationTimer` | per-frame callback | game loop |

## 10.4 Reading order suggestion

If you want to actually *use* this document to learn:

1. Part 1 top to bottom (the Java primer) — this is the "vomit cure".
2. Part 2.2 and 2.3 — how a frame flows and how the code is split.
3. Read the real files in this order, skimming Part 3 alongside:
   `Direction → Entity → Maze → SparseMatrix → RingBuffer → MazeGraph →
   SearchTree → Trie → CommandEngine → Pacman → Ghost → Game → Renderer →
   GameController`. That is almost exactly dependency order.
4. Part 4, once you've seen the code, to consolidate the AI.
5. Part 5 for the data structures, Part 6–8 for the "how", Part 9 for testing.

For the two recent features specifically:

- **Ghost-count choice**: read `Game.GhostMode`/`setGhostMode` (§3.11), the
  `active` flag (§3.10), and the `HUNTER` brain (§3.10, §4.10). The renderer and
  collision code both honour `active`.
- **FXML UI**: read `pacman.fxml` and `GameController` (§3.16) plus Part 8.

That is the complete deep dive. Nothing about this game is left unexplained.
