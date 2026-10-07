package com.crs.ui;

import com.crs.app.AppContext;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** The student area: header + three tabs (Browse, My Courses, My Waitlists). */
public class StudentHomePanel extends JPanel {

    public StudentHomePanel(AppContext ctx, Session session, ScreenNavigator navigator) {
        super(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse Courses", new BrowseCoursesPanel(ctx, session));
        tabs.addTab("My Courses", new MyCoursesPanel(ctx, session));
        tabs.addTab("My Waitlists", new WaitlistPanel(ctx, session));

        add(new HeaderBar(session, navigator), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
