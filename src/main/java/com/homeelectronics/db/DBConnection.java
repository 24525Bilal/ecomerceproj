package com.homeelectronics.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/home-electronics";
    private static final String USER = "postgres";
    private static final String PASSWORD = "786236";

    static {
        try {
            Class.forName("org.postgresql.Driver"); // 👈 force driver load
            System.out.println("✅ PostgreSQL Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("❌ PostgreSQL Driver not found. Make sure the JAR is in classpath.", e);
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
