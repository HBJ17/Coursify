package com.crs.model;

import java.time.LocalDateTime;

/** A student waiting for a seat in a full course. cgpa is stored so the queue can sort by it. */
public class WaitlistEntry {
    private int waitlistId;
    private final String studentId;
    private final String courseCode;
    private final double cgpa;
    private final LocalDateTime joinedTime;

    public WaitlistEntry(String studentId, String courseCode, double cgpa) {
        this(0, studentId, courseCode, cgpa, LocalDateTime.now());
    }

    public WaitlistEntry(int waitlistId, String studentId, String courseCode, double cgpa, LocalDateTime joinedTime) {
        this.waitlistId = waitlistId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.cgpa = cgpa;
        this.joinedTime = joinedTime;
    }

    public int getWaitlistId() { return waitlistId; }
    public String getStudentId() { return studentId; }
    public String getCourseCode() { return courseCode; }
    public double getCgpa() { return cgpa; }
    public LocalDateTime getJoinedTime() { return joinedTime; }

    public void setWaitlistId(int waitlistId) { this.waitlistId = waitlistId; }

    @Override
    public String toString() { return "Waitlist[" + studentId + " for " + courseCode + ", cgpa " + cgpa + "]"; }
}
