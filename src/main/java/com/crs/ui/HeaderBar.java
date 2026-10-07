package com.crs.ui;

import com.crs.model.Person;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Top strip on every logged-in screen: who is logged in, and a Logout button. */
public class HeaderBar extends JPanel {

    public HeaderBar(Session session, ScreenNavigator navigator) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        Person user = session.getUser();
        JLabel welcome = new JLabel("Welcome, " + user.getName() + " (" + user.getId() + ", " + user.getRole() + ")");
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 14f));

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            session.logout();
            navigator.showScreen(PanelFactory.LOGIN);
        });

        add(welcome, BorderLayout.WEST);
        add(logout, BorderLayout.EAST);
    }
}
