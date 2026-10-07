package com.crs.ui;

import com.crs.app.AppContext;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
            case ADMIN:   return adminPlaceholder();
            default: throw new IllegalArgumentException("Unknown screen: " + screenName);
        }
    }

    private JPanel adminPlaceholder() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new HeaderBar(session, navigator), BorderLayout.NORTH);
        panel.add(new JLabel("Admin dashboard (coming soon)", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }
}
