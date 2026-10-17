package com.inventory.repository;

import com.inventory.model.Part;
import com.inventory.model.Product;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<Integer, Part> parts = new LinkedHashMap<>();
    private final Map<Integer, Product> products = new LinkedHashMap<>();
    private int partSequence = 0;
    private int productSequence = 0;

    @Override public int nextPartId() { return ++partSequence; }
    @Override public int nextProductId() { return ++productSequence; }
    @Override public List<Part> findAllParts() { return List.copyOf(parts.values()); }
    @Override public List<Product> findAllProducts() { return List.copyOf(products.values()); }
    @Override public Optional<Part> findPart(int id) { return Optional.ofNullable(parts.get(id)); }
    @Override public Optional<Product> findProduct(int id) { return Optional.ofNullable(products.get(id)); }
    @Override public void savePart(Part part) {
        parts.put(part.getId(), part);
        partSequence = Math.max(partSequence, part.getId());
    }
    @Override public void saveProduct(Product product) {
        products.put(product.getId(), product);
        productSequence = Math.max(productSequence, product.getId());
    }
    @Override public boolean removePart(int id) { return parts.remove(id) != null; }
    @Override public boolean removeProduct(int id) { return products.remove(id) != null; }
}
