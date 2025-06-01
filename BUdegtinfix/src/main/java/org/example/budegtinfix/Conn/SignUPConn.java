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
import org.example.budegtinfix.Database.UserDB;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SignUPConn implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private TextField emailField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserDB.createUserTable();

        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordVisibleField.setVisible(true);
                passwordVisibleField.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
            } else {
                passwordVisibleField.setVisible(false);
                passwordVisibleField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
            }
        });

        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
    }

    @FXML
    protected void btnSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String passwordRaw = passwordField.getText();
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


        String hashedPassword;
        try {
            hashedPassword = passwordRaw;

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
            pstmt.setString(3, hashedPassword);
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Berhasil!", "Akun berhasil dibuat.");

            navigateToLogin(event);
        } catch (SQLException e) {
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
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/Login-view.fxml");

            if (fxmlLocation == null) {
                System.err.println("ERROR: FXML file 'Login-view.fxml' not found. Check its path and location.");
                showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal menemukan halaman login.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Budgetin - Login");
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
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        if (!password.matches(".*[^a-zA-Z0-9 ].*")) {
            return false;
        }
        return true;
    }
}