module wguc482.mainscreen {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;


    exports wguc482.PA.Controllers;
    opens wguc482.PA.Controllers to javafx.fxml;
    exports wguc482.PA.Model;
    opens wguc482.PA.Model to javafx.fxml;
}