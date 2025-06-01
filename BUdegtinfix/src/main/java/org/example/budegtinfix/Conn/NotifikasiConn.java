package org.example.budegtinfix.Conn;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Import URL

public class NotifikasiConn {

    @FXML
    private Button Kembali; // Pastikan id fx:id="Kembali" di FXML

    @FXML
    private void KembaliBtnClicked() {
        try {
            // Perbaikan utama: Pastikan jalur FXML sudah benar.
            // Asumsikan HomePage-view.fxml ada di src/main/resources/org/example/budegtinfix/
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/HomePage-view.fxml");

            if (fxmlLocation == null) {
                System.err.println("ERROR: FXML file 'HomePage-view.fxml' not found. Check its path and location.");
                // Anda mungkin ingin menampilkan Alert kepada pengguna di sini
                // showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal menemukan halaman utama.");
                return; // Keluar dari metode jika FXML tidak ditemukan
            }

            // Gunakan FXMLLoader.load(URL) yang sudah memiliki URL yang valid
            Parent root = FXMLLoader.load(fxmlLocation);

            // Mendapatkan Stage dari tombol yang memicu aksi
            // Ini adalah cara yang benar untuk mendapatkan Stage yang sedang aktif
            Stage stage = (Stage) Kembali.getScene().getWindow(); // Mendapatkan Stage dari tombol "Kembali" itu sendiri

            stage.setScene(new Scene(root));
            stage.setTitle("Budgetin - Home Page"); // Atur judul Stage baru
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Gagal memuat halaman: " + e.getMessage());
            // Anda mungkin ingin menampilkan Alert kepada pengguna di sini juga
            // showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Terjadi kesalahan saat memuat halaman.");
        } catch (NullPointerException e) {
            // Tangani NullPointerException jika getScene() atau getWindow() mengembalikan null
            // Ini bisa terjadi jika tombol 'Kembali' belum terpasang ke Scene saat metode dipanggil
            System.err.println("NPE: Tombol Kembali mungkin belum terpasang ke Scene.");
            e.printStackTrace();
        }
    }

}