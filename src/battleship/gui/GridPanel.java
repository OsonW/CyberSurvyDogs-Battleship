/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: GridPanel.java
 *
 * A Swing view of one 10x10 board built from a 2D array of JButtons, with
 * A-J row labels and 1-10 column headers. It renders cell state with colour
 * and reports clicks to a listener. Two instances are used: one for the
 * player's own board and one for the enemy (target) board.
 */
package battleship.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import battleship.model.Board;
import battleship.model.Cell;

public class GridPanel extends JPanel {

    /** Notified when the user clicks a playable cell. */
    public interface CellClickListener {
        void onCellClick(int row, int col);
    }

    private static final Color WATER = new Color(96, 150, 186);
    private static final Color SHIP = new Color(90, 90, 90);
    private static final Color HIT = new Color(200, 40, 40);
    private static final Color SUNK = new Color(120, 0, 0);
    private static final Color MISS = new Color(230, 230, 235);
    private static final Color PREVIEW_VALID = new Color(80, 200, 100);
    private static final Color PREVIEW_INVALID = new Color(220, 70, 70);

    private final JButton[][] buttons = new JButton[Board.SIZE][Board.SIZE];
    private CellClickListener listener;
    private boolean clickable;

    // Placement preview state. previewSize > 0 means a preview is active.
    private Board previewBoard;
    private int previewSize;
    private boolean previewHorizontal = true;
    private int hoverRow = -1, hoverCol = -1;

    public GridPanel(String title, boolean clickable) {
        this.clickable = clickable;
        setBorder(BorderFactory.createTitledBorder(title));
        setLayout(new GridLayout(Board.SIZE + 1, Board.SIZE + 1, 1, 1));

        // Header row: blank corner + column numbers 1..10.
        add(corner(""));
        for (int c = 1; c <= Board.SIZE; c++) add(corner(String.valueOf(c)));

        for (int r = 0; r < Board.SIZE; r++) {
            add(corner(String.valueOf((char) ('A' + r))));
            for (int c = 0; c < Board.SIZE; c++) {
                final int rr = r, cc = c;
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(34, 34));
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setBackground(WATER);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setFocusPainted(false);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (GridPanel.this.clickable && listener != null)
                            listener.onCellClick(rr, cc);
                    }
                });
                b.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (previewSize > 0 && previewBoard != null) {
                            hoverRow = rr;
                            hoverCol = cc;
                            renderOwnBoard(previewBoard);
                        }
                    }
                });
                buttons[r][c] = b;
                add(b);
            }
        }
    }

    private JLabel corner(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        return l;
    }

    public void setCellClickListener(CellClickListener l) { this.listener = l; }

    public void setClickable(boolean b) { this.clickable = b; }

    /**
     * Turn on a green/red ship preview that follows the mouse. Pass size = 0
     * (or call clearPlacementPreview) to turn it off.
     */
    public void setPlacementPreview(Board board, int size, boolean horizontal) {
        this.previewBoard = board;
        this.previewSize = size;
        this.previewHorizontal = horizontal;
        if (board != null) renderOwnBoard(board);
    }

    /** Disable the placement preview and clear any active overlay. */
    public void clearPlacementPreview() {
        this.previewSize = 0;
        this.hoverRow = -1;
        this.hoverCol = -1;
        Board last = this.previewBoard;
        this.previewBoard = null;
        if (last != null) renderOwnBoard(last);
    }

    /** Render the player's own board: ships are visible, plus incoming hits/misses. */
    public void renderOwnBoard(Board board) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Cell cell = board.getCell(r, c);
                JButton b = buttons[r][c];
                b.setText("");
                if (cell.isHit() && cell.hasShip()) {
                    b.setBackground(cell.getShip().isSunk() ? SUNK : HIT);
                    b.setText("X");
                } else if (cell.isHit()) {
                    b.setBackground(MISS);
                    b.setText("o");
                } else if (cell.hasShip()) {
                    b.setBackground(SHIP);
                } else {
                    b.setBackground(WATER);
                }
            }
        }
        applyPreviewOverlay();
    }

    /** Paint a green/red overlay where the current ship would land at the hovered cell. */
    private void applyPreviewOverlay() {
        if (previewSize <= 0 || previewBoard == null || hoverRow < 0 || hoverCol < 0) return;
        boolean valid = true;
        for (int i = 0; i < previewSize; i++) {
            int rr = previewHorizontal ? hoverRow : hoverRow + i;
            int cc = previewHorizontal ? hoverCol + i : hoverCol;
            if (rr < 0 || rr >= Board.SIZE || cc < 0 || cc >= Board.SIZE
                    || previewBoard.getCell(rr, cc).hasShip()) {
                valid = false;
                break;
            }
        }
        Color color = valid ? PREVIEW_VALID : PREVIEW_INVALID;
        for (int i = 0; i < previewSize; i++) {
            int rr = previewHorizontal ? hoverRow : hoverRow + i;
            int cc = previewHorizontal ? hoverCol + i : hoverCol;
            if (rr >= 0 && rr < Board.SIZE && cc >= 0 && cc < Board.SIZE) {
                buttons[rr][cc].setBackground(color);
            }
        }
    }

    /**
     * Render the enemy board from the player's point of view: only cells the
     * player has fired at are revealed. enemyOwnBoard supplies the truth for
     * hit/miss/sunk colouring; targetView records which cells were fired.
     */
    public void renderTargetView(Board enemyOwnBoard, Board targetView) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                JButton b = buttons[r][c];
                b.setText("");
                if (!targetView.getCell(r, c).isHit()) {
                    b.setBackground(WATER);
                } else {
                    Cell truth = enemyOwnBoard.getCell(r, c);
                    if (truth.hasShip()) {
                        b.setBackground(truth.getShip().isSunk() ? SUNK : HIT);
                        b.setText("X");
                    } else {
                        b.setBackground(MISS);
                        b.setText("o");
                    }
                }
            }
        }
    }
}
