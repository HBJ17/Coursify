package com.crs.exception;

public class DuplicateRegistrationException extends RegistrationException {
    public DuplicateRegistrationException(String courseCode) {
        super("You are already registered for " + courseCode + ".");
    }
}
