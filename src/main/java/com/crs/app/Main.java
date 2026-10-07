package com.crs.app;

import com.crs.exception.RegistrationException;
import com.crs.model.Person;

/**
 * Starting point. Right now it runs a small console demo to prove the skeleton works.
 * Member 4 will replace the demo with: SwingUtilities.invokeLater(() -> new MainFrame(ctx).setVisible(true));
 */
public class Main {
    public static void main(String[] args) throws Exception {
        AppContext ctx = AppContext.createWithFakes();

        ctx.getSubject().addListener(e -> System.out.println("  [event] " + e));

        Person p = ctx.getAuthService().login("S001", "pass");
        System.out.println("Logged in: " + p);

        System.out.println("\nAll courses:");
        ctx.getCourseService().getAllSorted().forEach(c -> System.out.println("  " + c));

        System.out.println("\nRegistering S001 for CS202 (only 1 seat):");
        ctx.getRegistrationService().register("S001", "CS202");

        System.out.println("\nS002 tries CS202 too:");
        try {
            ctx.getRegistrationService().register("S002", "CS202");
        } catch (RegistrationException e) {
            System.out.println("  Error shown to user: " + e.getMessage());
            ctx.getWaitlistService().join("S002", "CS202");
            System.out.println("  S002 waitlist position: " + ctx.getWaitlistService().positionOf("S002", "CS202"));
        }

        System.out.println("\nMy courses (S001): " + ctx.getRegistrationService().getMyCourses("S001"));
        System.out.println("\nTop courses by demand:");
        ctx.getAnalyticsService().topInDemand(3).forEach(d -> System.out.println("  " + d));
    }
}
