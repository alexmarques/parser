package com.ef;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Connection conn;

    private Database() {}

    public static Connection getConnection() {
        if(conn == null) {
            try {
                DriverManager.getConnection("url", "user", "password");
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


}
