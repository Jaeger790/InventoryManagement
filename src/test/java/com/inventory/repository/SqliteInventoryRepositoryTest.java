package com.inventory.repository;

import com.inventory.model.InHouse;
import com.inventory.model.Outsourced;
import com.inventory.model.Part;
import com.inventory.model.Product;
import com.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteInventoryRepositoryTest {
    @TempDir Path temporaryDirectory;

    private SqliteInventoryRepository open() {
        return new SqliteInventoryRepository(temporaryDirectory.resolve("nested/inventory.db"));
    }
    private InventoryService service() { return new InventoryService(open()); }

    @Test void databaseIsCreatedAndStartsEmpty() {
        SqliteInventoryRepository repository = open();
        assertTrue(java.nio.file.Files.isRegularFile(repository.getDatabasePath()));
        assertTrue(repository.findAllParts().isEmpty());
        assertTrue(repository.findAllProducts().isEmpty());
    }

    @Test void partsProductsAssociationsSurviveRestartAndRetainOrder() {
        InventoryService first = service();
        Part made = first.createPart(InventoryService.PartSource.IN_HOUSE,
                "Motor", new BigDecimal("11.20"), 3, 1, 8, "42");
        Part bought = first.createPart(InventoryService.PartSource.OUTSOURCED,
                "Screw", new BigDecimal("0.10"), 5, 0, 10, "ACME");
        Product product = first.createProduct("Assembly", new BigDecimal("55.99"), 1, 0, 4,
                List.of(bought.getId(), made.getId()));

        InventoryService second = service();
        assertInstanceOf(InHouse.class, second.getPart(made.getId()));
        assertEquals(42, ((InHouse) second.getPart(made.getId())).getMachineId());
        assertInstanceOf(Outsourced.class, second.getPart(bought.getId()));
        assertEquals("ACME", ((Outsourced) second.getPart(bought.getId())).getCompanyName());
        assertEquals(new BigDecimal("11.20"), second.getPart(made.getId()).getPrice());
        assertEquals(List.of(bought.getId(), made.getId()),
                second.getProduct(product.getId()).getAssociatedPartIds());
        assertEquals(new BigDecimal("55.99"), second.getProduct(product.getId()).getPrice());
    }

    @Test void editingPartSubtypeDoesNotBreakLinkAfterRestart() {
        InventoryService first = service();
        int partId = first.createPart(InventoryService.PartSource.IN_HOUSE,
                "Motor", BigDecimal.ONE, 1, 0, 3, "7").getId();
        int productId = first.createProduct("Device", BigDecimal.TEN, 1, 0, 2, List.of(partId)).getId();
        first.updatePart(partId, InventoryService.PartSource.OUTSOURCED,
                "New motor", new BigDecimal("2.45"), 1, 0, 3, "Vendor");
        InventoryService second = service();
        assertEquals("Vendor", ((Outsourced) second.getPart(partId)).getCompanyName());
        assertEquals(List.of(partId), second.getProduct(productId).getAssociatedPartIds());
    }

    @Test void editingProductAtomicallyUpdatesAssociations() {
        InventoryService first = service();
        int a = first.createPart(InventoryService.PartSource.IN_HOUSE, "A", BigDecimal.ONE, 1, 0, 2, "3").getId();
        int b = first.createPart(InventoryService.PartSource.IN_HOUSE, "B", BigDecimal.ONE, 1, 0, 2, "4").getId();
        int productId = first.createProduct("Device", BigDecimal.TEN, 1, 0, 2, List.of(a)).getId();
        first.updateProduct(productId, "Device 2", BigDecimal.TEN, 1, 0, 2, List.of(b));
        assertEquals(List.of(b), service().getProduct(productId).getAssociatedPartIds());
        assertEquals("Device 2", service().getProduct(productId).getName());
    }

    @Test void invalidAssociationRollsBackAllChanges() {
        SqliteInventoryRepository repository = open();
        InventoryService service = new InventoryService(repository);
        int a = service.createPart(InventoryService.PartSource.IN_HOUSE,
                "A", BigDecimal.ONE, 1, 0, 2, "3").getId();
        int productId = service.createProduct("Original", BigDecimal.TEN, 1, 0, 2, List.of(a)).getId();
        // Bypass the service to check foreign keys and rollback.
        Product invalid = new Product(productId, "Corrupt", BigDecimal.TEN, 1, 0, 2, List.of(999999));
        assertThrows(InventoryPersistenceException.class, () -> repository.saveProduct(invalid));
        Product unchanged = service().getProduct(productId);
        assertEquals("Original", unchanged.getName());
        assertEquals(List.of(a), unchanged.getAssociatedPartIds());
    }

    @Test void deletionsRespectForeignKeysAndIdsRemainMonotonicAcrossReopen() {
        InventoryService first = service();
        int id = first.createPart(InventoryService.PartSource.IN_HOUSE,
                "A", BigDecimal.ONE, 1, 0, 2, "3").getId();
        int productId = first.createProduct("B", BigDecimal.ONE, 1, 0, 2, List.of(id)).getId();
        assertThrows(IllegalStateException.class, () -> open().removePart(id));
        first.updateProduct(productId, "B", BigDecimal.ONE, 1, 0, 2, List.of());
        assertTrue(first.deleteProduct(productId));
        assertTrue(first.deletePart(id));
        InventoryService second = service();
        int nextId = second.createPart(InventoryService.PartSource.IN_HOUSE,
                "C", BigDecimal.ONE, 1, 0, 2, "3").getId();
        assertTrue(nextId > id);
    }

    @Test void refusesUnversionedPreexistingDatabaseWithoutOverwrite() throws Exception {
        Path existing = temporaryDirectory.resolve("legacy.db");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + existing);
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE historical_inventory(id INTEGER)");
            st.execute("INSERT INTO historical_inventory(id) VALUES(123)");
        }
        assertThrows(InventoryPersistenceException.class, () -> new SqliteInventoryRepository(existing));
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + existing);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM historical_inventory")) {
            assertTrue(rs.next());
            assertEquals(123, rs.getInt(1));
        }
    }

    @Test void preservesHighIdsWhenRepositoryImportsExternallyCreatedRecord() {
        SqliteInventoryRepository repo = open();
        repo.savePart(new InHouse(400, "Imported", BigDecimal.ONE, 0, 0, 1, 2));
        assertEquals(401, new SqliteInventoryRepository(repo.getDatabasePath()).nextPartId());
    }
}
