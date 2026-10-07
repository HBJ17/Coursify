package com.crs.ui;

import com.crs.app.AppContext;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Factory pattern: the only place that knows which JPanel class belongs to which screen name.
 * MainFrame just asks for a screen by name and never does "new LoginPanel(...)" itself.
 */
public class PanelFactory {
    public static final String LOGIN = "LOGIN";
    public static final String STUDENT = "STUDENT";
    public static final String ADMIN = "ADMIN";

    private final AppContext ctx;
    private final Session session;
    private final ScreenNavigator navigator;

    public PanelFactory(AppContext ctx, Session session, ScreenNavigator navigator) {
        this.ctx = ctx;
        this.session = session;
        this.navigator = navigator;
    }

    public JPanel create(String screenName) {
        switch (screenName) {
            case LOGIN:   return new LoginPanel(ctx.getAuthService(), session, navigator);
            case STUDENT: return new StudentHomePanel(ctx, session, navigator);
            case ADMIN:   return adminScreen();
            default: throw new IllegalArgumentException("Unknown screen: " + screenName);
        }
    }

    /** Admin area: header + dashboard. */
    private JPanel adminScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new HeaderBar(session, navigator), BorderLayout.NORTH);
        panel.add(new AdminDashboardPanel(ctx), BorderLayout.CENTER);
        return panel;
    }
}
