package org.example.budegtinfix.Database;

import java.sql.Connection;
import java.sql.SQLException;

import static org.example.budegtinfix.Database.CatatanDB.connect;

public class UserDB {

    public static void createUserTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                email TEXT NOT NULL,
                password TEXT NOT NULL
            );
        """;

        try (Connection conn = connect();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Gagal buat tabel: " + e.getMessage());
        }
    }

}
