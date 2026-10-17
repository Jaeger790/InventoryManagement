-- SQLite schema v1. IDs are allocated through id_sequences and never reused.
-- Prices are stored as decimal text so Java BigDecimal retains its precision.
CREATE TABLE id_sequences (
    entity TEXT PRIMARY KEY CHECK (entity IN ('part', 'product')),
    last_id INTEGER NOT NULL DEFAULT 0 CHECK (last_id >= 0)
);
INSERT INTO id_sequences(entity, last_id) VALUES ('part', 0), ('product', 0);

CREATE TABLE parts (
    id INTEGER PRIMARY KEY CHECK (id > 0),
    name TEXT NOT NULL CHECK (length(trim(name)) BETWEEN 1 AND 120),
    price TEXT NOT NULL,
    stock INTEGER NOT NULL,
    min_stock INTEGER NOT NULL,
    max_stock INTEGER NOT NULL,
    source_type TEXT NOT NULL CHECK (source_type IN ('IN_HOUSE', 'OUTSOURCED')),
    machine_id INTEGER,
    company_name TEXT,
    CHECK (min_stock >= 0 AND min_stock <= stock AND stock <= max_stock),
    CHECK ((source_type = 'IN_HOUSE' AND machine_id IS NOT NULL AND machine_id > 0 AND company_name IS NULL)
        OR (source_type = 'OUTSOURCED' AND machine_id IS NULL
            AND company_name IS NOT NULL AND length(trim(company_name)) > 0))
);

CREATE TABLE products (
    id INTEGER PRIMARY KEY CHECK (id > 0),
    name TEXT NOT NULL CHECK (length(trim(name)) BETWEEN 1 AND 120),
    price TEXT NOT NULL,
    stock INTEGER NOT NULL,
    min_stock INTEGER NOT NULL,
    max_stock INTEGER NOT NULL,
    CHECK (min_stock >= 0 AND min_stock <= stock AND stock <= max_stock)
);

CREATE TABLE product_parts (
    product_id INTEGER NOT NULL,
    part_id INTEGER NOT NULL,
    position INTEGER NOT NULL CHECK (position >= 0),
    PRIMARY KEY (product_id, part_id),
    UNIQUE (product_id, position),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    FOREIGN KEY (part_id) REFERENCES parts(id) ON DELETE RESTRICT
);
CREATE INDEX idx_product_parts_part ON product_parts(part_id);
