package com.crs.ui;

/**
 * Anything that can switch the visible screen. MainFrame implements it.
 * Panels and controllers depend on this small interface instead of on the JFrame,
 * so they can be created (and tested) without opening a window.
 */
@FunctionalInterface
public interface ScreenNavigator {
    void showScreen(String screenName);
}
