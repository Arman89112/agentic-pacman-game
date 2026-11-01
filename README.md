# Agentic Pac-Man Game

A Java-based Pac-Man game built with **JavaFX**, **FXML**, and custom data structures and pathfinding algorithms. The project combines classic arcade gameplay with multiple ghost strategies, selectable game modes, procedural sound effects, and memory-efficient maze storage.

The game renders its graphics on a JavaFX `Canvas`. Its interface is defined in FXML, while Java classes manage gameplay, rendering, entity movement, ghost behaviour, and the game loop. Sound effects are synthesized at runtime using Java's built-in `javax.sound.sampled` API, so external image and audio assets are not required.

---

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Game Modes](#game-modes)
- [Ghost Behaviour and AI](#ghost-behaviour-and-ai)
- [Data Structures and Algorithms](#data-structures-and-algorithms)
- [Project Structure](#project-structure)
- [Controls](#controls)
- [Building and Running](#building-and-running)
- [How the Main Components Work](#how-the-main-components-work)

---

## Features

- Classic Pac-Man movement, maze navigation, pellets, and power pellets.
- Multiple maze layouts with increasing difficulty.
- Four classic ghosts: Blinky, Pinky, Inky, and Clyde.
- Selectable ghost roster: **CLASSIC** (all ghosts) or **HUNTER** (solo hunter mode).
- Multiple ghost behaviour states, including house, leaving, normal, frightened, eaten, and entering.
- Scatter and chase behaviour modes.
- Pathfinding and decision-making algorithms, including A*, Dijkstra, Floyd–Warshall, tracking, and minimax-based reasoning.
- Keyboard input with buffered turns and smooth grid-based movement.
- FXML-based start/menu interface.
- JavaFX Canvas rendering for the maze, characters, HUD, and overlays.
- Procedurally generated retro sound effects.
- Cheat-code recognition using a Trie.
- Optional graph debugging overlay.
- Dense-to-sparse maze pellet storage to reduce memory usage as pellets are consumed.

## Technologies

- **Java** — application logic, game rules, entities, algorithms, and data structures.
- **JavaFX** — application window, Canvas rendering, animation loop, and user interface.
- **FXML** — declarative layout for the game screen and start menu.
- **CSS** — interface styling, where configured by the project.
- **`javax.sound.sampled`** — procedural audio playback using Java's built-in audio API.

---

## Game Modes

The start menu allows the player to choose the ghost roster:

- **CLASSIC** — play against the full ghost roster.
- **HUNTER** — play against the solo HUNTER ghost mode.

The selected mode is passed from the FXML controller to the game logic before a new game starts.

## Ghost Behaviour and AI

Ghosts use several states to control their movement and behaviour:

| State | Description |
| :--- | :--- |
| `HOUSE` | The ghost waits and moves inside the ghost house. |
| `LEAVING` | The ghost exits the ghost house. |
| `NORMAL` | The ghost follows its normal navigation logic. |
| `FRIGHTENED` | The ghost uses frightened behaviour, including less predictable direction choices. |
| `EATEN` | The ghost returns toward the ghost house after being eaten. |
| `ENTERING` | The ghost re-enters the house before becoming active again. |

The classic ghost targeting logic gives each ghost a different target:

- **Blinky** targets Pac-Man's current tile.
- **Pinky** targets a position several tiles ahead of Pac-Man.
- **Inky** calculates a target using Blinky's position and a point ahead of Pac-Man.
- **Clyde** chases Pac-Man when far away and returns to a corner target when close.

The game also switches between global **SCATTER** and **CHASE** modes. Additional agentic behaviours and the HUNTER brain use the project's pathfinding, tracking, and search components.

---

## Data Structures and Algorithms

The project includes the following data structures and algorithms as part of its gameplay systems:

| Component | Role in the game |
| :--- | :--- |
| `MazeGraph` | Converts maze decision points and corridors into a weighted graph. Supports A*, Dijkstra, and a one-time Floyd–Warshall all-pairs distance calculation. |
| `SearchTree` | Supports minimax search used by advanced ghost decision-making. |
| `RingBuffer` | Stores a fixed-capacity history of Pac-Man's recent movements for tracker-style prediction. |
| `Trie` | Recognizes typed cheat-code sequences. |
| `CommandEngine` | Sends typed characters to the Trie and reports completed commands. |
| `SparseMatrix` | Stores remaining pellets in COO (coordinate) form after the maze switches from dense to sparse storage. |
| `Maze` | Manages maze layouts, tile queries, pellet consumption, and the dense/sparse storage transition. |

### Dense-to-sparse maze storage

The maze initially stores tile information in a two-dimensional character array. When at least 80% of the pellets have been eaten, the game can convert the remaining pellet data into a COO sparse matrix. A packed wall bitmask preserves wall information while the sparse structure stores the remaining pellets.

This reduces the amount of storage used for the pellet data late in a level.

---

## Project Structure

The main Java source files are located in `src/com/pacman/`.

```text
agentic-pacman-game/
├── src/
│   └── com/
│       └── pacman/
│           ├── pacman.fxml
│           ├── GameController.java
│           ├── PacmanApp.java
│           ├── Game.java
│           ├── Renderer.java
│           ├── SoundManager.java
│           ├── Entity.java
│           ├── Pacman.java
│           ├── Ghost.java
│           ├── Maze.java
│           ├── Direction.java
│           ├── MazeGraph.java
│           ├── SearchTree.java
│           ├── RingBuffer.java
│           ├── Trie.java
│           ├── CommandEngine.java
│           └── SparseMatrix.java
├── test/
├── build.sh
├── run.sh
├── README.md
└── ...
```

The exact repository may contain additional configuration, documentation, or resource files.

### Main application files

| File | Responsibility |
| :--- | :--- |
| `pacman.fxml` | Defines the game interface, including the Canvas and start/menu overlay with ghost-mode selection. |
| `GameController.java` | FXML controller. Owns the game and renderer, connects menu and keyboard actions, and runs the `AnimationTimer` loop. |
| `PacmanApp.java` | JavaFX application entry point. Loads the FXML layout, creates the scene, displays the stage, and starts the controller. |
| `Game.java` | Controls game states, levels, score, lives, collisions, ghost targeting, game modes, and gameplay rules. |
| `Renderer.java` | Draws the maze, pellets, Pac-Man, ghosts, HUD, overlays, and optional graph-debug information on the JavaFX Canvas. |
| `SoundManager.java` | Generates and plays retro sound effects asynchronously. |
| `Entity.java` | Shared base class for moving entities. Handles pixel coordinates, tile conversion, grid alignment, and horizontal wrapping. |
| `Pacman.java` | Handles player movement, buffered direction input, turning, and movement-history recording. |
| `Ghost.java` | Implements ghost states, movement, path following, speed scaling, and AI behaviour. |
| `Maze.java` | Holds level layouts and manages walls, doors, pellets, power pellets, and dense/sparse pellet storage. |
| `Direction.java` | Defines movement directions, coordinate offsets, and opposite-direction lookup. |

### Data structure and algorithm files

| File | Responsibility |
| :--- | :--- |
| `MazeGraph.java` | Graph representation of the maze and pathfinding algorithms. |
| `SearchTree.java` | Minimax search tree for advanced ghost decision-making. |
| `RingBuffer.java` | Circular buffer for recent player movement history. |
| `Trie.java` | Prefix-tree structure for cheat-code recognition. |
| `CommandEngine.java` | Processes typed cheat-code input through the Trie. |
| `SparseMatrix.java` | Coordinate-list sparse storage for remaining pellets. |

---

## Controls

The game supports the following keyboard mappings:

| Key | Action |
| :--- | :--- |
| Arrow keys | Move Pac-Man |
| `W`, `A`, `S`, `D` | Move Pac-Man |
| `Space` / `Enter` | Start or confirm, depending on the current screen |
| `P` | Pause or resume |
| `M` | Mute or unmute sound |

The start menu is used to choose the ghost mode before starting a game.

---

## Building and Running

The project includes shell scripts for building and launching the game.

### Build

From the project root, run:

```bash
./build.sh
```

The build script compiles the Java source files into the `out/` directory.

### Run

From the project root, run:

```bash
./run.sh
```

The run script automatically builds the project if needed and launches the JavaFX application.

On Windows, run the scripts using an environment that supports shell scripts, such as Git Bash or WSL, unless Windows-specific script equivalents are provided in the repository.

Make sure the required JDK and JavaFX dependencies are available as configured by the project before building.

---

## How the Main Components Work

1. **Application startup:** `PacmanApp` loads `pacman.fxml` and creates the JavaFX scene.
2. **Controller initialization:** `GameController` receives the FXML controls and prepares the `Game` and `Renderer`.
3. **Mode selection:** The player chooses CLASSIC or HUNTER mode and starts a new game.
4. **Game loop:** A JavaFX `AnimationTimer` calculates elapsed time, updates the game, and asks the renderer to draw the current frame.
5. **Player and ghost updates:** `Pacman`, `Ghost`, and the shared `Entity` logic handle movement and grid positioning.
6. **Maze and collisions:** `Game` checks movement, pellet collection, ghost interactions, score, lives, and level progression.
7. **AI and data structures:** Graph algorithms, search, movement history, Trie-based commands, and sparse storage support the game's additional systems.
8. **Audio:** `SoundManager` synthesizes sound effects on a background worker thread.

---

## Documentation

For a more detailed explanation of the implementation, see the additional project documentation files in the repository, such as:

- `CODE_EXPLAINED.md`
- `GHOST_AI_AND_DATA_STRUCTURES.md`
- `JAVA_DEEP_DIVE.md`

---

## Project Note

This README describes the project based on the supplied project documentation. Keep the file list, controls, and build instructions synchronized with the actual source code and scripts as the project changes.
