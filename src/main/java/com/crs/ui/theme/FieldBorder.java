package com.crs.ui.theme;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.border.AbstractBorder;

/** Rounded outline for inputs. Turns indigo while the field has focus. */
public class FieldBorder extends AbstractBorder {
    private final int leftPadding;

    public FieldBorder() {
        this(10);
    }

    /** leftPadding leaves room for a leading icon. */
    public FieldBorder(int leftPadding) {
        this.leftPadding = leftPadding;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        boolean focused = c.isFocusOwner() || (c instanceof JComboBox<?> box && box.isPopupVisible());
        Graphics2D g2 = Draw.smooth(g);
        if (focused) {
            Draw.roundRect(g2, x, y, w, h, 7, null, Theme.PRIMARY_SOFT_BORDER);
            Draw.roundRect(g2, x + 1, y + 1, w - 2, h - 2, 6, null, Theme.PRIMARY);
        } else {
            Draw.roundRect(g2, x, y, w, h, 7, null, Theme.BORDER);
        }
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(8, leftPadding, 8, 10);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(8, leftPadding, 8, 10);
        return insets;
    }
}
