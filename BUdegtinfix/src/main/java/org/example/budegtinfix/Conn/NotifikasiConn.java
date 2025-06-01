package org.example.budegtinfix.Conn;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.budegtinfix.Database.TransaksiDAO; // Import TransaksiDAO yang sudah diperbarui
import org.example.budegtinfix.Conn.TransaksiConn.Transaksi; // Pastikan ini adalah model Transaksi yang benar
import org.example.budegtinfix.Session.Session;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter
import java.util.List;
import java.util.ResourceBundle;

public class NotifikasiConn implements Initializable {

    @FXML private Button Kembali;
    @FXML private Text helloUserText;

    private TransaksiDAO transaksiDAO;
    private ScheduledService<List<Transaksi>> notificationService;
    private LocalDateTime lastCheckTime;
    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Session.getIdUser() != 0 && Session.getNamaUser() != null) {
            helloUserText.setText("Halo, " + Session.getNamaUser() + "!");
        } else {
            helloUserText.setText("Halo, Pengguna!");
        }

        transaksiDAO = new TransaksiDAO();
        lastCheckTime = LocalDateTime.now();

        setupNotificationService();
    }

    private void setupNotificationService() {
        notificationService = new ScheduledService<List<Transaksi>>() {
            @Override
            protected Task<List<Transaksi>> createTask() {
                return new Task<List<Transaksi>>() {
                    @Override
                    protected List<Transaksi> call() throws Exception {
                        if (Session.getIdUser() == 0) {
                            System.out.println("No user logged in, skipping transaction check.");
                            return null;
                        }
                        List<Transaksi> newTransactions = transaksiDAO.getTransaksiTerbaru(Session.getIdUser(), lastCheckTime);
                        lastCheckTime = LocalDateTime.now();
                        return newTransactions;
                    }
                };
            }
        };

        notificationService.setPeriod(Duration.seconds(10)); // Cek setiap 10 detik
        notificationService.setDelay(Duration.seconds(5)); // Mulai cek setelah 5 detik pertama
        notificationService.setOnSucceeded(event -> {
            List<Transaksi> newTransactions = notificationService.getValue();
            if (newTransactions != null && !newTransactions.isEmpty()) {
                // Ada transaksi baru, tampilkan notifikasi
                Platform.runLater(() -> {
                    StringBuilder notifMessage = new StringBuilder("Transaksi baru telah ditambahkan:\n");
                    for (Transaksi t : newTransactions) {
                        // Asumsi tanggal di objek Transaksi adalah String 'yyyy-MM-dd HH:mm:ss' atau 'yyyy-MM-dd'
                        // Jika hanya tanggal, pertimbangkan untuk menambahkan kolom timestamp di DB
                        try {
                            LocalDateTime transDateTime;
                            if (t.getTanggal().length() > 10) { // Coba parse dengan DateTimeFormatter
                                transDateTime = LocalDateTime.parse(t.getTanggal(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            } else { // Jika hanya tanggal (yyyy-MM-dd)
                                transDateTime = LocalDate.parse(t.getTanggal(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
                            }
                            notifMessage.append("- ")
                                    .append(t.getJenis()).append(" (")
                                    .append(t.getKategori()).append("): ")
                                    .append("Rp").append(String.format("%,.2f", t.getJumlah()))
                                    .append(" pada ").append(transDateTime.format(DISPLAY_DATETIME_FORMATTER))
                                    .append("\n");
                        } catch (Exception e) {
                            System.err.println("Error parsing transaction date for notification: " + t.getTanggal() + " - " + e.getMessage());
                            notifMessage.append("- ").append(t.getJenis())
                                    .append(" (").append(t.getKategori())
                                    .append("): Rp").append(String.format("%,.2f", t.getJumlah()))
                                    .append(" (Tanggal tidak valid)\n");
                        }
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Notifikasi Transaksi Baru", notifMessage.toString());
                });
            }
        });

        // Tangani jika terjadi error pada service
        notificationService.setOnFailed(event -> {
            Throwable e = notificationService.getException();
            if (e != null) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error Notifikasi", "Gagal memuat transaksi: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });

        notificationService.start(); // Mulai service ketika inisialisasi
    }

    @FXML
    private void KembaliBtnClicked(ActionEvent event) {
        // Hentikan service ketika meninggalkan halaman ini untuk menghemat sumber daya
        if (notificationService != null) {
            notificationService.cancel();
            System.out.println("Notification service stopped.");
        }
        loadScene(event, "HomePage-view.fxml");
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}