package org.example.budegtinfix.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class TransaksiDB {

    public static void createTransaksiTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tanggal TEXT NOT NULL,
                jenis TEXT NOT NULL CHECK (jenis IN ('Pemasukan', 'Pengeluaran')),
                kategori TEXT NOT NULL,
                deskripsi TEXT,
                jumlah REAL NOT NULL,
                memiliki_dokumen INTEGER NOT NULL DEFAULT 0,
                gambar_path TEXT,
                user_id INTEGER,
                created_at TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
            """;

        try (Connection conn = CatatanDB.connect();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabel transaksi berhasil dibuat/ditemukan.");
            // Hapus bagian alter table di sini jika Anda sudah yakin kolom created_at selalu ada
        } catch (SQLException e) {
            System.err.println("Gagal membuat tabel transaksi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}