package com.crs.ui.theme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Small notification that slides in at the top right of the window and disappears after a few seconds,
 * e.g. "Registered for CS101 | Added to your schedule".
 */
public final class Toast {
    private static final String CURRENT = "coursify.toast";
    private static final int SHOW_MILLIS = 3500;

    private Toast() { }

    public static void show(Component parent, MessageDialog.Tone tone, String message, String detail) {
        JRootPane root = parent == null ? null : SwingUtilities.getRootPane(parent);
        if (root == null) { // not inside a window (yet): fall back to a dialog
            MessageDialog.show(parent, tone, message, detail == null ? "" : detail, List.of());
            return;
        }
        JLayeredPane layer = root.getLayeredPane();
        if (layer.getClientProperty(CURRENT) instanceof JComponent old) remove(layer, old);

        Color[] c = tone.colors();
        Card card = new Card(new BorderLayout(10, 0)).colors(c[1], c[2]).padding(10, 14, 10, 8);
        JLabel text = new JLabel(message, Icons.of(tone.icon, 18, c[0]), JLabel.LEFT);
        text.setIconTextGap(10);
        text.setFont(Theme.bold(13));
        text.setForeground(Theme.TEXT);
        card.add(text, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);
        if (detail != null) {
            JLabel more = new JLabel(detail);
            more.setFont(Theme.font(12));
            more.setForeground(Theme.MUTED);
            more.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 1, 0, 0, c[2]),
                    BorderFactory.createEmptyBorder(0, 10, 0, 0)));
            right.add(more, BorderLayout.CENTER);
        }
        UiButton close = new UiButton("", Icons.Name.CLOSE, UiButton.Variant.GHOST);
        close.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        right.add(close, BorderLayout.EAST);
        card.add(right, BorderLayout.EAST);

        Shadow view = new Shadow(card);
        Dimension size = view.getPreferredSize();
        view.setBounds(layer.getWidth() - size.width - 18, 64, size.width, size.height);
        layer.add(view, JLayeredPane.POPUP_LAYER);
        layer.putClientProperty(CURRENT, view);
        layer.revalidate();
        layer.repaint();

        Timer timer = new Timer(SHOW_MILLIS, e -> remove(layer, view));
        timer.setRepeats(false);
        timer.start();
        close.addActionListener(e -> {
            timer.stop();
            remove(layer, view);
        });
    }

    private static void remove(JLayeredPane layer, JComponent view) {
        if (view.getParent() == layer) {
            layer.remove(view);
            layer.repaint(view.getBounds());
        }
        if (layer.getClientProperty(CURRENT) == view) layer.putClientProperty(CURRENT, null);
    }

    /** Wraps the toast card and paints a soft shadow around it. */
    private static final class Shadow extends JPanel {
        private static final int SPREAD = 6;

        Shadow(JComponent content) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(SPREAD - 2, SPREAD, SPREAD + 2, SPREAD));
            add(content);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Draw.smooth(g);
            for (int i = SPREAD; i > 0; i--) {
                g2.setColor(new Color(15, 23, 42, 6));
                g2.fill(Draw.round(SPREAD - i, SPREAD - i + 2, getWidth() - 2 * (SPREAD - i), getHeight() - 2 * (SPREAD - i) - 2, 10 + i));
            }
            g2.dispose();
        }
    }
}
