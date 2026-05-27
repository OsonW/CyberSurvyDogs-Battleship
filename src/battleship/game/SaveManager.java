/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: SaveManager.java
 *
 * Saves and loads a game in progress as a plain-text file. The format
 * records the difficulty, whose turn it is, elapsed time, both fleets
 * (with orientation) and every shot fired, so a loaded game resumes
 * exactly where it left off.
 */
package battleship.game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import battleship.model.Board;
import battleship.model.Cell;
import battleship.model.Fleet;
import battleship.model.Ship;

public class SaveManager {

    /** Write the full game state to the given path. */
    public static void save(Game game, int elapsedSeconds, String path) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(path));
            pw.println("BATTLESHIP_SAVE v1");
            pw.println("difficulty=" + game.getAiPlayer().getDifficulty());
            pw.println("turn=" + (game.isHumanTurn() ? "human" : "ai"));
            pw.println("elapsed=" + elapsedSeconds);

            pw.println("# human ships");
            writeShips(pw, "H_SHIP", game.getHumanPlayer().getOwnBoard());
            pw.println("# ai ships");
            writeShips(pw, "A_SHIP", game.getAiPlayer().getOwnBoard());

            pw.println("# human shots (on ai board)");
            writeShots(pw, "H_SHOT", game.getAiPlayer().getOwnBoard());
            pw.println("# ai shots (on human board)");
            writeShots(pw, "A_SHOT", game.getHumanPlayer().getOwnBoard());

            pw.close();
        } catch (IOException e) {
            System.err.println("Could not save game: " + e.getMessage());
        }
    }

    private static void writeShips(PrintWriter pw, String tag, Board board) {
        for (Ship s : board.getShips()) {
            Cell first = s.getCells().get(0);
            Cell second = s.getCells().get(1);
            boolean horizontal = second.getCol() == first.getCol() + 1;
            pw.println(tag + " " + s.getName() + " " + first.getRow() + " "
                    + first.getCol() + " " + (horizontal ? "H" : "V"));
        }
    }

    private static void writeShots(PrintWriter pw, String tag, Board board) {
        for (int r = 0; r < board.getSize(); r++)
            for (int c = 0; c < board.getSize(); c++)
                if (board.getCell(r, c).isHit())
                    pw.println(tag + " " + r + " " + c);
    }

    /** Read elapsed seconds from a save file (0 if absent/unreadable). */
    public static int loadElapsed(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null)
                if (line.startsWith("elapsed="))
                    return Integer.parseInt(line.substring("elapsed=".length()).trim());
        } catch (IOException | NumberFormatException e) {
            // fall through to default
        }
        return 0;
    }

    /** Reconstruct a Game from a save file. */
    public static Game load(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        try {
            String header = br.readLine();
            if (header == null || !header.startsWith("BATTLESHIP_SAVE"))
                throw new IOException("Not a valid Battleship save file.");

            String difficulty = "Simple";
            String turn = "human";
            Board humanBoard = null, aiBoard = null;
            Game game = null;

            String line;
            // We need the Game built before placing ships; read difficulty first
            // by buffering header lines until the first ship line.
            java.util.List<String> lines = new java.util.ArrayList<String>();
            while ((line = br.readLine()) != null) lines.add(line);

            for (String l : lines) {
                if (l.startsWith("difficulty=")) difficulty = l.substring("difficulty=".length()).trim();
                else if (l.startsWith("turn=")) turn = l.substring("turn=".length()).trim();
            }

            game = new Game(difficulty);
            humanBoard = game.getHumanPlayer().getOwnBoard();
            aiBoard = game.getAiPlayer().getOwnBoard();
            humanBoard.reset();
            aiBoard.reset();

            // Place ships.
            for (String l : lines) {
                if (l.startsWith("H_SHIP ")) placeFromLine(humanBoard, l);
                else if (l.startsWith("A_SHIP ")) placeFromLine(aiBoard, l);
            }

            // Replay shots so hit/miss/sunk state and target views rebuild.
            for (String l : lines) {
                if (l.startsWith("H_SHOT ")) {
                    int[] rc = parseShot(l);
                    aiBoard.receiveAttack(rc[0], rc[1]);
                    game.getHumanPlayer().getTargetBoard().getCell(rc[0], rc[1]).markHit();
                } else if (l.startsWith("A_SHOT ")) {
                    int[] rc = parseShot(l);
                    humanBoard.receiveAttack(rc[0], rc[1]);
                    game.getAiPlayer().getTargetBoard().getCell(rc[0], rc[1]).markHit();
                }
            }

            game.setCurrentTurnHuman(turn.equals("human"));
            if (game.checkWin()) game.endGame();
            return game;
        } finally {
            br.close();
        }
    }

    private static void placeFromLine(Board board, String line) {
        // TAG Name row col H|V
        String[] p = line.split("\\s+");
        String name = p[1];
        int row = Integer.parseInt(p[2]);
        int col = Integer.parseInt(p[3]);
        boolean horizontal = p[4].equals("H");
        board.placeShip(new Ship(name, Fleet.sizeFor(name)), row, col, horizontal);
    }

    private static int[] parseShot(String line) {
        String[] p = line.split("\\s+");
        return new int[] { Integer.parseInt(p[1]), Integer.parseInt(p[2]) };
    }
}
