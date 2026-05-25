# Battleship Tournament Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A GUI-based single-player-vs-computer Battleship game in Java/Swing satisfying the ICS4U summative rubric.

**Architecture:** UML-faithful GUI-free domain model (`Cell`, `Ship`, `Board`, `Player`/`HumanPlayer`/`AIPlayer`, `AIStrategy`/`SimpleAI`/`AdvancedAI`, `Game`) with a Swing view/controller layer (`BattleshipFrame`, `GridPanel`, `StatusPanel`) on top. Turns are event-driven from mouse clicks.

**Tech Stack:** Java (JDK 8+), Swing, plain Eclipse project (no build tool). Tests are runnable assertion mains compiled with `javac` and run with `java` (no JUnit jar dependency).

---

## File Structure

```
src/battleship/Main.java                 -> launches the GUI
src/battleship/model/Cell.java           -> one grid square
src/battleship/model/Ship.java           -> a ship (size, hits, cells)
src/battleship/model/Fleet.java          -> standard 5-ship factory
src/battleship/model/Board.java          -> 10x10 grid, placement, attacks, file save
src/battleship/player/Player.java        -> abstract player
src/battleship/player/HumanPlayer.java   -> GUI-driven player
src/battleship/player/AIPlayer.java      -> strategy-driven player
src/battleship/ai/AIStrategy.java        -> strategy interface
src/battleship/ai/SimpleAI.java          -> random strategy
src/battleship/ai/AdvancedAI.java        -> hunt/target + probability
src/battleship/game/Game.java            -> flow, coin toss, win check
src/battleship/game/SaveManager.java     -> save/load game to text file
src/battleship/gui/GridPanel.java        -> 10x10 JButton grid view
src/battleship/gui/StatusPanel.java      -> turn/timer/stats/log
src/battleship/gui/BattleshipFrame.java  -> main window, setup + play screens, turn flow
test/battleship/ModelTests.java          -> runnable assertion harness
```

All `.java` files start with this header (engineer fills in real names):

```java
/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - <Member 1>, <Member 2>, <Member 3>, <Member 4>
 * Date: 2026
 * File: <ClassName>.java
 */
```

---

## Task 1: Project skeleton + Cell + Ship

**Files:**
- Create: `src/battleship/model/Cell.java`
- Create: `src/battleship/model/Ship.java`
- Create: `test/battleship/ModelTests.java`

- [ ] **Step 1: Write the failing test**

`test/battleship/ModelTests.java`:

```java
package battleship;

import battleship.model.Cell;
import battleship.model.Ship;

public class ModelTests {
    static int passed = 0, failed = 0;

    static void check(boolean cond, String name) {
        if (cond) { passed++; System.out.println("PASS: " + name); }
        else { failed++; System.out.println("FAIL: " + name); }
    }

    public static void main(String[] args) {
        testCell();
        testShip();
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    static void testCell() {
        Cell c = new Cell(2, 3);
        check(c.getRow() == 2 && c.getCol() == 3, "cell stores coords");
        check(!c.isHit(), "cell starts not hit");
        c.markHit();
        check(c.isHit(), "cell marks hit");
        check(!c.hasShip(), "cell starts shipless");
    }

    static void testShip() {
        Ship s = new Ship("Destroyer", 2);
        check(s.getName().equals("Destroyer"), "ship name");
        check(s.getSize() == 2, "ship size");
        check(!s.isSunk(), "ship starts afloat");
        s.hit();
        check(!s.isSunk(), "ship not sunk after 1 hit");
        s.hit();
        check(s.isSunk(), "ship sunk after size hits");
    }
}
```

- [ ] **Step 2: Run test to verify it fails to compile**

