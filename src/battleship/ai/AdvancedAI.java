/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: AdvancedAI.java
 *
 * The expert computer opponent. Every shot is the cell most likely to hold a
 * ship, scored by probability density instead of any fixed scan order: for
 * each remaining ship, count its legal placements (those not hitting a known
 * miss or sunk cell) and add weight to the untried cells they cover, then fire
 * at the highest-weighted cell, breaking ties at random.
 *
 *   HUNT   (no unsunk hits): every placement counts as 1, so density is how
 *          many ships could sit on a cell. Parity and edge effects emerge on
 *          their own.
 *   TARGET (unsunk hits exist): only placements covering an unsunk hit count,
 *          weighted 10^(hits covered) to finish the ship under attack.
 */
package battleship.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import battleship.model.Board;
import battleship.model.Fleet;
import battleship.model.Ship;

public class AdvancedAI implements AIStrategy {
    private boolean[][] knownHit;
    private boolean[][] knownMiss;
    private List<int[]> unsunkHits;
    private List<Integer> remainingSizes;
    private Random random;

    public AdvancedAI() {
        knownHit = new boolean[Board.SIZE][Board.SIZE];
        knownMiss = new boolean[Board.SIZE][Board.SIZE];
        unsunkHits = new ArrayList<int[]>();
        remainingSizes = new ArrayList<Integer>(Arrays.asList(5, 4, 3, 3, 2));
        random = new Random();
    }

    public int[] chooseTarget(Board enemyView) {
        return bestByDensity();
    }

    public void reportResult(int r, int c, boolean hit, boolean sunk, int sunkSize) {
        if (hit) {
            knownHit[r][c] = true;
            unsunkHits.add(new int[] { r, c });
        } else {
            knownMiss[r][c] = true;
        }
        if (sunk && sunkSize > 0) {
            remainingSizes.remove(Integer.valueOf(sunkSize));
            removeSunkShipCells(r, c, sunkSize);
        }
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

    /**
     * Build the density grid and return a random untried cell of maximum
     * density (the optimal shot). HUNT and TARGET differ only in which
     * placements count and how they are weighted; see {@link #accumulate}.
     */
    private int[] bestByDensity() {
        int n = Board.SIZE;
        boolean targeting = !unsunkHits.isEmpty();
        long[][] prob = new long[n][n];

        for (Integer sizeBox : remainingSizes) {
            int s = sizeBox.intValue();
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    if (c + s <= n) accumulate(prob, r, c, s, true, targeting);
                    if (r + s <= n) accumulate(prob, r, c, s, false, targeting);
                }
            }
        }

