/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: Cell.java
 *
 * Represents a single square on a 10x10 board. Tracks its coordinates,
 * whether it has been fired upon, and the ship (if any) occupying it.
 */
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

    /** Mark this cell as having been fired upon. */
    public void markHit() { this.hit = true; }

    /** @return true if this cell has been fired upon. */
    public boolean isHit() { return hit; }

    /** Reset the fired state (used when resetting a board). */
    public void clearHit() { this.hit = false; }

    /** @return true if a ship occupies this cell. */
    public boolean hasShip() { return hasShip; }

    /** Attach (or clear, if null) the ship occupying this cell. */
    public void setShip(Ship s) { this.ship = s; this.hasShip = (s != null); }

    public Ship getShip() { return ship; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
