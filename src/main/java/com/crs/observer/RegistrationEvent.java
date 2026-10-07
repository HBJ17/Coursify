package com.crs.observer;

import java.time.LocalDateTime;

/** Message sent to listeners whenever registrations change. */
public class RegistrationEvent {
    public enum Type { REGISTERED, CANCELLED, WAITLISTED, PROMOTED }

    private final Type type;
    private final String studentId;
    private final String courseCode;
    private final LocalDateTime time;

    public RegistrationEvent(Type type, String studentId, String courseCode) {
        this.type = type;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.time = LocalDateTime.now();
    }

    public Type getType() { return type; }
    public String getStudentId() { return studentId; }
    public String getCourseCode() { return courseCode; }
    public LocalDateTime getTime() { return time; }

    @Override
    public String toString() { return type + ": " + studentId + " / " + courseCode; }
}
