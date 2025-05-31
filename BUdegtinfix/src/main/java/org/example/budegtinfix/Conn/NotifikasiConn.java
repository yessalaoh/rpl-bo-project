package org.example.budegtinfix.Conn;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class NotifikasiConn {

    @FXML
    private Button Kembali;

    @FXML
    private void KembaliBtnClicked() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("HomePage-view.fxml"));
            Stage stage = (Stage) Kembali.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Gagal memuat halaman: " + e.getMessage());
        }
    }


}