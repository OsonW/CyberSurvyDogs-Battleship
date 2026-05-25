/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - <Member 1>, <Member 2>, <Member 3>, <Member 4>
 * Date: 2026
 * File: Ship.java
 *
 * Represents one ship: its name, length, the cells it occupies, and how
 * many of those cells have been hit. A ship is sunk once all its cells
 * have been hit.
 */
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

    /** Register a hit on this ship; marks it sunk once hits reach its size. */
    public void hit() {
        hits++;
        if (hits >= size) sunk = true;
    }

    public boolean isSunk() { return sunk; }
    public String getName() { return name; }
    public int getSize() { return size; }
    public int getHits() { return hits; }
    public ArrayList<Cell> getCells() { return cells; }

    /** Link a cell to this ship (called during placement). */
    public void addCell(Cell c) { cells.add(c); }
}
