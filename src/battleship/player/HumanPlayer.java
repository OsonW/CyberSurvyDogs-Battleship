/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - <Member 1>, <Member 2>, <Member 3>, <Member 4>
 * Date: 2026
 * File: HumanPlayer.java
 *
 * The human-controlled player. Ship placement and firing are driven by
 * mouse clicks in the GUI; handleMouseClick records the cell the player
 * last clicked, and makeMove returns it.
 */
package battleship.player;

public class HumanPlayer extends Player {
    private int[] selectedCell;

    public HumanPlayer(String name) {
        super(name);
        this.selectedCell = null;
    }

    /** Ships are placed via the setup screen directly onto ownBoard. */
    public void placeShips() { /* handled by setup screen */ }

    /** Record the cell the player clicked on the enemy grid. */
    public void handleMouseClick(int row, int col) {
        this.selectedCell = new int[] { row, col };
    }

    public int[] makeMove() { return selectedCell; }
}
