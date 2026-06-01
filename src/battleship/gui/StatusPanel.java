/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: StatusPanel.java
 *
 * Side panel showing the coin-toss result, whose turn it is, the elapsed
 * timer, live statistics for both sides, and a scrolling message log.
 */
package battleship.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class StatusPanel extends JPanel {
    private static final int WIDTH = 250;

    private JLabel coinTossLabel = new JLabel("Coin toss: -");
    private JLabel turnLabel = new JLabel("Turn: -");
    private JLabel timerLabel = new JLabel("Time: 00:00");
    private JLabel statsLabel = new JLabel();
    private JTextArea log = new JTextArea(12, 22);

    public StatusPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(UITheme.section("Status"));
        UITheme.fill(this, UITheme.PANEL_BG);
        setPreferredSize(new Dimension(WIDTH + 2 * UITheme.PAD, 0));

        coinTossLabel.setFont(UITheme.FONT_LABEL);
        turnLabel.setFont(UITheme.FONT_LABEL);
        timerLabel.setFont(UITheme.FONT_LABEL);
        coinTossLabel.setForeground(UITheme.ACCENT_DK);
        statsLabel.setFont(UITheme.FONT_BASE);

        coinTossLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        turnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(coinTossLabel);
        add(javax.swing.Box.createVerticalStrut(UITheme.GAP / 2));
        add(turnLabel);
        add(javax.swing.Box.createVerticalStrut(UITheme.GAP / 2));
        add(timerLabel);
        add(javax.swing.Box.createVerticalStrut(UITheme.PAD));
        add(statsLabel);
        add(javax.swing.Box.createVerticalStrut(UITheme.PAD));

        JLabel logTitle = new JLabel("Game Log:");
        logTitle.setFont(UITheme.FONT_BOLD);
        logTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(logTitle);
        add(javax.swing.Box.createVerticalStrut(UITheme.GAP / 2));

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(UITheme.FONT_BASE);
        JScrollPane scroll = new JScrollPane(log);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(WIDTH, 240));
        scroll.setMaximumSize(new Dimension(WIDTH, 600));
        add(scroll);

        setStats(0, 0, 0, 0, 0, 0, 5, 5);
    }

    public void setTurn(String text) { turnLabel.setText("Turn: " + text); }
    public void setCoinToss(String text) { coinTossLabel.setText("Coin toss: " + text); }
    public void setTimer(String text) { timerLabel.setText("Time: " + text); }

    public void setStats(int yourShots, int yourHits, int yourSunk,
                         int aiShots, int aiHits, int aiSunk,
                         int yourShipsLeft, int aiShipsLeft) {
        statsLabel.setText("<html>"
            + "<b>You</b> &nbsp; shots: " + yourShots + " &nbsp; hits: " + yourHits
            + " &nbsp; sunk: " + yourSunk + " &nbsp; ships left: " + yourShipsLeft + "<br>"
            + "<b>Computer</b> &nbsp; shots: " + aiShots + " &nbsp; hits: " + aiHits
            + " &nbsp; sunk: " + aiSunk + " &nbsp; ships left: " + aiShipsLeft
            + "</html>");
    }

    public void log(String message) {
        log.append(message + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    public void clearLog() { log.setText(""); }
}
