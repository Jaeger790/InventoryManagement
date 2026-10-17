package com.inventory.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryRulesTest {
    @Test void acceptsValidBounds() {
        assertDoesNotThrow(() -> InventoryRules.validate(1, "Brake", new BigDecimal("12.99"), 5, 0, 10));
    }
    @Test void rejectsEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> InventoryRules.validate(1, "  ", BigDecimal.ZERO, 1, 0, 3));
    }
    @Test void rejectsZeroId() {
        assertThrows(IllegalArgumentException.class, () -> InventoryRules.validate(0, "Part", BigDecimal.ZERO, 1, 0, 3));
    }
    @Test void rejectsNegativeBounds() {
        assertThrows(IllegalArgumentException.class, () -> InventoryRules.validate(1, "Part", BigDecimal.ZERO, 0, -1, 3));
    }
}
