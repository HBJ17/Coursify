package com.crs.ui;

import com.crs.model.Course;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Small helpers that turn model objects into the text shown in tables. */
public final class UiFormat {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private UiFormat() { }

    public static String timeSlot(Course c) {
        return c.getTimeSlot() == null ? "-" : c.getTimeSlot().toString();
    }

    public static String seats(Course c) {
        return c.getSeatsLeft() + " / " + c.getCapacity();
    }

    public static String prerequisites(Course c) {
        return c.getPrerequisites().isEmpty() ? "-" : String.join(", ", c.getPrerequisites());
    }

    public static String dateTime(LocalDateTime t) {
        return t == null ? "-" : t.format(DATE_TIME);
    }
}
