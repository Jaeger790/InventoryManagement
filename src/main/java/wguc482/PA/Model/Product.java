package wguc482.PA.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Product {
    private final ObservableList<Part> associatedParts = FXCollections.observableArrayList();
    private int id;
    private String name;
    private double price;
    private int stock;
    private int min;
    private int max;

    // Constructor for Product
    public Product(int id, String name, double price, int stock, int min, int max) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.min = min;
        this.max = max;
    }

    /**
     * @return
     *         Gets product id and returns it
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Get and set name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Get and set price
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Get and set stock
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Get and set min
    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    // Get and set max
    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    // Get all associated parts
    public ObservableList<Part> getAllAssociatedParts() {
        return associatedParts;
    }

    public void addAssociatedPart(Part part) {
        associatedParts.add(part);
    }

    public boolean deleteAssociatedPart(Part selectedAssociatedPart) {
        for (Part part : associatedParts) {
            associatedParts.remove(part);
        }
        return true;
    }

}