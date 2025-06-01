package org.example.budegtinfix.Database;

import org.example.budegtinfix.Conn.TransaksiConn.Transaksi; // Import kelas Transaksi nested Anda
import org.example.budegtinfix.Session.Session; // Untuk mendapatkan user_id
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    public List<Transaksi> getAllTransaksiForCurrentUser() {
        return getFilteredTransaksi(null, null, null, null);
    }

    public List<Transaksi> getFilteredTransaksi(String jenis, String kategori, LocalDate startDate, LocalDate endDate) {
        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT id, tanggal, jenis, kategori, deskripsi, jumlah, memiliki_dokumen FROM transaksi WHERE user_id = ?";
        StringBuilder whereClause = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();

        System.out.println("TransaksiDAO: Fetching data for user ID: " + Session.getIdUser());
        params.add(Session.getIdUser());

        if (jenis != null && !jenis.equals("Semua")) {
            whereClause.append(" AND jenis = ?");
            params.add(jenis);
        }
        if (kategori != null && !kategori.equals("Semua Kategori")) {
            whereClause.append(" AND kategori = ?");
            params.add(kategori);
        }
        if (startDate != null) {
            whereClause.append(" AND tanggal >= ?");
            params.add(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (endDate != null) {
            whereClause.append(" AND tanggal <= ?");
            params.add(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        sql += whereClause.toString();
        sql += " ORDER BY tanggal DESC, id DESC";

        System.out.println("TransaksiDAO: SQL Query: " + sql);
        System.out.println("TransaksiDAO: Parameters: " + params);

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String tanggal = rs.getString("tanggal");
                    String jenisDb = rs.getString("jenis");
                    String kategoriDb = rs.getString("kategori");
                    String deskripsi = rs.getString("deskripsi");
                    double jumlah = rs.getDouble("jumlah");
                    boolean memilikiDokumen = rs.getInt("memiliki_dokumen") == 1;

                    transaksiList.add(new Transaksi(id, tanggal, jenisDb, kategoriDb, deskripsi, jumlah, memilikiDokumen));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching filtered transactions: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("TransaksiDAO: Number of transactions fetched: " + transaksiList.size());
        return transaksiList;
    }

    public boolean deleteTransaksi(int id) {
        String sql = "DELETE FROM transaksi WHERE id = ? AND user_id = ?";
        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, Session.getIdUser());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addTransaksi(Transaksi transaksi) {
        String sql = "INSERT INTO transaksi(user_id, tanggal, jenis, kategori, deskripsi, jumlah, memiliki_dokumen) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Session.getIdUser());
            pstmt.setString(2, transaksi.getTanggal());
            pstmt.setString(3, transaksi.getJenis());
            pstmt.setString(4, transaksi.getKategori());
            pstmt.setString(5, transaksi.getDeskripsi());
            pstmt.setDouble(6, transaksi.getJumlah());
            pstmt.setInt(7, transaksi.isMemilikiDokumen() ? 1 : 0);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTransaksi(Transaksi transaksi) {
        String sql = "UPDATE transaksi SET tanggal = ?, jenis = ?, kategori = ?, deskripsi = ?, jumlah = ?, memiliki_dokumen = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaksi.getTanggal());
            pstmt.setString(2, transaksi.getJenis());
            pstmt.setString(3, transaksi.getKategori());
            pstmt.setString(4, transaksi.getDeskripsi());
            pstmt.setDouble(5, transaksi.getJumlah());
            pstmt.setInt(6, transaksi.isMemilikiDokumen() ? 1 : 0);
            pstmt.setInt(7, transaksi.getId());
            pstmt.setInt(8, Session.getIdUser());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}