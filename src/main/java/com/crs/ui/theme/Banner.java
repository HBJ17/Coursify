package com.crs.ui.theme;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JLabel;

/** One-line tinted notice with an icon, e.g. "Clash checks run automatically". */
public class Banner extends Card {

    public Banner(Icons.Name icon, String text, Chip.Style tone) {
        super(new BorderLayout(10, 0));
        Color[] c = Chip.colors(tone);
        colors(c[1], c[2]).padding(10, 14, 10, 14);
        JLabel label = new JLabel(text, Icons.of(icon, 18, c[0]), JLabel.LEFT);
        label.setIconTextGap(10);
        label.setFont(Theme.font(12.5f));
        label.setForeground(Theme.TEXT_SECONDARY);
        add(label, BorderLayout.CENTER);
    }

    public Banner action(JComponent action) {
        add(action, BorderLayout.EAST);
        return this;
    }
}
