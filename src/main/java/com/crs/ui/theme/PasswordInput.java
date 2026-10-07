package com.crs.ui.theme;

import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JPasswordField;

/** Password version of InputField (same look, hidden characters). */
public class PasswordInput extends JPasswordField {
    private final String placeholder;
    private final Icon icon;

    public PasswordInput(String placeholder, Icons.Name iconName) {
        this.placeholder = placeholder;
        this.icon = iconName == null ? null : Icons.of(iconName, 16, Theme.FAINT);
        InputField.style(this, icon != null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        InputField.paintBackground(g, this);
        super.paintComponent(g);
        InputField.paintExtras(g, this, icon, placeholder, getPassword().length == 0);
    }
}
