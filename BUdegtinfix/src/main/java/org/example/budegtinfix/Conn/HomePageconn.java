package org.example.budegtinfix.Conn;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.example.budegtinfix.Session.Session;

import java.io.IOException;
import java.util.Optional;

public class HomePageconn {

    @FXML
    private Text helloUserText;

    @FXML
    public void initialize() {
        String nama = Session.getNamaUser();
        if (nama != null) {
            helloUserText.setText("Hallo, " + nama);
        }
    }

    @FXML
    void btnNotifClicked(ActionEvent event) {
        loadScene(event, "Notifikasi-view.fxml");
    }

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
        loadScene(event, "Anggaran-view.fxml");
    }

    @FXML
    void laporanBtnClicked(ActionEvent event) {
        loadScene(event, "Diagram.fxml");
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

        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            Session.clearSession();
            loadScene(event, "Login-view.fxml");
        }
    }

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
