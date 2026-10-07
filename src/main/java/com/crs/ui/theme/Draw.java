package com.crs.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/** Small painting helpers shared by the custom components. */
public final class Draw {
    private Draw() { }

    /** A copy of g with smooth shapes and text. Remember to dispose() it. */
    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    public static RoundRectangle2D round(double x, double y, double w, double h, double radius) {
        return new RoundRectangle2D.Double(x, y, w, h, radius * 2, radius * 2);
    }

    /** Fills (and optionally outlines) a rounded rectangle. A null colour skips that part. */
    public static void roundRect(Graphics2D g, double x, double y, double w, double h, double radius,
                                 Color fill, Color stroke) {
        if (fill != null) {
            g.setColor(fill);
            g.fill(round(x, y, w, h, radius));
        }
        if (stroke != null) {
            g.setColor(stroke);
            g.draw(round(x + 0.5, y + 0.5, w - 1, h - 1, radius));
        }
    }

    /** Draws text vertically centred on centerY. Returns the text width. */
    public static int text(Graphics2D g, String text, Font font, Color color, int x, int centerY) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x, centerY + (fm.getAscent() - fm.getDescent()) / 2);
        return fm.stringWidth(text);
    }

    public static int width(Graphics2D g, String text, Font font) {
        return g.getFontMetrics(font).stringWidth(text);
    }

    /** Paints a small chip (badge) and returns its width, so the caller can place the next one. */
    public static int chip(Graphics2D g, String text, Font font, Chip.Style style, int x, int centerY) {
        Color[] c = Chip.colors(style);
        int w = width(g, text, font) + 12;
        int h = g.getFontMetrics(font).getHeight() + 2;
        roundRect(g, x, centerY - h / 2.0, w, h, 4, c[1], c[2]);
        text(g, text, font, c[0], x + 6, centerY);
        return w;
    }

    /** Text shortened with "..." so it fits in maxWidth. */
    public static String fit(Graphics2D g, String text, Font font, int maxWidth) {
        FontMetrics fm = g.getFontMetrics(font);
        if (fm.stringWidth(text) <= maxWidth) return text;
        String dots = "...";
        int end = text.length();
        while (end > 0 && fm.stringWidth(text.substring(0, end) + dots) > maxWidth) end--;
        return text.substring(0, end) + dots;
    }
}
