This Pac-Man game is written in Java using JavaFX for rendering and UI management, and Java's built-in javax.sound.sampled API for synthesized procedural audio (requiring no external image or sound assets).

All source files are located in src/com/pacman/:
1. PacmanApp.java – Application entry point, JavaFX stage/scene setup, keyboard input listener, and main game loop (AnimationTimer).
2. Game.java – Core game state machine, level management, score/lives tracking, scatter/chase mode switching, collision detection, and AI ghost targeting logic.
3. Renderer.java – Graphics engine rendering walls, pellets, Pac-Man, ghosts, HUD, and screen overlays onto a JavaFX Canvas.
4. SoundManager.java – Procedural sound synthesizer running on a background worker thread.
5. Entity.java – Abstract base class for moving objects (Pacman, Ghost), managing coordinate positioning, grid snapping, and screen wrapping.
6. Pacman.java – Player entity handling movement, input buffering, and corner-turning logic.
7. Ghost.java – Ghost entity managing behavior states (HOUSE, LEAVING, NORMAL, FRIGHTENED, EATEN, ENTERING), AI path selection, and speed scaling.
8. Maze.java – ASCII-based level map representation, tile querying (WALL, PELLET, POWER, DOOR, EMPTY), and pellet consumption.
9. Direction.java – Enum defining movement directions (UP, DOWN, LEFT, RIGHT, NONE), offsets (dx, dy), and opposite direction calculation.
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
- Purpose: JavaFX lifecycle management and game loop.
- Key Code:
- start(Stage stage): Sets up the window, canvas (CANVAS_W, CANVAS_H), key event listeners (Scene.setOnKeyPressed), and the AnimationTimer game loop.
- Game Loop: Calculates delta time (dt), calls game.update(dt) and renderer.render(gc, game) every frame.
- onKeyPressed(...): Maps arrow keys, WASD, Space/Enter, P (pause), and M (mute) to game actions.
Building and Running the Game
You can build and run the game using the provided shell scripts:
- Build: ./build.sh (compiles Java files into out/)
- Run: ./run.sh (automatically builds if needed and launches the JavaFX application)