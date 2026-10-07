package com.crs.ui;

import com.crs.app.AppContext;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/** The student area: header + three tabs (Browse, My Courses, My Waitlists). */
public class StudentHomePanel extends JPanel {

    public StudentHomePanel(AppContext ctx, Session session, ScreenNavigator navigator) {
        super(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse Courses", comingSoon("Browse Courses"));
        tabs.addTab("My Courses", comingSoon("My Courses"));
        tabs.addTab("My Waitlists", comingSoon("My Waitlists"));

        add(new HeaderBar(session, navigator), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private static JPanel comingSoon(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(name + " (coming soon)", SwingConstants.CENTER));
        return panel;
    }
}
