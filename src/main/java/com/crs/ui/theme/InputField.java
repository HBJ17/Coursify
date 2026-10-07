package com.crs.ui.theme;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/** Rounded text field with a grey placeholder and an optional icon on the left. */
public class InputField extends JTextField {
    private final String placeholder;
    private final Icon icon;

    public InputField(String placeholder) {
        this(placeholder, null);
    }

    public InputField(String placeholder, Icons.Name iconName) {
        this.placeholder = placeholder;
        this.icon = iconName == null ? null : Icons.of(iconName, 16, Theme.FAINT);
        style(this, icon != null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        paintBackground(g, this);
        super.paintComponent(g);
        paintExtras(g, this, icon, placeholder, getText().isEmpty());
    }

    /** Shared by InputField and PasswordInput. */
    static void style(JTextComponent field, boolean hasIcon) {
        field.setFont(Theme.font(13));
        field.setForeground(Theme.TEXT);
        field.setCaretColor(Theme.PRIMARY);
        field.setSelectionColor(Theme.PRIMARY_SOFT_BORDER);
        field.setOpaque(false);
        field.setBorder(new FieldBorder(hasIcon ? 34 : 10));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e) { field.repaint(); }
        });
    }

    static void paintBackground(Graphics g, JTextComponent field) {
        Graphics2D g2 = Draw.smooth(g);
        Draw.roundRect(g2, 0, 0, field.getWidth(), field.getHeight(), 7,
                field.isEnabled() ? Theme.SURFACE : Theme.SURFACE_MUTED, null);
        g2.dispose();
    }

    static void paintExtras(Graphics g, JTextComponent field, Icon icon, String placeholder, boolean empty) {
        Graphics2D g2 = Draw.smooth(g);
        int centerY = field.getHeight() / 2;
        if (icon != null) icon.paintIcon(field, g2, 11, centerY - icon.getIconHeight() / 2);
        if (empty && placeholder != null) {
            Draw.text(g2, placeholder, field.getFont(), Theme.FAINT, field.getInsets().left, centerY);
        }
        g2.dispose();
    }
}
