package com.inventory.repository;

import com.inventory.model.Part;
import com.inventory.model.Product;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    int nextPartId();
    int nextProductId();
    List<Part> findAllParts();
    List<Product> findAllProducts();
    Optional<Part> findPart(int id);
    Optional<Product> findProduct(int id);
    void savePart(Part part);
    void saveProduct(Product product);
    boolean removePart(int id);
    boolean removeProduct(int id);
}
