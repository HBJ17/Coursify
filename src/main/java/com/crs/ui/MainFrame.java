package com.crs.ui;

import com.crs.app.AppContext;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/** STUB. Member 4 builds the real window here (keep the constructor signature). JFrame + CardLayout. */
public class MainFrame extends JFrame {
    private final AppContext ctx;

    public MainFrame(AppContext ctx) {
        super("Course Registration System");
        this.ctx = ctx;
        add(new JLabel("UI coming soon (Member 4)", SwingConstants.CENTER));
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
