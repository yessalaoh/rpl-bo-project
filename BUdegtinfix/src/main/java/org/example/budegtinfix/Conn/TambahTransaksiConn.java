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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
    private Label fileNameLabel;

    private Stage dialogStage;
    private boolean transactionAdded = false;
    private File selectedDocumentFile;
    private int currentUserId;
    @FXML
    private void initialize() {
        transactionTypeComboBox.getItems().addAll("Pemasukan", "Pengeluaran");
        transactionTypeComboBox.getSelectionModel().selectFirst();

        categoryComboBox.getItems().addAll("Makanan", "Transportasi", "Gaji", "Tabungan",
                "Hiburan", "Tagihan", "Pendidikan", "Investasi", "Lain-lain");
        categoryComboBox.getSelectionModel().selectFirst();

        datePicker.setValue(LocalDate.now());

        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.]\\d*)?")) {
                amountTextField.setText(oldValue);
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Method baru untuk meng-set user_id
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public boolean isTransactionAdded() {
        return transactionAdded;
    }

    @FXML
    private void handleAddTransaction() {
        if (isInputValid()) {
            String type = transactionTypeComboBox.getSelectionModel().getSelectedItem();
            String category = categoryComboBox.getSelectionModel().getSelectedItem();
            double amount = Double.parseDouble(amountTextField.getText());
            LocalDate date = datePicker.getValue();
            String description = descriptionTextField.getText();
            String documentPath = saveDocumentFile(selectedDocumentFile);

            boolean success = simpanKeDatabase(type, category, amount, date, description, documentPath);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses!", "Transaksi berhasil ditambahkan.");
                transactionAdded = true;
                if (dialogStage != null) {
                    dialogStage.close();
                }
            }
        }
    }

    @FXML
    private void batalBtnClicked(ActionEvent event) {
        try {
            if (dialogStage != null) {
                dialogStage.close();
            } else {
                // Jika dialogStage null, tutup window saat ini
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.close();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Transaksi-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat halaman Transaksi");
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

        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            if (file.length() > (5 * 1024 * 1024)) {
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

    private String saveDocumentFile(File sourceFile) {
        if (sourceFile == null) return null;
        try {
            File destDir = new File("documents");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + sourceFile.getName();
            File destFile = new File(destDir, uniqueFileName);
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Gagal menyimpan dokumen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean simpanKeDatabase(String type, String category, double amount,
                                     LocalDate date, String description, String documentPath) {
        String url = "jdbc:sqlite:budgetin.db";
        String sql = "INSERT INTO transaksi (jenis, kategori, jumlah, tanggal, deskripsi, " +
                "memiliki_dokumen, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
            pstmt.setString(2, category);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, date.toString());
            pstmt.setString(5, description);
            pstmt.setInt(6, documentPath != null ? 1 : 0);
            pstmt.setInt(7, currentUserId); // Tambahkan user_id ke query

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Gagal menyimpan transaksi: " + e.getMessage());
            e.printStackTrace();
            return false;
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