package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.budegtinfix.Database.CatatanDB;
import org.example.budegtinfix.Database.UserDB; // Pastikan ini ada dan berfungsi

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

// Jika Anda ingin menggunakan BCrypt, Anda perlu menambahkan library ke pom.xml atau build.gradle
// Contoh dependency Maven:
/*
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
*/
// import org.mindrot.jbcrypt.BCrypt; // Uncomment jika Anda sudah menambahkan library BCrypt

public class SignUPConn implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField; // Untuk input password yang tersembunyi

    @FXML
    private TextField passwordVisibleField; // Untuk menampilkan password saat checkbox dicentang

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private TextField emailField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Pastikan UserDB.createUserTable() hanya dipanggil sekali saat aplikasi startup
        // atau pastikan tabel sudah ada untuk menghindari re-kreasi yang tidak perlu.
        UserDB.createUserTable();

        // Binding dua arah agar nilai passwordField dan passwordVisibleField selalu sama
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        // Listener untuk mengatur visibilitas field password
        showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) { // Jika checkbox dicentang (show password)
                passwordVisibleField.setVisible(true);
                passwordVisibleField.setManaged(true); // Memastikan field terlihat dan memengaruhi layout
                passwordField.setVisible(false);
                passwordField.setManaged(false); // Memastikan field tidak terlihat dan tidak memengaruhi layout
            } else { // Jika checkbox tidak dicentang (hide password)
                passwordVisibleField.setVisible(false);
                passwordVisibleField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
            }
        });

        // Set awal: passwordVisibleField tidak terlihat, passwordField terlihat
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true); // Pastikan ini juga di set
    }

    @FXML
    protected void btnSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String passwordRaw = passwordField.getText(); // Password mentah dari input
        String email = emailField.getText().trim();

        if (username.isEmpty() || passwordRaw.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Silakan isi semua kolom.");
            return;
        }

        if (!isPasswordValid(passwordRaw)) {
            showAlert(Alert.AlertType.ERROR, "Password Tidak Valid",
                    "Password harus minimal 8 karakter dan mengandung huruf besar, angka, dan simbol.");
            return;
        }

        // --- BAGIAN KRITIS: KEAMANAN PASSWORD ---
        // JANGAN PERNAH MENYIMPAN PASSWORD MENTAH DI DATABASE!
        // Gunakan hashing password yang kuat seperti BCrypt.

        String hashedPassword;
        try {
            // Contoh menggunakan BCrypt (jika Anda sudah menambahkan dependency jbcrypt)
            // hashedPassword = BCrypt.hashpw(passwordRaw, BCrypt.gensalt());
            // TODO: UNCOMMENT BARIS DI ATAS DAN HAPUS BARIS DI BAWAH KETIKA MENGGUNAKAN BCRYPT
            hashedPassword = passwordRaw; // HANYA UNTUK TUJUAN PENGEMBANGAN/DEBUGGING (TIDAK AMAN)

        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memproses password.");
            return;
        }

        String sql = "INSERT INTO users(username, email, password) VALUES(?, ?, ?)";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal terhubung ke database.");
                return;
            }

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword); // Simpan password yang sudah di-hash
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Berhasil!", "Akun berhasil dibuat.");

            // Arahkan ke halaman login setelah berhasil mendaftar
            navigateToLogin(event);
        } catch (SQLException e) {
            // Tangani error jika username atau email sudah terdaftar (tergantung constraint DB Anda)
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique constraint failed")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Username atau email sudah terdaftar.");
            } else {
                System.err.println("SQL Exception: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan saat menyimpan data.");
            }
        }
    }

    @FXML
    protected void goBackToLogin(ActionEvent event) {
        navigateToLogin(event);
    }

    private void navigateToLogin(ActionEvent event) {
        try {
            // Pastikan path ini benar sesuai struktur src/main/resources
            // Contoh: src/main/resources/org/example/budegtinfix/Login-view.fxml
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/Login-view.fxml");

            if (fxmlLocation == null) {
                System.err.println("ERROR: FXML file 'Login-view.fxml' not found. Check its path and location.");
                showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal menemukan halaman login.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation); // Gunakan instance FXMLLoader
            Parent root = loader.load();

            // Mendapatkan Stage dari event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Budgetin - Login"); // Atur judul Stage baru
            stage.show();
        } catch (IOException e) {
            System.err.println("IO Exception during navigation to Login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal membuka halaman login.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }
        if (!password.matches(".*[A-Z].*")) { // Harus mengandung setidaknya satu huruf besar
            return false;
        }
        if (!password.matches(".*\\d.*")) { // Harus mengandung setidaknya satu angka
            return false;
        }
        // Perbaikan: Harus mengandung setidaknya satu simbol (non-huruf, non-angka, non-spasi)
        if (!password.matches(".*[^a-zA-Z0-9 ].*")) {
            return false; // Jika tidak ada simbol, return false
        }
        return true;
    }
}