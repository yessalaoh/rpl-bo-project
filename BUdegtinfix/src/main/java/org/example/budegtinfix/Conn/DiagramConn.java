package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert; // Import Alert
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType; // Import ButtonType
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.budegtinfix.Database.CatatanDB; // Pastikan ini diimpor
import org.example.budegtinfix.Session.Session; // Penting: Import Session

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional; // Import Optional

public class DiagramConn {

    @FXML
    private Text helloUserText;

    @FXML
    private PieChart piePemasukan;

    @FXML
    private PieChart piePengeluaran;

    @FXML
    private Button NotifBtn, transaksiBtn, btnKategori, btnAnggaran, btnLaporan, PengaturanBtn, btnLogout;


    @FXML
    public void initialize() {
        if (Session.getIdUser() != 0 && Session.getNamaUser() != null) {
            helloUserText.setText("Halo, " + Session.getNamaUser() + "!");
        } else {
            helloUserText.setText("Halo, Pengguna!");
        }

        loadPieChartData();
    }

    private void loadPieChartData() {
        try (Connection connectDB = CatatanDB.connect()) {
            if (connectDB == null) {
                showAlert(Alert.AlertType.ERROR, "Kesalahan Koneksi", "Gagal terhubung ke database. Diagram tidak dapat dimuat.");
                System.err.println("Database connection is null. Cannot load pie chart data.");
                return;
            }

            if (piePengeluaran != null) piePengeluaran.getData().clear();
            if (piePemasukan != null) piePemasukan.getData().clear();

            int userId = Session.getIdUser();

            String sqlPengeluaran = "SELECT kategori, SUM(jumlah) AS total FROM transaksi WHERE jenis = 'Pengeluaran' AND user_id = ? GROUP BY kategori";
            try (PreparedStatement stmt1 = connectDB.prepareStatement(sqlPengeluaran)) {
                stmt1.setInt(1, userId);
                try (ResultSet rs1 = stmt1.executeQuery()) {
                    while (rs1.next()) {
                        if (piePengeluaran != null) {
                            piePengeluaran.getData().add(
                                    new PieChart.Data(rs1.getString("kategori"), rs1.getDouble("total"))
                            );
                        }
                    }
                }
            }

            String sqlPemasukan = "SELECT kategori, SUM(jumlah) AS total FROM transaksi WHERE jenis = 'Pemasukan' AND user_id = ? GROUP BY kategori";
            try (PreparedStatement stmt2 = connectDB.prepareStatement(sqlPemasukan)) {
                stmt2.setInt(1, userId);
                try (ResultSet rs2 = stmt2.executeQuery()) {
                    while (rs2.next()) {
                        if (piePemasukan != null) {
                            piePemasukan.getData().add(
                                    new PieChart.Data(rs2.getString("kategori"), rs2.getDouble("total"))
                            );
                        }
                    }
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal memuat data diagram: " + e.getMessage());
            System.err.println("Gagal memuat data diagram: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigate(String fxmlPath, ActionEvent event) {
        try {
            String fullPath = "/org/example/budegtinfix/" + fxmlPath;
            URL resourceUrl = getClass().getResource(fullPath);

            if (resourceUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "File FXML tidak ditemukan: " + fullPath);
                System.err.println("Gagal menemukan FXML: " + fullPath);
                return;
            }

            Parent root = FXMLLoader.load(resourceUrl);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman: " + fxmlPath + ". " + e.getMessage());
            System.err.println("Gagal membuka halaman: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML private void btnNotifClicked(ActionEvent event) {
        navigate("Notifikasi-view.fxml", event);
    }

    @FXML private void transaksiBtnClicked(ActionEvent event) {
        navigate("Transaksi-view.fxml", event);
    }

    @FXML private void kategoriBtnClicked(ActionEvent event) {
        navigate("Kategori-view.fxml", event);
    }

    @FXML private void anggaranBtnClicked(ActionEvent event) {
        navigate("Anggaran-view.fxml", event);
    }

    @FXML private void laporanBtnClicked(ActionEvent event) {
        navigate("Diagram-view.fxml", event);
    }

    @FXML private void pengaturanBtnClicked(ActionEvent event) {
        navigate("Pengaturan-view.fxml", event);
    }

    @FXML private void logoutBtnClicked(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin keluar?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Session.clearSession();
            navigate("Login-view.fxml", event);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}