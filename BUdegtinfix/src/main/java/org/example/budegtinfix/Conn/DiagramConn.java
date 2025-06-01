package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.budegtinfix.Database.TransaksiDB;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DiagramConn {

    @FXML
    private Text helloUserText;

    @FXML
    private PieChart piePemasukan;

    @FXML
    private PieChart piePengeluaran;

    @FXML
    private Button NotifBtn, transaksiBtn, btnKategori, btnAnggaran, btnLaporan, PengaturanBtn, btnLogout;

    private Connection connectDB;

    @FXML
    public void initialize() {
        connectDB = org.example.budegtinfix.Database.CatatanDB.connect();
        loadPieChartData();
    }

    private void loadPieChartData() {
        if (connectDB == null) {
            System.err.println("Database connection is null.");
            return;
        }

        try {
            if (piePengeluaran != null) piePengeluaran.getData().clear();
            if (piePemasukan != null) piePemasukan.getData().clear();

            // Query dan isi data pengeluaran
            String sqlPengeluaran = "SELECT kategori, SUM(jumlah) AS total FROM transaksi WHERE jenis = 'Pengeluaran' GROUP BY kategori";
            try (PreparedStatement stmt1 = connectDB.prepareStatement(sqlPengeluaran);
                 ResultSet rs1 = stmt1.executeQuery()) {

                while (rs1.next()) {
                    if (piePengeluaran != null) {
                        piePengeluaran.getData().add(
                                new PieChart.Data(rs1.getString("kategori"), rs1.getDouble("total"))
                        );
                    }
                }
            }

            // Query dan isi data pemasukan
            String sqlPemasukan = "SELECT kategori, SUM(jumlah) AS total FROM transaksi WHERE jenis = 'Pemasukan' GROUP BY kategori";
            try (PreparedStatement stmt2 = connectDB.prepareStatement(sqlPemasukan);
                 ResultSet rs2 = stmt2.executeQuery()) {

                while (rs2.next()) {
                    if (piePemasukan != null) {
                        piePemasukan.getData().add(
                                new PieChart.Data(rs2.getString("kategori"), rs2.getDouble("total"))
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal memuat data diagram: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigasi antar halaman
    private void navigate(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Gagal membuka halaman: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Handler tombol navigasi
    @FXML private void btnNotifClicked(ActionEvent event) {
        navigate("Notifikasi.fxml", event);
    }

    @FXML private void transaksiBtnClicked(ActionEvent event) {
        navigate("HomePage.fxml", event);
    }

    @FXML private void kategoriBtnClicked(ActionEvent event) {
        navigate("Kategori.fxml", event);
    }

    @FXML private void anggaranBtnClicked(ActionEvent event) {
        navigate("Anggaran.fxml", event);
    }

    @FXML private void laporanBtnClicked(ActionEvent event) {
        navigate("Laporan.fxml", event);
    }

    @FXML private void pengaturanBtnClicked(ActionEvent event) {
        navigate("Pengaturan.fxml", event);
    }

    @FXML private void logoutBtnClicked(ActionEvent event) {
        navigate("Login.fxml", event);
    }
}
