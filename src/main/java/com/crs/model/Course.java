package com.crs.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A course. Create one with the Builder pattern:
 *
 *   Course c = Course.builder("CS101", "Java Programming")
 *                    .credits(4).capacity(40)
 *                    .timeSlot(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(10,0)))
 *                    .prerequisites(List.of("CS100"))
 *                    .build();
 */
public class Course {
    private final String code;
    private String title;
    private int credits;
    private int capacity;
    private int seatsLeft;
    private TimeSlot timeSlot;
    private List<String> prerequisites;
    private int waitlistCap;

    private Course(Builder b) {
        this.code = b.code;
        setTitle(b.title);
        setCredits(b.credits);
        setCapacity(b.capacity);
        setSeatsLeft(b.seatsLeft < 0 ? b.capacity : b.seatsLeft);
        setTimeSlot(b.timeSlot);
        setPrerequisites(b.prerequisites);
        setWaitlistCap(b.waitlistCap);
    }

    public static Builder builder(String code, String title) { return new Builder(code, title); }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }
    public int getCapacity() { return capacity; }
    public int getSeatsLeft() { return seatsLeft; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public List<String> getPrerequisites() { return Collections.unmodifiableList(prerequisites); }
    public int getWaitlistCap() { return waitlistCap; }
    public boolean isFull() { return seatsLeft == 0; }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title cannot be empty");
        this.title = title;
    }

    public void setCredits(int credits) {
        if (credits < 0) throw new IllegalArgumentException("Credits cannot be negative");
        this.credits = credits;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
    }

    public void setSeatsLeft(int seatsLeft) {
        if (seatsLeft < 0 || seatsLeft > capacity)
            throw new IllegalArgumentException("Seats left must be between 0 and capacity (" + capacity + ")");
        this.seatsLeft = seatsLeft;
    }

    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; } // null = no fixed slot

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites == null ? new ArrayList<>() : new ArrayList<>(prerequisites);
    }

    public void setWaitlistCap(int waitlistCap) {
        if (waitlistCap < 0) throw new IllegalArgumentException("Waitlist cap cannot be negative");
        this.waitlistCap = waitlistCap;
    }

    @Override
    public boolean equals(Object o) { return o instanceof Course c && code.equals(c.code); }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() {
        return code + " - " + title + " (" + seatsLeft + "/" + capacity + " seats" +
               (timeSlot != null ? ", " + timeSlot : "") + ")";
    }

    /** Builder: required fields in the constructor, optional ones via methods. */
    public static class Builder {
        private final String code;
        private final String title;
        private int credits = 3;
        private int capacity = 30;
        private int seatsLeft = -1; // -1 means "same as capacity"
        private TimeSlot timeSlot;
        private List<String> prerequisites = new ArrayList<>();
        private int waitlistCap = 10;

        private Builder(String code, String title) {
            if (code == null || code.isBlank()) throw new IllegalArgumentException("Course code cannot be empty");
            this.code = code;
            this.title = title;
        }

        public Builder credits(int credits) { this.credits = credits; return this; }
        public Builder capacity(int capacity) { this.capacity = capacity; return this; }
        public Builder seatsLeft(int seatsLeft) { this.seatsLeft = seatsLeft; return this; }
        public Builder timeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; return this; }
        public Builder prerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; return this; }
        public Builder waitlistCap(int waitlistCap) { this.waitlistCap = waitlistCap; return this; }

        public Course build() { return new Course(this); }
    }
}
