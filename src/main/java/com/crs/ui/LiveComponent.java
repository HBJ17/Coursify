package com.crs.ui;

/**
 * A component that listens for registration events. MainFrame calls detach() on every
 * LiveComponent of a screen when that screen is closed, so old screens stop listening.
 */
public interface LiveComponent {
    void detach();
}
