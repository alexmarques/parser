package com.ef;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static Connection conn;

    private Database() {}

    public static Connection getConnection() {
        if(conn == null) {
            try {
                DriverManager.getConnection("jdbc:mysql://localhost:3306/parser", "root", "");
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return conn;
    }

    public static void releaseConnection() {
        if(conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                System.err.println("Unable to close database connection. " + e.getMessage());
            }
        }
    }

    public static void closeQuietly(Statement statement) {
        if(statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {

            }
        }
    }


}
