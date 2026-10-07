package com.crs.ui.theme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * Styled replacement for JOptionPane: a rounded card with a coloured icon, a title, a message,
 * optional chips (e.g. missing prerequisites) and buttons. The window behind it is dimmed.
 */
public final class MessageDialog {

    /** Colour and icon of a message. */
    public enum Tone {
        PRIMARY(Icons.Name.INFO, Chip.Style.PRIMARY),
        SUCCESS(Icons.Name.CHECK_CIRCLE, Chip.Style.SUCCESS),
        WARNING(Icons.Name.WARNING, Chip.Style.WARNING),
        DANGER(Icons.Name.ERROR, Chip.Style.DANGER),
        ORANGE(Icons.Name.CLOCK, Chip.Style.ORANGE),
        NEUTRAL(Icons.Name.INFO, Chip.Style.NEUTRAL);

        final Icons.Name icon;
        final Chip.Style style;

        Tone(Icons.Name icon, Chip.Style style) {
            this.icon = icon;
            this.style = style;
        }

        public Color[] colors() { return Chip.colors(style); }
    }

    private MessageDialog() { }

    /** Information or error with a single OK button. */
    public static void show(Component parent, Tone tone, String title, String message, List<String> chips) {
        open(parent, tone, title, message, chips, "OK", null);
    }

    /** Question with two buttons. Returns true when the first (yes) button was clicked. */
    public static boolean confirm(Component parent, Tone tone, String title, String message, String yes, String no) {
        return open(parent, tone, title, message, List.of(), yes, no);
    }

    private static boolean open(Component parent, Tone tone, String title, String message,
                                List<String> chips, String yes, String no) {
        if (GraphicsEnvironment.isHeadless()) return false;
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        boolean[] answer = {false};

        Color[] c = tone.colors();
        Card tile = new Card(new GridBagLayout()).colors(c[1], c[2]).radius(10).padding(0, 0, 0, 0);
        tile.setPreferredSize(new Dimension(42, 42));
        tile.add(new JLabel(Icons.of(tone.icon, 22, tone == Tone.NEUTRAL ? Theme.MUTED : c[0])));
        JPanel tileHolder = new JPanel(new BorderLayout());
        tileHolder.setOpaque(false);
        tileHolder.add(tile, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.bold(15.5f));
        titleLabel.setForeground(Theme.TEXT);
        JTextArea text = new JTextArea(message);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setFont(Theme.font(13));
        text.setForeground(Theme.MUTED);
        text.setSize(new Dimension(300, Short.MAX_VALUE)); // lets the text area work out its wrapped height

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(titleLabel, BorderLayout.NORTH);
        body.add(text, BorderLayout.CENTER);
        if (!chips.isEmpty()) {
            JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            chipRow.setOpaque(false);
            for (String chip : chips) {
                chipRow.add(new Chip(chip, tone.style).mono());
                chipRow.add(javax.swing.Box.createHorizontalStrut(6));
            }
            body.add(chipRow, BorderLayout.SOUTH);
        }

        UiButton yesButton = new UiButton(yes, tone == Tone.DANGER && no != null ? UiButton.Variant.DANGER : UiButton.Variant.PRIMARY);
        yesButton.addActionListener(e -> {
            answer[0] = true;
            dialog.dispose();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        if (no != null) {
            UiButton noButton = new UiButton(no, UiButton.Variant.SECONDARY);
            noButton.addActionListener(e -> dialog.dispose());
            buttons.add(noButton);
        }
        buttons.add(yesButton);

        Card card = new Card(new BorderLayout(14, 18)).padding(20, 20, 16, 20).radius(14);
        card.add(tileHolder, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(card);
        dialog.getRootPane().setDefaultButton(yesButton);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.pack();
        dialog.setSize(Math.max(420, dialog.getWidth()), dialog.getHeight());
        try {
            dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 28, 28));
        } catch (UnsupportedOperationException ignored) {
            // shaped windows not supported: square corners are fine
        }
        dialog.setLocationRelativeTo(owner);

        Runnable undim = dim(owner);
        dialog.setVisible(true); // blocks until the dialog is closed
        undim.run();
        return answer[0];
    }

    /** Darkens the owner window while the dialog is open. Returns the action that undoes it. */
    private static Runnable dim(Window owner) {
        if (!(owner instanceof RootPaneContainer container)) return () -> { };
        Component oldGlass = container.getGlassPane();
        JComponent shade = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(15, 23, 42, 70));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        container.setGlassPane(shade);
        shade.setVisible(true);
        return () -> {
            shade.setVisible(false);
            container.setGlassPane(oldGlass);
        };
    }
}
