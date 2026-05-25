/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - <Member 1>, <Member 2>, <Member 3>, <Member 4>
 * Date: 2026
 * File: AIPlayer.java
 *
 * Computer-controlled player. Delegates fleet placement and target
 * selection to a pluggable AIStrategy (SimpleAI or AdvancedAI) and feeds
 * shot results back to the strategy so it can adapt.
 */
package battleship.player;

import battleship.ai.AIStrategy;

public class AIPlayer extends Player {
    private AIStrategy strategy;
    private String difficulty;

    public AIPlayer(String name, AIStrategy strategy, String difficulty) {
        super(name);
        this.strategy = strategy;
        this.difficulty = difficulty;
    }

    public void placeShips() { strategy.placeShips(ownBoard); }

    public int[] makeMove() { return strategy.chooseTarget(targetBoard); }

    /** Pass the result of the last shot to the strategy (advanced hunt/target). */
    public void reportResult(int r, int c, boolean hit, boolean sunk) {
        strategy.reportResult(r, c, hit, sunk);
    }

    public void setStrategy(AIStrategy s) { this.strategy = s; }
    public String getDifficulty() { return difficulty; }
}
