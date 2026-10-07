package com.crs.ui.theme;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;

/** Indigo circle with the person's initials, e.g. "AK" for Arun Kumar. */
public class Avatar extends JComponent {
    private final String initials;
    private final int size;

    public Avatar(String fullName, int size) {
        this.initials = initialsOf(fullName);
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(getPreferredSize());
    }

    static String initialsOf(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (!part.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Draw.smooth(g);
        g2.setColor(Theme.PRIMARY);
        g2.fill(new Ellipse2D.Double(0, 0, size, size));
        int w = Draw.width(g2, initials, Theme.monoBold(size * 0.36f));
        Draw.text(g2, initials, Theme.monoBold(size * 0.36f), java.awt.Color.WHITE, (size - w) / 2, size / 2);
        g2.dispose();
    }
}
