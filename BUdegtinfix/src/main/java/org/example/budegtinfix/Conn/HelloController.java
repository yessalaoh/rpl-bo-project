package org.example.budegtinfix.Conn;

import javafx.event.ActionEvent; // Penting: Import ActionEvent
import javafx.fxml.FXMLLoader;
import javafx.scene.Node; // Penting: Import Node
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloController {
    public void btnLogin(ActionEvent event) throws IOException {
        loadScene("Login-view.fxml", event);
    }

    public void btnSignup(ActionEvent event) throws IOException {
        loadScene("SignUp-view.fxml", event);
    }

    private void loadScene(String fxmlFileName, ActionEvent event) throws IOException {
        URL fxmlLocation = getClass().getResource("/org/example/budegtinfix/" + fxmlFileName);

        if (fxmlLocation == null) {
            System.err.println("Gagal menemukan file FXML: " + fxmlFileName);
            throw new IOException("FXML file not found: " + fxmlFileName);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Scene newScene = new Scene(root);
        currentStage.setScene(newScene);
        currentStage.show();
    }
}