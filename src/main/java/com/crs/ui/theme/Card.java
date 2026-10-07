package com.crs.ui.theme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A white panel with rounded corners and a thin border, the basic building block of every screen.
 * Children are clipped to the rounded corners, so tables inside a card get rounded corners too.
 */
public class Card extends JPanel {
    private Color fill = Theme.SURFACE;
    private Color stroke = Theme.BORDER;
    private int radius = Theme.RADIUS;

    public Card() {
        this(new BorderLayout());
    }

    public Card(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
    }

    public Card colors(Color fill, Color stroke) {
        this.fill = fill;
        this.stroke = stroke;
        repaint();
        return this;
    }

    public Card padding(int top, int left, int bottom, int right) {
        setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return this;
    }

    public Card radius(int radius) {
        this.radius = radius;
        return this;
    }

    public Color getFill() { return fill; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Draw.smooth(g);
        Draw.roundRect(g2, 0, 0, getWidth(), getHeight(), radius, fill, null);
        g2.dispose();
    }

    /** After the children are painted: mask the corners with the parent colour, then draw the border. */
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = Draw.smooth(g);
        Area outside = new Area(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        outside.subtract(new Area(Draw.round(0, 0, getWidth(), getHeight(), radius)));
        g2.setColor(parentBackground());
        g2.fill(outside);
        Draw.roundRect(g2, 0, 0, getWidth(), getHeight(), radius, null, stroke);
        g2.dispose();
    }

    private Color parentBackground() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Card card) return card.fill;
            if (p.isOpaque()) return p.getBackground();
        }
        return Theme.BG;
    }
}
