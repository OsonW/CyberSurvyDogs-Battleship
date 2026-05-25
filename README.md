# CyberSurvyDogs-Battleship

GUI-based Battleship game (Human vs Computer) for the ICS4U Final Summative.
Java + Swing, built as a standard Eclipse project.

> **Group members:** _replace with your names_ — Member 1, Member 2, Member 3, Member 4

## Features

- 10x10 grid (rows A-J, columns 1-10), standard 5-ship fleet
  (Carrier 5, Battleship 4, Cruiser 3, Submarine 3, Destroyer 2).
- Mouse-driven GUI built from a 2D array of buttons, with colour-coded
  hits, misses, ships, and sunk ships.
- Manual ship placement (click + orientation toggle) **and** a Random button.
- Coin toss to decide who fires first.
- Two computer opponents, chosen at game start:
  - **Simple AI** - fires at random untried cells.
  - **Advanced AI** - probability-based hunting plus hit/target tracking
    (finds and finishes ships once it lands a hit).
- Live status panel: whose turn, elapsed timer, and per-side stats
  (shots, hits, ships sunk, ships remaining) plus a message log.
- **File handling:**
  - At game start the computer's fleet is written to `ai_ships.txt`
    (a readable map + each ship's coordinates) so it can be printed and
    verified while playing.
  - Save a game in progress and load it later (Game menu).
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

### Run the logic tests
```bash
javac -cp bin -d bin test/battleship/ModelTests.java
java -cp bin battleship.ModelTests
```

## How to play

1. On the setup screen, place all 5 ships:
   - Toggle **Orientation** for horizontal/vertical, then click a cell to drop
     the current ship, **or** press **Random Placement**.
2. Choose **AI Difficulty** (Simple or Advanced) and press **Start Game**.
3. A coin toss decides who fires first.
4. Click cells on the **Enemy Waters** board to fire. Red = hit, white = miss,
   dark red = sunk. Your own board (left) shows the computer's shots.
5. Sink the entire enemy fleet to win.

Use the **Game** menu to start a new game, save the current game, or load a save.

## Project structure

```
src/battleship/
  Main.java                 entry point
  model/   Cell, Ship, Fleet, Board
  player/  Player (abstract), HumanPlayer, AIPlayer
  ai/      AIStrategy, SimpleAI, AdvancedAI
  game/    Game, SaveManager
  gui/     BattleshipFrame, GridPanel, StatusPanel
test/battleship/ModelTests.java   logic test harness
docs/superpowers/  design spec and implementation plan
```
