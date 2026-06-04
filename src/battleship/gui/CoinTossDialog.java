/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: CoinTossDialog.java
 *
 * Modal coin-toss dialog. The player calls heads or tails, a flip animation
 * plays, the coin lands on a random side, and the result decides who starts:
 * a correct call means the player goes first, otherwise the computer does.
 */
package battleship.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class CoinTossDialog extends JDialog {

    private final Random random = new Random();

    private final CoinFace coin = new CoinFace();
    private final JLabel message = new JLabel("Call it!", SwingConstants.CENTER);
    private final JButton headsButton = new JButton("Heads");
    private final JButton tailsButton = new JButton("Tails");
    private final JButton beginButton = new JButton("Begin Battle");

    private boolean playerCalledHeads;
    private boolean landedHeads;
    private boolean humanStart;

    public CoinTossDialog(Frame owner) {
        super(owner, "Coin Toss", true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(UITheme.PAD, UITheme.PAD));
        content.setBorder(UITheme.pad(UITheme.PAD + 4));
        UITheme.fill(content, UITheme.PANEL_BG);

        // Fixed-width, two-line-capable message area so the result never clips.
        message.setFont(UITheme.FONT_LABEL.deriveFont(15f));
        message.setForeground(UITheme.ACCENT_DK);
        message.setPreferredSize(new Dimension(400, 60));
        content.add(message, BorderLayout.NORTH);

        coin.setPreferredSize(new Dimension(400, 170));
        UITheme.fill(coin, UITheme.PANEL_BG);
        content.add(coin, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.PAD, UITheme.GAP));
        UITheme.fill(buttons, UITheme.PANEL_BG);
        headsButton.addActionListener(e -> call(true));
        tailsButton.addActionListener(e -> call(false));
        beginButton.addActionListener(e -> dispose());
        UITheme.quiet(headsButton);
        UITheme.quiet(tailsButton);
        UITheme.primary(beginButton);
        beginButton.setVisible(false);
        buttons.add(headsButton);
        buttons.add(tailsButton);
        buttons.add(beginButton);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Whether the human player starts. Valid once the dialog has closed. */
    public boolean isHumanStart() {
        return humanStart;
    }

    private void call(boolean heads) {
        playerCalledHeads = heads;
        headsButton.setEnabled(false);
        tailsButton.setEnabled(false);
        message.setText("Flipping... you called " + (heads ? "Heads" : "Tails"));
        startFlip();
    }

    /** Spin the coin face for a couple of seconds, then settle on a result. */
    private void startFlip() {
        landedHeads = random.nextBoolean();
        // ~28 ticks; interval grows so the spin visibly slows before landing.
        Timer timer = new Timer(40, null);
        final int[] tick = { 0 };
        final int totalTicks = 28;
        timer.addActionListener(e -> {
            tick[0]++;
            coin.setSpinFrame(tick[0]);
            timer.setDelay(40 + tick[0] * 6);
            if (tick[0] >= totalTicks) {
                timer.stop();
                land();
            }
        });
        timer.start();
    }

    private void land() {
        coin.setResult(landedHeads);
        humanStart = (landedHeads == playerCalledHeads);
        String face = landedHeads ? "HEADS" : "TAILS";
        String called = playerCalledHeads ? "Heads" : "Tails";
        message.setText("<html><center>It's " + face
                + "! &nbsp; You called " + called + "<br>"
                + (humanStart ? "You start!" : "Computer starts.") + "</center></html>");
        headsButton.setVisible(false);
        tailsButton.setVisible(false);
        beginButton.setVisible(true);
        beginButton.requestFocusInWindow();
    }

    /** Draws a coin showing H/T, with a horizontal squish to fake a spin. */
    private static class CoinFace extends JPanel {
        private boolean showingHeads = true;
        private int spinFrame = -1; // -1 = static (not spinning)

        CoinFace() {
            setBackground(Color.WHITE);
        }

        void setSpinFrame(int frame) {
            this.spinFrame = frame;
            this.showingHeads = (frame % 2 == 0);
            repaint();
        }

        void setResult(boolean heads) {
            this.spinFrame = -1;
            this.showingHeads = heads;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 20;
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            // Squish horizontally while spinning to suggest a flipping coin.
            double squish = 1.0;
            if (spinFrame >= 0) {
                squish = Math.abs(Math.cos(spinFrame * 0.8));
                squish = 0.15 + 0.85 * squish;
            }
            int w = (int) (size * squish);
            int h = size;
            int x = cx - w / 2;
            int y = cy - h / 2;

            g2.setColor(new Color(0xD4, 0xAF, 0x37)); // gold
            g2.fillOval(x, y, w, h);
            g2.setColor(new Color(0xB8, 0x97, 0x20));
            g2.drawOval(x, y, w, h);

            // Only draw the letter when the coin is reasonably face-on.
            if (w > size * 0.5) {
                String letter = showingHeads ? "H" : "T";
                g2.setColor(new Color(0x5A, 0x3D, 0x00));
                Font f = getFont().deriveFont(Font.BOLD, size * 0.5f);
                g2.setFont(f);
                int sw = g2.getFontMetrics().stringWidth(letter);
                int ascent = g2.getFontMetrics().getAscent();
                g2.drawString(letter, cx - sw / 2, cy + ascent / 2 - 4);
            }
        }
    }
}
