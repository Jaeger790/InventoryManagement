package com.inventory.model;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class Part {
    private final int id;
    private final String name;
    private final BigDecimal price;
    private final int stock;
    private final int min;
    private final int max;

    protected Part(int id, String name, BigDecimal price, int stock, int min, int max) {
        InventoryRules.validate(id, name, price, stock, min, max);
        this.id = id;
        this.name = name.strip();
        this.price = price;
        this.stock = stock;
        this.min = min;
        this.max = max;
    }
    public final int getId() { return id; }
    public final String getName() { return name; }
    public final BigDecimal getPrice() { return price; }
    public final int getStock() { return stock; }
    public final int getMin() { return min; }
    public final int getMax() { return max; }
    @Override public final boolean equals(Object other) {
        return this == other || (other instanceof Part part && id == part.id);
    }
    @Override public final int hashCode() { return Objects.hash(id); }
}
