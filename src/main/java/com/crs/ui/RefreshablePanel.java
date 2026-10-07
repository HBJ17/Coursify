package com.crs.ui;

import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationListener;
import com.crs.observer.RegistrationSubject;
import java.awt.BorderLayout;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Base class for screens that show live data (Observer pattern).
 * It subscribes to the RegistrationSubject, and whenever something changes (register, cancel, waitlist,
 * promotion) it reloads: loadData() runs on a background thread, render() runs on the Swing thread.
 *
 * @param <T> the type of data this panel shows, e.g. List&lt;Course&gt;
 */
public abstract class RefreshablePanel<T> extends JPanel implements RegistrationListener {
    private final RegistrationSubject subject;
    protected final JLabel statusLabel = new JLabel(" ");

    protected RefreshablePanel(RegistrationSubject subject) {
        super(new BorderLayout(8, 8));
        this.subject = subject;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);
        subject.addListener(this);
    }

    /** Called by the subject, possibly from a background thread, so hop onto the Swing thread first. */
    @Override
    public void onRegistrationChanged(RegistrationEvent event) {
        SwingUtilities.invokeLater(this::reload);
    }

    /** Loads fresh data in the background and shows it when ready. */
    public void reload() {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return loadData();
            }

            @Override
            protected void done() {
                try {
                    render(get());
                    statusLabel.setText(" ");
                } catch (ExecutionException e) {
                    statusLabel.setText("Could not load data: " + e.getCause().getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /** Stop listening (called when the screen is closed, e.g. on logout). */
    public void detach() {
        subject.removeListener(this);
    }

    /** Runs on a background thread: call the services here. */
    protected abstract T loadData() throws Exception;

    /** Runs on the Swing thread: put the data into the table/labels here. */
    protected abstract void render(T data);
}
