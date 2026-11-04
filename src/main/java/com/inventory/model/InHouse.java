package com.inventory.model;

import java.math.BigDecimal;

public final class InHouse extends Part {
    private final int machineId;
    public InHouse(int id, String name, BigDecimal price, int stock, int min, int max, int machineId) {
        super(id, name, price, stock, min, max);
        if (machineId <= 0) throw new IllegalArgumentException("Machine ID must be positive.");
        this.machineId = machineId;
    }
    public int getMachineId() { return machineId; }
}
