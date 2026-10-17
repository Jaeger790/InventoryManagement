# Refactor notes

## Problems found in the original source

- Maven's JavaFX launcher referenced `wguc482.PA.HelloApplication`, a class that did not exist; project also mixed Java 19 README guidance with Java 17 Maven settings.
- Several controllers duplicated form validation and navigation; editing required separate almost-identical controllers.
- `Product.deleteAssociatedPart` iterated over a collection while removing items and ignored its `selectedAssociatedPart` argument.
- `Inventory.updatePart` / `updateProduct` appended a new object before removing the old one, changing ordering and breaking association references when a part was replaced.
- Part ID generation used counters with inconsistent calls; one path created temporary records with ID 0. ID reuse/collisions were possible.
- The old product price was parsed as an integer when creating a product, losing or rejecting decimal prices.
- Search sometimes appended a null part/product on failed numeric ID lookup and was implemented via key-typed callbacks.
- JavaFX collections were embedded in static global model state, making logic harder to isolate/test.
- `.idea/` and a bundled TTF font were committed. The new package omits editor metadata and the font; it uses standard fonts.

## Refactoring completed

- Package names and Maven coordinates now use `com.inventory`, with explicit `model`, `repository`, `service`, `controller` and `app` boundaries.
- One part form and one product form serve both add/edit. The original FXML visual layout is retained.
- Domain validation is centralized; updates preserve stable identifiers and fail without overwriting existing records.
- Products reference parts by ID; association lists are deduplicated and protected from external mutation.
- Deletion guards preserve referential integrity; user sees an informative error.
- BigDecimal replaces double for currency amounts.
- Continuous search supports name or numeric-ID substrings; zero matches simply yield an empty table.
- Added JUnit 5 domain/service tests and a GitHub Actions verify workflow.

## Compatibility limitations

- Not a binary- or API-compatible rewrite of course source packages; this is a cleaned-up 2.0 source project.
- Storage is still in memory, exactly as in the original design. Changes are not retained between launches.
- The original UI uses fixed-size AnchorPane layouts and is not yet responsive.
- JavaFX scene-level runtime testing still needs to be performed on a desktop machine with JavaFX dependencies installed.

## v2.1 SQLite persistence

- Added JDBC-backed `SqliteInventoryRepository` with versioned SQL schema and SQLite database location override.
- Preserved abstract `Part` subclasses with durable `source_type` metadata and stable many-to-many associations.
- Added atomic product/association writes, foreign-key enforcement, persistent ID allocation and precise decimal price storage.
- Removed default sample data from normal startup; `--demo-data` works on an empty database only.
- Added JUnit integration coverage and an offline SQL schema test.
- Added `docs/SQLITE_PERSISTENCE.md` covering paths, backup, restoration and MySQL migration planning.
