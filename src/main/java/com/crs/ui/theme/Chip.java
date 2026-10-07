package com.crs.ui.theme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/** A small coloured badge, e.g. "CS101", "CGPA 8.5", "Full". */
public class Chip extends JLabel {
    public enum Style { NEUTRAL, PRIMARY, SUCCESS, WARNING, DANGER, ORANGE }

    private Style style;
    private boolean pill;

    public Chip(String text, Style style) {
        super(text);
        setFont(Theme.bold(11));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));
        setStyle(style);
    }

    public Chip mono() {
        setFont(Theme.monoBold(11.5f));
        return this;
    }

    /** Fully rounded ends instead of slightly rounded corners. */
    public Chip pill() {
        pill = true;
        setBorder(BorderFactory.createEmptyBorder(2, 9, 2, 9));
        return this;
    }

    public Chip icon(Icons.Name name) {
        setIcon(Icons.of(name, 12, colors(style)[0]));
        setIconTextGap(4);
        return this;
    }

    public void setStyle(Style style) {
        this.style = style;
        setForeground(colors(style)[0]);
        repaint();
    }

    /** {text, background, border} colours for each style. */
    public static Color[] colors(Style style) {
        return switch (style) {
            case NEUTRAL -> new Color[]{Theme.TEXT_SECONDARY, Theme.SURFACE_MUTED, Theme.BORDER};
            case PRIMARY -> new Color[]{Theme.PRIMARY_HOVER, Theme.PRIMARY_SOFT, Theme.PRIMARY_SOFT_BORDER};
            case SUCCESS -> new Color[]{Theme.SUCCESS_TEXT, Theme.SUCCESS_SOFT, Theme.SUCCESS_BORDER};
            case WARNING -> new Color[]{Theme.WARNING_TEXT, Theme.WARNING_SOFT, Theme.WARNING_BORDER};
            case DANGER -> new Color[]{Theme.DANGER_TEXT, Theme.DANGER_SOFT, Theme.DANGER_BORDER};
            case ORANGE -> new Color[]{Theme.ORANGE_TEXT, Theme.ORANGE_SOFT, Theme.ORANGE_BORDER};
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color[] c = colors(style);
        Graphics2D g2 = Draw.smooth(g);
        Draw.roundRect(g2, 0, 0, getWidth(), getHeight(), pill ? getHeight() / 2.0 : 4, c[1], c[2]);
        g2.dispose();
        super.paintComponent(g);
    }
}
