package com.crs.ui.theme;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Slim rounded scrollbar without arrow buttons. */
public class ThinScrollBarUI extends BasicScrollBarUI {

    public static ComponentUI createUI(JComponent c) {
        return new ThinScrollBarUI();
    }

    @Override
    protected void configureScrollBarColors() {
        thumbColor = Theme.BORDER_STRONG;
        trackColor = Theme.SURFACE;
    }

    @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
    @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

    private static JButton zeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        g.setColor(c.getParent() != null ? c.getParent().getBackground() : Theme.SURFACE);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = Draw.smooth(g);
        boolean vertical = scrollbar.getOrientation() == VERTICAL;
        double x = r.x + (vertical ? 2 : 0), y = r.y + (vertical ? 0 : 2);
        double w = vertical ? r.width - 4 : r.width, h = vertical ? r.height : r.height - 4;
        Draw.roundRect(g2, x, y, w, h, Math.min(w, h) / 2, isThumbRollover() ? Theme.FAINT : thumbColor, null);
        g2.dispose();
    }
}
