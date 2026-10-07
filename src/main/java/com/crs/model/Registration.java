package com.crs.model;

import java.time.LocalDateTime;

/** One row of "student X is registered in course Y". */
public class Registration {
    private int regId; // 0 until the database gives it an ID
    private final String studentId;
    private final String courseCode;
    private RegistrationStatus status;
    private final LocalDateTime regTime;

    /** Use this when creating a brand new registration. */
    public Registration(String studentId, String courseCode) {
        this(0, studentId, courseCode, RegistrationStatus.ACTIVE, LocalDateTime.now());
    }

    /** Use this when loading an existing registration from the database. */
    public Registration(int regId, String studentId, String courseCode,
                        RegistrationStatus status, LocalDateTime regTime) {
        this.regId = regId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.status = status;
        this.regTime = regTime;
    }

    public int getRegId() { return regId; }
    public String getStudentId() { return studentId; }
    public String getCourseCode() { return courseCode; }
    public RegistrationStatus getStatus() { return status; }
    public LocalDateTime getRegTime() { return regTime; }

    public void setRegId(int regId) { this.regId = regId; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    @Override
    public String toString() { return "Registration[" + studentId + " -> " + courseCode + ", " + status + "]"; }
}
