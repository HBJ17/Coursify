package com.crs.ui.theme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Small summary tile: an uppercase label, a big number and a tinted icon (e.g. "TOTAL CREDITS 7"). */
public class StatCard extends Card {
    private final JLabel value = new JLabel("-");
    private final JLabel suffix = new JLabel();

    public StatCard(String title, Icons.Name icon, Chip.Style tone) {
        super(new BorderLayout(12, 0));
        padding(12, 14, 12, 14);

        JLabel label = new JLabel(title.toUpperCase());
        label.setFont(Theme.label(10.5f));
        label.setForeground(Theme.FAINT);

        value.setFont(Theme.bold(18));
        value.setForeground(Theme.TEXT);
        suffix.setFont(Theme.mono(11.5f));
        suffix.setForeground(Theme.FAINT);

        JPanel valueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valueRow.setOpaque(false);
        valueRow.add(value);
        valueRow.add(Box.createHorizontalStrut(6));
        valueRow.add(suffix);
        valueRow.setAlignmentX(LEFT_ALIGNMENT);
        label.setAlignmentX(LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(label);
        text.add(Box.createVerticalStrut(4));
        text.add(valueRow);

        Color[] c = Chip.colors(tone);
        Card tile = new Card(new GridBagLayout()).colors(c[1], null).radius(7).padding(0, 0, 0, 0);
        tile.setPreferredSize(new Dimension(34, 34));
        tile.add(new JLabel(Icons.of(icon, 18, tone == Chip.Style.NEUTRAL ? Theme.MUTED : c[0])));
        JPanel tileHolder = new JPanel(new GridBagLayout());
        tileHolder.setOpaque(false);
        tileHolder.add(tile);

        add(text, BorderLayout.CENTER);
        add(tileHolder, BorderLayout.EAST);
    }

    public void setValue(String main, String small) {
        value.setText(main);
        suffix.setText(small == null ? "" : small);
    }

    public void setValueColor(Color color) {
        value.setForeground(color);
    }
}
