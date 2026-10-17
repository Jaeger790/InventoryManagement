# Validation report — SQLite persistence upgrade (v2.1)

## Executed in this environment

- **PASS:** Java core source compilation with `javac --release 17` (models, service, in-memory and SQLite repositories). No external JAR is required for compiling the JDBC repository against `java.sql`.
- **PASS:** Existing **21** model/service smoke checks (without JDBC runtime).
- **PASS:** **75** static XML/FXML and controller wiring checks, including `pom.xml` parsing.
- **PASS:** **13** SQLite DDL/foreign-key checks executed against Python's `sqlite3` engine. Includes subtype constraints, association order/uniqueness, rejected nonexistent parts, delete restrictions, and proper deletion after unlinking.

The offline schema checks found and fixed a genuine SQLite issue: a `CHECK` expression can evaluate to NULL and not fail. The `IN_HOUSE` source constraint now explicitly requires `machine_id IS NOT NULL`.

## Included, but could not execute here

- JUnit SQLite JDBC integration tests (`SqliteInventoryRepositoryTest`): verify persisted reopen, subtype fidelity, decimal scale, ordered links, updates, transactional rollback on bad foreign keys, safe deletion, non-reused IDs and refusal to overwrite unknown existing databases.
- Full Maven build and JavaFX GUI launch: Maven/JavaFX artifacts and the native SQLite JDBC JAR are not cached in the environment; external Maven Central access is unavailable. **The included JUnit and application module path have not been independently runtime-verified**.
- GitHub Actions: will execute `./mvnw verify` when you commit/push.

## Verify on a network-connected desktop

```bash
./mvnw clean verify
./mvnw javafx:run
```

Then create two types of parts and a product associating both parts. Close the app completely, relaunch and verify all records and relationships remain. Edit one associated part, relaunch and verify the relationship remains. Remove the associations before attempting deletion. The default database is in your user profile, outside the source directory.

If Maven reports a module name or SQLite native-loading error, capture the full error for review before using the database with real inventory data. Back up `inventory.db` first if you have added records.

## October 2026 Maven source-compile correction

Corrected missing `java.math.BigDecimal` import in `ProductFormController` and
verified all main sources, including the `module-info.java` declaration, with
JDK 21 `javac --release 17` against temporary JavaFX API stub modules.
The stubbed compilation passed. Added a static import regression check.
Real Maven dependency resolution, JUnit execution with SQLite JDBC, and
JavaFX GUI launch still require a network-enabled development machine.
See `docs/BUILD_FIXES.md`.
