package com.crs.ui;

import com.crs.model.Course;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Small helpers that turn model objects into the text shown in tables. */
public final class UiFormat {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private UiFormat() { }

    /** e.g. "Mon 09:00–10:00", or "-" when the course has no fixed slot. */
    public static String timeSlot(Course c) {
        if (c.getTimeSlot() == null) return "-";
        String day = c.getTimeSlot().getDay().toString();
        return day.charAt(0) + day.substring(1, 3).toLowerCase() + " "
                + c.getTimeSlot().getStart() + "–" + c.getTimeSlot().getEnd();
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
