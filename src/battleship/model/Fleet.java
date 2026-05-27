/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: Fleet.java
 *
 * Factory for the standard 5-ship Battleship fleet. Each call returns a
 * fresh set of Ship objects so each board owns its own instances.
 */
package battleship.model;

import java.util.ArrayList;
import java.util.List;

public class Fleet {
    /** @return a new standard fleet: Carrier 5, Battleship 4, Cruiser 3, Submarine 3, Destroyer 2. */
    public static List<Ship> standardFleet() {
        List<Ship> ships = new ArrayList<Ship>();
        ships.add(new Ship("Carrier", 5));
        ships.add(new Ship("Battleship", 4));
        ships.add(new Ship("Cruiser", 3));
        ships.add(new Ship("Submarine", 3));
        ships.add(new Ship("Destroyer", 2));
        return ships;
    }

    /** @return the standard size for a ship of the given name (used when loading saves). */
    public static int sizeFor(String name) {
        switch (name) {
            case "Carrier": return 5;
            case "Battleship": return 4;
            case "Cruiser": return 3;
            case "Submarine": return 3;
            case "Destroyer": return 2;
            default: return 0;
        }
    }
}
