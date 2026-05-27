/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: Player.java
 *
 * Abstract base for a Battleship participant. Each player owns the board
 * holding their own fleet, and a target board recording the shots they
 * have fired at the opponent (hit/miss history).
 */
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

    /** Arrange this player's fleet on ownBoard. */
    public abstract void placeShips();

    /** @return the {row, col} this player wants to fire at next. */
    public abstract int[] makeMove();

    public String getName() { return name; }
    public Board getOwnBoard() { return ownBoard; }
    public Board getTargetBoard() { return targetBoard; }
}