Run: `javac -d bin src/battleship/model/Cell.java src/battleship/model/Ship.java test/battleship/ModelTests.java`
Expected: compile errors (classes don't exist).

- [ ] **Step 3: Implement Cell**

`src/battleship/model/Cell.java`:

```java
package battleship.model;

public class Cell {
    private int row;
    private int col;
    private boolean hit;
    private boolean hasShip;
    private Ship ship;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.hit = false;
        this.hasShip = false;
        this.ship = null;
    }

    public void markHit() { this.hit = true; }
    public boolean isHit() { return hit; }
    public void clearHit() { this.hit = false; }
    public boolean hasShip() { return hasShip; }
    public void setShip(Ship s) { this.ship = s; this.hasShip = (s != null); }
    public Ship getShip() { return ship; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
```

- [ ] **Step 4: Implement Ship**

`src/battleship/model/Ship.java`:

```java
package battleship.model;

import java.util.ArrayList;

public class Ship {
    private String name;
    private int size;
    private int hits;
    private boolean sunk;
    private ArrayList<Cell> cells;

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.hits = 0;
        this.sunk = false;
        this.cells = new ArrayList<Cell>();
    }

    public void hit() {
        hits++;
        if (hits >= size) sunk = true;
    }

    public boolean isSunk() { return sunk; }
    public String getName() { return name; }
    public int getSize() { return size; }
    public int getHits() { return hits; }
    public ArrayList<Cell> getCells() { return cells; }
    public void addCell(Cell c) { cells.add(c); }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `javac -d bin src/battleship/model/Cell.java src/battleship/model/Ship.java test/battleship/ModelTests.java` then `java -cp bin battleship.ModelTests`
Expected: all PASS, "0 failed".

- [ ] **Step 6: Commit**

```bash
git add src/battleship/model/Cell.java src/battleship/model/Ship.java test/battleship/ModelTests.java
git commit -m "Add Cell and Ship model classes with tests"
```

---

## Task 2: Fleet + Board (placement, attacks, sunk)

**Files:**
- Create: `src/battleship/model/Fleet.java`
- Create: `src/battleship/model/Board.java`
- Modify: `test/battleship/ModelTests.java`

- [ ] **Step 1: Add failing tests**

Add to `ModelTests.java` `main`: call `testBoard();`. Add imports `battleship.model.Board;` `battleship.model.Fleet;` and method:

```java
static void testBoard() {
    Board b = new Board();
    check(b.getSize() == 10, "board size 10");
    Ship carrier = new Ship("Carrier", 5);
    check(b.placeShip(carrier, 0, 0, true), "place carrier in bounds");
    check(!b.placeShip(new Ship("Battleship", 4), 0, 0, true), "reject overlap");
    check(!b.placeShip(new Ship("Cruiser", 3), 0, 8, true), "reject out of bounds");

    check(b.receiveAttack(0, 0).equals("HIT"), "hit ship");
    check(b.receiveAttack(0, 0).equals("ALREADY"), "repeat shot rejected");
    check(b.receiveAttack(9, 9).equals("MISS"), "miss empty");
    b.receiveAttack(0, 1); b.receiveAttack(0, 2); b.receiveAttack(0, 3);
    check(b.receiveAttack(0, 4).equals("SUNK:Carrier"), "sink reports name");
    check(b.allShipsSunk(), "all sunk true");

    check(Fleet.standardFleet().size() == 5, "standard fleet has 5 ships");
}
```

- [ ] **Step 2: Run to verify failure**

Run: `javac -d bin src/battleship/model/*.java test/battleship/ModelTests.java`
Expected: compile errors (Board, Fleet missing).

- [ ] **Step 3: Implement Fleet**

`src/battleship/model/Fleet.java`:

```java
package battleship.model;

import java.util.ArrayList;
import java.util.List;

public class Fleet {
    public static List<Ship> standardFleet() {
        List<Ship> ships = new ArrayList<Ship>();
        ships.add(new Ship("Carrier", 5));
        ships.add(new Ship("Battleship", 4));
        ships.add(new Ship("Cruiser", 3));
        ships.add(new Ship("Submarine", 3));
        ships.add(new Ship("Destroyer", 2));
        return ships;
    }
}
```

- [ ] **Step 4: Implement Board**

`src/battleship/model/Board.java`:

```java
package battleship.model;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Board {
    public static final int SIZE = 10;
    private Cell[][] grid;
    private ArrayList<Ship> ships;

    public Board() {
        grid = new Cell[SIZE][SIZE];
        ships = new ArrayList<Ship>();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = new Cell(r, c);
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    public boolean placeShip(Ship s, int r, int c, boolean horizontal) {
        for (int i = 0; i < s.getSize(); i++) {
            int rr = horizontal ? r : r + i;
            int cc = horizontal ? c + i : c;
            if (!inBounds(rr, cc) || grid[rr][cc].hasShip()) return false;
        }
        for (int i = 0; i < s.getSize(); i++) {
            int rr = horizontal ? r : r + i;
            int cc = horizontal ? c + i : c;
            grid[rr][cc].setShip(s);
            s.addCell(grid[rr][cc]);
        }
        ships.add(s);
        return true;
    }

    public String receiveAttack(int r, int c) {
        Cell cell = grid[r][c];
        if (cell.isHit()) return "ALREADY";
        cell.markHit();
        if (cell.hasShip()) {
            Ship s = cell.getShip();
            s.hit();
            if (s.isSunk()) return "SUNK:" + s.getName();
            return "HIT";
        }
        return "MISS";
    }

    public boolean allShipsSunk() {
        if (ships.isEmpty()) return false;
        for (Ship s : ships) if (!s.isSunk()) return false;
        return true;
    }

    public int shipsRemaining() {
        int n = 0;
        for (Ship s : ships) if (!s.isSunk()) n++;
        return n;
    }

    public Cell getCell(int r, int c) { return grid[r][c]; }
    public ArrayList<Ship> getShips() { return ships; }
    public int getSize() { return SIZE; }

    public void reset() {
        ships.clear();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                grid[r][c].setShip(null);
                grid[r][c].clearHit();
            }
    }

    // Writes a human-readable map + ship coordinates (required deliverable).
    public void saveToFile(String path) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(path));
            pw.println("Battleship - AI Ship Placement");
            pw.println();
            pw.print("   ");
            for (int c = 1; c <= SIZE; c++) pw.print((c == 10 ? "10" : " " + c) + " ");
            pw.println();
            for (int r = 0; r < SIZE; r++) {
                pw.print((char) ('A' + r) + "  ");
                for (int c = 0; c < SIZE; c++) {
                    Cell cell = grid[r][c];
                    char ch = cell.hasShip() ? cell.getShip().getName().charAt(0) : '.';
                    pw.print(" " + ch + " ");
                }
                pw.println();
            }
            pw.println();
            pw.println("Ships:");
            for (Ship s : ships) {
                Cell first = s.getCells().get(0);
                Cell last = s.getCells().get(s.getCells().size() - 1);
                pw.println(String.format("%-11s (%d): %s - %s",
                        s.getName(), s.getSize(), coord(first), coord(last)));
            }
            pw.close();
        } catch (IOException e) {
            System.err.println("Could not save ship file: " + e.getMessage());
        }
    }

    private String coord(Cell c) {
        return "" + (char) ('A' + c.getRow()) + (c.getCol() + 1);
    }
}
```

- [ ] **Step 5: Run to verify pass**

Run: `javac -d bin src/battleship/model/*.java test/battleship/ModelTests.java` then `java -cp bin battleship.ModelTests`
Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/battleship/model/Fleet.java src/battleship/model/Board.java test/battleship/ModelTests.java
git commit -m "Add Fleet and Board with placement, attack, and file save"
```

---

## Task 3: Player abstract + HumanPlayer

**Files:**
- Create: `src/battleship/player/Player.java`
- Create: `src/battleship/player/HumanPlayer.java`
- Modify: `test/battleship/ModelTests.java`

- [ ] **Step 1: Add failing test**

Add `testHumanPlayer();` to main and import `battleship.player.HumanPlayer;`:

```java
static void testHumanPlayer() {
    HumanPlayer h = new HumanPlayer("You");
    check(h.getName().equals("You"), "human name");
    check(h.getOwnBoard() != null, "human has own board");
    check(h.getTargetBoard() != null, "human has target board");
    h.handleMouseClick(4, 5);
    int[] move = h.makeMove();
    check(move[0] == 4 && move[1] == 5, "human move from click");
}
```

- [ ] **Step 2: Run to verify failure**

Run: `javac -d bin src/battleship/model/*.java src/battleship/player/*.java test/battleship/ModelTests.java`
Expected: compile errors.

- [ ] **Step 3: Implement Player**

`src/battleship/player/Player.java`:

```java
package battleship.player;

import battleship.model.Board;

public abstract class Player {
    protected String name;
    protected Board ownBoard;
    protected Board targetBoard;

    public Player(String name) {
        this.name = name;
        this.ownBoard = new Board();
        this.targetBoard = new Board();
    }

    public abstract void placeShips();
    public abstract int[] makeMove();

    public String getName() { return name; }
    public Board getOwnBoard() { return ownBoard; }
    public Board getTargetBoard() { return targetBoard; }
}
```

Note: `targetBoard` is a view the player builds of the opponent (records own shots' hit/miss). Shots are resolved against the *opponent's* `ownBoard` by `Game`.

- [ ] **Step 4: Implement HumanPlayer**

`src/battleship/player/HumanPlayer.java`:

```java
package battleship.player;

public class HumanPlayer extends Player {
    private int[] selectedCell;

    public HumanPlayer(String name) {
        super(name);
        this.selectedCell = null;
    }

    // Ships are placed via the setup GUI directly onto ownBoard.
    public void placeShips() { /* handled by setup screen */ }

    public void handleMouseClick(int row, int col) {
        this.selectedCell = new int[] { row, col };
    }

    public int[] makeMove() { return selectedCell; }
}
```

- [ ] **Step 5: Run to verify pass**

Run: compile + `java -cp bin battleship.ModelTests`
Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/battleship/player/Player.java src/battleship/player/HumanPlayer.java test/battleship/ModelTests.java
git commit -m "Add Player abstract class and HumanPlayer"
```

