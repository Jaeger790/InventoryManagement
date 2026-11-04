# Maven build fix — October 2026

## Problem reproduced

`src/main/java/com/inventory/controller/ProductFormController.java` declares
`TableColumn<Part, BigDecimal>` but omitted `import java.math.BigDecimal;`.
A real Java compiler therefore raises `cannot find symbol: class BigDecimal`.

## Correction

Added the missing Java standard-library import without changing the UI,
application database, persistence format, Maven dependency versions, or
existing project functionality. Strengthened `tools/verify_structure.py` with
a regression check for the missing import in controllers.

## Tests executed in the provided sandbox

- Full **main-source** `javac --release 17` check, including all JavaFX
  controllers and application entry point, using temporary API-signature stubs
  because real JavaFX JARs were unavailable: PASS.
- JPMS named-module `javac --release 17` check (including `module-info.java`),
  against temporary named API stubs: PASS.
- `bash tools/run_core_smoke.sh`: 21 domain smoke checks, static FXML/controller
  validations, and 13 Python SQLite schema checks: PASS.
- Source ZIP sanity and archive integrity: checked at release packaging.

**Important limitation:** the stub compilation checks validate Java source
signatures and the named-module declaration but do **not** replace testing
against real Maven dependencies. There is no network access to Maven Central
or JavaFX libraries in this execution environment, so `mvn clean verify`,
JUnit SQLite JDBC integration tests, and the JavaFX GUI launch **could not**
be run here. This release is source-compilation-verified, not fully GUI-run-verified.

## Verify on Manjaro Linux

```bash
cd InventoryManagement-refactored
mvn clean verify
mvn javafx:run
```

Create a part, create a product, associate them, close/reopen the program,
and ensure records persist. The SQLite database is in
`~/.inventory-management/inventory.db` by default; the build does not remove
or overwrite existing inventory data.
