package com.crs.ui;

import com.crs.app.AppContext;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The one and only window. A CardLayout holds the current screen (login, student area or admin area)
 * and showScreen(...) swaps it. A screen is rebuilt every time it is shown, so it always starts with fresh data.
 */
public class MainFrame extends JFrame implements ScreenNavigator {
    private final AppContext ctx;
    private final Session session = new Session();
    private final CardLayout cards = new CardLayout();
    private final JPanel cardHolder = new JPanel(cards);
    private JPanel currentScreen;

    public MainFrame(AppContext ctx) {
        super("Course Registration System");
        this.ctx = ctx;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        add(cardHolder, BorderLayout.CENTER);
        showScreen("LOGIN");
        pack();
        setLocationRelativeTo(null);
    }

    /** Removes the old screen, builds the requested one and makes it visible. */
    @Override
    public void showScreen(String screenName) {
        if (currentScreen != null) cardHolder.remove(currentScreen);
        currentScreen = buildScreen(screenName);
        cardHolder.add(currentScreen, screenName);
        cards.show(cardHolder, screenName);
        cardHolder.revalidate();
        cardHolder.repaint();
    }

    private JPanel buildScreen(String screenName) {
        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.add(new JLabel(screenName + " screen", SwingConstants.CENTER));
        return placeholder;
    }

    public AppContext getContext() { return ctx; }
    public Session getSession() { return session; }
}
