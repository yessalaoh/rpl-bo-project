package org.example.monitoringta1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class LoginConn {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField PasswordField;
    public static String tampung;
    private Connection conn;

//        public void setConnection() throws SQLException {
//            this.conn = DriverManager.getConnection("jdbc:sqlite:MEMBERSHIP.db");
//        }
//
//        public void initialize() throws SQLException {
//            setConnection();
//        }

        @FXML
        protected void BtnLogin(ActionEvent event) throws SQLException {
            final String username = usernameField.getText();
            final String password = PasswordField.getText();
            tampung = username;

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Form Error", "Please enter your username and password.");
                return;
            } else {
                try {
                    String query = "SELECT * FROM users WHERE username = ? AND password = ?";
                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setString(1, username);
                    statement.setString(2, password);
                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        int idUser = rs.getInt("id_USER");
                        String email = rs.getString("email");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Error logging in: " + e.getMessage());
                }
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
        protected void btnSignUp(ActionEvent event) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("Regis.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                System.out.println("IO Exception: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the sign-up page.");
            }

        }}