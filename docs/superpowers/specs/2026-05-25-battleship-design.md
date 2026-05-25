# Battleship Tournament — Design Spec

**Date:** 2026-05-25
**Project:** CyberSurvyDogs-Battleship (ICS4U Final Summative)
**Language/Toolkit:** Java (JDK 8+), Swing GUI, plain Eclipse project (no build tool)

## 1. Goal

A GUI-based, single-player-vs-computer Battleship game in Java that satisfies the
ICS4U summative rubric. The human plays against a computer AI on a 10×10 grid with
5 standard ships. The program demonstrates OOP, algorithms (AI hunt/target +
probability), data structures, GUI development, and file handling.

## 2. Scope

In scope:
- 10×10 grid (Rows A–J, Columns 1–10).
- Standard 5-ship fleet: Carrier (5), Battleship (4), Cruiser (3), Submarine (3), Destroyer (2).
- Human vs Computer gameplay, mouse-driven Swing GUI.
- Coin toss to decide who fires first.
- Two AI strategies selectable at start: Simple (random) and Advanced (hunt/target + probability).
- Manual human ship placement (click + rotate toggle) **and** a Random auto-place button.
- Live status: shots, hits, misses, ships sunk, ships remaining (both sides), whose turn, message log.
- Required file output: AI ship placement written to `ai_ships.txt` at game start.
- Bonus features: Save/Load full game in progress, game timer, score/stats panel, color-coded grid.

Out of scope:
- Sound effects (declined).
- Network/multiplayer.
- JavaFX.

## 3. Architecture

Pure domain model (matches the UML diagram exactly, no GUI code) + a Swing view/controller
layer on top. Turns are event-driven (driven by mouse clicks), not a blocking loop.

```
battleship.model   -> Cell, Ship, Board
battleship.player  -> Player (abstract), HumanPlayer, AIPlayer
battleship.ai      -> AIStrategy (interface), SimpleAI, AdvancedAI
battleship.game    -> Game
battleship.gui     -> BattleshipFrame, GridPanel, StatusPanel   (view layer; not in UML)
battleship.Main    -> entry point
```

### 3.1 Model classes (from UML)

**Cell**
- Fields: `int row`, `int col`, `boolean isHit`, `boolean hasShip`, `Ship ship`
- Methods: `markHit()`, `isHit()`, `setShip(Ship)`, `getShip()`

**Ship**
- Fields: `String name`, `int size`, `int hits`, `boolean isSunk`, `ArrayList<Cell> cells`
- Methods: `hit()`, `isSunk()`, `getName()`, `getSize()`

**Board**
- Fields: `Cell[][] grid`, `ArrayList<Ship> ships`, `int size = 10`
- Methods:
  - `boolean placeShip(Ship s, int r, int c, boolean horizontal)` — validates bounds + no overlap, links cells to ship, adds to `ships`. Returns false if invalid.
  - `String receiveAttack(int r, int c)` — returns `"MISS"`, `"HIT"`, or `"SUNK:<name>"`. `"ALREADY"` if already fired there.
  - `boolean allShipsSunk()`
  - `Cell getCell(int r, int c)`
  - `void reset()`
  - `void saveToFile(String path)` — writes human-readable ship layout (grid map + ship coordinates).

### 3.2 Player hierarchy (from UML)

**Player (abstract)**
- Fields: `String name`, `Board ownBoard`, `Board targetBoard`
- Methods: `abstract void placeShips()`, `abstract int[] makeMove()`, `Board getOwnBoard()`, `Board getTargetBoard()`

**HumanPlayer**
- Field: `int[] selectedCell`
- `placeShips()` — GUI-driven (placement handled in setup screen; method validates the chosen layout).
- `int[] makeMove()` — returns `selectedCell` set by the latest GUI click.
- `handleMouseClick(row, col)` — records the selected cell.

**AIPlayer**
- Fields: `AIStrategy strategy`, `String difficulty`
- `placeShips()` — delegates to `strategy.placeShips(ownBoard)`.
- `int[] makeMove()` — delegates to `strategy.chooseTarget(targetBoard)`.
- `setStrategy(AIStrategy)`.

### 3.3 AI strategies (from UML)

**AIStrategy (interface)**: `int[] chooseTarget(Board board)`, `void placeShips(Board board)`

**SimpleAI**
- Fields: `ArrayList<int[]> triedCells`, `Random random`
- `chooseTarget` — random previously-untried cell.
- `placeShips` — random valid placement of the 5-ship fleet.
- `randomUntriedCell()`.

