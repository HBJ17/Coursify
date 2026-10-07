package com.crs.controller;

import com.crs.exception.CourseFullException;
import com.crs.service.RegistrationService;
import com.crs.service.WaitlistService;
import com.crs.ui.Session;
import com.crs.ui.theme.MessageDialog.Tone;
import java.awt.Component;

/**
 * Handles the student's course actions. Every call runs in the background (see BaseController).
 * Clash, prerequisite and duplicate errors are shown as dialogs. A full course offers the waitlist instead.
 */
public class RegisterController extends BaseController {
    private final RegistrationService registrationService;
    private final WaitlistService waitlistService;
    private final Session session;

    public RegisterController(Component parent, RegistrationService registrationService,
                              WaitlistService waitlistService, Session session) {
        super(parent);
        this.registrationService = registrationService;
        this.waitlistService = waitlistService;
        this.session = session;
    }

    public void register(String courseCode) {
        if (courseCode == null) {
            showHint("Please select a course first.");
            return;
        }
        String studentId = session.getUserId();
        runAsync(() -> registrationService.register(studentId, courseCode),
                registration -> showSuccess("Registered for " + courseCode, "Added to your schedule"),
                error -> {
                    if (error instanceof CourseFullException) offerWaitlist(studentId, courseCode);
                    else showError(error);
                });
    }

    /** Cancels after a confirmation. The service then promotes the next waitlisted student automatically. */
    public void cancel(String courseCode) {
        if (courseCode == null) {
            showHint("Please select a course first.");
            return;
        }
        if (!confirm(Tone.DANGER, "Cancel registration?",
                "Cancel your registration for " + courseCode + "? The next student on the waitlist will get your seat.",
                "Cancel registration", "Keep it")) return;
        String studentId = session.getUserId();
        runAsync(() -> {
                    registrationService.cancel(studentId, courseCode);
                    return courseCode;
                },
                code -> showSuccess("Registration for " + code + " cancelled", "Your seat was released"));
    }

    /** Leaves a waitlist; afterSuccess lets the screen reload its list. */
    public void leaveWaitlist(String courseCode, Runnable afterSuccess) {
        if (courseCode == null) {
            showHint("Please select a waitlist first.");
            return;
        }
        if (!confirm(Tone.WARNING, "Leave waitlist?",
                "You'll lose your place in the queue for " + courseCode + ".", "Leave waitlist", "Stay")) return;
        String studentId = session.getUserId();
        runAsync(() -> {
                    waitlistService.leave(studentId, courseCode);
                    return courseCode;
                },
                code -> {
                    showSuccess("Left the waitlist for " + code, null);
                    afterSuccess.run();
                });
    }

    /** Looks up where the student would be in the queue, then asks whether to join. */
    private void offerWaitlist(String studentId, String courseCode) {
        runAsync(() -> waitlistService.getWaitlist(courseCode).size() + 1, position -> {
            if (!confirm(Tone.PRIMARY, courseCode + " is full",
                    "Join the waitlist? You'd be position #" + position
                            + ". When a seat frees up, #1 is registered automatically.",
                    "Join waitlist", "Not now")) return;
            runAsync(() -> {
                        waitlistService.join(studentId, courseCode);
                        return waitlistService.positionOf(studentId, courseCode);
                    },
                    joined -> showSuccess("Joined the waitlist for " + courseCode, "You're #" + joined + " in line"));
        });
    }
}
