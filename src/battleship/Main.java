/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - <Member 1>, <Member 2>, <Member 3>, <Member 4>
 * Date: 2026
 * File: Main.java
 *
 * Program entry point. Launches the Swing GUI on the Event Dispatch Thread.
 */
package battleship;

import javax.swing.SwingUtilities;

import battleship.gui.BattleshipFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BattleshipFrame frame = new BattleshipFrame();
                frame.setVisible(true);
            }
        });
    }
}
