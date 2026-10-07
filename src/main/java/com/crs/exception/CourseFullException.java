package com.crs.exception;

public class CourseFullException extends RegistrationException {
    private final String courseCode;

    public CourseFullException(String courseCode) {
        super("Course " + courseCode + " is full. You can join the waitlist.");
        this.courseCode = courseCode;
    }

    public String getCourseCode() { return courseCode; }
}
