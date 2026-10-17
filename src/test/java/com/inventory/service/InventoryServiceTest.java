package com.inventory.service;

import com.inventory.model.InHouse;
import com.inventory.model.Outsourced;
import com.inventory.model.Part;
import com.inventory.model.Product;
import com.inventory.repository.InMemoryInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {
    InventoryService service;
    @BeforeEach void setup() { service = new InventoryService(new InMemoryInventoryRepository()); }
    Part part(String name) {
        return service.createPart(InventoryService.PartSource.IN_HOUSE, name, new BigDecimal("12.50"), 5, 1, 9, "2");
    }
    Product product(List<Integer> partIds) {
        return service.createProduct("Bike", new BigDecimal("50.00"), 1, 0, 2, partIds);
    }
    @Test void createsInHousePart() {
        Part p = part("Wheel");
        assertInstanceOf(InHouse.class, p);
        assertEquals(2, ((InHouse) p).getMachineId());
        assertEquals(new BigDecimal("12.50"), p.getPrice());
    }
    @Test void createsOutsourcedPart() {
        Part p = service.createPart(InventoryService.PartSource.OUTSOURCED, "Bolt", BigDecimal.ZERO, 0, 0, 5, "Acme");
        assertEquals("Acme", ((Outsourced) p).getCompanyName());
    }
    @Test void idsStayUniqueAcrossDeletes() {
        int first = part("First").getId();
        service.deletePart(first);
        assertTrue(part("Second").getId() > first);
    }
    @Test void updatesPartWithoutBreakingProductAssociation() {
        Part old = part("Brake");
        Product prod = product(List.of(old.getId()));
        service.updatePart(old.getId(), InventoryService.PartSource.OUTSOURCED, "Brake v2", new BigDecimal("13"), 5, 0, 9, "Vendor");
        assertEquals("Brake v2", service.associatedParts(service.getProduct(prod.getId())).get(0).getName());
        assertEquals(List.of(old.getId()), service.getProduct(prod.getId()).getAssociatedPartIds());
    }
    @Test void associationMustReferToExistingPart() {
        assertThrows(IllegalArgumentException.class, () -> product(List.of(999)));
    }
    @Test void duplicateAssociationsAreCollapsed() {
        int id = part("Widget").getId();
        assertEquals(List.of(id), product(List.of(id, id)).getAssociatedPartIds());
    }
    @Test void cannotDeleteAssociatedPart() {
        Part p = part("Used"); product(List.of(p.getId()));
        assertThrows(IllegalStateException.class, () -> service.deletePart(p.getId()));
    }
    @Test void cannotDeleteProductUntilAssociationsRemoved() {
        Part p = part("Used"); Product prod = product(List.of(p.getId()));
        assertThrows(IllegalStateException.class, () -> service.deleteProduct(prod.getId()));
        service.updateProduct(prod.getId(), prod.getName(), prod.getPrice(), prod.getStock(), prod.getMin(), prod.getMax(), List.of());
        assertTrue(service.deleteProduct(prod.getId()));
        assertTrue(service.deletePart(p.getId()));
    }
    @Test void rejectsInvalidStockBounds() {
        assertThrows(IllegalArgumentException.class, () ->
            service.createPart(InventoryService.PartSource.IN_HOUSE, "Bolt", BigDecimal.ONE, 10, 5, 9, "1"));
        assertThrows(IllegalArgumentException.class, () ->
            service.createPart(InventoryService.PartSource.IN_HOUSE, "Bolt", BigDecimal.ONE, 1, 4, 9, "1"));
    }
    @Test void rejectsNegativeOrOverpreciseMoney() {
        assertThrows(IllegalArgumentException.class, () ->
            service.createPart(InventoryService.PartSource.IN_HOUSE, "Bolt", new BigDecimal("-1"), 1, 0, 2, "1"));
        assertThrows(IllegalArgumentException.class, () ->
            service.createPart(InventoryService.PartSource.IN_HOUSE, "Bolt", new BigDecimal("1.123"), 1, 0, 2, "1"));
    }
    @Test void failedUpdatePreservesExistingRecord() {
        Part p = part("Valid");
        assertThrows(IllegalArgumentException.class, () ->
            service.updatePart(p.getId(), InventoryService.PartSource.IN_HOUSE, "", BigDecimal.ONE, 1, 0, 3, "1"));
        assertEquals("Valid", service.getPart(p.getId()).getName());
    }
    @Test void searchByNameAndPartialNumericId() {
        Part p = part("Exhaust");
        assertEquals(p.getId(), service.searchParts("xha").get(0).getId());
        assertEquals(p.getId(), service.searchParts(Integer.toString(p.getId())).get(0).getId());
        assertTrue(service.searchProducts("unused").isEmpty());
    }
    @Test void rejectsUnknownUpdate() {
        assertThrows(IllegalArgumentException.class, () ->
            service.updateProduct(500, "Nope", BigDecimal.ONE, 0, 0, 1, List.of()));
    }
    @Test void returnedAssociationListCannotBeModified() {
        Part p = part("Part"); Product prod = product(List.of(p.getId()));
        assertThrows(UnsupportedOperationException.class, () -> prod.getAssociatedPartIds().clear());
    }
    @Test void failedCreateDoesNotInsertInvalidPart() {
        assertThrows(IllegalArgumentException.class, () -> service.createPart(
            InventoryService.PartSource.OUTSOURCED, "Part", BigDecimal.ONE, 1, 0, 2, " "));
        assertEquals(0, service.allParts().size());
    }
}
