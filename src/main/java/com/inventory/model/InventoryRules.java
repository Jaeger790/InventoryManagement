package com.inventory.model;

import java.math.BigDecimal;

public final class InventoryRules {
    private InventoryRules() {}

    public static void validate(int id, String name, BigDecimal price, int stock, int min, int max) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (name.strip().length() > 120) throw new IllegalArgumentException("Name must be 120 characters or fewer.");
        if (price == null || price.signum() < 0 || price.scale() > 2) {
            throw new IllegalArgumentException("Price must be nonnegative with at most two decimal places.");
        }
        if (min < 0 || max < 0 || stock < 0 || min > max || stock < min || stock > max) {
            throw new IllegalArgumentException("Inventory must satisfy 0 <= minimum <= stock <= maximum.");
        }
    }
}