        // Pick a random cell among those of maximum weight (all equally optimal).
        long best = 0;
        List<int[]> bestCells = new ArrayList<int[]>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (knownHit[r][c] || knownMiss[r][c]) continue;
                long p = prob[r][c];
                if (p > best) {
                    best = p;
                    bestCells.clear();
                    bestCells.add(new int[] { r, c });
                } else if (p == best && p > 0) {
                    bestCells.add(new int[] { r, c });
                }
            }
        }
        if (bestCells.isEmpty()) return firstUntried();
        return bestCells.get(random.nextInt(bestCells.size()));
    }

    /**
     * Add one ship placement's contribution to the density grid. Skips
     * impossible placements (overlapping a miss or sunk cell). In TARGET mode
     * ignores placements covering no unsunk hit; otherwise weights the
     * placement 10^(hits covered), which is 1 in HUNT mode.
     */
    private void accumulate(long[][] prob, int r, int c, int s, boolean horiz, boolean targeting) {
        int covered = placementCoverage(r, c, s, horiz);
        if (covered < 0) return;
        if (targeting && covered == 0) return;
        long w = pow10(covered);
        for (int i = 0; i < s; i++) {
            int rr = horiz ? r : r + i;
            int cc = horiz ? c + i : c;
            if (!knownHit[rr][cc] && !knownMiss[rr][cc]) prob[rr][cc] += w;
        }
    }

    /** Last-resort fallback: the first untried cell in row-major order. */
    private int[] firstUntried() {
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                if (!knownHit[r][c] && !knownMiss[r][c]) return new int[] { r, c };
        return new int[] { 0, 0 };
    }

    /**
     * @return -1 if the placement is impossible (overlaps a miss or a sunk
     *         ship cell), else the number of unsunk hits it would explain.
     */
    private int placementCoverage(int r, int c, int size, boolean horiz) {
        int covered = 0;
        for (int i = 0; i < size; i++) {
            int rr = horiz ? r : r + i;
            int cc = horiz ? c + i : c;
            if (knownMiss[rr][cc]) return -1;
            if (knownHit[rr][cc]) {
                if (isUnsunkHit(rr, cc)) covered++;
                else return -1;
            }
        }
        return covered;
    }

    private boolean isUnsunkHit(int r, int c) {
        for (int[] h : unsunkHits) if (h[0] == r && h[1] == c) return true;
        return false;
    }

    private long pow10(int e) {
        long v = 1L;
        for (int i = 0; i < e; i++) v *= 10L;
        return v;
    }

    /**
     * A SUNK shot at (r, c) means a straight line of `size` consecutive hits
     * passing through (r, c) was the just-sunk ship. Identify that line and
     * drop its cells from unsunkHits so target mode no longer chases them.
     */
    private void removeSunkShipCells(int r, int c, int size) {
        int hLen = 1 + consecHits(r, c, 0, -1) + consecHits(r, c, 0, 1);
        int vLen = 1 + consecHits(r, c, -1, 0) + consecHits(r, c, 1, 0);

        int dr, dc;
        if (hLen >= size && vLen >= size) {
            int hUnsunk = bestUnsunkLine(r, c, 0, 1, size);
            int vUnsunk = bestUnsunkLine(r, c, 1, 0, size);
            if (hUnsunk >= vUnsunk) { dr = 0; dc = 1; } else { dr = 1; dc = 0; }
        } else if (hLen >= size) { dr = 0; dc = 1; }
        else if (vLen >= size) { dr = 1; dc = 0; }
        else if (hLen >= vLen) { dr = 0; dc = 1; }
        else { dr = 1; dc = 0; }

        int bestK = 0;
        int bestUnsunk = -1;
        for (int k = 0; k < size; k++) {
            int sr = r - k * dr;
            int sc = c - k * dc;
            int er = sr + (size - 1) * dr;
            int ec = sc + (size - 1) * dc;
            if (sr < 0 || sc < 0 || er >= Board.SIZE || ec >= Board.SIZE) continue;
            int unsunkCount = 0;
            boolean allHit = true;
            for (int i = 0; i < size; i++) {
                int rr = sr + i * dr;
                int cc = sc + i * dc;
                if (!knownHit[rr][cc]) { allHit = false; break; }
                if (isUnsunkHit(rr, cc)) unsunkCount++;
            }
            if (allHit && unsunkCount > bestUnsunk) {
                bestUnsunk = unsunkCount;
                bestK = k;
            }
        }

        int sr = r - bestK * dr;
        int sc = c - bestK * dc;
        for (int i = 0; i < size; i++) {
            removeFromUnsunk(sr + i * dr, sc + i * dc);
        }
    }

    /** Max unsunk-hit count over any valid `size`-cell window through (r,c) along (dr,dc). */
    private int bestUnsunkLine(int r, int c, int dr, int dc, int size) {
        int best = -1;
        for (int k = 0; k < size; k++) {
            int sr = r - k * dr;
            int sc = c - k * dc;
            int er = sr + (size - 1) * dr;
            int ec = sc + (size - 1) * dc;
            if (sr < 0 || sc < 0 || er >= Board.SIZE || ec >= Board.SIZE) continue;
            int unsunkCount = 0;
            boolean allHit = true;
            for (int i = 0; i < size; i++) {
                int rr = sr + i * dr;
                int cc = sc + i * dc;
                if (!knownHit[rr][cc]) { allHit = false; break; }
                if (isUnsunkHit(rr, cc)) unsunkCount++;
            }
            if (allHit && unsunkCount > best) best = unsunkCount;
        }
        return best;
    }

    private int consecHits(int r, int c, int dr, int dc) {
        int count = 0;
        int rr = r + dr, cc = c + dc;
        while (rr >= 0 && cc >= 0 && rr < Board.SIZE && cc < Board.SIZE && knownHit[rr][cc]) {
            count++;
            rr += dr;
            cc += dc;
        }
        return count;
    }

    private void removeFromUnsunk(int r, int c) {
        Iterator<int[]> it = unsunkHits.iterator();
        while (it.hasNext()) {
            int[] h = it.next();
            if (h[0] == r && h[1] == c) { it.remove(); return; }
        }
    }
}
