package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.budegtinfix.Database.CatatanDB;
import org.example.budegtinfix.Session.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginConn {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    protected void login(ActionEvent event) {
        final String username = usernameField.getText().trim();
        final String passwordInput = passwordField.getText();

        if (username.isEmpty() || passwordInput.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Silakan masukkan username dan password.");
            return;
        }

        try (Connection conn = CatatanDB.connect()) {
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Koneksi ke database gagal.");
                return;
            }

            String query = "SELECT id, username, email FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, passwordInput);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        int idUser = rs.getInt("id");


                        Session.setNamaUser(username);
                        Session.setIdUser(idUser);

                        navigateToHome(event);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau password salah.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Kesalahan saat login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Login Error", "Terjadi kesalahan saat login. Coba lagi.");
        }
    }

    private void navigateToHome(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/HomePage-view.fxml");
            if (fxmlLocation == null) {
                System.err.println("FXML 'HomePage-view.fxml' tidak ditemukan.");
                showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal memuat halaman utama.");
                return;
            }

            Parent root = FXMLLoader.load(fxmlLocation);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal membuka halaman utama.");
        }
    }

    @FXML
    protected void signUp(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/SignUp-view.fxml");
            if (fxmlLocation == null) {
                System.err.println("FXML 'SignUp-view.fxml' tidak ditemukan.");
                showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal memuat halaman pendaftaran.");
                return;
            }

            Parent root = FXMLLoader.load(fxmlLocation);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal membuka halaman pendaftaran.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}