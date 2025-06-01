package org.example.budegtinfix.Conn;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.budegtinfix.Database.TransaksiDAO;
import org.example.budegtinfix.Session.Session;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.List;

public class TransaksiConn implements Initializable {

    // **1. Modifikasi Kelas Transaksi**
    public static class Transaksi {
        private final IntegerProperty id;
        private final StringProperty tanggal, jenis, kategori, deskripsi;
        private final DoubleProperty jumlah;
        private final BooleanProperty memilikiDokumen;
        private final StringProperty imageUrl; // Tambahkan properti untuk URL/path gambar

        public Transaksi(int id, String tanggal, String jenis, String kategori, String deskripsi, double jumlah, boolean memilikiDokumen, String imageUrl) {
            this.id = new SimpleIntegerProperty(id);
            this.tanggal = new SimpleStringProperty(tanggal);
            this.jenis = new SimpleStringProperty(jenis);
            this.kategori = new SimpleStringProperty(kategori);
            this.deskripsi = new SimpleStringProperty(deskripsi);
            this.jumlah = new SimpleDoubleProperty(jumlah);
            this.memilikiDokumen = new SimpleBooleanProperty(memilikiDokumen);
            this.imageUrl = new SimpleStringProperty(imageUrl); // Inisialisasi properti gambar
        }

        public int getId() { return id.get(); }
        public String getTanggal() { return tanggal.get(); }
        public String getJenis() { return jenis.get(); }
        public String getKategori() { return kategori.get(); }
        public String getDeskripsi() { return deskripsi.get(); }
        public double getJumlah() { return jumlah.get(); }
        public boolean isMemilikiDokumen() { return memilikiDokumen.get(); }
        public String getImageUrl() { return imageUrl.get(); } // Getter untuk imageUrl

        public IntegerProperty idProperty() { return id; }
        public StringProperty tanggalProperty() { return tanggal; }
        public StringProperty jenisProperty() { return jenis; }
        public StringProperty kategoriProperty() { return kategori; }
        public StringProperty deskripsiProperty() { return deskripsi; }
        public DoubleProperty jumlahProperty() { return jumlah; }
        public BooleanProperty memilikiDokumenProperty() { return memilikiDokumen; }
        public StringProperty imageUrlProperty() { return imageUrl; } // Property untuk imageUrl
    }

    @FXML private ComboBox<String> comboJenisTransaksi, comboKategori;
    @FXML private DatePicker datePickerStart, datePickerEnd;
    @FXML private Button btnApplyFilter, btnResetFilter, btnTambahTransaksi;
    @FXML private TreeTableView<Transaksi> treeTableTransaksi;
    @FXML private TreeTableColumn<Transaksi, String> colTanggal, colJenis, colKategori, colDeskripsi;
    @FXML private TreeTableColumn<Transaksi, Number> colJumlah;
    @FXML private TreeTableColumn<Transaksi, Boolean> colDokumen;
    @FXML private TreeTableColumn<Transaksi, Void> colAksi;

    // **PERBAIKAN: Hapus baris duplikat ini jika sudah ada di atas**
    // @FXML private TreeTableColumn<Transaksi, String> colGambar;

    // Pastikan hanya ada satu deklarasi ini:
    @FXML private TreeTableColumn<Transaksi, String> colGambar;

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private ObservableList<Transaksi> currentDisplayedTransaksi = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboJenisTransaksi.getItems().addAll("Semua", "Pemasukan", "Pengeluaran");
        comboJenisTransaksi.getSelectionModel().selectFirst();
        comboKategori.getItems().addAll("Semua Kategori", "Makanan", "Transportasi", "Gaji", "Tabungan", "Hiburan", "Tagihan", "Pendidikan", "Investasi", "Lain-lain");
        comboKategori.getSelectionModel().selectFirst();

