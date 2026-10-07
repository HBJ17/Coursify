package com.crs.controller;

import com.crs.exception.CourseFullException;
import com.crs.service.RegistrationService;
import com.crs.service.WaitlistService;
import com.crs.ui.Session;
import java.awt.Component;

/**
 * Handles the student's course actions. Every call runs in the background (see BaseController).
 * Clash, prerequisite and duplicate errors are shown as messages. A full course offers the waitlist instead.
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
            showInfo("Please select a course first.");
            return;
        }
        String studentId = session.getUserId();
        runAsync(() -> registrationService.register(studentId, courseCode),
                registration -> showInfo("You are registered for " + courseCode + "."),
                error -> {
                    if (error instanceof CourseFullException) offerWaitlist(studentId, courseCode);
                    else showError(error);
                });
    }

    /** Cancels after a confirmation. The service then promotes the next waitlisted student automatically. */
    public void cancel(String courseCode) {
        if (courseCode == null) {
            showInfo("Please select a course first.");
            return;
        }
        if (!confirm("Cancel your registration for " + courseCode + "?")) return;
        String studentId = session.getUserId();
        runAsync(() -> {
                    registrationService.cancel(studentId, courseCode);
                    return courseCode;
                },
                code -> showInfo("Your registration for " + code + " was cancelled."));
    }

    /** Leaves a waitlist; afterSuccess lets the screen reload its table. */
    public void leaveWaitlist(String courseCode, Runnable afterSuccess) {
        if (courseCode == null) {
            showInfo("Please select a waitlist first.");
            return;
        }
        if (!confirm("Leave the waitlist for " + courseCode + "?")) return;
        String studentId = session.getUserId();
        runAsync(() -> {
                    waitlistService.leave(studentId, courseCode);
                    return courseCode;
                },
                code -> afterSuccess.run());
    }

    private void offerWaitlist(String studentId, String courseCode) {
        if (!confirm(courseCode + " is full. Do you want to join the waitlist?")) return;
        runAsync(() -> {
                    waitlistService.join(studentId, courseCode);
                    return waitlistService.positionOf(studentId, courseCode);
                },
                position -> showInfo("You joined the waitlist for " + courseCode
                        + ". Your position: " + position + "."));
    }
}
