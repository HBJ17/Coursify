package com.crs.exception;

/** Wrong ID or password at login. */
public class AuthException extends Exception {
    public AuthException(String message) { super(message); }
}