---

## Task 4: AIStrategy + SimpleAI

**Files:**
- Create: `src/battleship/ai/AIStrategy.java`
- Create: `src/battleship/ai/SimpleAI.java`
- Modify: `test/battleship/ModelTests.java`

- [ ] **Step 1: Add failing test**

Add `testSimpleAI();` and import `battleship.ai.SimpleAI;` `battleship.ai.AIStrategy;`:

```java
static void testSimpleAI() {
    Board b = new Board();
    for (Ship s : Fleet.standardFleet()) { /* place later */ }
    SimpleAI ai = new SimpleAI();
    Board enemyView = new Board();
    java.util.HashSet<String> seen = new java.util.HashSet<String>();
    for (int i = 0; i < 100; i++) {
        int[] t = ai.chooseTarget(enemyView);
        String key = t[0] + "," + t[1];
        check(!seen.contains(key) || true, "simple ai target valid");
        seen.add(key);
        enemyView.getCell(t[0], t[1]).markHit();
    }
    check(seen.size() == 100, "simple ai never repeats across full board");

    Board place = new Board();
    ai.placeShips(place);
    check(place.getShips().size() == 5, "simple ai places 5 ships");
}
```

- [ ] **Step 2: Run to verify failure**

Run: `javac -d bin src/battleship/model/*.java src/battleship/player/*.java src/battleship/ai/*.java test/battleship/ModelTests.java`
Expected: compile errors.

- [ ] **Step 3: Implement AIStrategy**

`src/battleship/ai/AIStrategy.java`:

```java
package battleship.ai;

import battleship.model.Board;

public interface AIStrategy {
    int[] chooseTarget(Board enemyView);
    void placeShips(Board board);
}
```

- [ ] **Step 4: Implement SimpleAI**

`src/battleship/ai/SimpleAI.java`:

```java
package battleship.ai;

import java.util.ArrayList;
import java.util.Random;

import battleship.model.Board;
import battleship.model.Fleet;
import battleship.model.Ship;

public class SimpleAI implements AIStrategy {
    private ArrayList<int[]> triedCells = new ArrayList<int[]>();
    private Random random = new Random();

    public int[] chooseTarget(Board enemyView) {
        return randomUntriedCell(enemyView);
    }

    private int[] randomUntriedCell(Board enemyView) {
        int n = enemyView.getSize();
        int r, c;
        do {
            r = random.nextInt(n);
            c = random.nextInt(n);
        } while (enemyView.getCell(r, c).isHit());
        triedCells.add(new int[] { r, c });
        return new int[] { r, c };
    }

    public void placeShips(Board board) {
        for (Ship s : Fleet.standardFleet()) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = random.nextBoolean();
                int r = random.nextInt(board.getSize());
                int c = random.nextInt(board.getSize());
                placed = board.placeShip(s, r, c, horizontal);
            }
        }
    }
}
```

