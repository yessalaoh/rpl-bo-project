package org.example.budegtinfix.Database;

import org.example.budegtinfix.Conn.TransaksiConn;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<TransaksiConn.Transaksi> getAllTransaksi() {
        List<TransaksiConn.Transaksi> transaksiList = new ArrayList<>();
        String query = "SELECT * FROM transaksi ORDER BY tanggal DESC";

        try (Connection conn = CatatanDB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                LocalDate tanggal = rs.getDate("tanggal").toLocalDate();
                String jenis = rs.getString("jenis");
                String kategori = rs.getString("kategori");
                String deskripsi = rs.getString("deskripsi");
                double jumlah = rs.getDouble("jumlah");
                boolean memilikiDokumen = rs.getBoolean("memiliki_dokumen");

                transaksiList.add(new TransaksiConn.Transaksi(
                        rs.getInt("id"),
                        tanggal.format(DATE_FORMATTER),
                        jenis,
                        kategori,
                        deskripsi,
                        jumlah,
                        memilikiDokumen
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error mendapatkan transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return transaksiList;
    }

    public List<String> getAllKategori() {
        List<String> kategoriList = new ArrayList<>();
        String query = "SELECT DISTINCT kategori FROM transaksi";

        try (Connection conn = CatatanDB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                kategoriList.add(rs.getString("kategori"));
            }
        } catch (SQLException e) {
            System.err.println("Error mendapatkan kategori: " + e.getMessage());
            e.printStackTrace();
        }
        return kategoriList;
    }

    public boolean addTransaksi(TransaksiConn.Transaksi transaksi) {
        String query = "INSERT INTO transaksi (tanggal, jenis, kategori, deskripsi, jumlah, memiliki_dokumen) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            LocalDate tanggal = LocalDate.parse(transaksi.getTanggal(), DATE_FORMATTER);
            pstmt.setDate(1, Date.valueOf(tanggal));
            pstmt.setString(2, transaksi.getJenis());
            pstmt.setString(3, transaksi.getKategori());
            pstmt.setString(4, transaksi.getDeskripsi());
            pstmt.setDouble(5, transaksi.getJumlah());
            pstmt.setBoolean(6, transaksi.isMemilikiDokumen());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error menambah transaksi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateItem(TransaksiConn.Transaksi transaksi) {
        String query = "UPDATE transaksi SET tanggal=?, jenis=?, kategori=?, deskripsi=?, jumlah=?, memiliki_dokumen=? " +
                "WHERE id=?";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            LocalDate tanggal = LocalDate.parse(transaksi.getTanggal(), DATE_FORMATTER);
            pstmt.setDate(1, Date.valueOf(tanggal));
            pstmt.setString(2, transaksi.getJenis());
            pstmt.setString(3, transaksi.getKategori());
            pstmt.setString(4, transaksi.getDeskripsi());
            pstmt.setDouble(5, transaksi.getJumlah());
            pstmt.setBoolean(6, transaksi.isMemilikiDokumen());
            pstmt.setInt(7, transaksi.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error mengupdate transaksi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTransaksi(int id) {
        String query = "DELETE FROM transaksi WHERE id=?";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error menghapus transaksi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}