package com.crs.ui.theme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
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

    /** Children are clipped to the rounded shape, then the border is drawn on top. */
    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D clipped = (Graphics2D) g.create();
        clipped.clip(Draw.round(0, 0, getWidth(), getHeight(), radius));
        super.paintChildren(clipped);
        clipped.dispose();
        Graphics2D g2 = Draw.smooth(g);
        Draw.roundRect(g2, 0, 0, getWidth(), getHeight(), radius, null, stroke);
        g2.dispose();
    }
}
