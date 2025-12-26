package com.jhf.coupon.sql.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2TestDatabase - Utility for creating H2 in-memory database for testing
 * This allows DAO tests to run without requiring a real MySQL database
 */
public class H2TestDatabase {

    private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    private static Connection connection;
    private static boolean initialized = false;

    /**
     * Get a connection to the H2 test database
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
            if (!initialized) {
                initializeSchema();
                initialized = true;
            }
        }
        return connection;
    }

    /**
     * Initialize the database schema
     */
    private static void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create categories table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS categories (" +
                "ID INT PRIMARY KEY, " +
                "NAME VARCHAR(48)" +
                ")"
            );

            // Insert category data
            stmt.execute("INSERT INTO categories (ID, NAME) VALUES (10, 'SKYING')");
            stmt.execute("INSERT INTO categories (ID, NAME) VALUES (20, 'SKY_DIVING')");
            stmt.execute("INSERT INTO categories (ID, NAME) VALUES (30, 'FANCY_RESTAURANT')");
            stmt.execute("INSERT INTO categories (ID, NAME) VALUES (40, 'ALL_INCLUSIVE_VACATION')");

            // Create companies table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS companies (" +
                "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "NAME VARCHAR(48), " +
                "EMAIL VARCHAR(48), " +
                "PASSWORD VARCHAR(48)" +
                ")"
            );

            // Create customers table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS customers (" +
                "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "FIRST_NAME VARCHAR(48), " +
                "LAST_NAME VARCHAR(48), " +
                "EMAIL VARCHAR(48), " +
                "PASSWORD VARCHAR(48)" +
                ")"
            );

            // Create coupons table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS coupons (" +
                "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "COMPANY_ID INT, " +
                "CATEGORY_ID INT, " +
                "TITLE VARCHAR(48), " +
                "DESCRIPTION VARCHAR(48), " +
                "START_DATE DATE, " +
                "END_DATE DATE, " +
                "AMOUNT INT, " +
                "PRICE DOUBLE, " +
                "IMAGE VARCHAR(48), " +
                "FOREIGN KEY (COMPANY_ID) REFERENCES companies(ID) ON DELETE CASCADE, " +
                "FOREIGN KEY (CATEGORY_ID) REFERENCES categories(ID) ON DELETE CASCADE" +
                ")"
            );

            // Create customers_vs_coupons table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS customers_vs_coupons (" +
                "CUSTOMER_ID INT NOT NULL, " +
                "COUPON_ID INT NOT NULL, " +
                "PRIMARY KEY (CUSTOMER_ID, COUPON_ID), " +
                "FOREIGN KEY (CUSTOMER_ID) REFERENCES customers(ID) ON DELETE CASCADE, " +
                "FOREIGN KEY (COUPON_ID) REFERENCES coupons(ID) ON DELETE CASCADE" +
                ")"
            );
        }
    }

    /**
     * Clear all data from tables (useful between tests)
     */
    public static void clearAllTables() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE customers_vs_coupons");
            stmt.execute("TRUNCATE TABLE coupons");
            stmt.execute("TRUNCATE TABLE customers");
            stmt.execute("TRUNCATE TABLE companies");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    /**
     * Close the database connection
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
