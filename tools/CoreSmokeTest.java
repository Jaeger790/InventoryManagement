import com.inventory.model.*;
import com.inventory.repository.*;
import com.inventory.service.*;
import java.math.BigDecimal;
import java.util.List;

/** Zero-dependency smoke test when Maven / JavaFX cannot be downloaded. */
public final class CoreSmokeTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void fails(Class<? extends Throwable> type, Runnable action) {
        checks++;
        try { action.run(); }
        catch (Throwable actual) { if (type.isInstance(actual)) return; throw new AssertionError("Wrong exception", actual); }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    public static void main(String[] args) {
        var service = new InventoryService(new InMemoryInventoryRepository());
        var p1 = service.createPart(InventoryService.PartSource.IN_HOUSE, "Brake Lever", new BigDecimal("43.25"), 4, 2, 8, "300");
        var p2 = service.createPart(InventoryService.PartSource.OUTSOURCED, "Piston", new BigDecimal("850.99"), 1, 1, 2, "Wiseco");
        check(p1 instanceof InHouse && ((InHouse) p1).getMachineId() == 300, "InHouse subtype");
        check(p2 instanceof Outsourced && ((Outsourced) p2).getCompanyName().equals("Wiseco"), "Outsourced subtype");
        check(p1.getId() != p2.getId(), "Unique IDs");
        check(service.searchParts("brake").size() == 1, "Name search");
        check(service.searchParts(Integer.toString(p2.getId())).stream().anyMatch(x -> x.getId() == p2.getId()), "ID search");
        var product = service.createProduct("Motorcycle", new BigDecimal("2500.00"), 1, 0, 4, List.of(p1.getId(), p2.getId(), p1.getId()));
        check(product.getAssociatedPartIds().size() == 2, "Dedupe IDs");
        check(service.associatedParts(product).size() == 2, "Associations resolve");
        service.updatePart(p1.getId(), InventoryService.PartSource.OUTSOURCED, "Brake v2", new BigDecimal("44"), 4, 1, 8, "Vendor");
        check(service.associatedParts(service.getProduct(product.getId())).get(0).getName().equals("Brake v2"), "Link preserved on edit");
        fails(IllegalStateException.class, () -> service.deletePart(p1.getId()));
        fails(IllegalStateException.class, () -> service.deleteProduct(product.getId()));
        fails(IllegalArgumentException.class, () -> service.createProduct("Bad", BigDecimal.ZERO, 0, 0, 1, List.of(999)));
        fails(IllegalArgumentException.class, () -> service.createPart(InventoryService.PartSource.IN_HOUSE, "Fail", new BigDecimal("-1"), 1, 0, 3, "1"));
        fails(IllegalArgumentException.class, () -> service.updatePart(p1.getId(), InventoryService.PartSource.IN_HOUSE, "", BigDecimal.ZERO, 0, 0, 1, "1"));
        check(service.getPart(p1.getId()).getName().equals("Brake v2"), "Invalid edit atomicity");
        fails(UnsupportedOperationException.class, () -> product.getAssociatedPartIds().clear());
        var updated = service.updateProduct(product.getId(), "Motorcycle 2", new BigDecimal("2200.00"), 1, 0, 4, List.of());
        check(updated.getId() == product.getId(), "Product ID preserved");
        check(service.deleteProduct(product.getId()), "Product removal");
        check(service.deletePart(p1.getId()), "Part removal after unlink");
        check(service.deletePart(p2.getId()), "Second part removal");
        var next = service.createPart(InventoryService.PartSource.IN_HOUSE, "New", BigDecimal.ZERO, 0, 0, 0, "2");
        check(next.getId() > p2.getId(), "IDs not reused");
        check(service.allProducts().isEmpty(), "Empty catalog after delete");
        System.out.println("PASS: " + checks + " core smoke checks");
    }
}
