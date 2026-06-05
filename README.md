# CyberSurvyDogs-Battleship

GUI-based Battleship game (Human vs Computer) for the ICS4U Final Summative.
Java + Swing, built as a standard Eclipse project.

> **Group members:** Oson Wang, Olin Wang, Willey Yao

## Tech Stack

- **Language:** Java 17+ (uses only the standard library)
- **IDE:** Eclipse (standard Java project: `.classpath` / `.project` included)
- **GUI:** Java Swing + AWT
- **Libraries:** None, no external dependencies
- **Testing:** Custom lightweight assertion harness (`test/battleship/ModelTests.java`); no JUnit required

## Features

- 10x10 grid (rows A-J, columns 1-10), standard 5-ship fleet
  (Carrier 5, Battleship 4, Cruiser 3, Submarine 3, Destroyer 2).
- Themed, mouse-driven GUI built from a 2D array of buttons, with
  colour-coded hits, misses, ships, and sunk ships.
- Ship placement with quality-of-life helpers:
  - Click a cell to drop the current ship, with a **live preview** that
    highlights where it will land (and warns on invalid spots).
  - Toggle **Orientation** with the button or just press **R** to rotate.
  - **Random Placement** to auto-place the whole fleet, or **Reset
    Placement** to start over.
- Choose who fires first: **Me**, **AI**, or **Coin Toss** (an animated
  coin-toss dialog decides it).
- Two computer opponents, chosen at game start (Advanced is the default):
  - **Simple AI** - fires at random untried cells.
  - **Advanced AI** - hunts with a diagonal parity sweep tuned to the
    smallest remaining ship, then switches to probability-density
    targeting to find and finish a ship once it lands a hit.
- Live status panel: whose turn, elapsed timer, and per-side stats
  (shots, hits, ships sunk, ships remaining) plus a message log.
- **File handling:**
  - At game start the computer's fleet is written to `ai_ships.txt`
    (a readable map + each ship's coordinates) so it can be printed and
    verified while playing.
- Clear win condition with an end-of-game dialog.

## How to run

### In Eclipse
1. Import this folder as an existing project (`File > Open Projects from File System`).
2. Run `src/battleship/Main.java` as a Java Application.

### From the command line

```bash
javac -d bin src/battleship/model/*.java src/battleship/player/*.java src/battleship/ai/*.java src/battleship/game/*.java src/battleship/gui/*.java src/battleship/Main.java
java -cp bin battleship.Main
```

### Run logic tests

```bash
javac -cp bin -d bin test/battleship/ModelTests.java
java -cp bin battleship.ModelTests
```

## How to play

1. On the setup screen, place all 5 ships:
  - Press R (or the Orientation button) for horizontal/vertical, then click a cell to drop the current ship. A preview shows where it will go. Use Random Placement or Reset Placement as needed.
2. Choose AI Difficulty (Simple or Advanced) and Who Starts (Me / AI / Coin Toss), then press Start Game.
3. If you picked Coin Toss, an animated toss decides who fires first.
4. Click cells on the Enemy Waters board to fire. Red = hit, white = miss, dark red = sunk. Your own board (left) shows the computer's shots.
5. Sink the entire enemy fleet to win.
Use the Game menu to start a new game, save the current game, or load a save.

## Project structure

```
src/battleship/
  Main.java                       entry point
  model/                          Cell, Ship, Fleet, Board
  player/                         Player (abstract), HumanPlayer, AIPlayer
  ai/                             AIStrategy, SimpleAI, AdvancedAI
  game/                           Game, SaveManager
  gui/                            BattleshipFrame, GridPanel, StatusPanel, CoinTossDialog, UITheme
test/battleship/ModelTests.java   logic test harness
```