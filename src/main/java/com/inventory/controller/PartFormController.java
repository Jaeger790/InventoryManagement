package com.inventory.controller;

import com.inventory.app.SceneNavigator;
import com.inventory.model.InHouse;
import com.inventory.model.Outsourced;
import com.inventory.model.Part;
import com.inventory.service.InventoryService;
import com.inventory.repository.InventoryPersistenceException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public final class PartFormController {
    private final InventoryService service;
    private final SceneNavigator navigation;
    private final Part editing;
    public PartFormController(InventoryService service, SceneNavigator navigation, Part editing) {
        this.service = service; this.navigation = navigation; this.editing = editing;
    }
    @FXML private Label addPartHeading, machineIdL;
    @FXML private RadioButton inHouse, outsourced;
    @FXML private TextField partId, partName, partInventory, partPrice, partMax, partMin, machineId;
    @FXML private Button savePart, cancelButton;

    @FXML private void initialize() {
        partId.setEditable(false);
        partId.setDisable(true);
        inHouse.setOnAction(e -> updateSourceLabel());
        outsourced.setOnAction(e -> updateSourceLabel());
        savePart.setOnAction(e -> save());
        cancelButton.setOnAction(e -> navigation.showMain());
        if (editing != null) {
            addPartHeading.setText("Modify Part");
            partId.setText(Integer.toString(editing.getId()));
            partName.setText(editing.getName());
            partInventory.setText(Integer.toString(editing.getStock()));
            partPrice.setText(editing.getPrice().toPlainString());
            partMin.setText(Integer.toString(editing.getMin()));
            partMax.setText(Integer.toString(editing.getMax()));
            if (editing instanceof InHouse own) {
                inHouse.setSelected(true);
                machineId.setText(Integer.toString(own.getMachineId()));
            } else if (editing instanceof Outsourced vendor) {
                outsourced.setSelected(true);
                machineId.setText(vendor.getCompanyName());
            }
        }
        updateSourceLabel();
    }
    private void updateSourceLabel() {
        machineIdL.setText(inHouse.isSelected() ? "Machine ID" : "Company");
        machineId.setPromptText(inHouse.isSelected() ? "e.g. 100" : "Supplier name");
    }
    private void save() {
        try {
            var source = inHouse.isSelected() ? InventoryService.PartSource.IN_HOUSE : InventoryService.PartSource.OUTSOURCED;
            String name = partName.getText();
            var price = UiSupport.money(partPrice.getText());
            int stock = UiSupport.integer(partInventory.getText(), "Inventory");
            int min = UiSupport.integer(partMin.getText(), "Minimum");
            int max = UiSupport.integer(partMax.getText(), "Maximum");
            if (editing == null) service.createPart(source, name, price, stock, min, max, machineId.getText());
            else service.updatePart(editing.getId(), source, name, price, stock, min, max, machineId.getText());
            navigation.showMain();
        } catch (IllegalArgumentException | InventoryPersistenceException ex) { UiSupport.error(ex.getMessage()); }
    }
}
