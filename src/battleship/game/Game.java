/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - <Member 1>, <Member 2>, <Member 3>, <Member 4>
 * Date: 2026
 * File: Game.java
 *
 * Controls overall game flow: builds the two players, performs the coin
 * toss, resolves each side's shots against the opponent's board, tracks
 * whose turn it is, and detects the win condition.
 */
package battleship.game;

import java.util.Random;

import battleship.ai.AdvancedAI;
import battleship.ai.SimpleAI;
import battleship.model.Board;
import battleship.player.AIPlayer;
import battleship.player.HumanPlayer;
import battleship.player.Player;

public class Game {
    private HumanPlayer humanPlayer;
    private AIPlayer aiPlayer;
    private Player currentTurn;
    private boolean gameOver;
    private String winner;
    private String lastAIResult;
    private Random random = new Random();

    public Game(String difficulty) {
        humanPlayer = new HumanPlayer("You");
        if (difficulty.equalsIgnoreCase("Advanced")) {
            aiPlayer = new AIPlayer("Computer", new AdvancedAI(), "Advanced");
        } else {
            aiPlayer = new AIPlayer("Computer", new SimpleAI(), "Simple");
        }
        gameOver = false;
        winner = null;
    }

    /** Randomly choose and store who fires first. */
    public Player coinToss() {
        currentTurn = random.nextBoolean() ? humanPlayer : aiPlayer;
        return currentTurn;
    }

    /** Place the AI fleet, record it to file, and ensure a starting player. */
    public void startGame() {
        aiPlayer.placeShips();
        aiPlayer.getOwnBoard().saveToFile("ai_ships.txt");
        if (currentTurn == null) coinToss();
    }

    /**
     * Human fires at the AI's board.
     * @return "ALREADY", "MISS", "HIT", or "SUNK:<name>".
     */
    public String fireAtAI(int r, int c) {
        String result = aiPlayer.getOwnBoard().receiveAttack(r, c);
        if (!result.equals("ALREADY")) {
            humanPlayer.getTargetBoard().getCell(r, c).markHit();
            if (checkWin()) endGame();
        }
        return result;
    }

    /**
     * AI takes its turn, firing at the human's board.
     * @return the {row, col} the AI fired at. The result string is available
     *         via getLastAIResult().
     */
    public int[] aiTurn() {
        int[] move = aiPlayer.makeMove();
        int r = move[0], c = move[1];
        String result = humanPlayer.getOwnBoard().receiveAttack(r, c);
        boolean hit = result.startsWith("HIT") || result.startsWith("SUNK");
        boolean sunk = result.startsWith("SUNK");
        aiPlayer.reportResult(r, c, hit, sunk);
        aiPlayer.getTargetBoard().getCell(r, c).markHit();
        lastAIResult = result;
        if (checkWin()) endGame();
        return new int[] { r, c };
    }

    public String getLastAIResult() { return lastAIResult; }

    public boolean checkWin() {
        return humanPlayer.getOwnBoard().allShipsSunk()
            || aiPlayer.getOwnBoard().allShipsSunk();
    }

    public void switchTurn() {
        currentTurn = (currentTurn == humanPlayer) ? aiPlayer : humanPlayer;
    }

    public void endGame() {
        gameOver = true;
        if (aiPlayer.getOwnBoard().allShipsSunk()) winner = humanPlayer.getName();
        else if (humanPlayer.getOwnBoard().allShipsSunk()) winner = aiPlayer.getName();
    }

    /** Used by SaveManager to restore whose turn it is after loading. */
    public void setCurrentTurnHuman(boolean human) {
        currentTurn = human ? humanPlayer : aiPlayer;
    }

    public HumanPlayer getHumanPlayer() { return humanPlayer; }
    public AIPlayer getAiPlayer() { return aiPlayer; }
    public Player getCurrentTurn() { return currentTurn; }
    public boolean isHumanTurn() { return currentTurn == humanPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
}
