package com.inventory.app;

import com.inventory.controller.MainController;
import com.inventory.controller.PartFormController;
import com.inventory.controller.ProductFormController;
import com.inventory.model.Part;
import com.inventory.model.Product;
import com.inventory.service.InventoryService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public final class SceneNavigator {
    private final Stage stage;
    private final InventoryService service;
    public SceneNavigator(Stage stage, InventoryService service) {
        this.stage = stage;
        this.service = service;
    }
    public void showMain() { load("MainScreen.fxml", new MainController(service, this), "Inventory Management"); }
    public void showPartForm(Part part) {
        load("AddPart.fxml", new PartFormController(service, this, part), part == null ? "Add Part" : "Modify Part");
    }
    public void showProductForm(Product product) {
        load("AddProduct.fxml", new ProductFormController(service, this, product), product == null ? "Add Product" : "Modify Product");
    }
    private void load(String resource, Object controller, String title) {
        URL location = MainApp.class.getResource("/com/inventory/view/" + resource);
        if (location == null) throw new IllegalStateException("Missing FXML: " + resource);
        FXMLLoader loader = new FXMLLoader(location);
        loader.setController(controller);
        try {
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.sizeToScene();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load screen: " + resource, exception);
        }
    }
}
