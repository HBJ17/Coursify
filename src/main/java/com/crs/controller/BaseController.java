package com.crs.controller;

import com.crs.exception.AuthException;
import com.crs.exception.CourseFullException;
import com.crs.exception.DatabaseOperationException;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.exception.PrerequisiteNotMetException;
import com.crs.exception.RegistrationException;
import com.crs.exception.TimeConflictException;
import com.crs.ui.theme.MessageDialog;
import com.crs.ui.theme.MessageDialog.Tone;
import com.crs.ui.theme.Toast;
import java.awt.Component;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * Shared helper for every controller.
 * runAsync(...) runs a service call on a background thread (SwingWorker) so the window never freezes,
 * then hands the result back on the Swing thread. Errors become a styled dialog; successes a toast.
 */
public abstract class BaseController {
    protected final Component parent; // dialogs and toasts are shown over this component's window

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

    /** Shows the error in a dialog whose colour and title match the kind of problem. */
    protected void showError(Throwable error) {
        String message = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
        if (error instanceof TimeConflictException) {
            MessageDialog.show(parent, Tone.ORANGE, "Time clash", message, List.of());
        } else if (error instanceof PrerequisiteNotMetException missing) {
            MessageDialog.show(parent, Tone.DANGER, "Prerequisites missing",
                    "You need to complete these courses first:", missing.getMissing());
        } else if (error instanceof DuplicateRegistrationException) {
            MessageDialog.show(parent, Tone.NEUTRAL, "Already registered", message, List.of());
        } else if (error instanceof CourseFullException) {
            MessageDialog.show(parent, Tone.PRIMARY, "Course is full", message, List.of());
        } else if (error instanceof RegistrationException) {
            MessageDialog.show(parent, Tone.WARNING, "Registration not allowed", message, List.of());
        } else if (error instanceof AuthException) {
            MessageDialog.show(parent, Tone.DANGER, "Login failed", message, List.of());
        } else if (error instanceof DatabaseOperationException) {
            MessageDialog.show(parent, Tone.DANGER, "Database error", message, List.of());
        } else if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
            MessageDialog.show(parent, Tone.WARNING, "That didn't work", message, List.of());
        } else {
            MessageDialog.show(parent, Tone.DANGER, "Something went wrong", message, List.of());
        }
    }

    /** Green toast, e.g. "Course CS401 was added." */
    protected void showInfo(String message) {
        Toast.show(parent, Tone.SUCCESS, message, null);
    }

    /** Green toast with a short second part, e.g. "Registered for CS101 | Added to your schedule". */
    protected void showSuccess(String message, String detail) {
        Toast.show(parent, Tone.SUCCESS, message, detail);
    }

    /** Indigo toast for gentle hints, e.g. "Please select a course first." */
    protected void showHint(String message) {
        Toast.show(parent, Tone.PRIMARY, message, null);
    }

    protected boolean confirm(String question) {
        return MessageDialog.confirm(parent, Tone.PRIMARY, "Please confirm", question, "Yes", "Cancel");
    }

    protected boolean confirm(Tone tone, String title, String message, String yes, String no) {
        return MessageDialog.confirm(parent, tone, title, message, yes, no);
    }
}
