package com.crs.controller;

import com.crs.exception.AuthException;
import com.crs.exception.DatabaseOperationException;
import com.crs.exception.RegistrationException;
import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Shared helper for every controller.
 * runAsync(...) runs a service call on a background thread (SwingWorker) so the window never freezes,
 * then hands the result back on the Swing thread. Any exception is shown as a JOptionPane dialog.
 */
public abstract class BaseController {
    protected final Component parent; // dialogs are centred on this component

    protected BaseController(Component parent) {
        this.parent = parent;
    }

    /** Runs task in the background; onSuccess runs on the Swing thread. Errors become a dialog. */
    protected <T> void runAsync(Callable<T> task, Consumer<T> onSuccess) {
        runAsync(task, onSuccess, this::showError);
    }

    /** Same, but the caller decides what to do with an error (e.g. offer the waitlist). */
    protected <T> void runAsync(Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException e) {
                    onError.accept(e.getCause()); // the real exception thrown by the service
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /** Shows the error message with a title that matches the kind of problem. */
    protected void showError(Throwable error) {
        String title;
        if (error instanceof RegistrationException) title = "Registration not allowed";
        else if (error instanceof AuthException) title = "Login failed";
        else if (error instanceof DatabaseOperationException) title = "Database error";
        else title = "Something went wrong";

        String message = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(parent, message, "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(parent, question, "Please confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
