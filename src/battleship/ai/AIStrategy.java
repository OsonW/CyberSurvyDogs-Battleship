/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: AIStrategy.java
 *
 * Strategy interface for computer behaviour: how it places its fleet,
 * how it chooses where to fire, and how it reacts to the result of a shot.
 */
package battleship.ai;

import battleship.model.Board;

public interface AIStrategy {
    /** @param enemyView the AI's record of shots fired at the opponent. @return {row, col} to fire at. */
    int[] chooseTarget(Board enemyView);

    /** Arrange a full fleet on the given board. */
    void placeShips(Board board);

    /** Inform the strategy of the outcome of its last shot so it can adapt. */
    void reportResult(int r, int c, boolean hit, boolean sunk);
}
