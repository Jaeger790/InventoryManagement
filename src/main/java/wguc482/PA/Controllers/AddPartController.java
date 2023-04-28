/**
 * Controller for the Add Part screen
 * @author Brad Rott
 */
package wguc482.PA.Controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import wguc482.PA.Model.InHouse;
import wguc482.PA.Model.Inventory;
import wguc482.PA.Model.Outsourced;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AddPartController implements Initializable {
    private final MainController mainController = new MainController();
    @FXML
    public Button CancelButton, savePart;
    @FXML
    public RadioButton inHouse, outsourced;
    @FXML
    public TextField partId, partName, partInventory, partPrice, partMax, partMin, machineId;
    @FXML
    public Label machineIdL;
    private final String newId = idGenerator().toString();
    private static int partIdCounter = -1;

    private Integer idGenerator() {
        return partIdCounter += 2;
    }

    private void onInHouseRadio() {
        inHouse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                machineIdL.setText("Machine Id");
                machineIdL.setLayoutX(20);

            }
        });

    }

    private void onOutSourcedRadio() {
        outsourced.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                machineIdL.setText("Company");
                machineIdL.setLayoutX(30);
            }
        });
    }

    /**
     * Runtime Error: Target Invocation Exception: Error occurred when attempting to
     * switch scenes from the add part
     * screen back to the main screen. Error was resolved by removing the controller
     * parameter in the MainScreen.fxml
     * file and setting the controller to switch based on which scene was showing.
     **/
    private void onSaveClick() {
        savePart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (!textValidate() || !numberCheck() || !inventoryValidate()) {
                    return;
                }
                savePart();
                switchScene(actionEvent);
            }
        });
    }

    private void savePart() {
        if (inHouse.isSelected()) {
            InHouse saveInHouse = new InHouse(idGenerator(), partName.getText(),
                    Double.parseDouble(partPrice.getText()),
                    Integer.parseInt(partInventory.getText()), Integer.parseInt(partMin.getText()),
                    Integer.parseInt(partMax.getText()),
                    Integer.parseInt(machineId.getText()));
            Inventory.addPart(saveInHouse);

        } else {
            Outsourced saveOutsourced = new Outsourced(0, "", 0, 0, 0, 0, "");
            saveOutsourced.setId(idGenerator());
            saveOutsourced.setName(partName.getText());
            saveOutsourced.setPrice(Double.parseDouble(partPrice.getText()));
            saveOutsourced.setStock(Integer.parseInt(partInventory.getText()));
            saveOutsourced.setMin(Integer.parseInt(partMin.getText()));
            saveOutsourced.setMax(Integer.parseInt(partMax.getText()));
            saveOutsourced.setCompanyName(machineId.getText());
            Inventory.addPart(saveOutsourced);
        }
    }

    private boolean numberCheck() {
        Alert notNumeric = new Alert(Alert.AlertType.ERROR);
        notNumeric.setHeaderText("Numeric values only.");
        if (!partMin.getText().matches("[0-9]*")) {
            partMin.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Part min must be a numeric value.");
            notNumeric.show();
            return false;
        } else if (!partInventory.getText().matches("[0-9]*")) {
            partInventory.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Part inventory must be a numeric value.");
            notNumeric.show();
            return false;
        } else if (!partMax.getText().matches("[0-9]*")) {
            partMax.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Part max must be a numeric value.");
            notNumeric.show();
            return false;
        } else if (inHouse.isSelected() && !machineId.getText().matches("[0-9]*")) {
            machineId.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Machine id must be a numeric value.");
            notNumeric.show();
            return false;
        } else if (!partPrice.getText().matches("[0-9]+([,.][0-9]{1,2})?")) {
            partPrice.getStyleClass().add("textFieldError");
            notNumeric.setContentText("Part price must be a numeric value.");
            notNumeric.show();
            return false;
        }
        partInventory.getStyleClass().remove("textFieldError");
        partMin.getStyleClass().remove("textFieldError");
        partMax.getStyleClass().remove("textFieldError");
        machineId.getStyleClass().remove("textFieldError");
        partPrice.getStyleClass().remove("textFieldError");
        return true;
    }

    private boolean inventoryValidate() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Incorrect Inventory Allocation");
        int inv = Integer.parseInt(partInventory.getText());
        int max = Integer.parseInt(partMax.getText());
        int min = Integer.parseInt(partMin.getText());
        if (min > inv) {
            partInventory.getStyleClass().add("textFieldError");
            partMin.getStyleClass().add("textFieldError");
            alert.setContentText("Min value cannot be greater than inventory value");
            alert.show();
            return false;

        } else if (min > max) {
            partMin.getStyleClass().add("textFieldError");
            partMax.getStyleClass().add("textFieldError");
            alert.setContentText("Min value cannot be greater than max value");
            alert.show();
            return false;

        } else if (inv > max) {
            partInventory.getStyleClass().add("textFieldError");
            partMax.getStyleClass().add("textFieldError");
            alert.setContentText("Inventory value cannot be greater than max value");
            alert.show();
            return false;
        }
        partInventory.getStyleClass().remove("textFieldError");
        partMax.getStyleClass().remove("textFieldError");
        partMin.getStyleClass().remove("textFieldError");
        return true;
    }

    /**
     * Logic Error: Error fields were improperly applying and removing red border
     * around text boxes and displaying
     * the incorrect error message boxes. Altered for loops and conditional
     * statements to properly update the styles
     * and show the correct messages.
     * 
     * @return
     */
    private boolean textValidate() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String partNameT = partName.getText();
        String partInvT = partInventory.getText();
        String partPriceT = partPrice.getText();
        String partMaxT = partMax.getText();
        String partMinT = partMin.getText();
        String machineIdT = machineId.getText();
        List<String> fieldList = new ArrayList<>(
                Arrays.asList(partNameT, partInvT, partPriceT, partMaxT, partMinT, machineIdT));
        List<TextField> addPartFields = new ArrayList<>(Arrays.asList(partName, partInventory, partPrice,
                partMax, partMin, machineId));
        System.out.println(fieldList);
        partId.setText(newId);

        for (int i = 0; i < fieldList.size(); i++) {
            addPartFields.get(i).getStyleClass().remove("textFieldError");
            if (!partNameT.matches("[a-zA-Z0-9\\s]*")) {
                partName.getStyleClass().add("textFieldError");
                alert.setHeaderText("Improper part name");
                alert.setContentText(
                        "Part names must contain letters and numbers only.  Special characters and empty " +
                                "names are not allowed.");
                alert.show();
                return false;
            }
            partName.getStyleClass().remove("textFieldError");

            if (fieldList.get(i).isEmpty()) {
                addPartFields.get(i).getStyleClass().add("textFieldError");
                alert.setHeaderText("Empty field");
                alert.setContentText("Fill out all the fields");
                alert.show();
                return false;
            }
        }

        return true;
    }

    private void onCancelClick() {
        CancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switchScene(actionEvent);
            }
        });
    }

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        partId.setText("Disabled- Auto-gen");
        onCancelClick();
        onOutSourcedRadio();
        onInHouseRadio();
        onSaveClick();

    }
}
