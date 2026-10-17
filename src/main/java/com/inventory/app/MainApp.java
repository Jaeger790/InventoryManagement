package com.inventory.app;

import com.inventory.repository.SqliteInventoryRepository;
import com.inventory.repository.InventoryPersistenceException;
import com.inventory.service.InventoryService;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.util.List;

public final class MainApp extends Application {
    @Override public void start(Stage stage) {
        try {
            SqliteInventoryRepository repository = new SqliteInventoryRepository(
                    SqliteInventoryRepository.defaultDatabasePath());
            InventoryService service = new InventoryService(repository);
            if (getParameters().getRaw().contains("--demo-data")
                    && service.allParts().isEmpty() && service.allProducts().isEmpty()) {
                insertDemoData(service);
            }
            new SceneNavigator(stage, service).showMain();
            stage.show();
        } catch (InventoryPersistenceException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Inventory database error");
            alert.setHeaderText("Unable to open your inventory database");
            alert.setContentText(e.getMessage() + "\nYour existing data has not been replaced.");
            alert.showAndWait();
        }
    }

        private static void insertDemoData(InventoryService service) {
        int lever = service.createPart(InventoryService.PartSource.IN_HOUSE,
                "Brake Lever", new BigDecimal("43.25"), 4, 2, 8, "300").getId();
        int exhaust = service.createPart(InventoryService.PartSource.IN_HOUSE,
                "Exhaust", new BigDecimal("230.20"), 2, 1, 3, "250").getId();
        int piston = service.createPart(InventoryService.PartSource.OUTSOURCED,
                "Piston", new BigDecimal("850.99"), 1, 1, 2, "Wiseco").getId();
        service.createProduct("KTM XC 300", new BigDecimal("2800.00"), 1, 1, 4, List.of(lever, piston));
        service.createProduct("KTM Duke 790", new BigDecimal("11757.56"), 1, 1, 1, List.of(exhaust));
    }

    public static void main(String[] args) { launch(args); }
}
