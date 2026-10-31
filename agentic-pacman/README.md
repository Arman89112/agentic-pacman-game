This Pac-Man game is written in Java using JavaFX for rendering and UI management, and Java's built-in javax.sound.sampled API for synthesized procedural audio (requiring no external image or sound assets).

NOTE: For a complete, line-by-line explanation of every file written for a
Python programmer, see the top-level `CODE_EXPLAINED.md` in the project root.

All source files are located in src/com/pacman/:
1. pacman.fxml – FXML layout for the whole UI: the drawing Canvas plus the start/menu overlay where the player picks the ghost roster (1 Ghost HUNTER vs 4 Ghosts CLASSIC). Loaded by FXMLLoader.
2. GameController.java – @FXML controller bound to pacman.fxml: owns the Game + Renderer, the AnimationTimer game loop, keyboard/menu wiring, and the ghost-mode selection.
3. PacmanApp.java – Application entry point: loads pacman.fxml, builds the Scene around the injected controller, shows the Stage and starts the loop.
4. Game.java – Core game state machine, level management, score/lives tracking, scatter/chase mode switching, collision detection, ghost targeting, ghost-mode roster (ALL/SOLO), cheat timers, and the sparse-storage trigger.
5. Renderer.java – Graphics engine rendering walls, pellets (dense or sparse), Pac-Man, active ghosts, HUD, cheat tags, the optional graph debug overlay, and screen overlays onto a JavaFX Canvas.
6. SoundManager.java – Procedural sound synthesizer running on a background worker thread (including the cheat activation blip).
7. Entity.java – Abstract base class for moving objects (Pacman, Ghost), managing coordinate positioning, grid snapping, and screen wrapping.
8. Pacman.java – Player entity handling movement, input buffering, corner-turning logic, and the phase-3 movement history (RingBuffer).
9. Ghost.java – Ghost entity managing behavior states (HOUSE, LEAVING, NORMAL, FRIGHTENED, EATEN, ENTERING), the phase-2/3/4 agentic brains (A*, Dijkstra, tracker, minimax) plus the stronger HUNTER brain, path following and speed scaling.
10. Maze.java – ASCII-based level map representation, tile querying (WALL, PELLET, POWER, DOOR, EMPTY), pellet consumption, and the phase-6 dense/sparse dual representation.
11. Direction.java – Enum defining movement directions (UP, DOWN, LEFT, RIGHT, NONE), offsets (dx, dy), and opposite direction calculation.

