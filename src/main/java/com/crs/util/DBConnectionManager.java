package com.crs.util;

import java.sql.Connection;

/**
 * STUB. Member 1 replaces the method bodies (keep the signatures).
 * Singleton that reads db.properties and hands out Oracle JDBC connections.
 */
public final class DBConnectionManager {
    private static DBConnectionManager instance;

    private DBConnectionManager() { }

    public static synchronized DBConnectionManager getInstance() {
        if (instance == null) instance = new DBConnectionManager();
        return instance;
    }

    /** Returns a NEW connection. The caller closes it (try-with-resources). */
    public Connection getConnection() {
        throw new UnsupportedOperationException("TODO Member 1: DBConnectionManager.getConnection");
    }
}
