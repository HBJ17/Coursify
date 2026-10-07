package com.crs.ui;

import com.crs.controller.LoginController;
import com.crs.service.AuthService;
import com.crs.ui.theme.Card;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.Draw;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.InputField;
import com.crs.ui.theme.PasswordInput;
import com.crs.ui.theme.Theme;
import com.crs.ui.theme.UiButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** First screen: a centred card with ID + password, on a soft indigo dotted background. */
public class LoginPanel extends JPanel {
    private final InputField idField = new InputField("e.g. S001", Icons.Name.USER);
    private final PasswordInput passwordField = new PasswordInput("Your password", Icons.Name.LOCK);
    private final UiButton loginButton = new UiButton("Sign in", UiButton.Variant.PRIMARY);
    private final JLabel errorLabel = new JLabel(" ");

    public LoginPanel(AuthService authService, Session session, ScreenNavigator navigator) {
        super(new GridBagLayout());
        LoginController controller = new LoginController(this, authService, session, navigator);

        Card card = new Card(new GridBagLayout()).padding(32, 32, 28, 32).radius(14);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.insets = new Insets(0, 0, 6, 0);
        card.add(brand(), c);
        JLabel tagline = new JLabel("Register smarter. No clashes, no surprises.");
        tagline.setFont(Theme.font(13));
        tagline.setForeground(Theme.MUTED);
        c.insets = new Insets(0, 0, 24, 0);
        card.add(tagline, c);

        c.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("User ID"), c);
        c.insets = new Insets(0, 0, 14, 0);
        card.add(idField, c);
        c.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Password"), c);
        c.insets = new Insets(0, 0, 6, 0);
        card.add(passwordField, c);

        errorLabel.setFont(Theme.font(12));
        errorLabel.setForeground(Theme.DANGER_TEXT);
        errorLabel.setIconTextGap(6);
        c.insets = new Insets(0, 0, 10, 0);
        card.add(errorLabel, c);

        loginButton.setPreferredSize(new Dimension(0, 40));
        c.insets = new Insets(0, 0, 20, 0);
        card.add(loginButton, c);

        c.insets = new Insets(0, 0, 0, 0);
        card.add(demoHint(), c);
        card.setPreferredSize(new Dimension(400, card.getPreferredSize().height));

        add(card);

        Runnable doLogin = () -> {
            showError(null);
            loginButton.setEnabled(false); // stop double clicks while the check runs
            loginButton.setText("Signing in...");
            controller.login(idField.getText(), new String(passwordField.getPassword()),
                    () -> {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign in");
                    },
                    this::showError);
        };
        loginButton.addActionListener(e -> doLogin.run());
        idField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> doLogin.run()); // Enter key logs in
    }

    /** Shows (or with null, clears) the red message under the password field. */
    private void showError(String message) {
        if (message == null) {
            errorLabel.setText(" ");
            errorLabel.setIcon(null);
        } else {
            errorLabel.setText(message);
            errorLabel.setIcon(Icons.of(Icons.Name.ERROR, 14, Theme.DANGER));
        }
    }

    private static JPanel brand() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        Card tile = new Card(new GridBagLayout()).colors(Theme.PRIMARY, null).radius(9).padding(0, 0, 0, 0);
        tile.setPreferredSize(new Dimension(38, 38));
        tile.add(new JLabel(Icons.of(Icons.Name.SCHOOL, 22, Color.WHITE)));
        JLabel name = new JLabel("Coursify");
        name.setFont(Theme.bold(24));
        name.setForeground(Theme.TEXT);
        name.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 0));
        row.add(tile);
        row.add(name);
        return row;
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.bold(12));
        label.setForeground(Theme.TEXT_SECONDARY);
        return label;
    }

    private static JPanel demoHint() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        row.setOpaque(false);
        JLabel demo = new JLabel("Demo:");
        demo.setFont(Theme.font(12));
        demo.setForeground(Theme.FAINT);
        row.add(demo);
        row.add(new Chip("S001 / pass", Chip.Style.NEUTRAL).mono());
        JLabel student = new JLabel("student  ·");
        student.setFont(Theme.font(12));
        student.setForeground(Theme.FAINT);
        row.add(student);
        row.add(new Chip("A001 / admin", Chip.Style.PRIMARY).mono());
        JLabel admin = new JLabel("admin");
        admin.setFont(Theme.font(12));
        admin.setForeground(Theme.FAINT);
        row.add(admin);
        return row;
    }

    /** Soft indigo gradient with a dot pattern and two large faint circles. */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Draw.smooth(g);
        int w = getWidth(), h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_SOFT, 0, h, Theme.BG));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(0xE0E7FF));
        g2.fill(new Ellipse2D.Double(-w * 0.15, -h * 0.35, w * 0.55, w * 0.55));
        g2.setColor(new Color(0xEEF2FF));
        g2.fill(new Ellipse2D.Double(w * 0.7, h * 0.55, w * 0.45, w * 0.45));
        g2.setColor(new Color(0xC7D2FE));
        for (int y = 12; y < h; y += 24) {
            for (int x = 12; x < w; x += 24) g2.fill(new Ellipse2D.Double(x - 1, y - 1, 2, 2));
        }
        g2.dispose();
    }
}
