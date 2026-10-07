package com.crs.ui.theme;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;

/** Dropdown with the same rounded look as InputField and a chevron arrow. */
public class UiComboBox<E> extends JComboBox<E> {

    @SafeVarargs
    public UiComboBox(E... items) {
        super(items);
        setFont(Theme.font(13));
        setForeground(Theme.TEXT_SECONDARY);
        setBackground(Theme.SURFACE);
        setOpaque(false);
        setBorder(new FieldBorder(4));
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton(Icons.of(Icons.Name.CHEVRON_DOWN, 14, Theme.MUTED));
                arrow.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                arrow.setContentAreaFilled(false);
                arrow.setFocusPainted(false);
                arrow.setOpaque(false);
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // the rounded background is painted by the combo box itself
            }
        });
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
                label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                label.setFont(Theme.font(13));
                if (index == -1) label.setOpaque(false); // the closed box shows our own background
                return label;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Draw.smooth(g);
        Draw.roundRect(g2, 0, 0, getWidth(), getHeight(), 7, Theme.SURFACE, null);
        g2.dispose();
        super.paintComponent(g);
    }
}
