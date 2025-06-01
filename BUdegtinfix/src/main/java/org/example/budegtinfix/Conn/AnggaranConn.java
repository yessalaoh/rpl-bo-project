package org.example.budegtinfix.Conn;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.budegtinfix.Database.CatatanDB;
import org.example.budegtinfix.Session.Session;

import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class AnggaranConn {
    @FXML private TextField budgetNameField;
    @FXML private TextField budgetAmountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private PieChart budgetPieChart;

    @FXML
    public void initialize() {
        setupCategoryComboBox();
        loadBudgetData();
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Makanan", "Transportasi", "Tagihan",
                "Hiburan", "Pendidikan", "Lainnya"
        ));
    }

    @FXML
    private void createBudget() {
        if (validateInput()) {
            try {
                String name = budgetNameField.getText().trim();
                double amount = Double.parseDouble(budgetAmountField.getText().trim());
                String category = categoryComboBox.getValue();

                if (saveBudgetToDatabase(name, amount, category)) {
                    showSuccessAlert("Anggaran berhasil dibuat!");
                    clearForm();
                    updateBudgetChart();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Format jumlah harus berupa angka");
            }
        }
    }

    private boolean validateInput() {
        if (budgetNameField.getText().trim().isEmpty()) {
            showErrorAlert("Nama anggaran harus diisi");
            return false;
        }

        if (budgetAmountField.getText().trim().isEmpty()) {
            showErrorAlert("Jumlah anggaran harus diisi");
            return false;
        }

        try {
            double amount = Double.parseDouble(budgetAmountField.getText().trim());
            if (amount <= 0) {
                showErrorAlert("Jumlah anggaran harus lebih dari 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format jumlah tidak valid");
            return false;
        }

        if (categoryComboBox.getValue() == null) {
            showErrorAlert("Kategori harus dipilih");
            return false;
        }

        return true;
    }

    private boolean saveBudgetToDatabase(String name, double amount, String category) {
        String sql = "INSERT INTO anggaran (nama, jumlah, kategori, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, category);
            pstmt.setInt(4, getCurrentUserId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            showErrorAlert("Gagal menyimpan ke database: " + e.getMessage());
            return false;
        }
    }

    private void loadBudgetData() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        String sql = "SELECT kategori, SUM(jumlah) as total FROM anggaran WHERE user_id = ? GROUP BY kategori";

        try (Connection conn = CatatanDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();

            NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            while (rs.next()) {
                String kategori = rs.getString("kategori");
                double total = rs.getDouble("total");
                String formattedAmount = rupiahFormat.format(total);

                PieChart.Data slice = new PieChart.Data(
                        String.format("%s\n%s", kategori, formattedAmount),
                        total
                );
                pieData.add(slice);
            }

            budgetPieChart.setData(pieData);

            // Add tooltips to show exact values
            for (PieChart.Data data : budgetPieChart.getData()) {
                String tooltipText = String.format("%s: %s",
                        data.getName().split("\n")[0],
                        rupiahFormat.format(data.getPieValue()));

                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setStyle("-fx-font-size: 14;");
                Tooltip.install(data.getNode(), tooltip);

                // Add hover effect
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle("-fx-border-color: #333; -fx-border-width: 2;");
                });
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setStyle("");
                });
            }

        } catch (SQLException e) {
            showErrorAlert("Gagal memuat data anggaran: " + e.getMessage());
        }
    }

    private void updateBudgetChart() {
        loadBudgetData();
    }

    private int getCurrentUserId() {
        // In a real application, get this from your authentication system
        return 1; // Default user ID for demo
    }

    private void clearForm() {
        budgetNameField.clear();
        budgetAmountField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
    }

    // Navigation methods
    @FXML
    private void transaksiBtnClicked(javafx.event.ActionEvent event) {
        loadScene("/org/example/budegtinfix/Transaksi-view.fxml", event);
    }

    @FXML
    private void kategoriBtnClicked(javafx.event.ActionEvent event) {
        loadScene("/org/example/budegtinfix/kategori.fxml", event);
    }

    @FXML
    private void anggaranBtnClicked(javafx.event.ActionEvent event) {
        loadScene("/org/example/budegtinfix/Anggaran-view.fxml", event);
    }

    @FXML
    private void laporanBtnClicked(javafx.event.ActionEvent event) {
        loadScene("/org/example/budegtinfix/Diagram.fxml", event);
    }

    @FXML
    private void pengaturanBtnClicked(javafx.event.ActionEvent event) {
        loadScene("/org/example/budegtinfix/pengaturan.fxml", event);
    }

    @FXML
    private void logoutBtnClicked(javafx.event.ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Session.clearSession();  // Make sure you have this Session class
            loadScene("/org/example/budegtinfix/Login-view.fxml", event);
        }
    }

    @FXML
    private void notifikasiBtnClicked(javafx.event.ActionEvent event) {
        loadScene("/org/example/budegtinfix/Notiikasi-view.fxml", event);
    }

    private void loadScene(String fxmlPath, javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Gagal memuat halaman: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }

    private void showSuccessAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Sukses", message);
    }

    private void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}