package com.bank.utility;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to establish and manage PostgreSQL database connections.
 */
public class DatabaseUtil {

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            // Try loading from classpath first
            try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream("database.properties")) {
                if (input != null) {
                    PROPERTIES.load(input);
                } else {
                    // Fallback to direct file read if resources not in classpath during standalone execution
                    try (FileInputStream fileInput = new FileInputStream("src/main/resources/database.properties")) {
                        PROPERTIES.load(fileInput);
                    }
                }
            }
            // Register PostgreSQL JDBC Driver
            Class.forName(PROPERTIES.getProperty("db.driver", "org.postgresql.Driver"));
        } catch (Exception e) {
            System.err.println("Fatal: Failed to load database properties or register PostgreSQL driver: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database connection from the driver manager.
     */
    public static Connection getConnection() throws SQLException {
        String url = PROPERTIES.getProperty("db.url");
        String user = PROPERTIES.getProperty("db.username");
        String pass = PROPERTIES.getProperty("db.password");
        if (url == null || user == null || pass == null) {
            throw new SQLException("Database connection properties are missing.");
        }
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Safely closes one or more AutoCloseable resources.
     */
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    // Quietly ignore
                }
            }
        }
    }
}
