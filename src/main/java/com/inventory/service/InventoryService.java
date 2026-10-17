package com.inventory.service;

import com.inventory.model.InHouse;
import com.inventory.model.Outsourced;
import com.inventory.model.Part;
import com.inventory.model.Product;
import com.inventory.repository.InventoryRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class InventoryService {
    public enum PartSource { IN_HOUSE, OUTSOURCED }
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }
    public List<Part> allParts() { return repository.findAllParts(); }
    public List<Product> allProducts() { return repository.findAllProducts(); }
    public Part getPart(int id) {
        return repository.findPart(id).orElseThrow(() -> new IllegalArgumentException("Part not found: " + id));
    }
    public Product getProduct(int id) {
        return repository.findProduct(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Part createPart(PartSource source, String name, BigDecimal price, int stock, int min, int max,
                           String sourceDetail) {
        int id = repository.nextPartId();
        Part part = makePart(id, source, name, price, stock, min, max, sourceDetail);
        repository.savePart(part);
        return part;
    }
    public Part updatePart(int id, PartSource source, String name, BigDecimal price, int stock, int min, int max,
                           String sourceDetail) {
        getPart(id); // Reject missing parts before saving.
        Part part = makePart(id, source, name, price, stock, min, max, sourceDetail);
        repository.savePart(part);
        return part;
    }
    private Part makePart(int id, PartSource source, String name, BigDecimal price, int stock, int min, int max,
                          String sourceDetail) {
        Objects.requireNonNull(source, "Part source is required.");
        if (source == PartSource.IN_HOUSE) {
            int machineId;
            try { machineId = Integer.parseInt(sourceDetail == null ? "" : sourceDetail.strip()); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Machine ID must be a positive whole number."); }
            return new InHouse(id, name, price, stock, min, max, machineId);
        }
        return new Outsourced(id, name, price, stock, min, max, sourceDetail);
    }

    public boolean deletePart(int id) {
        if (repository.findPart(id).isEmpty()) return false;
        boolean used = repository.findAllProducts().stream()
                .anyMatch(product -> product.getAssociatedPartIds().contains(id));
        if (used) throw new IllegalStateException("Remove this part from its products before deleting it.");
        return repository.removePart(id);
    }

    public Product createProduct(String name, BigDecimal price, int stock, int min, int max,
                                 Collection<Integer> associatedIds) {
        validateAssociations(associatedIds);
        Product product = new Product(repository.nextProductId(), name, price, stock, min, max, associatedIds);
        repository.saveProduct(product);
        return product;
    }
    public Product updateProduct(int id, String name, BigDecimal price, int stock, int min, int max,
                                 Collection<Integer> associatedIds) {
        getProduct(id);
        validateAssociations(associatedIds);
        Product product = new Product(id, name, price, stock, min, max, associatedIds);
        repository.saveProduct(product);
        return product;
    }
    private void validateAssociations(Collection<Integer> ids) {
        if (ids == null) throw new IllegalArgumentException("Associated part list is required.");
        for (Integer id : ids) {
            if (id == null || repository.findPart(id).isEmpty()) {
                throw new IllegalArgumentException("Associated part does not exist: " + id);
            }
        }
    }
    public boolean deleteProduct(int id) {
        Product product = repository.findProduct(id).orElse(null);
        if (product == null) return false;
        if (!product.getAssociatedPartIds().isEmpty()) {
            throw new IllegalStateException("Remove associated parts before deleting this product.");
        }
        return repository.removeProduct(id);
    }
    public List<Part> associatedParts(Product product) {
        return product.getAssociatedPartIds().stream().map(this::getPart).toList();
    }
    public List<Part> searchParts(String query) {
        String text = normalized(query);
        if (text.isEmpty()) return allParts();
        return allParts().stream().filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(text)
                || Integer.toString(p.getId()).contains(text)).toList();
    }
    public List<Product> searchProducts(String query) {
        String text = normalized(query);
        if (text.isEmpty()) return allProducts();
        return allProducts().stream().filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(text)
                || Integer.toString(p.getId()).contains(text)).toList();
    }
    private String normalized(String query) { return query == null ? "" : query.strip().toLowerCase(Locale.ROOT); }
}
