package org.example.budegtinfix.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CatatanDB {
    private static final String URL = "jdbc:sqlite:Budgetin.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            System.out.println("Mencoba koneksi ke database: " + URL);
            conn = DriverManager.getConnection(URL);
            System.out.println("Koneksi ke database berhasil.");

        } catch (SQLException e) {
            System.err.println("Gagal koneksi ke SQLite: " + e.getMessage());
            e.printStackTrace();
            conn = null;
        }
        return conn;
    }
}