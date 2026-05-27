/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: AdvancedAI.java
 *
 * The expert computer opponent. It uses two modes:
 *   HUNT   - when no ship is partially hit, it fires at the cell with the
 *            highest probability of containing a ship (computed by counting
 *            how many ways remaining ships could fit over each untried cell).
 *   TARGET - after a hit, it queues the hit's neighbours and fires at them
 *            until the ship is sunk, then returns to hunting.
 */
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
        // Target mode: work through queued neighbours of known hits.
        while (!hitStack.isEmpty()) {
            int[] cand = hitStack.pop();
            if (!enemyView.getCell(cand[0], cand[1]).isHit()) {
                return cand;
            }
        }
        huntMode = true;
        return huntTarget(enemyView);
    }

    public void reportResult(int r, int c, boolean hit, boolean sunk) {
        if (sunk) {
            // Finished a ship: drop target state and resume hunting.
            hitStack.clear();
            currentHits.clear();
            huntMode = true;
            lastHit = null;
        } else if (hit) {
            huntMode = false;
            lastHit = new int[] { r, c };
            currentHits.add(new int[] { r, c });
            pushNeighbors(r, c);
        }
    }

    private void pushNeighbors(int r, int c) {
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < Board.SIZE && nc >= 0 && nc < Board.SIZE) {
                hitStack.push(new int[] { nr, nc });
            }
        }
    }

    /** @return the highest-probability untried cell for hunting. */
    private int[] huntTarget(Board enemyView) {
        updateProbGrid(enemyView);
        int n = enemyView.getSize();
        int best = -1, br = 0, bc = 0;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (!enemyView.getCell(r, c).isHit() && probGrid[r][c] > best) {
                    best = probGrid[r][c];
                    br = r;
                    bc = c;
                }
        return new int[] { br, bc };
    }

    /** Probability density: how many ways could each remaining ship fit over each cell. */
    private void updateProbGrid(Board enemyView) {
        int n = enemyView.getSize();
        probGrid = new int[n][n];
        int[] sizes = { 5, 4, 3, 3, 2 };
        for (int size : sizes) {
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    if (c + size <= n && fits(enemyView, r, c, size, true))
                        for (int i = 0; i < size; i++) probGrid[r][c + i]++;
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
