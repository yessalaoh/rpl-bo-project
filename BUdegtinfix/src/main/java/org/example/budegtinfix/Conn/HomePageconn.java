package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class HomePageconn {

    @FXML
    void btnNotifClicked(ActionEvent event) {
        loadScene(event, "Notifikasi-view.fxml");    }

    @FXML
    void transaksiBtnClicked(ActionEvent event) {
        loadScene(event, "Transaksi-view.fxml");
    }

    @FXML
    void kategoriBtnClicked(ActionEvent event) {
        loadScene(event, "Kategori.fxml");
    }

    @FXML
    void anggaranBtnClicked(ActionEvent event) {
        loadScene(event, "Anggaran.fxml");
    }

    @FXML
    void laporanBtnClicked(ActionEvent event) {
        loadScene(event, "Laporan.fxml");
    }

    @FXML
    void pengaturanBtnClicked(ActionEvent event) {
        loadScene(event, "Pengaturan.fxml");
    }

    @FXML
    void logoutBtnClicked(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin keluar?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadScene(event, "Login-view.fxml");
        } else {
            System.out.println("Logout dibatalkan.");
        }
    }

    // Method untuk memuat scene
    private void loadScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/budegtinfix/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
