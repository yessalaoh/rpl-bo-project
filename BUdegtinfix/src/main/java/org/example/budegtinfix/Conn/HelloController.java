package org.example.budegtinfix.Conn;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class HelloController {

    @FXML
    private void btnLogin(ActionEvent event) throws IOException {
        loadScene(event, "Login-view.fxml");
    }

    @FXML
    private void btnSignup(ActionEvent event) throws IOException {
        loadScene(event, "SignUp-view.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
