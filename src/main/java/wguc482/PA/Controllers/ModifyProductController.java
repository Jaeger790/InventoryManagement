package wguc482.PA.Controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import wguc482.PA.Model.Inventory;
import wguc482.PA.Model.Part;
import wguc482.PA.Model.Product;

public class ModifyProductController implements Initializable {
    public TableView<Part> associatedPartTable;
    public TableColumn<Part, Integer> associatedId;
    public TableColumn<Part, String> associatedName;
    public TableColumn<Part, Integer> associatedInv;
    public TableColumn<Part, Double> associatedPrice;
    public TableView<Part> partListTable;
    public TableColumn<Part, Integer> partId;
    public TableColumn<Part, String> partName;
    public TableColumn<Part, Integer> partInv;
    public TableColumn<Part, Double> partPrice;
    public Label productHeader;
    public Button associatedPartButton, saveProductButton, cancelAddProductB, removeAssociatedPartB;
    public TextField productIdT, productNameT, productInventoryT, productPriceT, productMaxT, productMinT, searchBox;
    private final MainController mainController = new MainController();
    private Product modProd;
    private Inventory inv;
    private final ObservableList<Part> partsAssociated = FXCollections.observableArrayList();

    private void getPartResults() {
        searchBox.setOnKeyTyped(actionEvent -> {
            String searchText = searchBox.getText();
            ObservableList<Part> parts = inv.lookupPart(searchText);

            if (parts.size() == 0) {
                try {
                    int id = Integer.parseInt(searchText);
                    Part prt = inv.lookupPart(id);
                    parts.add(prt);
                } catch (NumberFormatException n) {
                    Alert wrongInput = new Alert(Alert.AlertType.WARNING);
                    wrongInput.setTitle("Search Failed");
                    wrongInput.setHeaderText("Incorrect Input Detected!");
                    wrongInput.setContentText("Combinations of letters and numbers are not valid search parameters.");
                    wrongInput.showAndWait();
                }
            }
            partListTable.setItems(parts);
        });
    }

