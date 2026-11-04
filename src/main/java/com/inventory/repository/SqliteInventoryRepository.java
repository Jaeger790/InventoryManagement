package com.inventory.repository;

import com.inventory.model.InHouse;
import com.inventory.model.Outsourced;
import com.inventory.model.Part;
import com.inventory.model.Product;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SqliteInventoryRepository implements InventoryRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final String MIGRATION = "/com/inventory/db/migrations/V1__create_inventory.sql";
    private final Path path;

        public static Path defaultDatabasePath() {
        String configured = System.getProperty("inventory.db.path");
        if (configured == null || configured.isBlank()) configured = System.getenv("INVENTORY_DB_PATH");
        if (configured == null || configured.isBlank()) {
            return Path.of(System.getProperty("user.home"), ".inventory-management", "inventory.db")
                    .toAbsolutePath().normalize();
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    public SqliteInventoryRepository(Path databasePath) {
        path = Objects.requireNonNull(databasePath, "Database path is required").toAbsolutePath().normalize();
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            initialize();
        } catch (IOException | SQLException e) {
            throw new InventoryPersistenceException("Unable to initialize SQLite database at " + path, e);
        }
    }

    public Path getDatabasePath() { return path; }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        } catch (SQLException e) {
            try { connection.close(); } catch (SQLException ignored) { /* preserve original error */ }
            throw e;
        }
        return connection;
    }

    private void initialize() throws SQLException, IOException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            // Allow readers while a write is in progress.
            statement.execute("PRAGMA journal_mode = WAL");
            int version;
            try (ResultSet result = statement.executeQuery("PRAGMA user_version")) {
                version = result.getInt(1);
            }
            if (version > SCHEMA_VERSION) {
                throw new InventoryPersistenceException("Database schema " + version
                        + " is newer than this application supports (" + SCHEMA_VERSION + ").");
            }
            if (version == 0) {
                // Reject an unversioned existing database rather than overwriting a user's old data.
                try (ResultSet tables = statement.executeQuery("SELECT name FROM sqlite_master "
                        + "WHERE type='table' AND name NOT LIKE 'sqlite_%' LIMIT 1")) {
                    if (tables.next()) {
                        throw new InventoryPersistenceException("An existing unversioned database was found at "
                                + path + ". Back it up and migrate it explicitly; no data has been changed.");
                    }
                }
                String schema;
                try (InputStream input = SqliteInventoryRepository.class.getResourceAsStream(MIGRATION)) {
                    if (input == null) throw new IOException("Missing bundled database migration: " + MIGRATION);
                    schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                }
                connection.setAutoCommit(false);
                try {
                    for (String command : schema.split(";")) {
                        if (!command.isBlank()) statement.execute(command);
                    }
                    statement.execute("PRAGMA user_version = " + SCHEMA_VERSION);
                    connection.commit();
                } catch (SQLException | RuntimeException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        }
    }

    @FunctionalInterface private interface SqlOperation<T> { T apply(Connection connection) throws SQLException; }

    private <T> T read(String operation, SqlOperation<T> action) {
        try (Connection connection = connect()) {
            return action.apply(connection);
        } catch (SQLException e) {
            throw new InventoryPersistenceException("SQLite " + operation + " failed at " + path, e);
        }
    }

    private <T> T write(String operation, SqlOperation<T> action) {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                T value = action.apply(connection);
                connection.commit();
                return value;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new InventoryPersistenceException("SQLite " + operation + " failed at " + path, e);
        }
    }

    private int nextId(String entity) {
        return write("allocate " + entity + " ID", connection -> {
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE id_sequences SET last_id = last_id + 1 WHERE entity = ? AND last_id < 2147483647")) {
                update.setString(1, entity);
                if (update.executeUpdate() != 1) throw new SQLException("ID sequence missing or exhausted: " + entity);
            }
            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT last_id FROM id_sequences WHERE entity = ?")) {
                select.setString(1, entity);
                try (ResultSet result = select.executeQuery()) {
                    if (!result.next()) throw new SQLException("ID sequence missing: " + entity);
                    return result.getInt(1);
                }
            }
        });
    }

    @Override public int nextPartId() { return nextId("part"); }
    @Override public int nextProductId() { return nextId("product"); }

    @Override public List<Part> findAllParts() {
        return read("read parts", connection -> {
            List<Part> results = new ArrayList<>();
            try (PreparedStatement sql = connection.prepareStatement("SELECT * FROM parts ORDER BY id");
                 ResultSet rs = sql.executeQuery()) {
                while (rs.next()) results.add(mapPart(rs));
            }
            return List.copyOf(results);
        });
    }

    @Override public Optional<Part> findPart(int id) {
        return read("read part", connection -> {
            try (PreparedStatement sql = connection.prepareStatement("SELECT * FROM parts WHERE id = ?")) {
                sql.setInt(1, id);
                try (ResultSet rs = sql.executeQuery()) {
                    return rs.next() ? Optional.of(mapPart(rs)) : Optional.empty();
                }
            }
        });
    }

    private Part mapPart(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        BigDecimal price = new BigDecimal(rs.getString("price"));
        int stock = rs.getInt("stock"), min = rs.getInt("min_stock"), max = rs.getInt("max_stock");
        return switch (rs.getString("source_type")) {
            case "IN_HOUSE" -> new InHouse(id, name, price, stock, min, max, rs.getInt("machine_id"));
            case "OUTSOURCED" -> new Outsourced(id, name, price, stock, min, max, rs.getString("company_name"));
            default -> throw new SQLException("Unknown part source for ID " + id);
        };
    }

    @Override public void savePart(Part part) {
        Objects.requireNonNull(part, "Part is required");
        write("save part", connection -> {
            String sql = "INSERT INTO parts(id, name, price, stock, min_stock, max_stock, source_type, machine_id, company_name) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET "
                    + "name=excluded.name, price=excluded.price, stock=excluded.stock, "
                    + "min_stock=excluded.min_stock, max_stock=excluded.max_stock, "
                    + "source_type=excluded.source_type, machine_id=excluded.machine_id, "
                    + "company_name=excluded.company_name";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, part.getId());
                stmt.setString(2, part.getName());
                stmt.setString(3, part.getPrice().toPlainString());
                stmt.setInt(4, part.getStock());
                stmt.setInt(5, part.getMin());
                stmt.setInt(6, part.getMax());
                if (part instanceof InHouse inHouse) {
                    stmt.setString(7, "IN_HOUSE");
                    stmt.setInt(8, inHouse.getMachineId());
                    stmt.setNull(9, java.sql.Types.VARCHAR);
                } else if (part instanceof Outsourced outsourced) {
                    stmt.setString(7, "OUTSOURCED");
                    stmt.setNull(8, java.sql.Types.INTEGER);
                    stmt.setString(9, outsourced.getCompanyName());
                } else throw new IllegalArgumentException("Unsupported part type: " + part.getClass());
                stmt.executeUpdate();
            }
            bumpSequence(connection, "part", part.getId());
            return null;
        });
    }

    @Override public boolean removePart(int id) {
        return write("delete part", connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM parts WHERE id = ?")) {
                stmt.setInt(1, id);
                try { return stmt.executeUpdate() == 1; }
                catch (SQLException e) {
                    if (e.getErrorCode() == 19) throw new IllegalStateException(
                            "Remove this part from its products before deleting it.", e);
                    throw e;
                }
            }
        });
    }

    @Override public List<Product> findAllProducts() {
        return read("read products", connection -> {
            List<Product> products = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM products ORDER BY id");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) products.add(mapProduct(connection, rs));
            }
            return List.copyOf(products);
        });
    }

    @Override public Optional<Product> findProduct(int id) {
        return read("read product", connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM products WHERE id = ?")) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(mapProduct(connection, rs)) : Optional.empty();
                }
            }
        });
    }

    private Product mapProduct(Connection connection, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        List<Integer> associations = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT part_id FROM product_parts WHERE product_id = ? ORDER BY position")) {
            stmt.setInt(1, id);
            try (ResultSet ids = stmt.executeQuery()) {
                while (ids.next()) associations.add(ids.getInt(1));
            }
        }
        return new Product(id, rs.getString("name"), new BigDecimal(rs.getString("price")),
                rs.getInt("stock"), rs.getInt("min_stock"), rs.getInt("max_stock"), associations);
    }

    @Override public void saveProduct(Product product) {
        Objects.requireNonNull(product, "Product is required");
        write("save product", connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO products(id, name, price, stock, min_stock, max_stock) VALUES(?, ?, ?, ?, ?, ?) "
                            + "ON CONFLICT(id) DO UPDATE SET name=excluded.name, price=excluded.price, "
                            + "stock=excluded.stock, min_stock=excluded.min_stock, max_stock=excluded.max_stock")) {
                stmt.setInt(1, product.getId());
                stmt.setString(2, product.getName());
                stmt.setString(3, product.getPrice().toPlainString());
                stmt.setInt(4, product.getStock());
                stmt.setInt(5, product.getMin());
                stmt.setInt(6, product.getMax());
                stmt.executeUpdate();
            }
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM product_parts WHERE product_id = ?")) {
                delete.setInt(1, product.getId());
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO product_parts(product_id, part_id, position) VALUES(?, ?, ?)")) {
                int position = 0;
                for (int partId : product.getAssociatedPartIds()) {
                    insert.setInt(1, product.getId());
                    insert.setInt(2, partId);
                    insert.setInt(3, position++);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            bumpSequence(connection, "product", product.getId());
            return null;
        });
    }

    private void bumpSequence(Connection connection, String entity, int candidate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE id_sequences SET last_id = MAX(last_id, ?) WHERE entity = ?")) {
            statement.setInt(1, candidate);
            statement.setString(2, entity);
            if (statement.executeUpdate() != 1) throw new SQLException("Missing sequence: " + entity);
        }
    }

    @Override public boolean removeProduct(int id) {
        return write("delete product", connection -> {
            // Prevent deletion while associated, even if a caller bypasses InventoryService.
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM products WHERE id = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM product_parts WHERE product_id = ?)")) {
                stmt.setInt(1, id);
                stmt.setInt(2, id);
                return stmt.executeUpdate() == 1;
            }
        });
    }
}
