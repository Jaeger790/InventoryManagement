package com.inventory.controller;

import com.inventory.app.SceneNavigator;
import com.inventory.model.Part;
import com.inventory.model.Product;
import com.inventory.service.InventoryService;
import com.inventory.repository.InventoryPersistenceException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.math.BigDecimal;
import java.util.LinkedHashSet;

/** Stage associations until save so cancel leaves the product unchanged. */
public final class ProductFormController {
    private final InventoryService service;
    private final SceneNavigator navigation;
    private final Product editing;
    private final LinkedHashSet<Integer> selectedIds = new LinkedHashSet<>();
    public ProductFormController(InventoryService service, SceneNavigator navigation, Product editing) {
        this.service = service; this.navigation = navigation; this.editing = editing;
    }
    @FXML private TableView<Part> associatedPartTable, partListTable;
    @FXML private TableColumn<Part, Integer> associatedId, associatedInv, partId, partInv;
    @FXML private TableColumn<Part, String> associatedName, partName;
    @FXML private TableColumn<Part, BigDecimal> associatedPrice, partPrice;
    @FXML private Button associatedPartButton, saveProductButton, cancelAddProductB, removeAssociatedPartB;
    @FXML private TextField productIdT, productNameT, productInventoryT, productPriceT, productMaxT, productMinT, searchBox;
    @FXML private Label productHeader;

    @FXML private void initialize() {
        UiSupport.column(partId, Part::getId);
        UiSupport.column(partName, Part::getName);
        UiSupport.column(partInv, Part::getStock);
        UiSupport.moneyColumn(partPrice, Part::getPrice);
        UiSupport.column(associatedId, Part::getId);
        UiSupport.column(associatedName, Part::getName);
        UiSupport.column(associatedInv, Part::getStock);
        UiSupport.moneyColumn(associatedPrice, Part::getPrice);
        productIdT.setEditable(false); productIdT.setDisable(true);
        productIdT.setText("Auto-generated");
        if (editing != null) {
            productHeader.setText("Modify Product");
            productIdT.setText(Integer.toString(editing.getId()));
            productNameT.setText(editing.getName());
            productInventoryT.setText(Integer.toString(editing.getStock()));
            productPriceT.setText(editing.getPrice().toPlainString());
            productMinT.setText(Integer.toString(editing.getMin()));
            productMaxT.setText(Integer.toString(editing.getMax()));
            selectedIds.addAll(editing.getAssociatedPartIds());
        }
        refreshParts(); refreshAssociated();
        searchBox.textProperty().addListener((obs, old, value) -> refreshParts());
        associatedPartButton.setOnAction(e -> {
            Part selected = partListTable.getSelectionModel().getSelectedItem();
            if (selected == null) { UiSupport.error("Select a part to associate."); return; }
            if (!selectedIds.add(selected.getId())) { UiSupport.error("This part is already associated."); return; }
            refreshAssociated();
        });
        removeAssociatedPartB.setOnAction(e -> {
            Part selected = associatedPartTable.getSelectionModel().getSelectedItem();
            if (selected == null) { UiSupport.error("Select an associated part to remove."); return; }
            selectedIds.remove(selected.getId());
            refreshAssociated();
        });
        saveProductButton.setOnAction(e -> save());
        cancelAddProductB.setOnAction(e -> navigation.showMain());
    }
    private void refreshParts() {
        partListTable.setItems(FXCollections.observableArrayList(service.searchParts(searchBox.getText())));
    }
    private void refreshAssociated() {
        associatedPartTable.setItems(FXCollections.observableArrayList(
            selectedIds.stream().map(service::getPart).toList()));
    }
    private void save() {
        try {
            String name = productNameT.getText();
            var price = UiSupport.money(productPriceT.getText());
            int stock = UiSupport.integer(productInventoryT.getText(), "Inventory");
            int min = UiSupport.integer(productMinT.getText(), "Minimum");
            int max = UiSupport.integer(productMaxT.getText(), "Maximum");
            if (editing == null) service.createProduct(name, price, stock, min, max, selectedIds);
            else service.updateProduct(editing.getId(), name, price, stock, min, max, selectedIds);
            navigation.showMain();
        } catch (IllegalArgumentException | InventoryPersistenceException ex) { UiSupport.error(ex.getMessage()); }
    }
}
