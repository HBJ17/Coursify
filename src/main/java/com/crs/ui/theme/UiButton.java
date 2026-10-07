package com.crs.ui.theme;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;

/** Flat rounded button in one of the Coursify styles. Hover and pressed colours are built in. */
public class UiButton extends JButton {
    public enum Variant { PRIMARY, SECONDARY, DANGER, WARNING, GHOST, LINK, DANGER_LINK }

    private final Variant variant;
    private final Icons.Name iconName;

    public UiButton(String text, Variant variant) {
        this(text, null, variant);
    }

    public UiButton(String text, Icons.Name iconName, Variant variant) {
        super(text);
        this.variant = variant;
        this.iconName = iconName;
        setFont(variant == Variant.LINK || variant == Variant.DANGER_LINK ? Theme.font(12) : Theme.bold(12.5f));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setIconTextGap(6);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boolean link = variant == Variant.LINK || variant == Variant.DANGER_LINK;
        setBorder(link ? BorderFactory.createEmptyBorder(2, 2, 2, 2) : BorderFactory.createEmptyBorder(7, 14, 7, 14));
        getModel().addChangeListener(e -> updateForeground());
        updateForeground();
    }

    /** Text (and icon) colour, which for links changes on hover. */
    private void updateForeground() {
        boolean hover = getModel().isRollover();
        Color fg = switch (variant) {
            case PRIMARY -> Color.WHITE;
            case SECONDARY -> Theme.TEXT_SECONDARY;
            case DANGER -> Theme.DANGER_TEXT;
            case WARNING -> Theme.WARNING_TEXT;
            case GHOST -> hover ? Theme.TEXT : Theme.MUTED;
            case LINK -> hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY;
            case DANGER_LINK -> hover ? Theme.DANGER : Theme.MUTED;
        };
        if (!fg.equals(getForeground())) {
            setForeground(fg);
            if (iconName != null) setIcon(Icons.of(iconName, 16, fg));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Draw.smooth(g);
        if (!isEnabled()) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        Color[] c = background(getModel());
        Draw.roundRect(g2, 0, 0, getWidth(), getHeight(), 7, c[0], c[1]);
        super.paintComponent(g2);
        g2.dispose();
    }

    /** {fill, border} for the current state. Null means "don't paint". */
    private Color[] background(ButtonModel m) {
        boolean pressed = m.isPressed() && m.isArmed();
        boolean hover = m.isRollover() && isEnabled();
        return switch (variant) {
            case PRIMARY -> new Color[]{pressed ? Theme.PRIMARY_PRESSED : hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY, null};
            case SECONDARY -> new Color[]{hover ? Theme.SURFACE_MUTED : Theme.SURFACE, Theme.BORDER};
            case DANGER -> new Color[]{hover ? Theme.DANGER_SOFT : Theme.SURFACE, Theme.DANGER_BORDER};
            case WARNING -> new Color[]{hover ? new Color(0xFEF3C7) : Theme.WARNING_SOFT, Theme.WARNING_BORDER};
            case GHOST -> new Color[]{hover ? Theme.SURFACE_MUTED : null, null};
            case LINK, DANGER_LINK -> new Color[]{null, null};
        };
    }
}
