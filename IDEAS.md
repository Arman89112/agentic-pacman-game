# Future Ideas: Gameplay & AI

This document outlines potential future updates for the Agentic Pac-Man Game, focusing specifically on expanding gameplay mechanics and improving the Ghost AI.

## 🎮 Gameplay Features

*   **High Score System:** Implement a persistent high score tracker that saves the top 5 scores locally so players can compete.
*   **Bonus Fruit Items:** Add bonus fruits (cherries, strawberries, etc.) that spawn temporarily in the center of the maze for extra points.
*   **Difficulty Levels:** Create selectable difficulty modes (Easy, Medium, Hard) that adjust ghost base speeds, the duration of Power Pellets, and ghost aggression.
*   **Advanced Power-Ups:** Introduce new temporary power-ups, such as a "Freeze" item that stops ghosts for 3 seconds, or a "Speed Boost" for Pac-Man.
*   **New Level Layouts:** Design multiple maze layouts that increase in complexity, forcing players to learn new paths.
*   **Cheat Codes:** Fully implement the `Trie` command engine to recognize classic cheat codes (e.g., typing "INVINCIBLE" grants temporary immunity).

## 👻 Ghost AI Improvements

*   **Distinct Ghost Personalities:** Expand the ghost AI so each ghost uses a different pathfinding algorithm or targeting strategy (e.g., one uses Dijkstra to cut off escape routes, while another uses A* to chase directly).
*   **Cruise Elroy Mode:** Implement the classic arcade behavior where the last remaining ghost in a level speeds up significantly when only a few pellets are left.
*   **Advanced Minimax Integration:** Utilize the existing `SearchTree` to make ghosts predict Pac-Man's next 3-4 moves, rather than just reacting to his current position.
*   **Dynamic Scatter/Chase Modes:** Refine the AI to switch between "Scatter" (retreating to corners) and "Chase" (hunting Pac-Man) based on a strict timer or specific game events (like eating a Power Pellet).
*   **Flanking Behavior:** Program ghosts to coordinate with each other so they don't all take the same path, effectively trapping Pac-Man in a pincer movement.
