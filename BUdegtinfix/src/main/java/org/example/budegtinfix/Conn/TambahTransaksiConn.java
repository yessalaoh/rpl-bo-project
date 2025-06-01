package org.example.budegtinfix.Conn;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType; // Import ButtonType
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
// import javafx.fxml.FXMLLoader; // Duplikat
// import javafx.scene.Parent; // Duplikat
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Optional; // Import Optional
import java.util.UUID;
// import java.sql.Connection; // Tidak diperlukan lagi untuk ini karena memakai DAO
// import java.sql.DriverManager; // Tidak diperlukan lagi
// import java.sql.PreparedStatement; // Tidak diperlukan lagi
// import java.sql.SQLException; // Tidak diperlukan lagi di sini karena TransaksiDAO menangani

// Impor Transaksi dan TransaksiDAO yang diperlukan
import org.example.budegtinfix.Database.TransaksiDAO;
import org.example.budegtinfix.Conn.TransaksiConn.Transaksi; // Pastikan ini mengacu pada nested class Transaksi di TransaksiConn

public class TambahTransaksiConn {

    @FXML
    private ComboBox<String> transactionTypeComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TextField amountTextField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private Label fileNameLabel; // Label untuk menampilkan nama file yang dipilih

    private Stage dialogStage; // Untuk mereferensikan Stage dialog (jika dibuka sebagai dialog)
    private boolean transactionAdded = false; // Flag untuk mengetahui apakah transaksi berhasil ditambahkan
    private File selectedDocumentFile; // File dokumen yang dipilih
    private int currentUserId; // ID pengguna yang sedang login

    private TransaksiDAO transaksiDAO; // Instance dari TransaksiDAO