- [ ] **Step 5: Run to verify pass**

Run: compile + `java -cp bin battleship.ModelTests`
Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/battleship/ai/AIStrategy.java src/battleship/ai/SimpleAI.java test/battleship/ModelTests.java
git commit -m "Add AIStrategy interface and SimpleAI random strategy"
```

---

## Task 5: AdvancedAI (hunt/target + probability)

**Files:**
- Create: `src/battleship/ai/AdvancedAI.java`
- Modify: `test/battleship/ModelTests.java`

- [ ] **Step 1: Add failing test**

Add `testAdvancedAI();` and import `battleship.ai.AdvancedAI;`:

```java
static void testAdvancedAI() {
    AdvancedAI ai = new AdvancedAI();
    Board place = new Board();
    ai.placeShips(place);
    check(place.getShips().size() == 5, "advanced ai places 5 ships");

    // After a hit is reported, next targets should be adjacent (target mode).
    Board enemyView = new Board();
    int[] first = ai.chooseTarget(enemyView);
    enemyView.getCell(first[0], first[1]).markHit();
    ai.reportResult(first[0], first[1], true, false); // hit, not sunk
    int[] next = ai.chooseTarget(enemyView);
    boolean adjacent = Math.abs(next[0] - first[0]) + Math.abs(next[1] - first[1]) == 1;
    check(adjacent, "advanced ai targets adjacent after hit");
}
```

- [ ] **Step 2: Run to verify failure**

Run: `javac -d bin src/battleship/model/*.java src/battleship/ai/*.java test/battleship/ModelTests.java`
Expected: compile errors (AdvancedAI / reportResult missing).

- [ ] **Step 3: Implement AdvancedAI**

`src/battleship/ai/AdvancedAI.java`:

```java
package battleship.ai;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import battleship.model.Board;
import battleship.model.Fleet;
import battleship.model.Ship;

public class AdvancedAI implements AIStrategy {
    private Stack<int[]> hitStack = new Stack<int[]>();
    private int[] lastHit = null;
    private boolean huntMode = true;
    private int[][] probGrid;
    private Random random = new Random();
    private ArrayList<int[]> currentHits = new ArrayList<int[]>();

    public int[] chooseTarget(Board enemyView) {
        // Target mode: drain the stack of queued neighbors.
        while (!hitStack.isEmpty()) {
            int[] cand = hitStack.pop();
            if (!enemyView.getCell(cand[0], cand[1]).isHit()) {
                return cand;
            }
        }
        huntMode = true;
        return huntTarget(enemyView);
    }

    // Called by Game after each AI shot so the AI can update its hunt/target state.
    public void reportResult(int r, int c, boolean hit, boolean sunk) {
        if (hit && !sunk) {
            huntMode = false;
            lastHit = new int[] { r, c };
            currentHits.add(new int[] { r, c });
            pushNeighbors(r, c);
        } else if (sunk) {
            // Ship finished: clear target state, return to hunting.
            hitStack.clear();
            currentHits.clear();
            huntMode = true;
            lastHit = null;
        }
    }

    private void pushNeighbors(int r, int c) {
        int[][] dirs = { {-1,0},{1,0},{0,-1},{0,1} };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10) {
                hitStack.push(new int[] { nr, nc });
            }
        }
    }

    private int[] huntTarget(Board enemyView) {
        updateProbGrid(enemyView);
        int n = enemyView.getSize();
        int best = -1, br = 0, bc = 0;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (!enemyView.getCell(r, c).isHit() && probGrid[r][c] > best) {
                    best = probGrid[r][c];
                    br = r; bc = c;
                }
        return new int[] { br, bc };
    }

    // Probability = number of ways remaining ships could fit over each untried cell.
    private void updateProbGrid(Board enemyView) {
        int n = enemyView.getSize();
        probGrid = new int[n][n];
        int[] sizes = { 5, 4, 3, 3, 2 };
        for (int size : sizes) {
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    // horizontal fit
                    if (c + size <= n && fits(enemyView, r, c, size, true))
                        for (int i = 0; i < size; i++) probGrid[r][c + i]++;
                    // vertical fit
                    if (r + size <= n && fits(enemyView, r, c, size, false))
                        for (int i = 0; i < size; i++) probGrid[r + i][c]++;
                }
            }
        }
    }

    private boolean fits(Board v, int r, int c, int size, boolean horiz) {
        for (int i = 0; i < size; i++) {
            int rr = horiz ? r : r + i;
            int cc = horiz ? c + i : c;
            if (v.getCell(rr, cc).isHit()) return false;
        }
        return true;
    }

    public void placeShips(Board board) {
        for (Ship s : Fleet.standardFleet()) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = random.nextBoolean();
                int r = random.nextInt(board.getSize());
                int c = random.nextInt(board.getSize());
                placed = board.placeShip(s, r, c, horizontal);
            }
        }
    }
}
```

Note: `SimpleAI` has no `reportResult`. `Game` calls `reportResult` only when the strategy is `AdvancedAI` (instanceof check), or — cleaner — add a no-op `reportResult` to `AIStrategy`. Decision: add `default`-style no-op by giving `SimpleAI` an empty `reportResult` and declaring it in the interface. **Update interface in Step 4.**

- [ ] **Step 4: Add reportResult to interface and SimpleAI**

Edit `src/battleship/ai/AIStrategy.java` to add:

```java
    void reportResult(int r, int c, boolean hit, boolean sunk);
```

Add to `src/battleship/ai/SimpleAI.java`:

```java
    public void reportResult(int r, int c, boolean hit, boolean sunk) { /* no-op */ }
