package com.inventory.repository;

public final class InventoryPersistenceException extends RuntimeException {
    public InventoryPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
    public InventoryPersistenceException(String message) { super(message); }
}
