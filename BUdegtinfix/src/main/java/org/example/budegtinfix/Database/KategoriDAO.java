package org.example.budegtinfix.Database;

import org.example.budegtinfix.Conn.KategoriCon.Kategori;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KategoriDAO {

    // Ganti sesuai konfigurasi database kamu
    private static final String URL = "jdbc:mysql://localhost:3306/nama_database";
    private static final String USER = "db_user";
    private static final String PASSWORD = "db_password";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Ambil semua kategori berdasarkan user id
    public List<Kategori> getAllKategoriByUserId(int userId) throws SQLException {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT id, nama FROM kategori WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Kategori(rs.getInt("id"), rs.getString("nama")));
                }
            }
        }
        return list;
    }

    // Insert kategori baru
    public boolean insertKategori(int userId, String nama) throws SQLException {
        String sql = "INSERT INTO kategori (user_id, nama) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, nama);
            return ps.executeUpdate() > 0;
        }
    }

    // Update kategori berdasarkan id
    public boolean updateKategori(int id, String nama) throws SQLException {
        String sql = "UPDATE kategori SET nama = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Hapus kategori berdasarkan id
    public boolean deleteKategori(int id) throws SQLException {
        String sql = "DELETE FROM kategori WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