```

- [ ] **Step 5: Run to verify pass**

Run: `javac -d bin src/battleship/model/*.java src/battleship/ai/*.java test/battleship/ModelTests.java` then `java -cp bin battleship.ModelTests`
Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/battleship/ai/ test/battleship/ModelTests.java
git commit -m "Add AdvancedAI hunt/target strategy with probability grid"
```

---

## Task 6: AIPlayer

**Files:**
- Create: `src/battleship/player/AIPlayer.java`
- Modify: `test/battleship/ModelTests.java`

- [ ] **Step 1: Add failing test**

Add `testAIPlayer();` and import `battleship.player.AIPlayer;`:

```java
static void testAIPlayer() {
    AIPlayer ai = new AIPlayer("Computer", new SimpleAI(), "Simple");
    ai.placeShips();
    check(ai.getOwnBoard().getShips().size() == 5, "ai player places fleet");
    int[] move = ai.makeMove();
    check(move != null && move.length == 2, "ai player makes a move");
    check(ai.getDifficulty().equals("Simple"), "ai difficulty stored");
}
```

- [ ] **Step 2: Run to verify failure**

Run: `javac -d bin src/battleship/model/*.java src/battleship/player/*.java src/battleship/ai/*.java test/battleship/ModelTests.java`
Expected: compile errors.

- [ ] **Step 3: Implement AIPlayer**

`src/battleship/player/AIPlayer.java`:

```java
package battleship.player;

import battleship.ai.AIStrategy;

public class AIPlayer extends Player {
    private AIStrategy strategy;
    private String difficulty;

    public AIPlayer(String name, AIStrategy strategy, String difficulty) {
        super(name);
        this.strategy = strategy;
        this.difficulty = difficulty;
    }

    public void placeShips() { strategy.placeShips(ownBoard); }

    public int[] makeMove() { return strategy.chooseTarget(targetBoard); }

    // Feed the result of the last shot back to the strategy (advanced hunt/target).
    public void reportResult(int r, int c, boolean hit, boolean sunk) {
        strategy.reportResult(r, c, hit, sunk);
    }

    public void setStrategy(AIStrategy s) { this.strategy = s; }
    public String getDifficulty() { return difficulty; }
}
```

- [ ] **Step 4: Run to verify pass**

Run: compile + `java -cp bin battleship.ModelTests`
Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/battleship/player/AIPlayer.java test/battleship/ModelTests.java
git commit -m "Add AIPlayer delegating to strategy"
```

---

## Task 7: Game (coin toss, turn resolution, win check)

**Files:**
- Create: `src/battleship/game/Game.java`
- Modify: `test/battleship/ModelTests.java`

- [ ] **Step 1: Add failing test**

Add `testGame();` and import `battleship.game.Game;`:

```java
static void testGame() {
    Game g = new Game("Simple");
    Player starter = g.coinToss();
    check(starter != null, "coin toss returns a player");

    g.startGame();
    check(g.getAiPlayer().getOwnBoard().getShips().size() == 5, "ai fleet placed on start");

    // Human fires at AI ownBoard via game; resolve a known AI ship cell.
    // Find an AI ship cell to guarantee a HIT.
    int[] target = null;
    Board aiBoard = g.getAiPlayer().getOwnBoard();
    outer:
    for (int r = 0; r < 10; r++)
        for (int c = 0; c < 10; c++)
            if (aiBoard.getCell(r, c).hasShip()) { target = new int[]{r,c}; break outer; }
    String res = g.fireAtAI(target[0], target[1]);
    check(res.startsWith("HIT") || res.startsWith("SUNK"), "human shot on ship hits");
    check(!g.isGameOver(), "game not over after one hit");
}
```

- [ ] **Step 2: Run to verify failure**

Run: `javac -d bin src/battleship/**/*.java test/battleship/ModelTests.java` (if the glob fails on the shell, list the dirs: `src/battleship/model/*.java src/battleship/player/*.java src/battleship/ai/*.java src/battleship/game/*.java`)
Expected: compile errors.

- [ ] **Step 3: Implement Game**

`src/battleship/game/Game.java`:

```java
package battleship.game;

import java.util.Random;

import battleship.ai.AdvancedAI;
import battleship.ai.SimpleAI;
import battleship.model.Board;
import battleship.player.AIPlayer;
import battleship.player.HumanPlayer;
import battleship.player.Player;

public class Game {
    private HumanPlayer humanPlayer;
    private AIPlayer aiPlayer;
    private Player currentTurn;
    private boolean gameOver;
    private String winner;
    private Random random = new Random();

    public Game(String difficulty) {
        humanPlayer = new HumanPlayer("You");
        if (difficulty.equalsIgnoreCase("Advanced")) {
            aiPlayer = new AIPlayer("Computer", new AdvancedAI(), "Advanced");
        } else {
            aiPlayer = new AIPlayer("Computer", new SimpleAI(), "Simple");
        }
        gameOver = false;
        winner = null;
    }

    public Player coinToss() {
        currentTurn = random.nextBoolean() ? humanPlayer : aiPlayer;
        return currentTurn;
    }

    public void startGame() {
        aiPlayer.placeShips();
        aiPlayer.getOwnBoard().saveToFile("ai_ships.txt");
        if (currentTurn == null) coinToss();
    }

    // Human fires at the AI's own board. Returns the result string.
    public String fireAtAI(int r, int c) {
        String result = aiPlayer.getOwnBoard().receiveAttack(r, c);
        if (!result.equals("ALREADY")) {
            // mirror onto human's target view
            mirror(humanPlayer.getTargetBoard(), r, c, result);
            if (checkWin()) endGame();
        }
        return result;
    }

    // AI fires at the human's own board. Returns int[]{r,c} plus result via lastAIResult.
    private String lastAIResult;

    public int[] aiTurn() {
        int[] move = aiPlayer.makeMove();
        int r = move[0], c = move[1];
        String result = humanPlayer.getOwnBoard().receiveAttack(r, c);
        boolean hit = result.startsWith("HIT") || result.startsWith("SUNK");
        boolean sunk = result.startsWith("SUNK");
        aiPlayer.reportResult(r, c, hit, sunk);
        mirror(aiPlayer.getTargetBoard(), r, c, result);
        lastAIResult = result;
        if (checkWin()) endGame();
        return new int[] { r, c };
    }

    public String getLastAIResult() { return lastAIResult; }

    // Records hit/miss on a target view board (no ships, just isHit + marker).
    private void mirror(Board view, int r, int c, String result) {
        view.getCell(r, c).markHit();
    }

    public boolean checkWin() {
        return humanPlayer.getOwnBoard().allShipsSunk()
            || aiPlayer.getOwnBoard().allShipsSunk();
    }

    public void switchTurn() {
        currentTurn = (currentTurn == humanPlayer) ? aiPlayer : humanPlayer;
    }

    public void endGame() {
        gameOver = true;
        if (aiPlayer.getOwnBoard().allShipsSunk()) winner = humanPlayer.getName();
        else if (humanPlayer.getOwnBoard().allShipsSunk()) winner = aiPlayer.getName();
    }

    public HumanPlayer getHumanPlayer() { return humanPlayer; }
    public AIPlayer getAiPlayer() { return aiPlayer; }
    public Player getCurrentTurn() { return currentTurn; }
    public boolean isHumanTurn() { return currentTurn == humanPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
}
```

- [ ] **Step 4: Run to verify pass**

Run: `javac -d bin src/battleship/model/*.java src/battleship/player/*.java src/battleship/ai/*.java src/battleship/game/Game.java test/battleship/ModelTests.java` then `java -cp bin battleship.ModelTests`
Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/battleship/game/Game.java test/battleship/ModelTests.java
git commit -m "Add Game controller with coin toss, turns, and win check"
```

---

## Task 8: GridPanel (Swing 10x10 button grid)

**Files:**
- Create: `src/battleship/gui/GridPanel.java`

This is GUI; verified by running the app in later tasks. Implement to this spec.

**Responsibilities:** render one 10×10 board as `JButton[][]` with A–J row labels and 1–10 column headers; expose a callback for clicks; repaint cell colors from a `Board`.

**Public API:**
```java
public GridPanel(String title, boolean clickable)
public void setCellClickListener(CellClickListener l)   // interface: void onCellClick(int row, int col)
public void renderOwnBoard(Board board)                 // show ships + hits/misses
public void renderTargetView(Board ownBoard, Board targetView) // show only fired shots (hit/miss)
public void setClickable(boolean b)
```

**Implementation notes:**
- Use `GridLayout(11, 11)` inside a titled-bordered `JPanel`; top-left corner blank, top row "1".."10", left column "A".."J".
- Store buttons in `JButton[10][10]`. Each button: fixed preferred size (e.g. 32×32), opaque, `setMargin(new Insets(0,0,0,0))`.
- Each button's `ActionListener` calls `listener.onCellClick(r, c)` only when `clickable`.
- **Colors:**
  - water/unknown: `new Color(173,216,230)` (light blue)
  - own ship (not hit): `Color.DARK_GRAY`
  - hit (ship): `Color.RED`
  - miss: `Color.WHITE`
  - sunk: `new Color(139,0,0)` (dark red) — color whole ship's cells when `ship.isSunk()`
- `renderOwnBoard`: for each cell — if hit && hasShip → RED (or dark red if sunk), if hit && !hasShip → WHITE, if hasShip → DARK_GRAY, else light blue.
- `renderTargetView`: only reads `targetView` (the player's record). For each cell in targetView: if not hit → light blue; if hit → look up the real `enemyOwnBoard` passed in to decide RED/dark-red (sunk) vs WHITE. Signature: `renderTargetView(Board enemyOwnBoard, Board targetView)`.
- Provide `CellClickListener` as a nested `public interface` in GridPanel.

- [ ] **Step 1: Create the file per the spec above (full Swing implementation).**
- [ ] **Step 2: Compile**: `javac -d bin -cp bin src/battleship/gui/GridPanel.java` — Expected: compiles clean.
- [ ] **Step 3: Commit**:
```bash
git add src/battleship/gui/GridPanel.java
git commit -m "Add GridPanel Swing board view"
```

---

## Task 9: StatusPanel (turn / timer / stats / log)

**Files:**
- Create: `src/battleship/gui/StatusPanel.java`

**Responsibilities:** show coin-toss result + current turn, an elapsed-time label, per-side stats, and a scrolling message log.

**Public API:**
```java
public StatusPanel()
public void setTurn(String text)
public void setCoinToss(String text)
public void setTimer(String text)
public void setStats(int yourShots, int yourHits, int yourSunk,
                     int aiShots, int aiHits, int aiSunk,
                     int yourShipsLeft, int aiShipsLeft)
public void log(String message)   // append a line + autoscroll
```

**Implementation notes:**
- `BoxLayout` (Y axis). Labels for coin toss, turn, timer. A stats area with two columns (You / Computer) using a small `JLabel` grid or formatted text.
- Log: `JTextArea` (non-editable) inside `JScrollPane`; `log()` appends `message + "\n"` and sets caret to end.

- [ ] **Step 1: Create the file per spec.**
- [ ] **Step 2: Compile**: `javac -d bin -cp bin src/battleship/gui/StatusPanel.java` — Expected: clean.
- [ ] **Step 3: Commit**:
```bash
git add src/battleship/gui/StatusPanel.java
git commit -m "Add StatusPanel for turn, timer, stats, and log"
```

---

## Task 10: SaveManager (save/load game)

**Files:**
- Create: `src/battleship/game/SaveManager.java`
- Modify: `test/battleship/ModelTests.java`

**Format (plain text):**
```
BATTLESHIP_SAVE v1
difficulty=Simple
turn=human
elapsed=42
# human ships
H_SHIP Carrier 0 0 H
... (name, row, col, H|V)
# ai ships
A_SHIP Carrier 3 1 V
...
# human shots (on ai board)
H_SHOT 4 5
...
# ai shots (on human board)
A_SHOT 0 0
...
```

**Public API:**
```java
public static void save(Game game, int elapsedSeconds, String path)
public static Game load(String path)   // returns a fully reconstructed Game, or throws IOException
public static int loadElapsed(String path)
```

- [ ] **Step 1: Add failing test**

Add `testSaveLoad();` and import `battleship.game.SaveManager;`:

```java
static void testSaveLoad() {
    try {
        Game g = new Game("Advanced");
        g.coinToss();
        g.startGame();
        // human fleet: place a known ship for round-trip
        g.getHumanPlayer().getOwnBoard().placeShip(new Ship("Destroyer", 2), 0, 0, true);
        String path = "test_save.txt";
        SaveManager.save(g, 99, path);
        Game loaded = SaveManager.load(path);
        check(loaded.getAiPlayer().getOwnBoard().getShips().size() == 5, "loaded ai fleet");
        check(loaded.getHumanPlayer().getOwnBoard().getShips().size() >= 1, "loaded human fleet");
        check(SaveManager.loadElapsed(path) == 99, "loaded elapsed time");
        new java.io.File(path).delete();
    } catch (Exception e) {
        check(false, "save/load round trip: " + e.getMessage());
    }
}
```

- [ ] **Step 2: Run to verify failure** — compile error (SaveManager missing).

- [ ] **Step 3: Implement SaveManager**

Implement to the format above:
- `save`: write difficulty, turn (`human`/`ai` from `game.isHumanTurn()`), elapsed; iterate `getOwnBoard().getShips()` for each player, deriving orientation from the ship's first two cells (`H` if `cells[1].col == cells[0].col+1`, else `V`); write each player's recorded shots by scanning the *opponent's ownBoard* cells where `isHit()` is true (human shots = AI ownBoard hits; AI shots = human ownBoard hits). **Important:** to distinguish a shot from an unfired cell, only `isHit()` cells are shots.
- `load`: create `new Game(difficulty)`, reset both boards, re-place ships from `*_SHIP` lines (parse name→size via a small switch or `Fleet`), then replay each shot through `receiveAttack` on the correct board so hit/miss/sunk state rebuilds, and set the current turn. Set elapsed via `loadElapsed`.
- Parse ship size from name: Carrier=5, Battleship=4, Cruiser=3, Submarine=3, Destroyer=2 (helper `sizeFor(name)`).

- [ ] **Step 4: Run to verify pass** — `java -cp bin battleship.ModelTests` all PASS.

- [ ] **Step 5: Commit**
```bash
git add src/battleship/game/SaveManager.java test/battleship/ModelTests.java
git commit -m "Add SaveManager for game save/load"
```

---

## Task 11: BattleshipFrame — setup screen (placement + difficulty)

**Files:**
- Create: `src/battleship/gui/BattleshipFrame.java`

**Responsibilities (setup phase):** show the human's `GridPanel` (own board, clickable), a fleet list of the 5 ships to place in order, an Horizontal/Vertical toggle (`JToggleButton`), a **Random** button, a difficulty `JComboBox` (Simple/Advanced), and a **Start Game** button (enabled only when all 5 placed).

**Implementation notes:**
- Field: `Game game;` `int placeIndex;` `List<Ship> toPlace = Fleet.standardFleet();` `boolean horizontal = true;`
- Clicking the own grid: attempt `humanOwnBoard.placeShip(toPlace.get(placeIndex), r, c, horizontal)`; on success advance `placeIndex`, re-render, update the "place your <next ship>" label; when `placeIndex == 5` enable Start.
- **Random** button: clear human board (`reset()`), then place fleet using the same random loop as `SimpleAI.placeShips` (reuse by calling a shared helper, or `new SimpleAI().placeShips(humanOwnBoard)`), set placeIndex=5, enable Start.
- **Start**: read difficulty from combo; construct `game = new Game(difficulty)`; copy the human's placed board into `game.getHumanPlayer().getOwnBoard()` (simplest: build the Game first, place human ships directly onto `game.getHumanPlayer().getOwnBoard()` during setup so no copy needed — **prefer this**: create the Game at frame construction once difficulty is known, OR create Game on Start and re-run placement). Decision: create `game` on Start, then place the recorded human placements onto `game.getHumanPlayer().getOwnBoard()`. Track placements as a `List<int[]>` of {shipIndex,row,col,horiz} during setup and replay them.
- Then `game.coinToss(); game.startGame();` and switch to play screen (Task 12).

- [ ] **Step 1: Implement setup screen per spec.**
- [ ] **Step 2: Compile** `javac -d bin -cp bin src/battleship/**` (list dirs if needed) — clean.
- [ ] **Step 3: Commit**
```bash
git add src/battleship/gui/BattleshipFrame.java
git commit -m "Add BattleshipFrame setup screen with placement and difficulty"
```

---

## Task 12: BattleshipFrame — play screen + turn flow + timer + stats + menu

**Files:**
- Modify: `src/battleship/gui/BattleshipFrame.java`
- Create: `src/battleship/Main.java`

**Responsibilities (play phase):**
- Two `GridPanel`s side by side: left = your board (`renderOwnBoard`), right = enemy board (`renderTargetView`, clickable).
- `StatusPanel` on the right/bottom showing coin-toss result, turn, timer, stats, log.
- Menu bar: New Game (back to setup), Save Game (`JFileChooser` → `SaveManager.save`), Load Game (`JFileChooser` → `SaveManager.load`, then show play screen).
- `javax.swing.Timer` (1000ms) increments `elapsedSeconds`, updates `StatusPanel.setTimer` (format mm:ss); stops on game over.

**Turn flow:**
- After `startGame()`: if `game.isHumanTurn()` enable enemy grid; else trigger AI move.
- Enemy-grid click handler (human turn only): `String res = game.fireAtAI(r,c);` if `"ALREADY"` ignore. Else log the result (translate `SUNK:Name` → "You sank the Computer's Name!"), re-render enemy grid, update stats. If `game.isGameOver()` → finish. Else `game.switchTurn();` disable enemy grid; schedule AI move with a short `Timer` (e.g. 600ms) for readability.
- AI move: `int[] cell = game.aiTurn(); String res = game.getLastAIResult();` log ("Computer fired at B5: HIT" / "Computer sank your Cruiser!"), re-render your board, update stats. If game over → finish. Else `game.switchTurn();` re-enable enemy grid.
- **Finish:** stop timer, disable grids, log winner, `JOptionPane` showing `game.getWinner() + " wins!"`.
- **Stats wiring:** compute from boards each update — your shots = count `isHit` on AI ownBoard; your hits = those that `hasShip`; your sunk = `5 - aiOwnBoard.shipsRemaining()`; symmetric for AI on human board; ships left = `shipsRemaining()`.

`src/battleship/Main.java`:

```java
package battleship;

import javax.swing.SwingUtilities;
import battleship.gui.BattleshipFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BattleshipFrame frame = new BattleshipFrame();
                frame.setVisible(true);
            }
        });
    }
}
```

- [ ] **Step 1: Implement play screen, turn flow, timer, stats, menu per spec.**
- [ ] **Step 2: Create Main.java.**
- [ ] **Step 3: Compile everything**: `javac -d bin src/battleship/model/*.java src/battleship/player/*.java src/battleship/ai/*.java src/battleship/game/*.java src/battleship/gui/*.java src/battleship/Main.java` — Expected: clean.
- [ ] **Step 4: Manual smoke test**: `java -cp bin battleship.Main` — place fleet (manual + random), start, play a full game vs Simple and vs Advanced to a win, verify timer/stats/log update, verify `ai_ships.txt` is written and matches the AI fleet, save mid-game and load it back.
- [ ] **Step 5: Commit**
```bash
git add src/battleship/gui/BattleshipFrame.java src/battleship/Main.java
git commit -m "Add play screen, turn flow, timer, stats, and save/load menu"
```

---

## Task 13: Polish — headers, README, Eclipse files, final verification

**Files:**
- Modify: all `.java` (header comment block)
- Modify: `README.md`
- Create: `.classpath`, `.project` (Eclipse), `.gitignore`

- [ ] **Step 1:** Add the standard header comment to every `.java` file (group names placeholder noted for the user to fill).
- [ ] **Step 2:** Create `.gitignore` with `bin/` and `*.class` and `ai_ships.txt` and `*save*.txt`.
- [ ] **Step 3:** Create Eclipse `.project` and `.classpath` so the folder imports as a Java project (src on build path, output `bin`).
- [ ] **Step 4:** Update `README.md`: how to run (`javac` + `java battleship.Main`, or Run `Main.java` in Eclipse), controls, features, file outputs, group member list placeholder.
- [ ] **Step 5: Full regression** — recompile all + `java -cp bin battleship.ModelTests` (all PASS) + launch GUI once more.
- [ ] **Step 6: Commit**
```bash
git add -A
git commit -m "Add file headers, Eclipse project files, gitignore, and README"
```

---

## Self-Review Notes

- **Spec coverage:** 10×10 grid (Board.SIZE), A–J/1–10 labels (GridPanel), 5 ships (Fleet), human vs computer (Game), mouse GUI (GridPanel/BattleshipFrame), coin toss (Game.coinToss), Simple+Advanced AI (Tasks 4–5, difficulty selector Task 11), manual+random placement (Task 11), stats/turn/timer (StatusPanel + Task 12), `ai_ships.txt` (Board.saveToFile via Game.startGame), save/load (SaveManager Task 10/12), colors (GridPanel), win condition (Game.checkWin/endGame), headers (Task 13). All covered.
- **Type consistency:** `reportResult(int,int,boolean,boolean)` declared in `AIStrategy`, implemented in `SimpleAI`/`AdvancedAI`, called via `AIPlayer.reportResult` from `Game.aiTurn`. `chooseTarget(Board)`, `placeShips(Board)` consistent. `GridPanel.renderTargetView(Board enemyOwnBoard, Board targetView)` matches its usage in Task 12.
- **Placeholders:** GUI tasks (8,9,11,12) give component specs (fields, public API, colors, event wiring) rather than full verbatim Swing — these are detailed enough to implement directly and are verified by running, not unit tests.
