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
import javafx.scene.text.Text;
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

    public static class Transaksi {
        private final IntegerProperty id;
        private final StringProperty tanggal, jenis, kategori, deskripsi;
        private final DoubleProperty jumlah;
        private final BooleanProperty memilikiDokumen;
        private final StringProperty imageUrl;

        public Transaksi(int id, String tanggal, String jenis, String kategori, String deskripsi, double jumlah, boolean memilikiDokumen, String imageUrl) {
            this.id = new SimpleIntegerProperty(id);
            this.tanggal = new SimpleStringProperty(tanggal);
            this.jenis = new SimpleStringProperty(jenis);
            this.kategori = new SimpleStringProperty(kategori);
            this.deskripsi = new SimpleStringProperty(deskripsi);
            this.jumlah = new SimpleDoubleProperty(jumlah);
            this.memilikiDokumen = new SimpleBooleanProperty(memilikiDokumen);
            this.imageUrl = new SimpleStringProperty(imageUrl);
        }

        public int getId() { return id.get(); }
        public String getTanggal() { return tanggal.get(); }
        public String getJenis() { return jenis.get(); }
        public String getKategori() { return kategori.get(); }
        public String getDeskripsi() { return deskripsi.get(); }
        public double getJumlah() { return jumlah.get(); }
        public boolean isMemilikiDokumen() { return memilikiDokumen.get(); }
        public String getImageUrl() { return imageUrl.get(); }

        public IntegerProperty idProperty() { return id; }
        public StringProperty tanggalProperty() { return tanggal; }
        public StringProperty jenisProperty() { return jenis; }
        public StringProperty kategoriProperty() { return kategori; }
        public StringProperty deskripsiProperty() { return deskripsi; }
        public DoubleProperty jumlahProperty() { return jumlah; }
        public BooleanProperty memilikiDokumenProperty() { return memilikiDokumen; }
        public StringProperty imageUrlProperty() { return imageUrl; }
    }

    @FXML private ComboBox<String> comboJenisTransaksi, comboKategori;
    @FXML private DatePicker datePickerStart, datePickerEnd;
    @FXML private Button btnApplyFilter, btnResetFilter, btnTambahTransaksi;
    @FXML private TreeTableView<Transaksi> treeTableTransaksi;
    @FXML private TreeTableColumn<Transaksi, String> colTanggal, colJenis, colKategori, colDeskripsi;
    @FXML private TreeTableColumn<Transaksi, Number> colJumlah;
    @FXML private TreeTableColumn<Transaksi, Boolean> colDokumen;
    @FXML private TreeTableColumn<Transaksi, Void> colAksi;
    @FXML private TreeTableColumn<Transaksi, String> colGambar;

    @FXML private Text helloUserText;
    @FXML private Label labelSaldo; // **DEKLARASI LABEL SALDO BARU**

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private ObservableList<Transaksi> currentDisplayedTransaksi = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.getIdUser() != 0 && Session.getNamaUser() != null) {
            helloUserText.setText("Halo, " + Session.getNamaUser() + "!");
        } else {
            helloUserText.setText("Halo, Pengguna!");
        }

        comboJenisTransaksi.getItems().addAll("Semua", "Pemasukan", "Pengeluaran");
        comboJenisTransaksi.getSelectionModel().selectFirst();
        comboKategori.getItems().addAll("Semua Kategori", "Makanan", "Transportasi", "Gaji", "Tabungan", "Hiburan", "Tagihan", "Pendidikan", "Investasi", "Lain-lain");
        comboKategori.getSelectionModel().selectFirst();

        setupTreeTableView();
        loadAllDataForCurrentUser();
        updateSaldoDisplay(); // **Panggil metode untuk memperbarui saldo saat inisialisasi**
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
                        // Pastikan path ini benar untuk ikon lampiran Anda
                        icon.setImage(new Image(getClass().getResourceAsStream("/org/example/budegtinfix/Image/attachment.png")));
                        setGraphic(icon);
                    } catch (Exception e) {
                        System.err.println("Gagal load ikon dokumen: " + e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });

        colGambar.setCellValueFactory(new TreeItemPropertyValueFactory<>("imageUrl"));

        colGambar.setCellFactory(tc -> new TreeTableCell<Transaksi, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
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
                        if (imageUrl.startsWith("file:/")) {
                            image = new Image(imageUrl, true);
                        } else {
                            URL resourceUrl = getClass().getResource(imageUrl);
                            if (resourceUrl != null) {
                                image = new Image(resourceUrl.toExternalForm(), true);
                            } else {
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
                        setText("Error Load");
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
            updateSaldoDisplay(); // **Perbarui saldo setelah memuat data**
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data dari database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFilteredDataFromDatabase(String jenis, String kategori, LocalDate startDate, LocalDate endDate) {
        try {
            currentDisplayedTransaksi.setAll(transaksiDAO.getFilteredTransaksi(jenis, kategori, startDate, endDate));
            refreshTreeTable(currentDisplayedTransaksi);
            // Saldo tidak perlu diperbarui di sini karena filter tidak mengubah total saldo
            // unless Anda ingin menampilkan saldo hanya untuk transaksi yang difilter.
            // Untuk saldo keseluruhan, updateSaldoDisplay() hanya dipanggil di loadAllDataForCurrentUser()
            // dan setelah transaksi ditambahkan/dihapus.
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
                    applyFilterClicked(); // Muat ulang data yang difilter
                    updateSaldoDisplay(); // **Perbarui saldo setelah penghapusan**
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

            Stage dialogStage = new Stage();
            dialogStage.setScene(new Scene(root));
            dialogStage.setTitle("Tambah Transaksi");
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if (controller.isTransactionAdded()) {
                applyFilterClicked(); // Muat ulang data yang difilter
                updateSaldoDisplay(); // **Perbarui saldo setelah penambahan**
            }
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
        // Saldo tidak perlu diperbarui di sini karena filter tidak mengubah total saldo
    }

    @FXML
    private void resetFilterClicked() {
        comboJenisTransaksi.getSelectionModel().selectFirst();
        comboKategori.getSelectionModel().selectFirst();
        datePickerStart.setValue(null);
        datePickerEnd.setValue(null);
        loadAllDataForCurrentUser(); // Ini akan memanggil updateSaldoDisplay()
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // **METODE BARU: Perbarui Tampilan Saldo**
    private void updateSaldoDisplay() {
        if (Session.getIdUser() != 0) {
            double saldo = transaksiDAO.getTotalSaldo(Session.getIdUser());
            labelSaldo.setText(String.format("Saldo: Rp %,.2f", saldo));
            // Anda bisa tambahkan logika untuk mengubah warna saldo jika negatif, dll.
            if (saldo < 0) {
                labelSaldo.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: red;");
            } else {
                labelSaldo.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #0698ff;");
            }
        } else {
            labelSaldo.setText("Saldo: N/A");
            labelSaldo.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: gray;");
        }
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