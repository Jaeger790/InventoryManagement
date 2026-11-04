package com.inventory.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.function.Function;

final class UiSupport {
    private UiSupport() {}
    static <T, V> void column(TableColumn<T, V> column, Function<T, V> getter) {
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getter.apply(cell.getValue())));
    }
    static <T> void moneyColumn(TableColumn<T, BigDecimal> column, Function<T, BigDecimal> getter) {
        column(column, getter);
        column.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : NumberFormat.getCurrencyInstance().format(value));
            }
        });
    }
    static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Inventory Management");
        alert.setHeaderText("Unable to complete action");
        alert.setContentText(message);
        alert.showAndWait();
    }
    static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirm action");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
    static int integer(String value, String label) {
        try { return Integer.parseInt(value == null ? "" : value.strip()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException(label + " must be a whole number."); }
    }
    static BigDecimal money(String value) {
        try { return new BigDecimal(value == null ? "" : value.strip()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Price must be a valid number."); }
    }
}
