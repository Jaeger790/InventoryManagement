package com.inventory.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** Part IDs keep associations intact when a part is edited. */
public final class Product {
    private final int id;
    private final String name;
    private final BigDecimal price;
    private final int stock;
    private final int min;
    private final int max;
    private final List<Integer> associatedPartIds;

    public Product(int id, String name, BigDecimal price, int stock, int min, int max,
                   Collection<Integer> associatedPartIds) {
        InventoryRules.validate(id, name, price, stock, min, max);
        if (associatedPartIds == null) throw new IllegalArgumentException("Associated parts cannot be null.");
        if (associatedPartIds.stream().anyMatch(partId -> partId == null || partId <= 0)) {
            throw new IllegalArgumentException("Associated part IDs must be positive.");
        }
        this.id = id;
        this.name = name.strip();
        this.price = price;
        this.stock = stock;
        this.min = min;
        this.max = max;
        this.associatedPartIds = List.copyOf(new LinkedHashSet<>(associatedPartIds));
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public int getMin() { return min; }
    public int getMax() { return max; }
    public List<Integer> getAssociatedPartIds() { return associatedPartIds; }
}
