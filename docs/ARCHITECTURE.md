# Architecture

```text
JavaFX FXML screens <-> controllers -> InventoryService -> InventoryRepository
                                 |                 |
                                 |                 +--> SqliteInventoryRepository (default); InMemoryInventoryRepository (tests)
                                 |
                                 +--> domain models: Part (abstract), InHouse, Outsourced, Product
```

- **Domain**: immutable `Part` subclasses and `Product`. A product stores **part IDs** rather than object references. All stock/price invariants are validated in constructors.
- **Service**: the only API used by controllers for mutations. Validates associations and prevents removing referenced parts and products with associations.
- **Repository**: a small interface with a SQLite JDBC implementation using transactions, foreign keys, and persistent monotonic IDs. The in-memory implementation remains useful for unit tests.
- **UI**: `SceneNavigator` owns the shared service and loads three FXML screens: home, part form, product form. Both forms support add and edit.

## Decisions and tradeoffs

- **SQLite is the default**: the local database is automatically initialized, reopened across launches, and never silently replaced with in-memory storage. Demo content is opt-in.
- **BigDecimal for money**: avoids floating-point issues in price validation and representation, though there is no tax/currency management.
- **Immutable domain objects**: editing a part replaces it by ID; references remain valid. Controllers stage product associations until save.
- **Deleting records**: disallowed while referenced. First unlink parts from products to delete either safely.
- **Single-user**: repository is not designed for concurrent access or network clients.
- **Schema migration**: a version-1 SQL migration is applied to a new database and tracked with `PRAGMA user_version`. Unknown/unversioned existing schemas are refused to avoid data loss. The original MySQL dataset is not available for automatic import.

## Suggested upgrades
1. **Database migration tooling** to import an old MySQL dump and support future schema versions.
2. **Stock movement events** (receipts, builds, adjustments) with audit records rather than directly editing stock.
3. **Unit price vs. cost** and bills of materials with **quantities per part** (current associations mean membership only).
4. **Availability / assembly planning** calculating buildable units from part quantities and stock.
5. **CSV import/export** with a preview, validation, and duplicate handling.
6. **Inventory alerts**, reorder points, and low-stock highlighting.
7. **Dark mode / responsive layout**, accessible keyboard navigation, validation feedback per field.
8. **Packaging** via `jpackage`, platform-specific installers, and release artifacts.
