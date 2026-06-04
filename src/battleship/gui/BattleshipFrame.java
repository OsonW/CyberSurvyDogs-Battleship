/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: BattleshipFrame.java
 *
 * Main application window. Drives two screens via a CardLayout:
 *   SETUP - the player places their fleet (click to place, toggle
 *           orientation, or use Random) and chooses the AI difficulty.
 *   PLAY  - two boards side by side; clicking the enemy board fires. The
 *           frame orchestrates the turn flow, timer, live stats, message
 *           log, and the New/Save/Load menu.
 */
package battleship.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import battleship.ai.SimpleAI;
import battleship.game.Game;
import battleship.game.SaveManager;
import battleship.model.Board;
import battleship.model.Cell;
import battleship.model.Fleet;
import battleship.model.Ship;

public class BattleshipFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    // ----- Setup screen state -----
    private GridPanel setupGrid;
    private Board humanSetupBoard;
    private List<Ship> fleetToPlace;
    private int placeIndex;
    private boolean horizontal = true;
    private JLabel placeInstruction;
    private JButton startButton;
    private JButton orientButton;
    private JComboBox<String> difficultyBox;
    private JComboBox<String> whoStartsBox;

    // ----- Play screen state -----
    private Game game;
    private GridPanel yourGrid;
    private GridPanel enemyGrid;
    private StatusPanel statusPanel;
    private Timer clockTimer;
    private int elapsedSeconds;
    private String coinTossMessage; // status text describing how the starter was decided

    public BattleshipFrame() {
        super("Battleship Tournament - CyberSurvyDogs");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(buildMenuBar());

        root.add(buildSetupPanel(), "setup");
        root.add(buildPlayPanel(), "play");

        JPanel shell = new JPanel(new BorderLayout());
        UITheme.fill(shell, UITheme.APP_BG);
        shell.add(buildHeader(), BorderLayout.NORTH);
        shell.add(root, BorderLayout.CENTER);
        UITheme.fill(root, UITheme.APP_BG);
        setContentPane(shell);

        installRotateKey();
        resetSetup();
        cards.show(root, "setup");
        pack();
        setLocationRelativeTo(null);
    }

    /** Bind the R key to rotate ship orientation while the setup screen is showing. */
    private void installRotateKey() {
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotate");
        root.getActionMap().put("rotate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (orientButton != null && orientButton.isShowing()) {
                    orientButton.doClick();
                }
            }
        });
    }

    // =================== Menu ===================

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Game");

        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> {
            stopTimer();
            resetSetup();
            cards.show(root, "setup");
        });

        JMenuItem save = new JMenuItem("Save Game");
        save.addActionListener(e -> saveGame());

        JMenuItem load = new JMenuItem("Load Game");
        load.addActionListener(e -> loadGame());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        menu.add(newGame);
        menu.add(save);
        menu.add(load);
        menu.addSeparator();
        menu.add(exit);
        bar.add(menu);
        return bar;
    }

    // =================== Header ===================

    /** A slim app bar shown above both screens. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        UITheme.fill(header, UITheme.ACCENT_DK);
        header.setBorder(BorderFactory.createEmptyBorder(10, UITheme.PAD, 10, UITheme.PAD));

        JLabel title = new JLabel("Battleship Tournament");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("Cyber Survy Dogs", SwingConstants.RIGHT);
        sub.setFont(UITheme.FONT_BASE);
        sub.setForeground(new Color(0xC9, 0xDC, 0xE6));
        header.add(sub, BorderLayout.EAST);
        return header;
    }

    // =================== Setup screen ===================

    private JPanel buildSetupPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.PAD, UITheme.PAD));
        panel.setBorder(UITheme.pad(UITheme.PAD));
        UITheme.fill(panel, UITheme.APP_BG);

        setupGrid = new GridPanel("Place Your Fleet", true);
        setupGrid.setCellClickListener((r, c) -> handleSetupClick(r, c));

        // Center the grid at its fixed preferred size so it never stretches
        // (e.g. when the controls panel width changes).
        JPanel gridHolder = new JPanel(new GridBagLayout());
        UITheme.fill(gridHolder, UITheme.APP_BG);
        gridHolder.add(setupGrid);
        panel.add(gridHolder, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(0, 1, UITheme.GAP, UITheme.GAP));
        controls.setBorder(UITheme.section("Setup"));
        UITheme.fill(controls, UITheme.PANEL_BG);

        placeInstruction = new JLabel();
        placeInstruction.setFont(UITheme.FONT_BOLD);
        controls.add(placeInstruction);

        orientButton = new JButton("Orientation: Horizontal");
        UITheme.quiet(orientButton);
        orientButton.addActionListener(e -> {
            horizontal = !horizontal;
            orientButton.setText("Orientation: " + (horizontal ? "Horizontal" : "Vertical"));
            refreshPlacementPreview();
        });
        controls.add(orientButton);

        JLabel rotateHint = new JLabel("Press R to rotate");
        rotateHint.setFont(UITheme.FONT_BASE);
        rotateHint.setForeground(UITheme.TEXT_MUTED);
        controls.add(rotateHint);

        JButton randomButton = new JButton("Random Placement");
        UITheme.quiet(randomButton);
        randomButton.addActionListener(e -> randomPlacement());
        controls.add(randomButton);

        JButton resetButton = new JButton("Reset Placement");
        UITheme.quiet(resetButton);
        resetButton.addActionListener(e -> resetSetup());
        controls.add(resetButton);

        controls.add(new JLabel("AI Difficulty:"));
        difficultyBox = new JComboBox<>(new String[] { "Simple", "Advanced" });
        difficultyBox.setSelectedItem("Advanced");
        controls.add(difficultyBox);

        controls.add(new JLabel("Who Starts:"));
        whoStartsBox = new JComboBox<>(new String[] { "Me", "AI", "Coin Toss" });
        whoStartsBox.setSelectedItem("Coin Toss");
        controls.add(whoStartsBox);

        startButton = new JButton("Start Game");
        startButton.setEnabled(false);
        UITheme.primary(startButton);
        startButton.addActionListener(e -> startGame());
        controls.add(startButton);

        JPanel east = new JPanel(new BorderLayout());
        UITheme.fill(east, UITheme.APP_BG);
        east.add(controls, BorderLayout.NORTH);
        // Pin a consistent width so changing label text never reflows the layout.
        controls.setPreferredSize(new Dimension(240, controls.getPreferredSize().height));
        panel.add(east, BorderLayout.EAST);
        return panel;
    }

    private void resetSetup() {
        humanSetupBoard = new Board();
        fleetToPlace = Fleet.standardFleet();
        placeIndex = 0;
        if (startButton != null) startButton.setEnabled(false);
        if (setupGrid != null) {
            setupGrid.renderOwnBoard(humanSetupBoard);
            refreshPlacementPreview();
        }
        updateInstruction();
    }

    /** Push the current ship's size + orientation into the setup grid's preview. */
    private void refreshPlacementPreview() {
        if (setupGrid == null) return;
        if (placeIndex >= fleetToPlace.size()) {
            setupGrid.clearPlacementPreview();
        } else {
            Ship next = fleetToPlace.get(placeIndex);
            setupGrid.setPlacementPreview(humanSetupBoard, next.getSize(), horizontal);
        }
    }

    private void updateInstruction() {
        String body;
        if (placeIndex >= fleetToPlace.size()) {
            body = "All ships placed. Press Start Game.";
        } else {
            Ship s = fleetToPlace.get(placeIndex);
            body = "Place: " + s.getName() + " (size " + s.getSize() + ")";
        }
        // Fixed width + HTML wrapping keeps the panel from reflowing on text changes.
        placeInstruction.setText("<html><div style='width:200px'>" + body + "</div></html>");
    }

    private void handleSetupClick(int r, int c) {
        if (placeIndex >= fleetToPlace.size()) return;
        Ship current = fleetToPlace.get(placeIndex);
        Ship copy = new Ship(current.getName(), current.getSize());
        if (humanSetupBoard.placeShip(copy, r, c, horizontal)) {
            placeIndex++;
            setupGrid.renderOwnBoard(humanSetupBoard);
            refreshPlacementPreview();
            updateInstruction();
            if (placeIndex >= fleetToPlace.size()) startButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid placement (out of bounds or overlapping). Try again.");
        }
    }

    private void randomPlacement() {
        humanSetupBoard = new Board();
        new SimpleAI().placeShips(humanSetupBoard);
        placeIndex = fleetToPlace.size();
        // Clear the preview first: it still references the previous (now
        // discarded) board and would otherwise repaint over the new ships.
        setupGrid.clearPlacementPreview();
        setupGrid.renderOwnBoard(humanSetupBoard);
        updateInstruction();
        startButton.setEnabled(true);
    }

    private void startGame() {
        String difficulty = (String) difficultyBox.getSelectedItem();
        game = new Game(difficulty);
        transferFleet(humanSetupBoard, game.getHumanPlayer().getOwnBoard());

        String whoStarts = (String) whoStartsBox.getSelectedItem();
        if ("Me".equals(whoStarts)) {
            game.setCurrentTurnHuman(true);
            coinTossMessage = "You chose to start";
        } else if ("AI".equals(whoStarts)) {
            game.setCurrentTurnHuman(false);
            coinTossMessage = "Computer starts";
        } else { // Coin Toss
            CoinTossDialog dialog = new CoinTossDialog(this);
            dialog.setVisible(true); // modal: blocks until the player begins
            boolean humanStart = dialog.isHumanStart();
            game.setCurrentTurnHuman(humanStart);
            coinTossMessage = humanStart ? "You win the toss!" : "Computer wins the toss";
        }

        game.startGame();
        beginPlay(true);
    }

    /** Copy ships (name, position, orientation) from one board onto another. */
    private void transferFleet(Board from, Board to) {
        for (Ship s : from.getShips()) {
            Cell first = s.getCells().get(0);
            Cell second = s.getCells().get(1);
            boolean horiz = second.getCol() == first.getCol() + 1;
            to.placeShip(new Ship(s.getName(), s.getSize()),
                    first.getRow(), first.getCol(), horiz);
        }
    }

    // =================== Play screen ===================

    private JPanel buildPlayPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.PAD, UITheme.PAD));
        panel.setBorder(UITheme.pad(UITheme.PAD));
        UITheme.fill(panel, UITheme.APP_BG);

        JPanel boards = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.PAD + 4, 0));
        UITheme.fill(boards, UITheme.APP_BG);
        yourGrid = new GridPanel("Your Fleet", false);
        enemyGrid = new GridPanel("Enemy Waters (click to fire)", true);
        enemyGrid.setCellClickListener((r, c) -> handleEnemyClick(r, c));
        boards.add(yourGrid);
        boards.add(enemyGrid);
        panel.add(boards, BorderLayout.CENTER);

        statusPanel = new StatusPanel();
        panel.add(statusPanel, BorderLayout.EAST);
        return panel;
    }

    /** Enter the play screen for a freshly started game. */
    private void beginPlay(boolean freshGame) {
        cards.show(root, "play");
        if (freshGame) {
            elapsedSeconds = 0;
            statusPanel.clearLog();
            boolean humanFirst = game.isHumanTurn();
            statusPanel.setCoinToss(coinTossMessage != null ? coinTossMessage
                    : (humanFirst ? "You win the toss!" : "Computer wins the toss"));
            statusPanel.log(humanFirst ? "You go first." : "Computer goes first.");
        }
        renderAll();
        startTimer();
        pack();
        setLocationRelativeTo(null);

        if (game.isGameOver()) {
            finishGame();
            return;
        }
        if (game.isHumanTurn()) {
            statusPanel.setTurn("You");
            enemyGrid.setClickable(true);
        } else {
            statusPanel.setTurn("Computer");
            enemyGrid.setClickable(false);
            scheduleAIMove();
        }
    }

    private void handleEnemyClick(int r, int c) {
        if (game == null || game.isGameOver() || !game.isHumanTurn()) return;
        String res = game.fireAtAI(r, c);
        if (res.equals("ALREADY")) return;

        logHumanShot(r, c, res);
        renderAll();

        if (game.isGameOver()) { finishGame(); return; }
        game.switchTurn();
        statusPanel.setTurn("Computer");
        enemyGrid.setClickable(false);
        scheduleAIMove();
    }

    private void scheduleAIMove() {
        Timer t = new Timer(650, null);
        t.setRepeats(false);
        t.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doAIMove(); }
        });
        t.start();
    }

    private void doAIMove() {
        int[] cell = game.aiTurn();
        String res = game.getLastAIResult();
        logAIShot(cell[0], cell[1], res);
        renderAll();

        if (game.isGameOver()) { finishGame(); return; }
        game.switchTurn();
        statusPanel.setTurn("You");
        enemyGrid.setClickable(true);
    }

    private void finishGame() {
        stopTimer();
        enemyGrid.setClickable(false);
        String winner = game.getWinner();
        statusPanel.setTurn("Game over");
        statusPanel.log("=== " + winner + " wins! ===");
        JOptionPane.showMessageDialog(this, winner + " wins!", "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void renderAll() {
        yourGrid.renderOwnBoard(game.getHumanPlayer().getOwnBoard());
        enemyGrid.renderTargetView(game.getAiPlayer().getOwnBoard(),
                game.getHumanPlayer().getTargetBoard());
        updateStats();
    }

    private void updateStats() {
        Board ai = game.getAiPlayer().getOwnBoard();
        Board human = game.getHumanPlayer().getOwnBoard();
        int yourShots = countShots(ai), yourHits = countHits(ai);
        int aiShots = countShots(human), aiHits = countHits(human);
        statusPanel.setStats(
                yourShots, yourHits, 5 - ai.shipsRemaining(),
                aiShots, aiHits, 5 - human.shipsRemaining(),
                human.shipsRemaining(), ai.shipsRemaining());
    }

    private int countShots(Board b) {
        int n = 0;
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                if (b.getCell(r, c).isHit()) n++;
        return n;
    }

    private int countHits(Board b) {
        int n = 0;
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++)
                if (b.getCell(r, c).isHit() && b.getCell(r, c).hasShip()) n++;
        return n;
    }

    private void logHumanShot(int r, int c, String res) {
        String at = coord(r, c);
        if (res.startsWith("SUNK:")) statusPanel.log("You sank the Computer's " + res.substring(5) + "!");
        else if (res.equals("HIT")) statusPanel.log("You fired at " + at + ": HIT");
        else statusPanel.log("You fired at " + at + ": miss");
    }

    private void logAIShot(int r, int c, String res) {
        String at = coord(r, c);
        if (res.startsWith("SUNK:")) statusPanel.log("Computer sank your " + res.substring(5) + "!");
        else if (res.equals("HIT")) statusPanel.log("Computer fired at " + at + ": HIT");
        else statusPanel.log("Computer fired at " + at + ": miss");
    }

    private String coord(int r, int c) {
        return "" + (char) ('A' + r) + (c + 1);
    }

    // =================== Timer ===================

    private void startTimer() {
        stopTimer();
        statusPanel.setTimer(formatTime(elapsedSeconds));
        clockTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                elapsedSeconds++;
                statusPanel.setTimer(formatTime(elapsedSeconds));
            }
        });
        clockTimer.start();
    }

    private void stopTimer() {
        if (clockTimer != null) clockTimer.stop();
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    // =================== Save / Load ===================

    private void saveGame() {
        if (game == null) {
            JOptionPane.showMessageDialog(this, "Start a game before saving.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("battleship_save.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            SaveManager.save(game, elapsedSeconds, chooser.getSelectedFile().getPath());
            statusPanel.log("Game saved.");
        }
    }

    private void loadGame() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getPath();
        try {
            game = SaveManager.load(path);
            elapsedSeconds = SaveManager.loadElapsed(path);
            statusPanel.clearLog();
            statusPanel.setCoinToss("Loaded game");
            statusPanel.log("Loaded game from file.");
            beginPlay(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load game: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
