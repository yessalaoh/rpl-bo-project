package org.example.budegtinfix.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CatatanDB {
    private static final String URL = "jdbc:sqlite:budgetin.db";
    private static boolean tablesInitialized = false;

    public static Connection connect() {
        Connection conn = null;
        try {
            System.out.println("Mencoba koneksi ke database: " + URL);
            conn = DriverManager.getConnection(URL);
            System.out.println("Koneksi ke database berhasil.");

            if (!tablesInitialized) {
                initializeTables(conn);
                tablesInitialized = true;
            }

        } catch (SQLException e) {
            System.err.println("Gagal koneksi ke SQLite: " + e.getMessage());
            e.printStackTrace();
            conn = null;
        }
        return conn;
    }

    private static void initializeTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Tabel users (sudah ada)
            String createUserTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

            // Tabel transaksi (sudah ada)
            String createTransaksiTable = """
            CREATE TABLE IF NOT EXISTS transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tanggal TEXT NOT NULL,
                jenis TEXT NOT NULL CHECK (jenis IN ('Pemasukan', 'Pengeluaran')),
                kategori TEXT NOT NULL,
                deskripsi TEXT,
                jumlah REAL NOT NULL,
                memiliki_dokumen INTEGER NOT NULL DEFAULT 0,
                user_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
            """;

            // Tabel kategori (sudah ada)
            String createKategoriTable = """
            CREATE TABLE IF NOT EXISTS kategori (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL UNIQUE,
                jenis TEXT NOT NULL CHECK (jenis IN ('Pemasukan', 'Pengeluaran')),
                user_id INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
            """;

            // Tabel baru anggaran (tambahkan ini)
            String createAnggaranTable = """
            CREATE TABLE IF NOT EXISTS anggaran (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                jumlah REAL NOT NULL,
                kategori TEXT NOT NULL,
                user_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
            """;

            // Eksekusi semua perintah CREATE TABLE
            stmt.execute(createUserTable);
            stmt.execute(createTransaksiTable);
            stmt.execute(createKategoriTable);
            stmt.execute(createAnggaranTable);  // Tambahkan ini

            System.out.println("Tabel-tabel berhasil diinisialisasi");

            initializeDefaultCategories(conn);

        } catch (SQLException e) {
            System.err.println("Gagal menginisialisasi tabel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeDefaultCategories(Connection conn) {
        String checkCategories = "SELECT COUNT(*) FROM kategori";
        String insertCategories = """
            INSERT OR IGNORE INTO kategori (nama, jenis, user_id) VALUES 
                ('Gaji', 'Pemasukan', 0),
                ('Freelance', 'Pemasukan', 0),
                ('Investasi', 'Pemasukan', 0),
                ('Makanan', 'Pengeluaran', 0),
                ('Transportasi', 'Pengeluaran', 0),
                ('Tagihan', 'Pengeluaran', 0),
                ('Hiburan', 'Pengeluaran', 0),
                ('Pendidikan', 'Pengeluaran', 0);
            """;

        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(checkCategories)) {

            if (rs.getInt(1) == 0) {
                stmt.executeUpdate(insertCategories);
                System.out.println("Kategori default berhasil ditambahkan");
            }
        } catch (SQLException e) {
            System.err.println("Gagal menambahkan kategori default: " + e.getMessage());
        }
    }
}