package com.crs.ui.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Simple line icons drawn with Java 2D, so the app needs no image files.
 * Each icon is drawn on a 24x24 grid and scaled to the requested size.
 */
public final class Icons {
    public enum Name {
        SCHOOL, SEARCH, BOOK, HOURGLASS, DASHBOARD, LIST, LOGOUT, CHECK, CHECK_CIRCLE, INFO, WARNING,
        ERROR, CLOSE, CLOCK, CALENDAR, PLUS, REFRESH, TRASH, USER, LOCK, CHEVRON_DOWN, TRENDING, BOOKMARK
    }

    private Icons() { }

    public static Icon of(Name name, int size, Color color) {
        return new LineIcon(name, size, color);
    }

    private record LineIcon(Name name, int size, Color color) implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Draw.smooth(g);
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(name, g2);
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    private static void draw(Name name, Graphics2D g) {
        switch (name) {
            case SEARCH -> { circle(g, 11, 11, 7); line(g, 16.5, 16.5, 21, 21); }
            case SCHOOL -> {
                poly(g, true, 2, 9, 12, 4, 22, 9, 12, 14);
                Path2D p = new Path2D.Double();
                p.moveTo(6, 11.5); p.lineTo(6, 16); p.quadTo(12, 20.5, 18, 16); p.lineTo(18, 11.5);
                g.draw(p);
                line(g, 22, 9, 22, 15);
            }
            case BOOK -> { g.draw(Draw.round(4, 3, 16, 18, 2)); line(g, 8, 3, 8, 21); line(g, 11.5, 8, 16, 8); }
            case HOURGLASS -> {
                line(g, 6, 3, 18, 3); line(g, 6, 21, 18, 21);
                poly(g, false, 7, 3, 7, 6, 12, 12, 7, 18, 7, 21);
                poly(g, false, 17, 3, 17, 6, 12, 12, 17, 18, 17, 21);
            }
            case DASHBOARD -> {
                g.draw(Draw.round(3, 3, 7, 9, 1.5)); g.draw(Draw.round(14, 3, 7, 5, 1.5));
                g.draw(Draw.round(14, 12, 7, 9, 1.5)); g.draw(Draw.round(3, 16, 7, 5, 1.5));
            }
            case LIST -> {
                line(g, 9, 6, 21, 6); line(g, 9, 12, 21, 12); line(g, 9, 18, 21, 18);
                dot(g, 4, 6, 1.3); dot(g, 4, 12, 1.3); dot(g, 4, 18, 1.3);
            }
            case LOGOUT -> {
                poly(g, false, 9, 21, 5, 21, 5, 3, 9, 3);
                poly(g, false, 16, 17, 21, 12, 16, 7);
                line(g, 21, 12, 9, 12);
            }
            case CHECK -> poly(g, false, 5, 12.5, 10, 17.5, 19, 7);
            case CHECK_CIRCLE -> { circle(g, 12, 12, 9); poly(g, false, 8, 12.5, 11, 15.5, 16, 9.5); }
            case INFO -> { circle(g, 12, 12, 9); line(g, 12, 11, 12, 16); dot(g, 12, 8, 1.2); }
            case WARNING -> { poly(g, true, 12, 3, 22, 20, 2, 20); line(g, 12, 9, 12, 13.5); dot(g, 12, 16.8, 1.2); }
            case ERROR -> { circle(g, 12, 12, 9); line(g, 9, 9, 15, 15); line(g, 15, 9, 9, 15); }
            case CLOSE -> { line(g, 6, 6, 18, 18); line(g, 18, 6, 6, 18); }
            case CLOCK -> { circle(g, 12, 12, 9); poly(g, false, 12, 7, 12, 12, 15.5, 14); }
            case CALENDAR -> {
                g.draw(Draw.round(3, 5, 18, 16, 2)); line(g, 3, 10, 21, 10);
                line(g, 8, 3, 8, 7); line(g, 16, 3, 16, 7);
            }
            case PLUS -> { line(g, 12, 5, 12, 19); line(g, 5, 12, 19, 12); }
            case REFRESH -> {
                g.draw(new Arc2D.Double(4, 4, 16, 16, 40, 290, Arc2D.OPEN));
                poly(g, false, 19.5, 3.5, 19.5, 8.5, 14.5, 8.5);
            }
            case TRASH -> {
                line(g, 4, 7, 20, 7);
                poly(g, false, 6, 7, 7, 21, 17, 21, 18, 7);
                poly(g, false, 9, 7, 9, 4, 15, 4, 15, 7);
            }
            case USER -> {
                circle(g, 12, 8, 4);
                Path2D p = new Path2D.Double();
                p.moveTo(4, 21); p.quadTo(4, 14, 12, 14); p.quadTo(20, 14, 20, 21);
                g.draw(p);
            }
            case LOCK -> {
                g.draw(Draw.round(5, 11, 14, 10, 2));
                Path2D p = new Path2D.Double();
                p.moveTo(8, 11); p.lineTo(8, 7.5); p.curveTo(8, 2.5, 16, 2.5, 16, 7.5); p.lineTo(16, 11);
                g.draw(p);
            }
            case CHEVRON_DOWN -> poly(g, false, 6, 9, 12, 15, 18, 9);
            case TRENDING -> { poly(g, false, 3, 17, 9, 11, 13, 15, 21, 7); poly(g, false, 15, 7, 21, 7, 21, 13); }
            case BOOKMARK -> poly(g, true, 6, 3, 18, 3, 18, 21, 12, 17, 6, 21);
        }
    }

    private static void line(Graphics2D g, double x1, double y1, double x2, double y2) {
        g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    private static void circle(Graphics2D g, double cx, double cy, double r) {
        g.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
    }

    private static void dot(Graphics2D g, double cx, double cy, double r) {
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
    }

    /** Polyline through the (x, y) pairs; closed = join the last point back to the first. */
    private static void poly(Graphics2D g, boolean closed, double... pts) {
        Path2D p = new Path2D.Double();
        p.moveTo(pts[0], pts[1]);
        for (int i = 2; i < pts.length; i += 2) p.lineTo(pts[i], pts[i + 1]);
        if (closed) p.closePath();
        g.draw(p);
    }
}