**AdvancedAI**
- Fields: `Stack<int[]> hitStack`, `int[] lastHit`, `boolean huntMode`, `int[][] probGrid`
- `chooseTarget`:
  - Target mode (stack non-empty): pop a queued neighbor of a known hit.
  - Hunt mode: pick the highest-probability cell from `probGrid` (parity + ship-fit density).
- On a hit: push valid orthogonal neighbors onto `hitStack`, record `lastHit`, enter target mode.
- On a sink: clear the stack for that ship, return to hunt mode.
- `placeShips` — same random valid placement as Simple (placement strategy not graded differently).
- `updateProbGrid()` — recomputes density of remaining-ship fits over untried cells.
- `huntTarget()` — helper returning the next hunt cell.

### 3.4 Game (from UML)

- Fields: `HumanPlayer humanPlayer`, `AIPlayer aiPlayer`, `Player currentTurn`, `boolean isGameOver`, `String winner`
- Methods:
  - `startGame()` — both players place ships, AI writes `ai_ships.txt`, coin toss sets `currentTurn`.
  - `Player coinToss()` — random; returns starting player.
  - `void takeTurn(Player p)` — resolves one shot against the opponent's board, updates state.
  - `boolean checkWin()` — true if either board `allShipsSunk()`.
  - `void switchTurn()`.
  - `void endGame()` — sets `isGameOver`, `winner`.

### 3.5 GUI layer (new; not in UML)

- **BattleshipFrame** (`JFrame`): owns the `Game`, menu bar (New/Save/Load), setup screen, and play screen; orchestrates turn flow on the Swing thread.
- **GridPanel** (`JPanel`): a 10×10 array of `JButton` with A–J / 1–10 labels; one for own board, one for enemy board. Renders cell state via color.
- **StatusPanel** (`JPanel`): coin-toss result, current turn, timer, per-side stats, scrolling message log.

Turn flow (event-driven):
1. Setup screen: human places fleet (manual or Random), picks difficulty, clicks Start.
2. `Game.startGame()` runs coin toss; AI writes `ai_ships.txt`.
3. If human's turn: enable enemy grid; a click resolves the human shot, updates UI.
4. If AI's turn: AI shot resolved (brief `javax.swing.Timer` delay for readability), updates UI.
5. After each shot, `checkWin()`; on win, disable grids, stop timer, show winner.

## 4. Data / file formats

**`ai_ships.txt`** (required, written at game start):
```
Battleship - AI Ship Placement
Generated: <timestamp>

   1 2 3 4 5 6 7 8 9 10
A  . . C C C C C . . .
... (10 rows; ship initial where a ship sits, '.' empty)

Ships:
Carrier    (5): A3-A7
Battleship (4): C1-C4
Cruiser    (3): ...
Submarine  (3): ...
Destroyer  (2): ...
```

**Save game file** (bonus, plain text): version tag, difficulty, whose turn, elapsed seconds,
then for each board the ship list (name + coordinates + horizontal flag) and the set of fired
shots (with hit/miss). Load reconstructs both `Board`s and resumes.

## 5. Color coding

- Empty/unknown: light gray/blue
- Own ship: gray
- Hit: red
- Miss: white (with a dot) / light blue
- Sunk ship: dark red

## 6. Error handling

- `placeShip` rejects out-of-bounds/overlap; setup screen blocks Start until all 5 ships placed.
- `receiveAttack` returns `"ALREADY"` for repeat shots; GUI ignores already-fired cells.
- Save/Load wrapped in try/catch with a user-facing dialog on failure; corrupt save files are
  rejected with a message rather than crashing.

## 7. Testing

- Model unit checks (runnable `main`/JUnit-style asserts where practical): ship placement
  validity (bounds, overlap), attack resolution (miss/hit/sunk transitions), `allShipsSunk`,
  coin-toss returns a valid player, AdvancedAI finishes a ship once hit, SimpleAI never repeats
  a cell.
- Manual GUI testing: place fleet both ways, play a full game vs each AI to a win, save mid-game
  and reload, verify `ai_ships.txt` matches the on-screen AI fleet, timer/stats update correctly.

## 8. Coding standards (rubric)

- Header comment block on every `.java` file: group member names, date, assignment title.
- Meaningful names, consistent indentation/braces, methods kept small and single-purpose,
  variable/method documentation comments where purpose is non-obvious.

## 9. Deviations from the UML

- Human `makeMove()` is event-driven (returns the GUI-selected cell) rather than blocking — Swing
  requires this. All UML methods are still present.
- GUI classes (`BattleshipFrame`, `GridPanel`, `StatusPanel`) are added on top of the UML model;
  the UML domain classes remain GUI-free.
