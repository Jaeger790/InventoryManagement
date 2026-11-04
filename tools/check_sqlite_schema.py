#!/usr/bin/env python3
"""Offline SQL schema smoke checks with Python's SQLite engine.

This verifies shipped DDL, constraints and relational behavior, but NOT the Java JDBC implementation.
The Java repository integration tests live in src/test/java/.../SqliteInventoryRepositoryTest.java.
"""
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCHEMA = (ROOT / 'src/main/resources/com/inventory/db/migrations/V1__create_inventory.sql').read_text()
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript(SCHEMA)
checks = 0

def expect(check, label):
    global checks
    assert check, label
    checks += 1

def rejects(query, params=()):
    global checks
    try:
        conn.execute(query, params)
    except sqlite3.IntegrityError:
        checks += 1
        return
    raise AssertionError('Should reject: ' + query)

expect({row[0] for row in conn.execute("SELECT name FROM sqlite_master WHERE type='table'")} >=
       {'parts', 'products', 'product_parts', 'id_sequences'}, 'Required tables')
expect(conn.execute('PRAGMA foreign_keys').fetchone()[0] == 1, 'Foreign keys enabled')
expect(conn.execute('SELECT COUNT(*) FROM id_sequences').fetchone()[0] == 2, 'ID sequences')
conn.execute('INSERT INTO parts VALUES(1,?,?,?,?,?,?,?,?)', ('Wheel', '12.50', 2, 0, 10, 'IN_HOUSE', 42, None))
conn.execute('INSERT INTO parts VALUES(2,?,?,?,?,?,?,?,?)', ('Bolt', '0.25', 9, 0, 15, 'OUTSOURCED', None, 'ACME'))
conn.execute('INSERT INTO products VALUES(1,?,?,?,?,?)', ('Bike', '100.00', 1, 0, 2))
conn.execute('INSERT INTO product_parts VALUES(1,2,0)')
conn.execute('INSERT INTO product_parts VALUES(1,1,1)')
expect([r[0] for r in conn.execute('SELECT part_id FROM product_parts WHERE product_id=1 ORDER BY position')] ==
       [2,1], 'Preserves associated part ordering')
rejects('INSERT INTO product_parts VALUES(1,999,2)')
rejects('DELETE FROM parts WHERE id=1')
rejects('DELETE FROM products WHERE id=1')
rejects('INSERT INTO product_parts VALUES(1,2,3)')
rejects('INSERT INTO product_parts VALUES(1,1,0)')
rejects('INSERT INTO parts VALUES(3,?,?,?,?,?,?,?,?)', ('Wrong', '1', 10, 0, 9, 'IN_HOUSE', 1, None))
rejects('INSERT INTO parts VALUES(3,?,?,?,?,?,?,?,?)', ('Wrong', '1', 1, 0, 9, 'OUTSOURCED', 1, None))
rejects('INSERT INTO parts VALUES(3,?,?,?,?,?,?,?,?)', ('Wrong', '1', 1, 0, 9, 'IN_HOUSE', None, None))
conn.execute('DELETE FROM product_parts WHERE product_id=1')
conn.execute('DELETE FROM products WHERE id=1')
conn.execute('DELETE FROM parts WHERE id=1')
expect(conn.execute('SELECT COUNT(*) FROM products').fetchone()[0] == 0, 'Deletion after unlink')
print(f'PASS: {checks} SQLite schema and constraint checks (Python SQLite, not Java JDBC)')
