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
import javafx.util.Callback;
import org.example.budegtinfix.Database.TransaksiDAO;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class TransaksiConn implements Initializable {
    public static class Transaksi {
        private final IntegerProperty id;
        private final StringProperty tanggal;
        private final StringProperty jenis;
        private final StringProperty kategori;
        private final StringProperty deskripsi;
        private final DoubleProperty jumlah;
        private final BooleanProperty memilikiDokumen;

        public Transaksi(int id, String tanggal, String jenis, String kategori,
                         String deskripsi, double jumlah, boolean memilikiDokumen) {
            this.id = new SimpleIntegerProperty(id);
            this.tanggal = new SimpleStringProperty(tanggal);
            this.jenis = new SimpleStringProperty(jenis);
            this.kategori = new SimpleStringProperty(kategori);
            this.deskripsi = new SimpleStringProperty(deskripsi);
            this.jumlah = new SimpleDoubleProperty(jumlah);
            this.memilikiDokumen = new SimpleBooleanProperty(memilikiDokumen);
        }

        public Transaksi(String tanggal, String jenis, String kategori,
                         String deskripsi, double jumlah, boolean memilikiDokumen) {
            this(0, tanggal, jenis, kategori, deskripsi, jumlah, memilikiDokumen);
        }

        public int getId() { return id.get(); }
        public String getTanggal() { return tanggal.get(); }
        public String getJenis() { return jenis.get(); }
        public String getKategori() { return kategori.get(); }
        public String getDeskripsi() { return deskripsi.get(); }
        public double getJumlah() { return jumlah.get(); }
        public boolean isMemilikiDokumen() { return memilikiDokumen.get(); }

        public IntegerProperty idProperty() { return id; }
        public StringProperty tanggalProperty() { return tanggal; }
        public StringProperty jenisProperty() { return jenis; }
        public StringProperty kategoriProperty() { return kategori; }
        public StringProperty deskripsiProperty() { return deskripsi; }
        public DoubleProperty jumlahProperty() { return jumlah; }
        public BooleanProperty memilikiDokumenProperty() { return memilikiDokumen; }
    }

    @FXML private ComboBox<String> comboJenisTransaksi;
    @FXML private DatePicker datePickerStart;
    @FXML private DatePicker datePickerEnd;
    @FXML private ComboBox<String> comboKategori;
    @FXML private Button btnApplyFilter;
    @FXML private Button btnResetFilter;
    @FXML private TreeTableView<Transaksi> treeTableTransaksi;
    @FXML private TreeTableColumn<Transaksi, String> colTanggal;
    @FXML private TreeTableColumn<Transaksi, String> colJenis;
    @FXML private TreeTableColumn<Transaksi, String> colKategori;
    @FXML private TreeTableColumn<Transaksi, String> colDeskripsi;
    @FXML private TreeTableColumn<Transaksi, Number> colJumlah;
    @FXML private TreeTableColumn<Transaksi, Boolean> colDokumen;
    @FXML private TreeTableColumn<Transaksi, Void> colAksi;
    @FXML private Button btnTambahTransaksi;

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final ObservableList<Transaksi> dataTransaksi = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboJenisTransaksi.getItems().addAll("Semua", "Pemasukan", "Pengeluaran");
        comboJenisTransaksi.getSelectionModel().selectFirst();

        comboKategori.getItems().addAll(
                "Semua Kategori",
                "Makanan",
                "Transportasi",
                "Gaji",
                "Tabungan",
                "Hiburan",
                "Tagihan",
                "Pendidikan",
                "Investasi",
                "Lain-lain"
        );
        comboKategori.getSelectionModel().selectFirst();
        comboKategori.setStyle("-fx-font-size: 14px;");

        setupTreeTableView();

        loadDataFromDatabase();
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
                        if ("Pemasukan".equals(treeItem.getValue().getJenis())) {
                            setStyle("-fx-text-fill: green;");
                        } else {
                            setStyle("-fx-text-fill: red;");
                        }
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
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if (item) {
                        try {
                            icon.setImage(new Image(getClass().getResourceAsStream("/org/example/budegtinfix/images/attachment.png")));
                            setGraphic(icon);
                        } catch (Exception e) {
                            setGraphic(null);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        colAksi.setCellFactory(new Callback<>() {
            @Override
            public TreeTableCell<Transaksi, Void> call(TreeTableColumn<Transaksi, Void> param) {
                return new TreeTableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Hapus");

                    {
                        editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12;");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12;");

//                        editBtn.setOnAction(event -> {
//                            Transaksi transaksi = getTreeTableRow().getItem();
//                            editTransaksi(transaksi);
//                        });

                        deleteBtn.setOnAction(event -> {
                            Transaksi transaksi = getTreeTableRow().getItem();
                            hapusTransaksi(transaksi);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(5, editBtn, deleteBtn);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    private void loadDataFromDatabase() {
        try {
            dataTransaksi.clear();
            dataTransaksi.addAll(transaksiDAO.getAllTransaksi());

            TreeItem<Transaksi> root = new TreeItem<>();
            dataTransaksi.forEach(transaksi -> root.getChildren().add(new TreeItem<>(transaksi)));
            treeTableTransaksi.setRoot(root);
            treeTableTransaksi.setShowRoot(false);
        } catch (Exception e) {
            showAlert("Error", "Gagal memuat data dari database");
            e.printStackTrace();
        }
    }

//    private void editTransaksi(Transaksi transaksi) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/budegtinfix/EditTransaksi-view.fxml"));
//            Parent root = loader.load();
//
//            EditTransaksiConn controller = loader.getController();
//            controller.setTransaksiData(transaksi);
//            controller.setTransaksiDAO(transaksiDAO);
//            controller.setTransaksiConn(this);
//
//            Stage stage = new Stage();
//            stage.setScene(new Scene(root));
//            stage.setTitle("Edit Transaksi");
//            stage.showAndWait();
//
//            loadDataFromDatabase();
//        } catch (IOException e) {
//            e.printStackTrace();
//            showAlert("Error", "Gagal membuka form edit transaksi");
//        }
//    }

    private void hapusTransaksi(Transaksi transaksi) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin menghapus transaksi ini?\n" +
                "Tanggal: " + transaksi.getTanggal() + "\n" +
                "Deskripsi: " + transaksi.getDeskripsi() + "\n" +
                "Jumlah: Rp" + String.format("%,.2f", transaksi.getJumlah()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (transaksiDAO.deleteTransaksi(transaksi.getId())) {
                    dataTransaksi.remove(transaksi);
                    TreeItem<Transaksi> root = new TreeItem<>();
                    dataTransaksi.forEach(t -> root.getChildren().add(new TreeItem<>(t)));
                    treeTableTransaksi.setRoot(root);
                    showAlert("Sukses", "Transaksi berhasil dihapus");
                }
            } catch (Exception e) {
                showAlert("Error", "Gagal menghapus transaksi dari database");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void tambahBtnClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/budegtinfix/TambahTransaksi-view.fxml"));
            Parent root = loader.load();

//            TambahTransaksiConn controller = loader.getController();
//            controller.setTransaksiDAO(transaksiDAO);
//            controller.setTransaksiConn(this);


            Stage stage = new Stage();
//            controller.setDialogStage(stage);
            stage.setScene(new Scene(root));
            stage.setTitle("Tambah Transaksi");
            stage.showAndWait();

            loadDataFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form tambah transaksi");
        }
    }

    @FXML
    private void applyFilterClicked() {
        String jenis = comboJenisTransaksi.getValue();
        LocalDate startDate = datePickerStart.getValue();
        LocalDate endDate = datePickerEnd.getValue();
        String kategori = comboKategori.getValue();

        ObservableList<Transaksi> filteredData = FXCollections.observableArrayList();

        for (Transaksi transaksi : dataTransaksi) {
            boolean match = true;

            if (!"Semua".equals(jenis) && !jenis.equals(transaksi.getJenis())) {
                match = false;
            }

            if (startDate != null || endDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate transaksiDate = LocalDate.parse(transaksi.getTanggal(), formatter);

                if (startDate != null && transaksiDate.isBefore(startDate)) {
                    match = false;
                }

                if (endDate != null && transaksiDate.isAfter(endDate)) {
                    match = false;
                }
            }

            if (kategori != null && !"Semua Kategori".equals(kategori) && !kategori.equals(transaksi.getKategori())) {
                match = false;
            }

            if (match) {
                filteredData.add(transaksi);
            }
        }

        TreeItem<Transaksi> root = new TreeItem<>();
        filteredData.forEach(transaksi -> root.getChildren().add(new TreeItem<>(transaksi)));
        treeTableTransaksi.setRoot(root);
    }

    @FXML
    private void resetFilterClicked() {
        comboJenisTransaksi.getSelectionModel().selectFirst();
        datePickerStart.setValue(null);
        datePickerEnd.setValue(null);
        comboKategori.getSelectionModel().selectFirst();

        TreeItem<Transaksi> root = new TreeItem<>();
        dataTransaksi.forEach(transaksi -> root.getChildren().add(new TreeItem<>(transaksi)));
        treeTableTransaksi.setRoot(root);
    }



    @FXML
    private void pengaturanBtnClicked(ActionEvent event) {
        loadScene(event, "Pengaturan-view.fxml");
    }

    @FXML
    private void notifikasiBtnClicked(ActionEvent event) {
        loadScene(event, "Notifikasi-view.fxml");
    }

    @FXML
    private void laporanBtnClicked(ActionEvent event) {
        loadScene(event, "Laporan-view.fxml");
    }

    @FXML
    private void anggaranBtnClicked(ActionEvent event) {
        loadScene(event, "Anggaran-view.fxml");
    }

    @FXML
    private void kategoriBtnClicked(ActionEvent event) {
        loadScene(event, "Kategori-view.fxml");
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
            showAlert("Error", "Gagal memuat halaman: " + fxmlFile);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}