Phase 2–6 data structures and algorithms:
12. MazeGraph.java – Phase 2: turns decision points into graph nodes and corridors into weighted edges; provides A*, Dijkstra and a one-time Floyd–Warshall all-pairs distance matrix.
13. SearchTree.java – Phase 4: minimax search tree used by the BOSS and HUNTER ghosts to reason about future moves.
14. RingBuffer.java – Phase 3: fixed-capacity circular buffer storing Pac-Man's last moves so the tracker ghost can predict him.
15. Trie.java – Phase 5: character trie used to recognise typed cheat codes.
16. CommandEngine.java – Phase 5: feeds typed characters into the Trie and reports completed commands.
17. SparseMatrix.java – Phase 6: COO sparse storage (row/col/value arrays) plus a packed wall bitmask that replaces the dense grid once most pellets are eaten.
Detailed Code Walkthrough by File
1. Direction.java
- Purpose: Defines directional vectors for grid navigation.
- Key Code:
- Constants: UP(0, -1), LEFT(-1, 0), DOWN(0, 1), RIGHT(1, 0), NONE(0, 0).
- Fields: dx and dy representing grid step deltas.
- Method opposite(): Returns the reverse direction (e.g., UP returns DOWN), essential for ghost reversals and instant player reversals.
2. Entity.java
- Purpose: Abstract base class for Pacman and Ghost.
- Key Code:
- Position: double x, y (pixel coordinates of the center).
- Grid conversion: tileX() and tileY() convert pixel coordinates to grid column/row indices using Math.floor(x / tile).
- Screen Wrapping: wrapX() handles horizontal screen wrapping when entities pass through side tunnels.
- Centering: atCenter(step) checks if the entity is close enough to a tile center to make a turn. snapToCenter() snaps coordinates precisely to the center of the current tile.
3. Maze.java
- Purpose: Manages the static level layout (LAYOUT array of 31 strings, 28 columns each) and mutable pellet states.
- Key Code:
- Legend: '#' (wall), '.' (pellet), 'o' (power pellet), '-' (ghost house door), ' ' (empty).
- reset(): Populates the 2D tiles[][] array from LAYOUT and counts total pellets.
- canMove(c, r, dir, canUseDoor): Checks whether an entity at tile (c, r) can move one step in dir. Walls block all; doors block Pac-Man but allow ghosts when canUseDoor is true.
- eat(c, r): Consumes pellets at the specified tile, reducing pelletsLeft and returning 0 (none), 1 (pellet), or 2 (power pellet).
4. Pacman.java
- Purpose: Manages player movement, input buffering, and wall collisions.
- Key Code:
- Speed: Initialized to 128 pixels per second.
- desired: Stores the player's next requested turn direction.
- update(dt):
- Allows instant reversal if the player presses the opposite direction (desired == dir.opposite()).
- When aligned with a tile center (centered), applies a buffered turn (desired) if the path is open (maze.canMove).
- If blocked by a wall at the center, Pac-Man stops moving (dir = Direction.NONE) but keeps facing the wall. Otherwise, updates x and y based on speed and dt.
5. Ghost.java
- Purpose: Implements ghost behaviors (Blinky, Pinky, Inky, Clyde) across multiple states.
- Key Code:
- Enums: Kind (BLINKY, PINKY, INKY, CLYDE) and State (HOUSE, LEAVING, NORMAL, FRIGHTENED, EATEN, ENTERING).
- update(dt, game): Routes behavior depending on current state:
- HOUSE: Bobs up and down until houseTimer expires.
- LEAVING: Moves out of the ghost house to the hallway.
- NORMAL/FRIGHTENED: Navigates the maze using grid movement (gridMove).
- EATEN: Returns rapidly to the ghost house center as a pair of eyes.
- ENTERING: Descends back into the house to revive.
- chooseDirection(...): Implements classic AI targeting. In NORMAL mode, ghosts evaluate all valid outgoing directions (excluding immediate reversal) and pick the tile center closest to their target coordinate. In FRIGHTENED mode, they pick randomly.
6. Game.java
- Purpose: The central controller coordinating game state, rules, score, and ghost targeting.
- Key Code:
- States: TITLE, READY, PLAYING, PAUSED, DYING, LEVEL_COMPLETE, GAME_OVER.
- Modes: Alternates globally between SCATTER and CHASE modes according to standard arcade timers.
- targetFor(Ghost g, col, row): Defines AI destination targets for each ghost:
- Blinky: Targets Pac-Man's exact tile (px, py).
- Pinky: Targets 4 tiles ahead of Pac-Man.
- Inky: Uses a vector calculation involving Blinky's position and 2 tiles ahead of Pac-Man.
- Clyde: Targets Pac-Man if far away (>8 tiles), otherwise falls back to his corner scatter target.
- checkCollisions(): Checks distance between Pac-Man and ghosts. If normal, Pac-Man dies (die()). If frightened, ghost is eaten (g.state = EATEN), increasing combo multiplier and score.
7. SoundManager.java
- Purpose: Generates retro sound effects programmatically without external audio files.
- Key Code:
- Uses a single-thread executor pool (pacman-sound) to play audio asynchronously.
- play(freq, ms, vol): Synthesizes sine-wave audio buffers at 44.1kHz sample rate with amplitude envelopes and feeds them to Java's SourceDataLine.
- Methods: chomp(), power(), eatGhost(), death(), levelUp().
8. Renderer.java
- Purpose: Draws all visual elements on a JavaFX GraphicsContext.
- Key Code:
- Draws rounded maze walls (drawWalls), ghost house doors (drawDoor), pellets/power pellets (drawPellets), Pac-Man with animated mouth arcs (drawPacman), ghosts with body curves, eyes, and blinking frightened states (drawGhosts/drawGhostBody/drawEyes), the HUD score bar (drawHud), and pause/game-over overlays (drawOverlay).
9. PacmanApp.java
- Purpose: JavaFX lifecycle management and FXML bootstrap.
- Key Code:
- start(Stage stage): loads pacman.fxml with FXMLLoader, wraps the returned root in a Scene (CANVAS_W, CANVAS_H), hands the scene to the injected GameController and shows the Stage.
- The game window is no longer built by hand in Java; the Canvas, the start menu and the ghost-mode radio buttons all live in pacman.fxml.
10. GameController.java
- Purpose: FXML controller + game loop.
- Key Code:
- @FXML fields (canvas, menu, soloGhost, allGhosts, startButton, statusLabel) are injected by FXMLLoader.
- initialize(): creates the Game and Renderer and sizes the Canvas.
- onStart(): reads the selected ghost mode (SOLO/HUNTER vs ALL/CLASSIC), calls game.setGhostMode(...) and game.startNewGame(), hides the menu and begins updating.
- AnimationTimer loop: computes delta time, calls game.update(dt) and renderer.render(gc, game); when GAME_OVER it re-shows the FXML menu so a new mode can be chosen.
Building and Running the Game
You can build and run the game using the provided shell scripts:
- Build: ./build.sh (compiles Java files into out/)
- Run: ./run.sh (automatically builds if needed and launches the JavaFX application)