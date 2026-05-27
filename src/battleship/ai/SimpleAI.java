/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: SimpleAI.java
 *
 * The basic computer opponent. It fires at random previously-untried
 * cells and places its fleet randomly. It does not adapt to hits.
 */
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

    /** @return a random cell that has not been fired upon yet. */
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

    /** Simple AI ignores results; it has no hunt/target behaviour. */
    public void reportResult(int r, int c, boolean hit, boolean sunk, int sunkSize) { /* no-op */ }
}
