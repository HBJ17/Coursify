package com.crs.app;

import com.crs.ui.MainFrame;
import com.crs.ui.theme.MessageDialog;
import com.crs.ui.theme.Theme;
import java.util.List;
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
                MessageDialog.show(null, MessageDialog.Tone.DANGER, "Coursify could not start",
                        String.valueOf(e.getMessage()), List.of());
                System.exit(1);
            }
        });
    }
}
