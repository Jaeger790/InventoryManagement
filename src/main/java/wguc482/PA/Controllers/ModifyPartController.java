package wguc482.PA.Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import wguc482.PA.Model.InHouse;
import wguc482.PA.Model.Inventory;
import wguc482.PA.Model.Outsourced;
import wguc482.PA.Model.Part;

public class ModifyPartController implements Initializable {

    private final MainController mainController = new MainController();

    public Button CancelButton, savePart;

    public RadioButton inHouse, outsourced;

    public TextField partId, partName, partInventory, partPrice, partMax, partMin, machineId;

    public Label addPartHeading, partIdL, maxL, minL, priceL, inventoryL, partNameL, machineIdL;

    private Part modPart;

    private void checkPartType() {
        String idT = Integer.toString(modPart.getId());
        partId.setText(idT);
        if (modPart instanceof Outsourced) {
            outsourced.setSelected(true);
            partName.setText(modPart.getName());
            partInventory.setText(String.valueOf(modPart.getStock()));
            partPrice.setText(String.valueOf(modPart.getPrice()));
            partMax.setText(String.valueOf(modPart.getMax()));
            partMin.setText(String.valueOf(modPart.getMin()));
            machineIdL.setText("Company");
            machineIdL.setLayoutX(30);
            machineId.setText(String.valueOf(((Outsourced) modPart).getCompanyName(modPart)));
        } else {
            inHouse.setSelected(true);
            partName.setText(modPart.getName());
            partInventory.setText(String.valueOf(modPart.getStock()));
            partPrice.setText(String.valueOf(modPart.getPrice()));
            partMax.setText(String.valueOf(modPart.getMax()));
            partMin.setText(String.valueOf(modPart.getMin()));
            machineIdL.setText("Machine Id");
            machineIdL.setLayoutX(20);
            machineId.setText(String.valueOf(((InHouse) modPart).getMachineId(modPart)));
        }
        partId.setText(idT);

    }

    public void fillPartFields(Part selectedPart) {
        modPart = selectedPart;

    }

    private void onInHouseRadio() {
        inHouse.setOnAction(actionEvent -> {
            machineIdL.setText("Machine Id");
            machineIdL.setLayoutX(20);
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
        String idT = Integer.toString(modPart.getId());
        partId.setText(idT);

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

    /**
     * Logic error: validation to ensure that the inventory allocation was properly
     * assigned was not stopping the
     * saving of parts. The display errors were showing but the part was saved
     * regardless of the error. Changed the
     * inventoryValidate from void to boolean and added return statements for each
     * failed condition.
     */
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
        partInventory.getStyleClass().remove("textFieldError");
        partInventory.getStyleClass().remove("textFieldError");
        return true;
    }

    private void saveModifiedPart() {
        savePart.setOnAction(actionEvent -> {
            if (!textValidate() || !numberCheck() || !inventoryValidate()) {
                return;
            }
            savePart();
            switchScene(actionEvent);

        });
    }

    private void savePart() {
        if (inHouse.isSelected()) {

            InHouse newMod = new InHouse(0, "", 0, 0, 0, 0, 0);
            newMod.setId(modPart.getId());
            newMod.setName(partName.getText());
            newMod.setPrice(Double.parseDouble(partPrice.getText()));
            newMod.setStock(Integer.parseInt(partInventory.getText()));
            newMod.setMin(Integer.parseInt(partMin.getText()));
            newMod.setMax(Integer.parseInt(partMax.getText()));
            newMod.setMachineId(Integer.parseInt(machineId.getText()));
            Inventory.updatePart(newMod, modPart);
        } else {
            Outsourced saveOutsourced = new Outsourced(0, "", 0, 0, 0, 0, "");
            saveOutsourced.setId(modPart.getId());
            saveOutsourced.setName(partName.getText());
            saveOutsourced.setPrice(Double.parseDouble(partPrice.getText()));
            saveOutsourced.setStock(Integer.parseInt(partInventory.getText()));
            saveOutsourced.setMin(Integer.parseInt(partMin.getText()));
            saveOutsourced.setMax(Integer.parseInt(partMax.getText()));
            saveOutsourced.setCompanyName(machineId.getText());
            Inventory.updatePart(saveOutsourced, modPart);
            System.out.println(saveOutsourced);
        }
    }

    private void onCancelClick() {
        CancelButton.setOnAction(actionEvent -> switchScene(actionEvent));
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
        addPartHeading.setText("Modify Part");
        checkPartType();
        onCancelClick();
        onInHouseRadio();
        onOutSourcedRadio();
        saveModifiedPart();
    }
}
