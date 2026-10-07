package com.crs.exception;

/**
 * Parent of all "you can't register for this" errors.
 * The UI can catch this one type and show e.getMessage() in a JOptionPane.
 */
public class RegistrationException extends Exception {
    public RegistrationException(String message) { super(message); }
}
