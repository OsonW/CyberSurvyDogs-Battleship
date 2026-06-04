/*
 * Battleship Tournament - ICS4U Final Summative
 * Group: CyberSurvyDogs - Olin Wang, Oson Wang, Willey Yao
 * Date: 2026
 * File: UITheme.java
 *
 * Central place for the application's modern look: installs the Nimbus
 * look-and-feel with a tuned palette, and exposes shared colours, fonts,
 * spacing, and small helpers so every screen stays visually consistent.
 */
package battleship.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

public final class UITheme {

    private UITheme() { }

    // ----- Palette -----
    public static final Color APP_BG     = new Color(0xEC, 0xF1, 0xF5);
    public static final Color PANEL_BG   = new Color(0xF7, 0xFA, 0xFC);
    public static final Color ACCENT     = new Color(0x1C, 0x6E, 0x8C); // teal-blue
    public static final Color ACCENT_DK  = new Color(0x14, 0x53, 0x6B);
    public static final Color TEXT       = new Color(0x22, 0x2B, 0x32);
    public static final Color TEXT_MUTED = new Color(0x5C, 0x6B, 0x77);

    // ----- Fonts -----
    public static final Font FONT_BASE  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD  = FONT_BASE.deriveFont(Font.BOLD, 13f);
    public static final Font FONT_LABEL = FONT_BASE.deriveFont(Font.BOLD, 14f);
    public static final Font FONT_TITLE = FONT_BASE.deriveFont(Font.BOLD, 22f);

    // ----- Spacing -----
    public static final int PAD = 12;
    public static final int GAP = 8;

    /** Install Nimbus and apply the palette/font globally. Call once at startup. */
    public static void apply() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", PANEL_BG);
            UIManager.put("background", PANEL_BG);
            UIManager.put("nimbusBase", ACCENT_DK);
            UIManager.put("nimbusBlueGrey", new Color(0xC9, 0xD4, 0xDD));
            UIManager.put("nimbusFocus", ACCENT);
            UIManager.put("nimbusSelectionBackground", ACCENT);
            UIManager.put("text", TEXT);
            UIManager.getLookAndFeelDefaults().put("defaultFont", FONT_BASE);
        } catch (Exception ignored) {
            // Fall back to the default look-and-feel silently.
        }
    }

    /** Empty border with uniform padding on all sides. */
    public static Border pad(int all) {
        return BorderFactory.createEmptyBorder(all, all, all, all);
    }

    /** Titled section border with a little inner breathing room. */
    public static Border section(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                pad(GAP));
    }

    /** Give a button the accented "primary action" emphasis. */
    public static void primary(JButton b) {
        // Basic UI keeps a solid, readable fill under Nimbus even when disabled,
        // instead of a stray blue-outlined empty box.
        b.setUI(new BasicButtonUI());
        b.setFont(FONT_LABEL);
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        quiet(b);
    }

    /** Remove the focus ring / tab outline (and keep the button out of tab order). */
    public static void quiet(AbstractButton b) {
        b.setFocusPainted(false);
        b.setFocusable(false);
    }

    /** Mark a component opaque with the given background (handy under Nimbus). */
    public static void fill(JComponent c, Color bg) {
        c.setOpaque(true);
        c.setBackground(bg);
    }
}
