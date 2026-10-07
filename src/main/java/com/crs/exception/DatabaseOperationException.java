package com.crs.exception;

/**
 * Wraps SQLException so the UI never sees raw JDBC errors.
 * It's unchecked (RuntimeException) so interfaces don't need "throws" everywhere.
 *
 * Usage inside a DAO:
 *   catch (SQLException e) { throw new DatabaseOperationException("Could not load courses", e); }
 */
public class DatabaseOperationException extends RuntimeException {
    public DatabaseOperationException(String message, Throwable cause) { super(message, cause); }
}