        setupTreeTableView();
        loadAllDataForCurrentUser(); // Memuat semua data awal untuk user saat ini
    }

    private void setupTreeTableView() {
        colTanggal.setCellValueFactory(new TreeItemPropertyValueFactory<>("tanggal"));
        colJenis.setCellValueFactory(new TreeItemPropertyValueFactory<>("jenis"));
        colKategori.setCellValueFactory(new TreeItemPropertyValueFactory<>("kategori"));
        colDeskripsi.setCellValueFactory(new TreeItemPropertyValueFactory<>("deskripsi"));
        colJumlah.setCellValueFactory(new TreeItemPropertyValueFactory<>("jumlah"));
        colDokumen.setCellValueFactory(new TreeItemPropertyValueFactory<>("memilikiDokumen"));

        colJumlah.setCellFactory(tc -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("Rp%,.2f", item.doubleValue()));
                    TreeItem<Transaksi> treeItem = getTreeTableRow().getTreeItem();
                    if (treeItem != null && treeItem.getValue() != null) {
                        setStyle("-fx-text-fill: " + ("Pemasukan".equals(treeItem.getValue().getJenis()) ? "green" : "red") + ";");
                    }
                }
            }
        });

        colDokumen.setCellFactory(tc -> new TreeTableCell<>() {
            private final ImageView icon = new ImageView();
            {
                icon.setFitHeight(16);
                icon.setFitWidth(16);
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || !item) {
                    setGraphic(null);
                } else {
                    try {
                        icon.setImage(new Image(getClass().getResourceAsStream("/org/example/budegtinfix/images/attachment.png")));
                        setGraphic(icon);
                    } catch (Exception e) {
                        System.err.println("Gagal load ikon dokumen: " + e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });

        // **3. Implementasi CellFactory untuk colGambar**
        colGambar.setCellValueFactory(new TreeItemPropertyValueFactory<>("imageUrl")); // Mengambil URL gambar dari properti imageUrl

        colGambar.setCellFactory(tc -> new TreeTableCell<Transaksi, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50); // Atur tinggi gambar
                imageView.setPreserveRatio(true); // Pertahankan rasio aspek
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        Image image;
                        if (imageUrl.startsWith("file:/")) { // Ini menunjukkan URI file lokal
                            image = new Image(imageUrl, true); // true untuk background loading
                        } else { // Asumsikan itu adalah path resource internal
                            URL resourceUrl = getClass().getResource(imageUrl);
                            if (resourceUrl != null) {
                                image = new Image(resourceUrl.toExternalForm(), true);
                            } else {
                                // Jika tidak ditemukan sebagai resource, coba sebagai file path
                                File file = new File(imageUrl);
                                if (file.exists()) {
                                    image = new Image(file.toURI().toString(), true);
                                } else {
                                    System.err.println("Gambar tidak ditemukan sebagai resource atau file: " + imageUrl);
                                    setGraphic(null);
                                    setText("Gambar Tidak Ada");
                                    return;
                                }
                            }
                        }
                        imageView.setImage(image);
                        setGraphic(imageView);
                        setText(null);
                    } catch (Exception e) {
                        System.err.println("Gagal memuat gambar dari URL/Path: " + imageUrl + " - " + e.getMessage());
                        setGraphic(null);
                        setText("Error Load"); // Tampilkan teks jika gambar gagal dimuat
                    }
                }
            }
        });


        colAksi.setCellFactory(tc -> new TreeTableCell<>() {
            private final Button deleteBtn = new Button("Hapus");
            {
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12;");
                deleteBtn.setOnAction(event -> hapusTransaksi(getTreeTableRow().getItem()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTreeTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, deleteBtn));
                }
            }
        });
    }

    private void loadAllDataForCurrentUser() {
        try {
            currentDisplayedTransaksi.setAll(transaksiDAO.getAllTransaksiForCurrentUser());
            refreshTreeTable(currentDisplayedTransaksi);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data dari database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFilteredDataFromDatabase(String jenis, String kategori, LocalDate startDate, LocalDate endDate) {
        try {
            currentDisplayedTransaksi.setAll(transaksiDAO.getFilteredTransaksi(jenis, kategori, startDate, endDate));
            refreshTreeTable(currentDisplayedTransaksi);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data transaksi berdasarkan filter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshTreeTable(ObservableList<Transaksi> list) {
        TreeItem<Transaksi> root = new TreeItem<>();
        if (list != null && !list.isEmpty()) {
            list.forEach(t -> root.getChildren().add(new TreeItem<>(t)));
        }
        treeTableTransaksi.setRoot(root);
        treeTableTransaksi.setShowRoot(false);
    }

    private void hapusTransaksi(Transaksi transaksi) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin menghapus transaksi?\n" + transaksi.getDeskripsi());

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (transaksiDAO.deleteTransaksi(transaksi.getId())) {
                    applyFilterClicked();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Transaksi berhasil dihapus.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus transaksi dari database.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan saat menghapus transaksi: " + e.getMessage());
            }
        }
    }

    @FXML
    private void tambahBtnClicked(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/TambahTransaksi-view.fxml");
            System.out.println("FXML Location: " + fxmlLocation);

            if (fxmlLocation == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "File FXML TambahTransaksi-view.fxml tidak ditemukan! Periksa path resource.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            int currentUserId = Session.getIdUser();
            TambahTransaksiConn controller = loader.getController();
            controller.setCurrentUserId(currentUserId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Tambah Transaksi");
            stage.showAndWait();

            applyFilterClicked();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka form tambah transaksi: " + e.getMessage());
        }
    }

    @FXML
    private void applyFilterClicked() {
        String jenis = comboJenisTransaksi.getValue();
        String kategori = comboKategori.getValue();
        LocalDate start = datePickerStart.getValue();
        LocalDate end = datePickerEnd.getValue();

        loadFilteredDataFromDatabase(jenis, kategori, start, end);
    }

    @FXML
    private void resetFilterClicked() {
        comboJenisTransaksi.getSelectionModel().selectFirst();
        comboKategori.getSelectionModel().selectFirst();
        datePickerStart.setValue(null);
        datePickerEnd.setValue(null);
        loadAllDataForCurrentUser();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation Buttons
    @FXML private void pengaturanBtnClicked(ActionEvent e) { loadScene(e, "Pengaturan-view.fxml"); }
    @FXML private void notifikasiBtnClicked(ActionEvent e) { loadScene(e, "Notifikasi-view.fxml"); }
    @FXML private void laporanBtnClicked(ActionEvent e) { loadScene(e, "Diagram.fxml"); }
    @FXML private void anggaranBtnClicked(ActionEvent e) { loadScene(e, "Anggaran-view.fxml"); }
    @FXML private void kategoriBtnClicked(ActionEvent e) { loadScene(e, "Kategori-view.fxml"); }
    @FXML private void logoutBtnClicked(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setContentText("Yakin ingin logout?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Session.clearSession();
            loadScene(e, "Login-view.fxml");
        }
    }

    private void loadScene(ActionEvent event, String fxmlFile) {
        try {
            URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/" + fxmlFile);
            if (fxmlLocation == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "File FXML " + fxmlFile + " tidak ditemukan! Periksa path resource.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat halaman: " + fxmlFile);
            e.printStackTrace();
        }
    }
}