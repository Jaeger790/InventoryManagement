# Inventory Management

A JavaFX desktop demonstration of object-oriented inventory management. Create, search, edit, and delete **in-house** or **outsourced parts**, then associate them with **products**.

The original project was developed as a WGU software-development application. This refactor keeps the abstract `Part` hierarchy while separating business logic from presentation and adding meaningful automated tests.

## Features

- Add and modify parts with machine IDs or supplier company names.
- Create and update products with any number of associated parts.
- Search by case-insensitive name or part/product ID fragment.
- Validate prices and stock bounds (`0 <= min <= stock <= max`).
- Keep product-to-part associations valid when part details change.
- Prevent deleting a part that is used by a product; prevent deleting a product before removing its associations.
- **SQLite persistence**: saved parts, products and associations survive closing and reopening the app.
- Optional sample dataset (`--demo-data`) on an empty database only.

**Data storage:** SQLite on your local computer. The default database is `~/.inventory-management/inventory.db` (Windows: `%USERPROFILE%\.inventory-management\inventory.db`). Data saves automatically. See [SQLite storage, backup and migration guide](docs/SQLITE_PERSISTENCE.md).

## Requirements

- JDK 17 or newer (Java 17 is the project's baseline)
- Maven 3.9+ **or** bundled Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Desktop graphical environment for JavaFX
- Internet access for the initial Maven dependency download

## Build and run

On macOS / Linux:

```bash
./mvnw clean test
./mvnw javafx:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

If the Maven Wrapper is blocked, install Maven and replace `./mvnw` with `mvn`.

## Manjaro Linux (Maven)

Install Java and Maven, then from the extracted project directory (the directory containing `pom.xml`):

```bash
sudo pacman -Syu jdk21-openjdk maven
sudo archlinux-java set java-21-openjdk
java -version
mvn -version
mvn clean verify
mvn javafx:run
```

The project targets Java 17 bytecode, so JDK 21 is suitable. Maven downloads the JavaFX and SQLite JDBC dependencies on the first run. If Maven fails, copy its **first `[ERROR]` lines**, not only the final `BUILD FAILURE` summary. The included `./mvnw` wrapper may be used instead of `mvn`.

For the October 2026 source-compile fix and verification scope, see `docs/BUILD_FIXES.md`.

## Example workflow

1. Start the app. Your database starts empty on first launch; add a part or optionally use `--demo-data` for examples.
2. Add an **In House** part with a positive machine ID or an **Outsourced** part with a company name.
3. Add a product and select parts from the available-parts table.
4. Modify an associated part: the product still links to the part by its stable ID.
5. Attempt to delete an associated part; the app explains why it is blocked.
6. Remove the association from its product, then delete the part.

## Structure

```text
src/main/java/com/inventory/
  app/           # MainApp + scene navigation
  controller/    # JavaFX event handling and validation presentation
  model/         # Immutable Part, InHouse, Outsourced, Product, inventory rules
  repository/    # Storage interface, SQLite implementation + in-memory test implementation
  service/       # Business workflows and association integrity
src/main/resources/com/inventory/view/   # FXML and CSS
src/test/java/com/inventory/             # JUnit tests
docs/                                   # Architecture and change notes
.github/workflows/                      # CI test/build
```

See [architecture and upgrade suggestions](docs/ARCHITECTURE.md) and [refactor changelog](docs/REFACTOR_NOTES.md).

## Technical design

An abstract `Part` defines shared fields and validation. `InHouse` and `Outsourced` extend `Part` with their own data. Products track unique part IDs; the `InventoryService` resolves and validates relationships independently of the JavaFX user interface. `InventoryRepository` has SQLite storage for the app and an in-memory implementation for service tests.

## Testing

`./mvnw test` runs JUnit 5 model/service regression tests without launching a UI. GitHub Actions runs `./mvnw verify` on pushes and pull requests. Graphical smoke testing is also recommended: try add/edit/delete/search on each screen.

## Database location and backups

The database file is created automatically and changes are saved immediately. The location may be overridden using `INVENTORY_DB_PATH` or JVM property `inventory.db.path`. **Close the app before copying the database for backup**, and include any `.db-wal` or `.db-shm` files when present. Do not store your active database inside the repository. See [SQLite persistence guide](docs/SQLITE_PERSISTENCE.md).

## Planned enhancements

Bills of materials with part quantities, stock audit trails, CSV import/export, low-stock alerts, MySQL import and distributable desktop installers. Details in `docs/ARCHITECTURE.md`.

## Offline core verification

Without Maven or a GUI, run `bash tools/run_core_smoke.sh` on macOS/Linux with JDK 17+ and Python 3. It compiles the model/repository/service with `javac`, runs core smoke assertions, validates FXML structure, and checks the SQLite schema with Python. This **does not** run Java JDBC integration tests or launch the desktop UI.
