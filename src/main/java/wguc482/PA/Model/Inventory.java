package wguc482.PA.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Inventory {
    private static final ObservableList<Part> allParts = FXCollections.observableArrayList();
    private static final ObservableList<Product> allProducts = FXCollections.observableArrayList();

    private static void addTestData() {
        Part brakeLever = new InHouse(50, "Brake Lever", 43.25, 4, 2, 8, 300);
        Inventory.addPart(brakeLever);
        Part pipe = new InHouse(65, "Exhaust", 230.20, 2, 1, 3, 250);
        Inventory.addPart(pipe);
        Part piston = new Outsourced(105, "Piston", 850.99, 1, 1, 2, "Weisco");
        Inventory.addPart(piston);

        Product dirtBike = new Product(42, "KTM XC 300", 2800.00, 1, 1, 4);
        Inventory.addProduct(dirtBike);
        Product motorcycle = new Product(34, "KTM Duke 790", 11757.56, 1, 1, 1);
        Inventory.addProduct(motorcycle);
    }

    static {
        addTestData();
    }

    public static void addPart(Part part) {
        allParts.add(part);
    }

    public static void addProduct(Product product) {
        allProducts.add(product);
    }

    public static boolean deletePart(Part selectedPart) {
        if (allParts.contains(selectedPart)) {
            allParts.remove(selectedPart);
            return true;
        }
        return false;
    }

    public static boolean deleteProduct(Product selectedProduct) {
        if (allProducts.contains(selectedProduct)) {
            allProducts.remove(selectedProduct);
            return true;
        }
        return false;
    }

    public static void updateProduct(Product newProduct, Product oldProduct) {
        addProduct(newProduct);
        deleteProduct(oldProduct);
    }

    public static void updatePart(Part newPart, Part oldPart) {
        addPart(newPart);
        deletePart(oldPart);
    }

    public ObservableList<Part> lookupPart(String partialName) {
        ObservableList<Part> partNames = FXCollections.observableArrayList();
        ObservableList<Part> allParts = Inventory.getAllParts();

        for (Part prt : allParts) {
            if (prt.getName().toUpperCase().contains(partialName.toUpperCase()) ||
                    prt.getName().toLowerCase().contains(partialName.toLowerCase())) {

                partNames.add(prt);
            }
        }
        return partNames;
    }

    public Part lookupPart(int id) {
        ObservableList<Part> allParts = Inventory.getAllParts();
        for (Part prt : allParts) {
            if (prt.getId() == id) {
                return prt;
            }
        }
        return null;
    }

    public Product lookupProduct(int id) {
        ObservableList<Product> allProducts = Inventory.getAllProducts();
        for (Product prod : allProducts) {
            if (prod.getId() == id) {
                return prod;
            }
        }
        return null;
    }

    public ObservableList<Product> lookupProduct(String partialName) {
        ObservableList<Product> productNames = FXCollections.observableArrayList();
        ObservableList<Product> allProducts = Inventory.getAllProducts();

        for (Product prod : allProducts) {
            if (prod.getName().toUpperCase().contains(partialName.toUpperCase()) ||
                    prod.getName().toLowerCase().contains(partialName.toLowerCase())) {
                productNames.add(prod);
            }
        }
        return productNames;
    }

    public static ObservableList<Product> getAllProducts() {
        return allProducts;
    }

    public static ObservableList<Part> getAllParts() {
        return allParts;
    }

}
