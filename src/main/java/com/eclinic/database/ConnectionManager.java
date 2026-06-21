package com.eclinic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    private static final int POOL_SIZE = 10;
    private static final long BORROW_TIMEOUT_SECONDS = 10;

    private static BlockingQueue<Connection> pool;

    public static synchronized void init(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;

        pool = new ArrayBlockingQueue<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                pool.offer(createPhysicalConnection());
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize connection pool", e);
            }
        }
    }

    private static Connection createPhysicalConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    public static Connection getConnection() throws SQLException {
        if (pool == null) {
            throw new SQLException("Database connection not initialized. Call ConnectionManager.init() first.");
        }
        Connection conn;
        try {
            conn = pool.poll(BORROW_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a connection", e);
        }
        if (conn == null) {
            throw new SQLException("Timed out waiting for an available DB connection (pool size: " + POOL_SIZE + ")");
        }
        if (!isValid(conn)) {
            conn = createPhysicalConnection(); 
        }
        return conn;
    }

    private static boolean isValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    public static void closeConnection(Connection conn) {
        if (conn == null || pool == null) return;
        boolean returned = pool.offer(conn);
        if (!returned) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}