package com.inventory.model;

import java.math.BigDecimal;

public final class Outsourced extends Part {
    private final String companyName;
    public Outsourced(int id, String name, BigDecimal price, int stock, int min, int max, String companyName) {
        super(id, name, price, stock, min, max);
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name is required.");
        }
        this.companyName = companyName.strip();
    }
    public String getCompanyName() { return companyName; }
}
