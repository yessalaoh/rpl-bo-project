module org.example.budegtinfix {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.sql;

    opens org.example.budegtinfix to javafx.fxml;
    exports org.example.budegtinfix;
    exports org.example.budegtinfix.Conn;
    opens org.example.budegtinfix.Conn to javafx.fxml;
    exports org.example.budegtinfix.Database;
    opens org.example.budegtinfix.Database to javafx.fxml;
}