package com.inventory.controller;

import com.inventory.app.SceneNavigator;
import com.inventory.model.Part;
import com.inventory.model.Product;
import com.inventory.service.InventoryService;
import com.inventory.repository.InventoryPersistenceException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import java.math.BigDecimal;

public final class MainController {
    private final InventoryService service;
    private final SceneNavigator navigation;
    public MainController(InventoryService service, SceneNavigator navigation) {
        this.service = service;
        this.navigation = navigation;
    }
    @FXML private TableView<Part> partsTable;
    @FXML private TableColumn<Part, Integer> partId, partInventory;
    @FXML private TableColumn<Part, String> partName;
    @FXML private TableColumn<Part, BigDecimal> partPrice;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> productId, productInventory;
    @FXML private TableColumn<Product, String> productName;
    @FXML private TableColumn<Product, BigDecimal> productPrice;
    @FXML private TextField partSearchField, productSearchField;
    @FXML private Button productDeleteB, modifyProductButton, addProductButton;
    @FXML private Button modifyPartB, deletePartB, addPartB, exitButton;

    @FXML private void initialize() {
        UiSupport.column(partId, Part::getId);
        UiSupport.column(partInventory, Part::getStock);
        UiSupport.column(partName, Part::getName);
        UiSupport.moneyColumn(partPrice, Part::getPrice);
        UiSupport.column(productId, Product::getId);
        UiSupport.column(productInventory, Product::getStock);
        UiSupport.column(productName, Product::getName);
        UiSupport.moneyColumn(productPrice, Product::getPrice);
        refreshParts();
        refreshProducts();
        partSearchField.textProperty().addListener((obs, old, value) -> refreshParts());
        productSearchField.textProperty().addListener((obs, old, value) -> refreshProducts());
        addPartB.setOnAction(e -> navigation.showPartForm(null));
        addProductButton.setOnAction(e -> navigation.showProductForm(null));
        modifyPartB.setOnAction(e -> {
            Part selected = partsTable.getSelectionModel().getSelectedItem();
            if (selected == null) UiSupport.error("Select a part to modify.");
            else navigation.showPartForm(selected);
        });
        modifyProductButton.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected == null) UiSupport.error("Select a product to modify.");
            else navigation.showProductForm(selected);
        });
        deletePartB.setOnAction(e -> deletePart());
        productDeleteB.setOnAction(e -> deleteProduct());
        exitButton.setOnAction(e -> { if (UiSupport.confirm("Exit Inventory Management?")) Platform.exit(); });
    }
    private void refreshParts() {
        partsTable.setItems(FXCollections.observableArrayList(service.searchParts(partSearchField.getText())));
    }
    private void refreshProducts() {
        productsTable.setItems(FXCollections.observableArrayList(service.searchProducts(productSearchField.getText())));
    }
    private void deletePart() {
        Part selected = partsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { UiSupport.error("Select a part to delete."); return; }
        if (!UiSupport.confirm("Delete part '" + selected.getName() + "'?")) return;
        try { service.deletePart(selected.getId()); refreshParts(); }
        catch (IllegalStateException | InventoryPersistenceException ex) { UiSupport.error(ex.getMessage()); }
    }
    private void deleteProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { UiSupport.error("Select a product to delete."); return; }
        if (!UiSupport.confirm("Delete product '" + selected.getName() + "'?")) return;
        try { service.deleteProduct(selected.getId()); refreshProducts(); }
        catch (IllegalStateException | InventoryPersistenceException ex) { UiSupport.error(ex.getMessage()); }
    }
}
