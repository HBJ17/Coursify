package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The one and only window. A CardLayout holds the current screen (login, student area or admin area)
 * and showScreen(...) swaps it. A screen is rebuilt every time it is shown, so it always starts with fresh data.
 */
public class MainFrame extends JFrame implements ScreenNavigator {
    private final AppContext ctx;
    private final Session session = new Session();
    private final CardLayout cards = new CardLayout();
    private final JPanel cardHolder = new JPanel(cards);
    private final PanelFactory factory;
    private JPanel currentScreen;

    public MainFrame(AppContext ctx) {
        super("Coursify — Course Registration");
        this.ctx = ctx;
        this.factory = new PanelFactory(ctx, session, this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        cardHolder.setBackground(Theme.BG);
        add(cardHolder, BorderLayout.CENTER);
        showScreen(PanelFactory.LOGIN);
        setSize(1280, 800);
        setLocationRelativeTo(null);
    }

    /** Removes the old screen, builds the requested one and makes it visible. */
    @Override
    public void showScreen(String screenName) {
        if (currentScreen != null) {
            detachListeners(currentScreen); // old screen must stop listening for events
            cardHolder.remove(currentScreen);
        }
        currentScreen = factory.create(screenName);
        cardHolder.add(currentScreen, screenName);
        cards.show(cardHolder, screenName);
        cardHolder.revalidate();
        cardHolder.repaint();
    }

    /** Walks through every child component and unsubscribes everything that listens for events. */
    private static void detachListeners(Container container) {
        if (container instanceof LiveComponent live) live.detach();
        for (Component child : container.getComponents()) {
            if (child instanceof LiveComponent live) live.detach();
            if (child instanceof Container inner) detachListeners(inner);
        }
    }

    public AppContext getContext() { return ctx; }
    public Session getSession() { return session; }
}
