module com.inventory.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    exports com.inventory.app;
    opens com.inventory.controller to javafx.fxml;
}
