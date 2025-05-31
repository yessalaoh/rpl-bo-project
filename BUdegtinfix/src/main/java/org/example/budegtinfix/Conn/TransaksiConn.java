package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class TransaksiConn {

    @FXML private TreeTableView<?> treeTableTransaksi;
    @FXML private TreeTableColumn<?, ?> colTransaksi;
    @FXML private TreeTableColumn<?, ?> colSpacer;
    @FXML private TreeTableColumn<?, ?> colDeskripsi;
    @FXML private TreeTableColumn<?, ?> colKategori;
    @FXML private TreeTableColumn<?, ?> colJumlah;


    @FXML
    private void transaksiBtnClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifikasi");
        alert.setHeaderText(null);
        alert.setContentText("Anda sedang berada di halaman transaksi");
        alert.showAndWait();
    }

    @FXML
    private void pengaturanBtnClicked(ActionEvent event) {
        loadScene(event, "Pengaturan.fxml");
    }

    @FXML
    private void notifikasiBtnClicked(ActionEvent event) {
        loadScene(event, "Notifikasi-view.fxml");
    }
    @FXML
    private void laporanBtnClicked(ActionEvent event) {
        loadScene(event, "Laporan.fxml");
    }

    @FXML
    private void anggaranBtnClicked(ActionEvent event) {
        loadScene(event, "Anggaran.fxml");
    }

    @FXML
    private void kategoriBtnClicked(ActionEvent event) {
        loadScene(event, "Kategori.fxml");
    }

    @FXML
    private void logoutBtnClicked(ActionEvent event) {
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

    @FXML
    private void tambahBtnClicked(ActionEvent event) {
        loadScene(event, "TambahTransaksi.fxml");
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

    // === Getter untuk akses dari controller lain ===

    public TreeTableView<?> getTreeTableTransaksi() {
        return treeTableTransaksi;
    }

    public TreeTableColumn<?, ?> getColTransaksi() {
        return colTransaksi;
    }

    public TreeTableColumn<?, ?> getColSpacer() {
        return colSpacer;
    }

    public TreeTableColumn<?, ?> getColDeskripsi() {
        return colDeskripsi;
    }

    public TreeTableColumn<?, ?> getColKategori() {
        return colKategori;
    }

    public TreeTableColumn<?, ?> getColJumlah() {
        return colJumlah;
    }
}
