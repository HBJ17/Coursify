package com.crs.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * The Coursify design tokens: colours, fonts and corner radius, in one place.
 * Every screen uses these constants, so changing the look means changing this file only.
 */
public final class Theme {
    // Brand (indigo)
    public static final Color PRIMARY = new Color(0x4F46E5);
    public static final Color PRIMARY_HOVER = new Color(0x4338CA);
    public static final Color PRIMARY_PRESSED = new Color(0x3730A3);
    public static final Color PRIMARY_SOFT = new Color(0xEEF2FF);
    public static final Color PRIMARY_SOFT_BORDER = new Color(0xC7D2FE);

    // Surfaces
    public static final Color BG = new Color(0xF8FAFC);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_MUTED = new Color(0xF1F5F9);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color BORDER_STRONG = new Color(0xCBD5E1);
    public static final Color DIVIDER = new Color(0xF1F5F9);

    // Text
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color TEXT_SECONDARY = new Color(0x334155);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color FAINT = new Color(0x94A3B8);

    // Status colours: main, text, soft background, border
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color SUCCESS_TEXT = new Color(0x047857);
    public static final Color SUCCESS_SOFT = new Color(0xECFDF5);
    public static final Color SUCCESS_BORDER = new Color(0xA7F3D0);

    public static final Color WARNING = new Color(0xD97706);
    public static final Color WARNING_TEXT = new Color(0x92400E);
    public static final Color WARNING_SOFT = new Color(0xFFFBEB);
    public static final Color WARNING_BORDER = new Color(0xFCD34D);

    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_TEXT = new Color(0xB91C1C);
    public static final Color DANGER_SOFT = new Color(0xFEF2F2);
    public static final Color DANGER_BORDER = new Color(0xFECACA);

    public static final Color ORANGE = new Color(0xEA580C);
    public static final Color ORANGE_TEXT = new Color(0x9A3412);
    public static final Color ORANGE_SOFT = new Color(0xFFF7ED);
    public static final Color ORANGE_BORDER = new Color(0xFED7AA);

    public static final int RADIUS = 10;
    public static final int RADIUS_SMALL = 6;

    private static final String SANS = pickFamily(Font.SANS_SERIF, "Inter", "Segoe UI", "Helvetica Neue", "Arial");
    private static final String MONO = pickFamily(Font.MONOSPACED, "JetBrains Mono", "Cascadia Mono", "Consolas", "Menlo");

    private Theme() { }

    public static Font font(float size) { return new Font(SANS, Font.PLAIN, 12).deriveFont(size); }
    public static Font bold(float size) { return new Font(SANS, Font.BOLD, 12).deriveFont(size); }
    public static Font mono(float size) { return new Font(MONO, Font.PLAIN, 12).deriveFont(size); }
    public static Font monoBold(float size) { return new Font(MONO, Font.BOLD, 12).deriveFont(size); }

    /** Small uppercase section labels ("ACADEMIC") with a little extra letter spacing. */
    public static Font label(float size) {
        return bold(size).deriveFont(Map.of(TextAttribute.TRACKING, 0.06f));
    }

    /** First installed font family from the wish list, otherwise the logical fallback. */
    private static String pickFamily(String fallback, String... wanted) {
        try {
            List<String> installed = Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            for (String family : wanted) {
                if (installed.contains(family)) return family;
            }
        } catch (RuntimeException ignored) {
            // no font list available (unusual); use the fallback
        }
        return fallback;
    }

    /**
     * Call once at start-up, before any window is built. Uses the cross-platform look and feel
     * so the app looks the same on every PC, then swaps in our fonts and colours.
     */
    public static void install() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignored) {
            // keep whatever look and feel is active
        }

        FontUIResource base = new FontUIResource(font(13));
        for (Object key : UIManager.getLookAndFeelDefaults().keySet().toArray()) {
            if (UIManager.get(key) instanceof FontUIResource) UIManager.put(key, base);
        }

        put("Panel.background", BG);
        put("Label.foreground", TEXT);
        put("OptionPane.background", SURFACE);
        put("Viewport.background", SURFACE);
        put("ScrollPane.background", SURFACE);
        put("Table.background", SURFACE);
        put("Table.foreground", TEXT);
        put("Table.selectionBackground", PRIMARY_SOFT);
        put("Table.selectionForeground", TEXT);
        put("Table.gridColor", DIVIDER);
        put("TableHeader.background", BG);
        put("TextField.selectionBackground", PRIMARY_SOFT_BORDER);
        put("TextField.caretForeground", PRIMARY);
        put("PasswordField.selectionBackground", PRIMARY_SOFT_BORDER);
        put("ComboBox.background", SURFACE);
        put("ComboBox.selectionBackground", PRIMARY_SOFT);
        put("ComboBox.selectionForeground", TEXT);
        put("List.selectionBackground", PRIMARY_SOFT);
        put("List.selectionForeground", TEXT);
        put("ToolTip.background", TEXT);
        put("ToolTip.foreground", SURFACE);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder()));
        UIManager.put("PopupMenu.border", new BorderUIResource(BorderFactory.createLineBorder(BORDER)));
        UIManager.put("ScrollBarUI", ThinScrollBarUI.class.getName());
        UIManager.put("ScrollBar.width", 10);
    }

    private static void put(String key, Color color) {
        UIManager.put(key, new ColorUIResource(color));
    }
}
