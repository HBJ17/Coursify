package com.crs.app;

import com.crs.ui.MainFrame;
import com.crs.ui.theme.Theme;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Starting point. Opens the Swing window on the Event Dispatch Thread.
 * AppContext.createDefault() uses Oracle when src/main/resources/db.properties exists, otherwise the fakes.
 * Run with --console for the old text demo.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--console")) {
            ConsoleDemo.main(args);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Theme.install(); // Coursify fonts, colours and scrollbars
            try {
                new MainFrame(AppContext.createDefault()).setVisible(true);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null, "Could not start: " + e.getMessage(),
                        "Coursify", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
