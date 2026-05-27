/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: AdvancedAI.java
 *
 * The expert computer opponent. It uses two modes:
 *   HUNT   - no unsunk hits known. Sweeps the board diagonally from the
 *            top-left corner, restricted to a parity pattern tuned to the
 *            smallest remaining ship: spacing = smallestRemaining - 1.
 *            (Smallest = 2 -> every 2nd cell along an anti-diagonal;
 *             smallest = 3 -> every 3rd; etc.) This guarantees every ship
 *            of length >= smallest must overlap at least one swept cell,
 *            so the AI cannot miss any ship while hunting. As ships are
 *            sunk and the smallest changes, the parity widens automatically.
 *   TARGET - one or more unsunk hits exist. Builds a probability density
 *            grid restricted to placements that cover the unsunk hits,
 *            weighting placements exponentially by how many unsunk hits
 *            they explain. Fires at the highest-scoring untried cell so
 *            it optimally finishes the ship under attack.
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
        if (!unsunkHits.isEmpty()) {
            int[] t = bestTargetCell();
            if (t != null) return t;
        }
        return diagonalHuntCell();
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
     * Hunt by sweeping anti-diagonals from the top-left corner, restricted
     * to the parity grid (r + c) % smallest == 0. Falls back to any untried
     * cell (still in diagonal order) if the parity grid is exhausted.
     */
    private int[] diagonalHuntCell() {
        int n = Board.SIZE;
        int s = smallestRemaining();
        int[] parityHit = sweep(n, s);
        if (parityHit != null) return parityHit;
        int[] anyHit = sweep(n, 1);
        if (anyHit != null) return anyHit;
        return new int[] { 0, 0 };
    }

    /** Visit cells in anti-diagonal order from (0,0); return first untried cell with (r+c) % step == 0. */
    private int[] sweep(int n, int step) {
        for (int d = 0; d <= 2 * (n - 1); d++) {
            if (d % step != 0) continue;
            int rStart = Math.max(0, d - (n - 1));
            int rEnd = Math.min(n - 1, d);
            for (int r = rStart; r <= rEnd; r++) {
                int c = d - r;
                if (!knownHit[r][c] && !knownMiss[r][c]) {
                    return new int[] { r, c };
                }
            }
        }
        return null;
    }

    private int smallestRemaining() {
        int min = Integer.MAX_VALUE;
        for (Integer sz : remainingSizes) if (sz.intValue() < min) min = sz.intValue();
        return min == Integer.MAX_VALUE ? 1 : min;
    }

    /**
     * Target mode: build a probability density grid over all remaining ship
     * placements that are consistent with what we know AND cover at least
     * one unsunk hit. Placements covering more unsunk hits get exponentially
     * more weight (10^covered) so the AI strongly prefers shots that finish
     * the ship currently under attack.
     */
    private int[] bestTargetCell() {
        int n = Board.SIZE;
        long[][] prob = new long[n][n];

        for (Integer sizeBox : remainingSizes) {
            int s = sizeBox.intValue();
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    if (c + s <= n) {
                        int covered = placementCoverage(r, c, s, true);
                        if (covered > 0) {
                            long w = pow10(covered);
                            for (int i = 0; i < s; i++) {
                                if (!knownHit[r][c + i] && !knownMiss[r][c + i]) {
                                    prob[r][c + i] += w;
                                }
                            }
                        }
                    }
                    if (r + s <= n) {
                        int covered = placementCoverage(r, c, s, false);
                        if (covered > 0) {
                            long w = pow10(covered);
                            for (int i = 0; i < s; i++) {
                                if (!knownHit[r + i][c] && !knownMiss[r + i][c]) {
                                    prob[r + i][c] += w;
                                }
                            }
                        }
                    }
                }
            }
        }

        long best = 0;
        int br = -1, bc = -1;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (knownHit[r][c] || knownMiss[r][c]) continue;
                if (prob[r][c] > best) {
                    best = prob[r][c];
                    br = r;
                    bc = c;
                }
            }
        }
        if (br < 0) return null;
        return new int[] { br, bc };
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
