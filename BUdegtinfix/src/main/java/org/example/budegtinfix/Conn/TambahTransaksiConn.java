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
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path; // Import Path
import java.nio.file.Paths; // Import Paths
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter
import java.util.Optional;
import java.util.UUID;

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

    private Stage dialogStage;
    private boolean transactionAdded = false;
    private File selectedDocumentFile; // Ini sekarang akan menyimpan file yang dipilih pengguna
    private int currentUserId; // ID pengguna yang sedang login

    private TransaksiDAO transaksiDAO;

    @FXML
    private void initialize() {
        transaksiDAO = new TransaksiDAO();

        transactionTypeComboBox.getItems().addAll("Pemasukan", "Pengeluaran");
        transactionTypeComboBox.getSelectionModel().selectFirst();

        categoryComboBox.getItems().addAll("Makanan", "Transportasi", "Gaji", "Tabungan",
                "Hiburan", "Tagihan", "Pendidikan", "Investasi", "Lain-lain");
        categoryComboBox.getSelectionModel().selectFirst();

        datePicker.setValue(LocalDate.now());

        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.]\\d*)?")) {
                amountTextField.setText(oldValue);
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("TambahTransaksiConn: User ID disetel: " + this.currentUserId);
    }

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

            // **PERBAIKAN 1: Panggil saveDocumentFile dan dapatkan path gambar**
            String imageUrl = saveDocumentFile(selectedDocumentFile); // Ini akan menjadi path yang disimpan di DB

            // **PERBAIKAN 2: Sesuaikan konstruktor Transaksi untuk menerima imageUrl**
            // Note: memilikiDokumen kini akan disetel berdasarkan apakah imageUrl ada atau tidak
            Transaksi newTransaksi = new Transaksi(
                    0, // ID akan diabaikan oleh DAO saat INSERT karena auto-increment
                    date.format(DateTimeFormatter.ISO_LOCAL_DATE), // Format tanggal ke String
                    type,
                    category,
                    description,
                    amount,
                    imageUrl != null && !imageUrl.isEmpty(), // memilikiDokumen adalah true jika ada imageUrl
                    imageUrl // Teruskan imageUrl
            );

            // Memanggil addTransaksi dari TransaksiDAO
            boolean success = transaksiDAO.addTransaksi(newTransaksi);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses!", "Transaksi berhasil ditambahkan.");
                transactionAdded = true;

                if (dialogStage != null) {
                    dialogStage.close();
                } else {
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
        if (!amountTextField.getText().isEmpty() || !descriptionTextField.getText().isEmpty() || selectedDocumentFile != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Konfirmasi Pembatalan");
            confirm.setHeaderText(null);
            confirm.setContentText("Anda memiliki perubahan yang belum disimpan. Yakin ingin membatalkan?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
        }

        if (dialogStage != null) {
            dialogStage.close();
        } else {
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
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"), // PDF tidak akan ditampilkan sebagai gambar
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage currentStage = (Stage) fileNameLabel.getScene().getWindow();
        File file = fileChooser.showOpenDialog(currentStage);
        if (file != null) {
            if (file.length() > (5 * 1024 * 1024)) { // 5 MB
                showAlert(Alert.AlertType.ERROR, "Kesalahan", "Ukuran file melebihi batas 5 MB.");
                fileNameLabel.setText("Ukuran maks: 5 MB");
                selectedDocumentFile = null; // Reset file yang dipilih jika terlalu besar
            } else {
                // Hanya izinkan gambar untuk ditampilkan di colGambar, PDF tetap bisa diupload
                String fileName = file.getName();
                String fileExtension = "";
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    fileExtension = fileName.substring(i+1).toLowerCase();
                }

                if (fileExtension.equals("png") || fileExtension.equals("jpg") ||
                        fileExtension.equals("jpeg") || fileExtension.equals("gif")) {
                    selectedDocumentFile = file;
                    fileNameLabel.setText(selectedDocumentFile.getName());
                    showAlert(Alert.AlertType.INFORMATION, "Sukses",
                            "Gambar " + selectedDocumentFile.getName() + " siap diunggah.");
                } else if (fileExtension.equals("pdf")) {
                    selectedDocumentFile = file;
                    fileNameLabel.setText(selectedDocumentFile.getName() + " (PDF)");
                    showAlert(Alert.AlertType.INFORMATION, "Sukses",
                            "Dokumen PDF " + selectedDocumentFile.getName() + " siap diunggah (tidak akan ditampilkan sebagai gambar).");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Kesalahan", "Tipe file tidak didukung untuk tampilan gambar.");
                    fileNameLabel.setText("Pilih file gambar atau PDF");
                    selectedDocumentFile = null;
                }
            }
        }
    }

    // **PERBAIKAN 3: Method ini sekarang mengembalikan path string yang akan disimpan di DB**
    // Mengubah lokasi penyimpanan ke dalam folder 'uploads' di direktori kerja aplikasi
    private String saveDocumentFile(File sourceFile) {
        if (sourceFile == null) {
            return null;
        }
        try {
            // Dapatkan direktori kerja aplikasi (misalnya di mana JAR dieksekusi)
            // Atau Anda bisa menentukan path absolut yang lebih spesifik
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir); // Buat direktori jika belum ada
            }

            // Gunakan UUID untuk nama file unik untuk menghindari konflik
            String fileExtension = "";
            String fileName = sourceFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                fileExtension = fileName.substring(dotIndex); // Termasuk titik, e.g., ".png"
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path destinationPath = uploadDir.resolve(uniqueFileName);

            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // Mengembalikan path relatif atau absolut, tergantung bagaimana Anda akan memuatnya
            // Untuk pemuatan di TransaksiConn.java, `file:/` URI adalah yang terbaik untuk file lokal
            return destinationPath.toUri().toString(); // Mengembalikan URI lengkap (misal: file:/C:/path/to/project/uploads/unique.png)
            // Atau jika Anda ingin path relatif dari root proyek, Anda perlu logika lebih lanjut
            // return "uploads/" + uniqueFileName; // Hanya jika struktur proyek Anda konsisten
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Gagal menyimpan dokumen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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