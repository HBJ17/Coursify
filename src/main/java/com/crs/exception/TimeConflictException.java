package com.crs.exception;

public class TimeConflictException extends RegistrationException {
    public TimeConflictException(String courseCode, String clashingCourseCode) {
        super(courseCode + " clashes with your registered course " + clashingCourseCode + ".");
    }
}
