package com.crs.model;

/** One row of the analytics dashboard: how popular a course is. */
public class CourseDemand {
    private final String courseCode;
    private final String title;
    private final int capacity;
    private final int registered;
    private final int waitlisted;

    public CourseDemand(String courseCode, String title, int capacity, int registered, int waitlisted) {
        this.courseCode = courseCode;
        this.title = title;
        this.capacity = capacity;
        this.registered = registered;
        this.waitlisted = waitlisted;
    }

    public String getCourseCode() { return courseCode; }
    public String getTitle() { return title; }
    public int getCapacity() { return capacity; }
    public int getRegistered() { return registered; }
    public int getWaitlisted() { return waitlisted; }

    /** Demand compared to capacity. Above 1.0 means more people want it than there are seats. */
    public double getDemandScore() { return (registered + waitlisted) / (double) capacity; }

    @Override
    public String toString() {
        return String.format("%s: %d registered, %d waiting, demand %.2f", courseCode, registered, waitlisted, getDemandScore());
    }
}
