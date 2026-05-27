/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: ModelTests.java
 *
 * Lightweight assertion harness for the non-GUI game logic. Run with:
 *   java -cp bin battleship.ModelTests
 * Prints PASS/FAIL per check and exits non-zero if anything fails.
 */
package battleship;

import battleship.ai.AdvancedAI;
import battleship.ai.SimpleAI;
import battleship.game.Game;
import battleship.game.SaveManager;
import battleship.model.Board;
import battleship.model.Fleet;
import battleship.model.Cell;
import battleship.model.Ship;
import battleship.player.AIPlayer;
import battleship.player.HumanPlayer;
import battleship.player.Player;

public class ModelTests {
    static int passed = 0, failed = 0;

    static void check(boolean cond, String name) {
        if (cond) { passed++; System.out.println("PASS: " + name); }
        else { failed++; System.out.println("FAIL: " + name); }
    }

    public static void main(String[] args) {
        testCell();
        testShip();
        testBoard();
        testHumanPlayer();
        testSimpleAI();
        testAdvancedAI();
        testAIPlayer();
        testGame();
        testSaveLoad();
        testFullGame();
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

    static void testHumanPlayer() {
        HumanPlayer h = new HumanPlayer("You");
        check(h.getName().equals("You"), "human name");
        check(h.getOwnBoard() != null, "human has own board");
        check(h.getTargetBoard() != null, "human has target board");
        h.handleMouseClick(4, 5);
        int[] move = h.makeMove();
        check(move[0] == 4 && move[1] == 5, "human move from click");
    }

    static void testSimpleAI() {
        SimpleAI ai = new SimpleAI();
        Board enemyView = new Board();
        java.util.HashSet<String> seen = new java.util.HashSet<String>();
        for (int i = 0; i < 100; i++) {
            int[] t = ai.chooseTarget(enemyView);
            seen.add(t[0] + "," + t[1]);
            enemyView.getCell(t[0], t[1]).markHit();
        }
        check(seen.size() == 100, "simple ai never repeats across full board");

        Board place = new Board();
        ai.placeShips(place);
        check(place.getShips().size() == 5, "simple ai places 5 ships");
    }

    static void testAdvancedAI() {
        AdvancedAI ai = new AdvancedAI();
        Board place = new Board();
        ai.placeShips(place);
        check(place.getShips().size() == 5, "advanced ai places 5 ships");

        Board enemyView = new Board();
        int[] first = ai.chooseTarget(enemyView);
        enemyView.getCell(first[0], first[1]).markHit();
        ai.reportResult(first[0], first[1], true, false, 0);
        int[] next = ai.chooseTarget(enemyView);
        boolean adjacent = Math.abs(next[0] - first[0]) + Math.abs(next[1] - first[1]) == 1;
        check(adjacent, "advanced ai targets adjacent after hit");

        check(first[0] == 0 && first[1] == 0, "advanced ai opens at top-left corner");

        AdvancedAI ai2 = new AdvancedAI();
        Board view2 = new Board();
        int[] m1 = ai2.chooseTarget(view2);
        view2.getCell(m1[0], m1[1]).markHit();
        ai2.reportResult(m1[0], m1[1], false, false, 0);
        int[] m2 = ai2.chooseTarget(view2);
        check((m1[0] + m1[1]) % 2 == 0 && (m2[0] + m2[1]) % 2 == 0,
                "advanced ai hunts on parity grid for smallest=2");
    }

    static void testAIPlayer() {
        AIPlayer ai = new AIPlayer("Computer", new SimpleAI(), "Simple");
        ai.placeShips();
        check(ai.getOwnBoard().getShips().size() == 5, "ai player places fleet");
        int[] move = ai.makeMove();
        check(move != null && move.length == 2, "ai player makes a move");
        check(ai.getDifficulty().equals("Simple"), "ai difficulty stored");
    }

    static void testGame() {
        Game g = new Game("Simple");
        Player starter = g.coinToss();
        check(starter != null, "coin toss returns a player");

        g.startGame();
        check(g.getAiPlayer().getOwnBoard().getShips().size() == 5, "ai fleet placed on start");

        int[] target = null;
        Board aiBoard = g.getAiPlayer().getOwnBoard();
        outer:
        for (int r = 0; r < 10; r++)
            for (int c = 0; c < 10; c++)
                if (aiBoard.getCell(r, c).hasShip()) { target = new int[]{r, c}; break outer; }
        String res = g.fireAtAI(target[0], target[1]);
        check(res.startsWith("HIT") || res.startsWith("SUNK"), "human shot on ship hits");
        check(!g.isGameOver(), "game not over after one hit");
    }

    // Play a complete game to a win against the Advanced AI, exercising the
    // full turn flow, hunt/target logic, and win detection end to end.
    static void testFullGame() {
        Game g = new Game("Advanced");
        g.coinToss();
        g.startGame();
        new SimpleAI().placeShips(g.getHumanPlayer().getOwnBoard());

        Board aiBoard = g.getAiPlayer().getOwnBoard();
        int guard = 0;
        for (int r = 0; r < 10 && !g.isGameOver(); r++) {
            for (int c = 0; c < 10 && !g.isGameOver(); c++) {
                if (!aiBoard.getCell(r, c).isHit()) g.fireAtAI(r, c);
                if (!g.isGameOver()) g.aiTurn();
                guard++;
            }
        }
        check(guard <= 100, "full game completes within bounded turns");
        check(g.isGameOver(), "full game reaches game over");
        check(g.getWinner() != null, "full game has a winner");
        check(g.getWinner().equals("You") || g.getWinner().equals("Computer"),
                "winner is a valid player");
    }

    static void testSaveLoad() {
        try {
            Game g = new Game("Advanced");
            g.coinToss();
            g.startGame();
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
}
