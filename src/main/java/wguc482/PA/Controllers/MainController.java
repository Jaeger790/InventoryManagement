/**
 * Controller for the main screen
 * @author Brad Rott
 */
package wguc482.PA.Controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import wguc482.PA.Model.Inventory;
import wguc482.PA.Model.Part;
import wguc482.PA.Model.Product;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final Inventory inv = new Inventory();
    @FXML
    protected TableView<Product> productsTable;
    @FXML
    protected TableColumn<Product, Integer> productId;
    @FXML
    protected TableColumn<Product, String> productName;
    @FXML
    protected TableColumn<Product, Integer> productInventory;
    @FXML
    protected TableColumn<Product, Double> productPrice;

    @FXML
    public TableView<Part> partsTable;
    @FXML
    protected TableColumn<Part, Integer> partId;
    @FXML
    protected TableColumn<Part, String> partName;
    @FXML
    protected TableColumn<Part, Integer> partInventory;
    @FXML
    protected TableColumn<Part, Double> partPrice;
    public Button productDeleteB, modifyProductButton, addProductButton, modifyPartB, deletePartB, addPartB, exitButton;
    @FXML
    protected TextField partSearchField, productSearchField;
    private final Alert noSelection = new Alert(Alert.AlertType.ERROR);

    /**
     * Runtime Error:
     * Exception in thread "JavaFX Application Thread" java.lang.RuntimeException:
     * java.lang.reflect.InvocationTargetException
     * at
     * javafx.fxml/javafx.fxml.FXMLLoader$MethodHandler.invoke(FXMLLoader.java:1857)
     * ---------------------------------------------------------------------------------------------------------------------------
     * Runtime error when attempting to open parts window using fxml loader. Would
     * not open "/AddPart.fxml"
     * The error was resolved by altering the path of the fxml file for the FXML
     * loader object to "AddPart.fxml".
     * The method for opening the Add Part Form was moved to the AddController.
     **/

    private void onAddPartButtonClick() {
        addPartB.setOnAction(this::addPartScene);
    }

    @FXML
    protected void onModifyPartClick() {
        modifyPartB.setOnAction(this::modifyPartScene);
    }

    protected void onPartDeleteClick() {
        deletePartB.setOnAction(actionEvent -> {
            Part selectedPart = partsTable.getSelectionModel().getSelectedItem();
            try {
                if (selectedPart == null) {
                    throw new Exception("No part selected.");
                }
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you wish to delete this part?");
                alert.setTitle("Delete Part?");
                alert.setHeaderText("Are you sure you wish to delete this part?");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Inventory.getAllParts().remove(selectedPart);
                }
            } catch (Exception exception) {
                noSelection.setTitle("Error");
                noSelection.setHeaderText("No Part Selected!");
                noSelection.setContentText("You must first select a part from the table to delete it.");
                noSelection.show();
            }

        });
    }

    private void onAddProductClick() {
        addProductButton.setOnAction(this::addProductScene);
    }

    private void onProdModClick() {
        modifyProductButton.setOnAction(this::modifyProductScene);
    }

    protected void onProductDeleteClick() {
        productDeleteB.setOnAction(actionEvent -> {
            Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            try {
                if (selectedProduct == null) {
                    throw new Exception("No product selected.");
                }
            } catch (Exception exception) {
                noSelection.setTitle("Error");
                noSelection.setHeaderText("No Product Selected!");
                noSelection.setContentText("You must first select a product from the table to delete it.");
                noSelection.show();
                return;
            }
            if (!selectedProduct.getAllAssociatedParts().isEmpty()) {
                noSelection.setTitle("Error");
                noSelection.setHeaderText("Product has associated parts assigned to it!");
                noSelection.setContentText("You must first remove a products associated parts before deleting the" +
                        " product.");
                noSelection.show();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you wish to delete this product?");
            alert.setTitle("Delete Product?");
            alert.setHeaderText("Are you sure you wish to delete this product?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Inventory.getAllProducts().remove(selectedProduct);
            }
        });
    }

    private void onExitButtonClick() {
        exitButton.setOnAction(actionEvent -> {
            Alert confirmE = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?");
            confirmE.setTitle("Exit Application?");
            confirmE.setHeaderText("Close Program?");
            Optional<ButtonType> result = confirmE.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.exit(0);
            }
        });

    }

    /**
     * Future enhancement:
     * Enable searches for partial id number matches
     * Create separate condition for names that include numbers
     */
    @FXML
    private void getPartResults() {
        partSearchField.setOnKeyTyped(actionEvent -> {

            String searchText = partSearchField.getText();
            ObservableList<Part> parts = inv.lookupPart(searchText);

            if (parts.size() == 0) {
                try {
                    int id = Integer.parseInt(searchText);
                    Part prt = inv.lookupPart(id);
                    parts.add(prt);
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                }
            }
            partsTable.setItems(parts);
        });

    }

    /**
     * Future enhancement:
     * Separate searches based on data type entered (int:id, string:name)
     * Create separate condition for names that include numbers
     */
    @FXML
    private void getProductResults() {
        productSearchField.setOnKeyTyped(actionEvent -> {
            String searchText = productSearchField.getText();
            ObservableList<Product> products = inv.lookupProduct(searchText);

            if (products.size() == 0) {
                try {
                    int id = Integer.parseInt(searchText);
                    Product prod = inv.lookupProduct(id);
                    products.add(prod);
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                }
            }

            productsTable.setItems(products);
            formatProductPriceCol();
        });
    }

    private void addPartScene(ActionEvent actionEvent) {
        AddPartController addPartController = new AddPartController();
        try {
            Stage partStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/wguc482/PA/AddPart.fxml")));
            loader.setController(addPartController);

            Parent root = loader.load();
            Scene scene = new Scene(root);
            partStage.setScene(scene);
            partStage.setTitle("Add Part");
            partStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void modifyPartScene(ActionEvent actionEvent) {
        ModifyPartController modifyPartController = new ModifyPartController();
        Part selectedPart = partsTable.getSelectionModel().getSelectedItem();

        try {
            if (selectedPart == null) {
                throw new InvocationTargetException(null);
            }
        } catch (InvocationTargetException e) {
            Alert noSelection = new Alert(Alert.AlertType.ERROR);
            noSelection.setTitle("Error");
            noSelection.setHeaderText("No Part Selected!");
            noSelection.setContentText("You must first select a part from the table to modify it.");
            noSelection.show();
            return;
        }
        try {
            Stage partStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/wguc482/PA/AddPart.fxml")));
            loader.setController(modifyPartController);
            modifyPartController.fillPartFields(selectedPart);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            partStage.setScene(scene);
            partStage.setTitle("Modify Part");
            partStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addProductScene(ActionEvent actionEvent) {
        AddProductController addProductController = new AddProductController();
        try {
            Stage productStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/wguc482/PA/AddProduct.fxml")));
            loader.setController(addProductController);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            productStage.setScene(scene);
            productStage.setTitle("Main Screen");
            productStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void modifyProductScene(ActionEvent actionEvent) {
        ModifyProductController modifyProductController = new ModifyProductController();
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();

        try {
            if (selectedProduct == null) {
                throw new InvocationTargetException(null);
            }
        } catch (InvocationTargetException e) {
            noSelection.setTitle("Error");
            noSelection.setHeaderText("No Part Selected!");
            noSelection.setContentText("You must first select a part from the table to modify it.");
            noSelection.show();
            return;
        }
        try {
            Stage productStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/wguc482/PA/AddProduct.fxml")));
            loader.setController(modifyProductController);
            modifyProductController.getProductFields(selectedProduct);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            productStage.setScene(scene);
            productStage.setTitle("Modify Product");
            productStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void formatProductPriceCol() {
        productPrice.setCellFactory(format -> new TableCell<>() {
            @Override
            protected void updateItem(Double productPrice, boolean emptyCell) {
                super.updateItem(productPrice, emptyCell);
                if (emptyCell) {
                    setText(null);
                } else {
                    if (productPrice == null) {
                        setText(null);
                        return;
                    }
                    setText(NumberFormat.getCurrencyInstance().format(productPrice.doubleValue()));
                }
            }
        });
    }

    private void formatPartPriceCol() {
        partPrice.setCellFactory(format -> new TableCell<>() {
            @Override
            protected void updateItem(Double partPrice, boolean emptyCell) {
                super.updateItem(partPrice, emptyCell);
                if (emptyCell) {
                    setText(null);
                } else {
                    if (partPrice == null) {
                        setText(null);
                        return;
                    }
                    setText(NumberFormat.getCurrencyInstance().format(partPrice.doubleValue()));
                }
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        onAddProductClick();
        onModifyPartClick();
        onAddPartButtonClick();
        onProdModClick();
        onPartDeleteClick();
        onProductDeleteClick();
        onExitButtonClick();
        getPartResults();
        getProductResults();

        partsTable.setItems(Inventory.getAllParts());

        partId.setCellValueFactory(new PropertyValueFactory<>("id"));
        partName.setCellValueFactory(new PropertyValueFactory<>("name"));
        partInventory.setCellValueFactory(new PropertyValueFactory<>("stock"));
        partPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatPartPriceCol();

        productsTable.setItems(Inventory.getAllProducts());

        productId.setCellValueFactory(new PropertyValueFactory<>("id"));
        productName.setCellValueFactory(new PropertyValueFactory<>("name"));
        productInventory.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatProductPriceCol();


    }
}
