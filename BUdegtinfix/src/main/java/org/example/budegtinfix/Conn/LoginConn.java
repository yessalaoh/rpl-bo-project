package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
// import javafx.scene.control.TextField; // Hapus atau biarkan jika masih ada TextField lain
import javafx.scene.control.PasswordField; // <--- BARIS INI PENTING! Import PasswordField
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.budegtinfix.Database.CatatanDB;

public class LoginConn {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField; // <--- UBAH TIPE INI DARI TextField MENJADI PasswordField

    @FXML
    protected void login(ActionEvent event) {
        final String username = usernameField.getText().trim();
        final String passwordInput = passwordField.getText(); // getText() tetap berfungsi pada PasswordField

        if (username.isEmpty() || passwordInput.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please enter your username and password.");
            return;
        }

        try (Connection conn = CatatanDB.connect()) {
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
                return;
            }

            String query = "SELECT id, username, email, password FROM users WHERE username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        String hashedPasswordFromDb = rs.getString("password");

                        // PERINGATAN KEAMANAN KRITIS: JANGAN PERNAH MEMBANDINGKAN PASSWORD MENTAH!
                        // Gunakan hashing password yang kuat (misalnya BCrypt.checkpw)
                        if (passwordInput.equals(hashedPasswordFromDb)) {
                            int idUser = rs.getInt("id");
                            String email = rs.getString("email");
                            navigateToHome(event, username, email);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password.");
                        }

                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Login Error", "An error occurred during login. Please try again later.");
        }
    }

    private void navigateToHome(ActionEvent event, String username, String email) {
        try {
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/HomePage-view.fxml");
            if (fxmlLocation == null) {
                System.err.println("ERROR: FXML file 'HomePage-view.fxml' not found. Check its path and location.");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to find home page FXML.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Budgetin - Home");
            stage.show();
        } catch (IOException e) {
            System.err.println("IO Exception during navigation to Home: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the home page.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void signUp(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/SignUp-view.fxml");

            if (fxmlLocation == null) {
                System.err.println("ERROR: FXML file 'SignUp-view.fxml' not found. Check its path and location.");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to find sign-up page FXML.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Budgetin - Sign Up");
            stage.show();
        } catch (IOException e) {
            System.err.println("IO Exception during navigation to Sign Up: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the sign-up page.");
        }
    }
}