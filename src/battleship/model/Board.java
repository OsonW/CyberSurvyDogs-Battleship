/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - <Member 1>, <Member 2>, <Member 3>, <Member 4>
 * Date: 2026
 * File: Board.java
 *
 * A 10x10 grid of Cells. Handles ship placement (with bounds/overlap
 * validation), resolving incoming attacks, tracking when all ships are
 * sunk, and writing a human-readable ship map to a file.
 */
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

    /**
     * Place a ship starting at (r,c) extending right (horizontal) or down.
     * @return false if the placement is out of bounds or overlaps another ship.
     */
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

    /**
     * Resolve an attack on (r,c).
     * @return "ALREADY" if already fired, "MISS", "HIT", or "SUNK:<name>".
     */
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

    /** @return true if there is at least one ship and every ship is sunk. */
    public boolean allShipsSunk() {
        if (ships.isEmpty()) return false;
        for (Ship s : ships) if (!s.isSunk()) return false;
        return true;
    }

    /** @return how many ships are still afloat. */
    public int shipsRemaining() {
        int n = 0;
        for (Ship s : ships) if (!s.isSunk()) n++;
        return n;
    }

    public Cell getCell(int r, int c) { return grid[r][c]; }
    public ArrayList<Ship> getShips() { return ships; }
    public int getSize() { return SIZE; }

    /** Clear all ships and fired state, returning the board to empty. */
    public void reset() {
        ships.clear();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                grid[r][c].setShip(null);
                grid[r][c].clearHit();
            }
    }

    /**
     * Write a human-readable map of this board's ships plus each ship's
     * coordinates to the given path. Used to record the AI's placement so
     * it can be printed and verified while playing.
     */
    public void saveToFile(String path) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(path));
            pw.println("Battleship - AI Ship Placement");
            pw.println("Legend: C=Carrier  B=Battleship  R=Cruiser  S=Submarine  D=Destroyer");
            pw.println();
            pw.print("    ");
            for (int c = 1; c <= SIZE; c++) pw.print(String.format("%-3d", c));
            pw.println();
            for (int r = 0; r < SIZE; r++) {
                pw.print((char) ('A' + r) + "   ");
                for (int c = 0; c < SIZE; c++) {
                    Cell cell = grid[r][c];
                    char ch = cell.hasShip() ? symbolFor(cell.getShip().getName()) : '.';
                    pw.print(ch + "  ");
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

    private char symbolFor(String name) {
        switch (name) {
            case "Carrier": return 'C';
            case "Battleship": return 'B';
            case "Cruiser": return 'R';
            case "Submarine": return 'S';
            case "Destroyer": return 'D';
            default: return '#';
        }
    }
}
