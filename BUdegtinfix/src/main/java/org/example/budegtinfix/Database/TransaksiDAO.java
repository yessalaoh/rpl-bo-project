package org.example.budegtinfix.Database;

import org.example.budegtinfix.Conn.TransaksiConn.Transaksi;
import org.example.budegtinfix.Session.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    // FORMAT TANGGAL YANG KONSISTEN UNTUK KOLOM 'tanggal' (hanya tanggal)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    // FORMAT TANGGAL WAKTU UNTUK KOLOM 'created_at' (tanggal dan waktu)
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Transaksi> getAllTransaksiForCurrentUser() {
        return getFilteredTransaksi(null, null, null, null);
    }

    public List<Transaksi> getFilteredTransaksi(String jenis, String kategori, LocalDate startDate, LocalDate endDate) {
        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT id, tanggal, jenis, kategori, deskripsi, jumlah, memiliki_dokumen, gambar_path, created_at FROM transaksi WHERE user_id = ?";
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
            params.add(startDate.format(DATE_FORMATTER));
        }
        if (endDate != null) {
            whereClause.append(" AND tanggal <= ?");
            params.add(endDate.format(DATE_FORMATTER));
        }

        sql += whereClause.toString();
        sql += " ORDER BY tanggal DESC, id DESC";

        System.out.println("TransaksiDAO: SQL Query (getFilteredTransaksi): " + sql);
        System.out.println("TransaksiDAO: Parameters (getFilteredTransaksi): " + params);

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
                    String imageUrl = rs.getString("gambar_path");

                    transaksiList.add(new Transaksi(id, tanggal, jenisDb, kategoriDb, deskripsi, jumlah, memilikiDokumen, imageUrl));
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
        String sql = "INSERT INTO transaksi(user_id, tanggal, jenis, kategori, deskripsi, jumlah, memiliki_dokumen, gambar_path) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Session.getIdUser());
            pstmt.setString(2, transaksi.getTanggal());
            pstmt.setString(3, transaksi.getJenis());
            pstmt.setString(4, transaksi.getKategori());
            pstmt.setString(5, transaksi.getDeskripsi());
            pstmt.setDouble(6, transaksi.getJumlah());
            pstmt.setInt(7, transaksi.isMemilikiDokumen() ? 1 : 0);
            pstmt.setString(8, transaksi.getImageUrl());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTransaksi(Transaksi transaksi) {
        String sql = "UPDATE transaksi SET tanggal = ?, jenis = ?, kategori = ?, deskripsi = ?, jumlah = ?, memiliki_dokumen = ?, gambar_path = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaksi.getTanggal());
            pstmt.setString(2, transaksi.getJenis());
            pstmt.setString(3, transaksi.getKategori());
            pstmt.setString(4, transaksi.getDeskripsi());
            pstmt.setDouble(5, transaksi.getJumlah());
            pstmt.setInt(6, transaksi.isMemilikiDokumen() ? 1 : 0);
            pstmt.setString(7, transaksi.getImageUrl());
            pstmt.setInt(8, transaksi.getId());
            pstmt.setInt(9, Session.getIdUser());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaksi> getTransaksiTerbaru(int idUser, LocalDateTime lastCheckTime) {
        List<Transaksi> transaksiList = new ArrayList<>();
        String sql = "SELECT id, tanggal, jenis, kategori, deskripsi, jumlah, memiliki_dokumen, gambar_path, created_at FROM transaksi WHERE user_id = ? AND created_at > ? ORDER BY created_at DESC";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUser);
            pstmt.setString(2, lastCheckTime.format(DATETIME_FORMATTER));

            System.out.println("TransaksiDAO (getTransaksiTerbaru): Executing query: " + pstmt.toString());
            System.out.println("TransaksiDAO (getTransaksiTerbaru): lastCheckTime param: " + lastCheckTime.format(DATETIME_FORMATTER));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transaksiList.add(new Transaksi(
                            rs.getInt("id"),
                            rs.getString("tanggal"),
                            rs.getString("jenis"),
                            rs.getString("kategori"),
                            rs.getString("deskripsi"),
                            rs.getDouble("jumlah"),
                            rs.getInt("memiliki_dokumen") == 1,
                            rs.getString("gambar_path")
                    ));
                    System.out.println("TransaksiDAO (getTransaksiTerbaru): Found new transaction: " + rs.getString("deskripsi") + " at " + rs.getString("created_at"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching new transactions for notification: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("TransaksiDAO (getTransaksiTerbaru): Number of new transactions found: " + transaksiList.size());
        return transaksiList;
    }

    // **METODE BARU: Hitung Saldo Total**
    public double getTotalSaldo(int userId) {
        double totalSaldo = 0.0;
        String sql = "SELECT jenis, jumlah FROM transaksi WHERE user_id = ?";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String jenis = rs.getString("jenis");
                    double jumlah = rs.getDouble("jumlah");

                    if ("Pemasukan".equals(jenis)) {
                        totalSaldo += jumlah;
                    } else if ("Pengeluaran".equals(jenis)) {
                        totalSaldo -= jumlah;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total balance: " + e.getMessage());
            e.printStackTrace();
        }
        return totalSaldo;
    }
}