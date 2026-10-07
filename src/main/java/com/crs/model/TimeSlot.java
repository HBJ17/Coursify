package com.crs.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/** When a course meets, e.g. MONDAY 09:00-10:00. Immutable (cannot change after creation). */
public final class TimeSlot {
    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;

    public TimeSlot(DayOfWeek day, LocalTime start, LocalTime end) {
        if (day == null || start == null || end == null) throw new IllegalArgumentException("TimeSlot fields cannot be null");
        if (!start.isBefore(end)) throw new IllegalArgumentException("Start time must be before end time");
        this.day = day;
        this.start = start;
        this.end = end;
    }

    public DayOfWeek getDay() { return day; }
    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot t)) return false;
        return day == t.day && start.equals(t.start) && end.equals(t.end);
    }

    @Override
    public int hashCode() { return Objects.hash(day, start, end); }

    @Override
    public String toString() { return day.toString().substring(0, 3) + " " + start + "-" + end; }
}
