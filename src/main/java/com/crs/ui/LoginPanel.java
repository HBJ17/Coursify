package com.crs.ui;

import com.crs.controller.LoginController;
import com.crs.service.AuthService;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** First screen: ID + password. The same form is used by students and admins. */
public class LoginPanel extends JPanel {
    private final JTextField idField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton loginButton = new JButton("Login");

    public LoginPanel(AuthService authService, Session session, ScreenNavigator navigator) {
        super(new GridBagLayout());
        LoginController controller = new LoginController(this, authService, session, navigator);

        JLabel title = new JLabel("Course Registration System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel hint = new JLabel("Sample logins: S001 / pass (student), A001 / admin (admin)");

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(title, c);
        c.gridy = 1;
        add(hint, c);

        c.gridwidth = 1; c.anchor = GridBagConstraints.EAST;
        c.gridy = 2; add(new JLabel("ID:"), c);
        c.gridy = 3; add(new JLabel("Password:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        c.gridy = 2; add(idField, c);
        c.gridy = 3; add(passwordField, c);
        c.gridy = 4; add(loginButton, c);

        Runnable doLogin = () -> {
            loginButton.setEnabled(false); // stop double clicks while the check runs
            controller.login(idField.getText(), new String(passwordField.getPassword()),
                    () -> loginButton.setEnabled(true));
        };
        loginButton.addActionListener(e -> doLogin.run());
        passwordField.addActionListener(e -> doLogin.run()); // Enter key logs in
    }
}
