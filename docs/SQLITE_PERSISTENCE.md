# SQLite persistence — design and operations

## Storage location

When launched normally, Inventory Management automatically creates a local SQLite file:

- **Linux/macOS:** `~/.inventory-management/inventory.db`
- **Windows:** `%USERPROFILE%\.inventory-management\inventory.db`

The directory is created on first launch. An existing valid database is reopened on subsequent launches. **No server, MySQL installation, username, or password is required.** The new database starts empty. Demo records are never inserted into a database containing inventory.

Override the path with `INVENTORY_DB_PATH` (environment variable) or `-Dinventory.db.path=/absolute/path/file.db` (JVM system property). If both are set, the JVM property wins. A relative override is resolved against the current working directory. Use a **local disk**, not a network share or synchronized cloud-drive directory, for active SQLite files.

## How it works

The `SqliteInventoryRepository` implements `InventoryRepository`. It uses JDBC and the Xerial SQLite driver. The `InventoryService` and all JavaFX controllers remain database-agnostic.

Schema migration `V1__create_inventory.sql` establishes:

- `parts`: both subclasses in one table, with `source_type`, `machine_id`, and `company_name`.
- `products`: product records.
- `product_parts`: many-to-many associations, including display order. `(product_id, part_id)` is unique.
- `id_sequences`: transactional monotonic part and product ID allocators, including across restarts and deletes.

Foreign keys are turned on for **every connection**. `saveProduct` updates the product and its ordered associations in **one transaction**. Part and product prices are stored as TEXT and restored to `BigDecimal` without converting through binary floating-point. Writes either commit or roll back. The app does **not** silently start with an empty in-memory repository if opening SQLite fails.

SQLite `PRAGMA user_version` tracks the schema at version 1. Startup rejects an unknown newer version, or an existing unversioned database with user tables, instead of overwriting its content. Any later schema upgrades should add a versioned, tested migration rather than editing an already-applied migration.

## Backup / restore

1. Close Inventory Management.
2. Copy `inventory.db` to a safe backup location. With the application closed, SQLite's WAL file is checkpointed by normal shutdown in typical operation, but for complete safety copy **the `.db` plus any `.db-wal` and `.db-shm` files that exist** as one set, or use SQLite's online backup API while the app is open.
3. Restore with the application closed and keep the database files together.
4. Do **not** check your database into a public Git repository. `.gitignore` excludes `.db`, `.db-wal`, and related SQLite files.

The schema does **not** currently perform automatic backups or encryption at rest. Use OS disk encryption and regular backups if your inventory data matters.

## Demo records

By default, first launch is empty. To populate example parts and products, launch the application with argument `--demo-data` **against an empty database only**. This option does not replace existing records. It is intended solely for screenshots and evaluation.

## Testing

- `./mvnw clean verify` compiles the application and runs **JUnit database integration tests** using temporary SQLite files, including reopen/restart, polymorphic round-trip, relational integrity, atomic rollback, subtype editing, monotonic IDs and legacy-file refusal.
- `bash tools/run_core_smoke.sh` runs the Java model/service smoke suite, FXML structural checks and Python SQLite schema constraints (no Maven required). The Python checks verify the DDL but do not substitute for the JDBC tests.
- `./mvnw javafx:run` runs the desktop UI (requires graphics).

## MySQL compatibility / migration roadmap

The **repository interface** is portable: a MySQL implementation can be added later without rewriting the business logic or GUI. SQLite SQL syntax is intentionally isolated to the SQLite repository and migration. Do not point the SQLite driver directly at an old MySQL database dump.

If the original MySQL database or a `.sql` export becomes available, a migration tool can map old `parts`, `products`, and association tables to these new tables, validating IDs, uniqueness, part subtype fields, stock rules, money precision, and preserving referential integrity. Import into a **backup/copy** of a SQLite database and compare row counts before switching. The original MySQL records are **not recoverable from source code alone**; the current release does not claim to import lost records automatically.
