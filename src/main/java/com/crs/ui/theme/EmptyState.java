package com.crs.ui.theme;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Friendly placeholder shown when a list is empty: icon, title and a hint. */
public class EmptyState extends JPanel {

    public EmptyState(Icons.Name icon, String title, String hint) {
        super(new GridBagLayout());
        setOpaque(false);

        Card circle = new Card(new GridBagLayout()).colors(Theme.SURFACE_MUTED, null).radius(28).padding(0, 0, 0, 0);
        circle.setPreferredSize(new Dimension(56, 56));
        circle.add(new JLabel(Icons.of(icon, 26, Theme.FAINT)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(Theme.bold(15));
        titleLabel.setForeground(Theme.TEXT);

        JLabel hintLabel = new JLabel("<html><div style='text-align:center;width:300px'>" + hint + "</div></html>",
                SwingConstants.CENTER);
        hintLabel.setFont(Theme.font(12.5f));
        hintLabel.setForeground(Theme.MUTED);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(4, 0, 4, 0);
        add(circle, c);
        c.insets = new Insets(12, 0, 2, 0);
        add(titleLabel, c);
        c.insets = new Insets(2, 0, 0, 0);
        add(hintLabel, c);
    }
}
