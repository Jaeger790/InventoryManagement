#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
OUTPUT="$(mktemp -d)"
trap 'rm -rf "$OUTPUT"' EXIT
javac --release 17 -d "$OUTPUT" $(find src/main/java/com/inventory/model src/main/java/com/inventory/repository src/main/java/com/inventory/service -name '*.java') tools/CoreSmokeTest.java
java -ea -cp "$OUTPUT" CoreSmokeTest
python3 tools/verify_structure.py
python3 tools/check_sqlite_schema.py
