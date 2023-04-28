/**
 * Main Application
 * @author Bradley Rott
 * @version 1.0
 */
package wguc482.PA.Model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import wguc482.PA.Controllers.MainController;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        MainController mainController = new MainController();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/wguc482/PA/MainScreen.fxml"));
        fxmlLoader.setController(mainController);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Inventory Management: C482 PA");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