    private void onAddPartClick() {
        associatedPartButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Part selectedPart = partListTable.getSelectionModel().getSelectedItem();
                try {
                    if (selectedPart == null) {
                        throw new InvocationTargetException(null);

                    }
                } catch (InvocationTargetException e) {
                    Alert noSelection = new Alert(Alert.AlertType.ERROR);
                    noSelection.setTitle("Error");
                    noSelection.setHeaderText("No Part Selected!");
                    noSelection.setContentText("You must first select a part to associate it to a product.");
                    noSelection.show();
                    return;
                }
                partsAssociated.add(selectedPart);
                associatedPartTable.setItems(partsAssociated);
                System.out.println(partsAssociated);
            }
        });
    }

    private void removeAssociatedPart() {
        removeAssociatedPartB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                Part selectedPart = associatedPartTable.getSelectionModel().getSelectedItem();

                try {
                    if (selectedPart == null) {
                        throw new InvocationTargetException(null);
                    }
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you wish to delete this part?");
                    alert.setTitle("Delete Part?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        partsAssociated.remove(selectedPart);
                        associatedPartTable.setItems(partsAssociated);
                        System.out.println(partsAssociated);
                    }
                } catch (InvocationTargetException e) {
                    Alert noSelection = new Alert(Alert.AlertType.ERROR);
                    noSelection.setTitle("Error");
                    noSelection.setHeaderText("No Part Selected!");
                    noSelection.setContentText("You must first select an associated part from the table to remove it.");
                    noSelection.show();
                }

            }
        });
    }

    /**
     * Logic Error: Added and removed associated parts were not being updated to the
     * global list. The table was
     * instead resetting all parts that were added to the local scene list of
     * associated parts. Added a constructor
     * for the ModifyPartsController that holds parameters for reference variables
     * carried over from the main screen.
     */
    private void onSaveClick() {
        saveProductButton.setOnAction(actionEvent -> {
            if (!textValidate() || !numberCheck() || !inventoryValidate()) {
                return;
            }
            saveProduct();
            switchScene(actionEvent);
        });
    }

    private boolean numberCheck() {
        Alert notNumeric = new Alert(Alert.AlertType.ERROR);
        notNumeric.setHeaderText("Numeric values only.");
        productInventoryT.getStyleClass().remove("textFieldError");
        productMinT.getStyleClass().remove("textFieldError");
        productMaxT.getStyleClass().remove("textFieldError");
        partPrice.getStyleClass().remove("textFieldError");
        if (!productMinT.getText().matches("[0-9]*")) {
            productMinT.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Product min must be a numeric value.");
            notNumeric.show();
            return false;
        } else if (!productInventoryT.getText().matches("[0-9]*")) {
            productInventoryT.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Product inventory must be a numeric value.");
            notNumeric.show();
            return false;
        } else if (!productMaxT.getText().matches("[0-9]*")) {
            productMaxT.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Product max must be a numeric value.");
            notNumeric.show();
            return false;
        } else if (!productPriceT.getText().matches("[0-9]+([,.][0-9]{1,2})?")) {
            productPriceT.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Product price must be a numeric value.");
            notNumeric.show();
            return false;
        }
        productInventoryT.getStyleClass().remove("textFieldError");
        productMinT.getStyleClass().remove("textFieldError");
        productMaxT.getStyleClass().remove("textFieldError");
        partPrice.getStyleClass().remove("textFieldError");
        return true;
    }

    private boolean inventoryValidate() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Incorrect Inventory Allocation");
        int inv = Integer.parseInt(productInventoryT.getText());
        int max = Integer.parseInt(productMaxT.getText());
        int min = Integer.parseInt(productMinT.getText());

        if (min > inv) {
            productInventoryT.getStyleClass().add("textFieldError");
            productMinT.getStyleClass().add("textFieldError");
            alert.setContentText("Min value cannot be greater than inventory value");
            alert.show();
            return false;
        } else if (min > max) {
            productMinT.getStyleClass().add("textFieldError");
            productMaxT.getStyleClass().add("textFieldError");
            alert.setContentText("Min value cannot be greater than max value");
            alert.show();
            return false;

        } else if (inv > max) {
            productInventoryT.getStyleClass().add("textFieldError");
            productMaxT.getStyleClass().add("textFieldError");
            alert.setContentText("Inventory value cannot be greater than max value");
            alert.show();
            return false;
        }
        productInventoryT.getStyleClass().remove("textFieldError");
        productMaxT.getStyleClass().remove("textFieldError");
        productMinT.getStyleClass().remove("textFieldError");
        return true;
    }

    private boolean textValidate() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String prodName = productNameT.getText();
        String prodInv = productInventoryT.getText();
        String prodPrice = productPriceT.getText();
        String prodMax = productMaxT.getText();
        String prodMin = productMinT.getText();
        List<String> fieldList = new ArrayList<>(Arrays.asList(prodName, prodInv, prodPrice, prodMax, prodMin));
        List<TextField> addProdFields = new ArrayList<>(Arrays.asList(productNameT, productInventoryT, productPriceT,
                productMaxT, productMinT));
        System.out.println(fieldList);

        productNameT.getStyleClass().remove("textFieldError");

        if (!prodName.matches("^[a-zA-Z0-9\\s]*")) {
            productNameT.getStyleClass().add("textFieldError");
            alert.setHeaderText("Improper product name");
            alert.setContentText("Product names must contain letters and numbers only.  Special characters and " +
                    "empty names are not allowed.");
            alert.show();
            return false;
        }

        productNameT.getStyleClass().remove("textFieldError");
        for (int i = 0; i < fieldList.size(); i++) {
            if (fieldList.get(i).isEmpty()) {
                addProdFields.get(i).getStyleClass().add("textFieldError");
                alert.setHeaderText("Empty field");
                alert.setContentText("Fill out all the fields");
                alert.show();
                return false;
            }
        }

        return true;
    }

    private void saveProduct() {
        Product newProd = new Product(0, "", 0, 0, 0, 0);
        newProd.setId(modProd.getId());
        newProd.setName(productNameT.getText());
        newProd.setPrice(Double.parseDouble(productPriceT.getText()));
        newProd.setStock(Integer.parseInt(productInventoryT.getText()));
        newProd.setMin(Integer.parseInt(productMinT.getText()));
        newProd.setMax(Integer.parseInt(productMaxT.getText()));
        Inventory.updateProduct(newProd, modProd);
        for (Part part : partsAssociated) {
            newProd.addAssociatedPart(part);
        }
    }

    private void onCancelClick() {
        cancelAddProductB.setOnAction(actionEvent -> {
            partsAssociated.removeAll();
            switchScene(actionEvent);
        });
    }

    /**
     *
     * @method switchScene
     *         Future Enhancement: Use separate windows to load the add and modify
     *         scenes. Only allow one window to be
     *         opened at a time.
     */
    private void switchScene(ActionEvent actionEvent) {
        try {
            Stage mainStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/wguc482/PA/MainScreen.fxml")));
            loader.setController(mainController);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.setTitle("Main Screen");
            mainStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getProductFields(Product selectedProduct) {
        modProd = selectedProduct;

    }

    private void fillProductFields() {
        String idT = Integer.toString(modProd.getId());
        productIdT.setText(idT);
        productNameT.setText(modProd.getName());
        productInventoryT.setText(String.valueOf(modProd.getStock()));
        productPriceT.setText(String.valueOf(modProd.getPrice()));
        productMaxT.setText(String.valueOf(modProd.getMax()));
        productMinT.setText(String.valueOf(modProd.getMin()));
        for (int i = 0; i < modProd.getAllAssociatedParts().size(); i++) {
            partsAssociated.add(modProd.getAllAssociatedParts().get(i));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        NumberFormat formatPriceColumns = NumberFormat.getCurrencyInstance();
        productIdT.setDisable(true);
        productHeader.setText("Modify Product");
        onCancelClick();
        getPartResults();
        fillProductFields();
        onAddPartClick();
        removeAssociatedPart();
        onSaveClick();

        partListTable.setItems(Inventory.getAllParts());
        partId.setCellValueFactory(new PropertyValueFactory<>("id"));
        partName.setCellValueFactory(new PropertyValueFactory<>("name"));
        partInv.setCellValueFactory(new PropertyValueFactory<>("stock"));
        partPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        partPrice.setCellFactory(format -> new TableCell<>() {
            @Override
            protected void updateItem(Double partPrice, boolean emptyCell) {
                super.updateItem(partPrice, emptyCell);
                if (emptyCell) {
                    setText(null);
                } else {
                    setText(formatPriceColumns.format(partPrice));
                }
            }
        });

        associatedPartTable.setItems(modProd.getAllAssociatedParts());
        associatedId.setCellValueFactory(new PropertyValueFactory<>("id"));
        associatedName.setCellValueFactory(new PropertyValueFactory<>("name"));
        associatedInv.setCellValueFactory(new PropertyValueFactory<>("stock"));
        associatedPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        associatedPrice.setCellFactory(format -> new TableCell<>() {
            @Override
            protected void updateItem(Double associatedPrice, boolean emptyCell) {
                super.updateItem(associatedPrice, emptyCell);
                if (emptyCell) {
                    setText(null);
                } else {
                    setText(formatPriceColumns.format(associatedPrice));
                }
            }
        });
    }
}