    @FXML
    private void initialize() {
        // Inisialisasi TransaksiDAO
        transaksiDAO = new TransaksiDAO(); // Atau disuntikkan jika menggunakan pola DI

        transactionTypeComboBox.getItems().addAll("Pemasukan", "Pengeluaran");
        transactionTypeComboBox.getSelectionModel().selectFirst();

        categoryComboBox.getItems().addAll("Makanan", "Transportasi", "Gaji", "Tabungan",
                "Hiburan", "Tagihan", "Pendidikan", "Investasi", "Lain-lain");
        categoryComboBox.getSelectionModel().selectFirst();

        datePicker.setValue(LocalDate.now());

        // Listener untuk amountTextField: hanya izinkan angka dan satu desimal
        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.]\\d*)?")) { // Memperbaiki regex untuk titik desimal
                amountTextField.setText(oldValue);
            }
        });
    }

    // Dipanggil oleh parent controller (misalnya TransaksiConn) untuk meneruskan Stage dialog
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Dipanggil oleh parent controller (misalnya TransaksiConn) untuk meneruskan ID user
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("TambahTransaksiConn: User ID disetel: " + this.currentUserId);
    }

    // Getter untuk mengetahui apakah transaksi berhasil ditambahkan (berguna untuk parent controller)
    public boolean isTransactionAdded() {
        return transactionAdded;
    }

    @FXML
    private void handleAddTransaction(ActionEvent event) {
        if (isInputValid()) {
            String type = transactionTypeComboBox.getSelectionModel().getSelectedItem();
            String category = categoryComboBox.getSelectionModel().getSelectedItem();
            double amount = Double.parseDouble(amountTextField.getText());
            LocalDate date = datePicker.getValue();
            String description = descriptionTextField.getText();
            String documentPath = saveDocumentFile(selectedDocumentFile); // Path absolut ke dokumen (atau null)

            // Membuat objek Transaksi
            Transaksi newTransaksi = new Transaksi(
                    0, // ID akan diabaikan oleh DAO saat INSERT karena auto-increment
                    date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), // Format tanggal ke String
                    type,
                    category,
                    description,
                    amount,
                    documentPath != null // memilikiDokumen adalah boolean, true jika documentPath tidak null
            );

            // Memanggil addTransaksi dari TransaksiDAO
            boolean success = transaksiDAO.addTransaksi(newTransaksi);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses!", "Transaksi berhasil ditambahkan.");
                transactionAdded = true; // Setel flag sukses

                // Tutup dialog ini. TransaksiConn yang memanggil yang akan me-refresh data.
                if (dialogStage != null) {
                    dialogStage.close();
                } else {
                    // Jika tidak dibuka sebagai dialog, tutup jendela saat ini
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal!", "Gagal menambahkan transaksi ke database.");
            }
        }
    }

    @FXML
    private void batalBtnClicked(ActionEvent event) {
        // Tampilkan konfirmasi jika ada input yang belum disimpan
        if (!amountTextField.getText().isEmpty() || descriptionTextField.getText().isEmpty()) { // Contoh sederhana
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Konfirmasi Pembatalan");
            confirm.setHeaderText(null);
            confirm.setContentText("Anda memiliki perubahan yang belum disimpan. Yakin ingin membatalkan?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return; // Jangan tutup jika pengguna tidak mengkonfirmasi
            }
        }

        // Tutup dialog ini. TransaksiConn yang memanggil yang akan me-refresh data (atau tidak).
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            // Jika tidak dibuka sebagai dialog, tutup jendela saat ini
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleUploadDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Dokumen Pendukung");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Pastikan showOpenDialog dipanggil dengan stage yang benar
        Stage currentStage = (Stage) fileNameLabel.getScene().getWindow(); // Ambil stage dari elemen UI mana pun
        File file = fileChooser.showOpenDialog(currentStage);
        if (file != null) {
            if (file.length() > (5 * 1024 * 1024)) { // 5 MB
                showAlert(Alert.AlertType.ERROR, "Kesalahan", "Ukuran file melebihi batas 5 MB.");
                fileNameLabel.setText("Ukuran maks: 5 MB");
                selectedDocumentFile = null;
            } else {
                selectedDocumentFile = file;
                fileNameLabel.setText(selectedDocumentFile.getName());
                showAlert(Alert.AlertType.INFORMATION, "Sukses",
                        "Dokumen " + selectedDocumentFile.getName() + " siap diunggah.");
            }
        }
    }

    // Method ini bertanggung jawab untuk menyimpan file fisik, bukan ke database
    private String saveDocumentFile(File sourceFile) {
        if (sourceFile == null) return null;
        try {
            // Direktori 'documents' harus ada di root proyek atau di tempat yang dapat ditulis
            File destDir = new File("documents");
            if (!destDir.exists()) {
                destDir.mkdirs(); // Buat direktori jika belum ada
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + sourceFile.getName();
            File destFile = new File(destDir, uniqueFileName);
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destFile.getAbsolutePath(); // Kembalikan path absolut ke file yang disimpan
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Gagal menyimpan dokumen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Method simpanKeDatabase ini dihapus dan diganti dengan panggilan ke TransaksiDAO
    // private boolean simpanKeDatabase(...) { ... }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (transactionTypeComboBox.getSelectionModel().getSelectedItem() == null ||
                transactionTypeComboBox.getSelectionModel().getSelectedItem().isEmpty()) {
            errorMessage.append("Pilih Jenis Transaksi (Pemasukan/Pengeluaran)!\n");
        }
        if (categoryComboBox.getSelectionModel().getSelectedItem() == null ||
                categoryComboBox.getSelectionModel().getSelectedItem().isEmpty()) {
            errorMessage.append("Pilih Kategori Transaksi!\n");
        }
        if (amountTextField.getText() == null || amountTextField.getText().isEmpty()) {
            errorMessage.append("Jumlah transaksi tidak boleh kosong!\n");
        } else {
            try {
                double amount = Double.parseDouble(amountTextField.getText());
                if (amount <= 0) {
                    errorMessage.append("Jumlah transaksi harus lebih dari nol!\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Jumlah transaksi harus berupa angka yang valid!\n");
            }
        }
        if (datePicker.getValue() == null) {
            errorMessage.append("Pilih Tanggal Transaksi!\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Input", errorMessage.toString());
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}