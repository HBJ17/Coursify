package com.crs.util;

import com.crs.exception.DatabaseOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton: only ONE DBConnectionManager exists in the whole app.
 * It reads db.properties (Oracle URL, user, password) once and hands out JDBC connections.
 */
public final class DBConnectionManager {
    private static DBConnectionManager instance;

    private final String url;
    private final String user;
    private final String password;

    // Private constructor: nobody else can create one, so the settings are loaded only once.
    private DBConnectionManager() {
        Properties props = new Properties();
        try (InputStream in = DBConnectionManager.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new DatabaseOperationException(
                        "db.properties not found. Copy db.properties.example to db.properties "
                        + "in src/main/resources and fill in your Oracle details.", null);
            }
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseOperationException("Could not read db.properties", e);
        }
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");
        if (url == null || user == null || password == null) {
            throw new DatabaseOperationException(
                    "db.properties must contain db.url, db.user and db.password", null);
        }
    }

    public static synchronized DBConnectionManager getInstance() {
        if (instance == null) instance = new DBConnectionManager();
        return instance;
    }

    /** Returns a NEW connection. The caller closes it (try-with-resources). */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not connect to the Oracle database: " + e.getMessage(), e);
        }
    }
}
