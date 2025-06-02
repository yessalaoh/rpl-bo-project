package org.example.budegtinfix.Conn;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.budegtinfix.Database.KategoriDAO;
import org.example.budegtinfix.Session.Session;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class KategoriCon implements Initializable {

    @FXML
    private TableView<Kategori> tableKategori;

    @FXML
    private TableColumn<Kategori, String> colNamaKategori;

    @FXML
    private TextField txtNamaKategori;

    @FXML
    private Button btnTambah, btnEdit, btnHapus, btnReset;

    private final KategoriDAO kategoriDAO = new KategoriDAO();
    private final ObservableList<Kategori> kategoriList = FXCollections.observableArrayList();
    private Kategori selectedKategori = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNamaKategori.setCellValueFactory(new PropertyValueFactory<>("nama"));

        // Disable tombol Edit dan Hapus saat awal aplikasi
        btnEdit.setDisable(true);
        btnHapus.setDisable(true);

        loadKategoriData();
        setupTableSelectionListener();

        // Optional: Disable tombol Tambah jika nama kategori kosong
        btnTambah.disableProperty().bind(
                txtNamaKategori.textProperty().isEmpty()
        );
    }

    private void loadKategoriData() {
        try {
            kategoriList.setAll(kategoriDAO.getAllKategoriByUserId(Session.getIdUser()));
            tableKategori.setItems(kategoriList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data kategori: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableSelectionListener() {
        tableKategori.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedKategori = newSelection;
                txtNamaKategori.setText(selectedKategori.getNama());
                btnEdit.setDisable(false);
                btnHapus.setDisable(false);
            } else {
                resetForm();  // Jika tidak ada yang dipilih, reset form
            }
        });
    }

    @FXML
    private void handleTambah(ActionEvent event) {
        String namaKategori = txtNamaKategori.getText().trim();
        if (namaKategori.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama kategori tidak boleh kosong.");
            return;
        }

        try {
            if (kategoriDAO.insertKategori(Session.getIdUser(), namaKategori)) {
                loadKategoriData();
                resetForm();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kategori berhasil ditambahkan.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menambahkan kategori.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan saat menambahkan kategori: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (selectedKategori == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kategori yang ingin diedit.");
            return;
        }

        String namaKategori = txtNamaKategori.getText().trim();
        if (namaKategori.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama kategori tidak boleh kosong.");
            return;
        }

        try {
            if (kategoriDAO.updateKategori(selectedKategori.getId(), namaKategori)) {
                loadKategoriData();
                resetForm();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kategori berhasil diperbarui.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memperbarui kategori.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan saat memperbarui kategori: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHapus(ActionEvent event) {
        if (selectedKategori == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kategori yang ingin dihapus.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin menghapus kategori \"" + selectedKategori.getNama() + "\"?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (kategoriDAO.deleteKategori(selectedKategori.getId())) {
                    loadKategoriData();
                    resetForm();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kategori berhasil dihapus.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus kategori.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan saat menghapus kategori: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleReset(ActionEvent event) {
        resetForm();
    }

    private void resetForm() {
        txtNamaKategori.clear();
        tableKategori.getSelectionModel().clearSelection();
        selectedKategori = null;
        btnEdit.setDisable(true);
        btnHapus.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Kategori {
        private final int id;
        private final StringProperty nama;

        public Kategori(int id, String nama) {
            this.id = id;
            this.nama = new SimpleStringProperty(nama);
        }

        public int getId() {
            return id;
        }

        public String getNama() {
            return nama.get();
        }

        public StringProperty namaProperty() {
            return nama;
        }
    }
}
