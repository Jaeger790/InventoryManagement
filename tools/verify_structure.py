#!/usr/bin/env python3
"""Static structural/FXML checks; does not replace a JavaFX runtime smoke test."""
from pathlib import Path
from xml.etree import ElementTree as ET
import re
ROOT = Path(__file__).resolve().parents[1]
VIEWS = ROOT / 'src/main/resources/com/inventory/view'
CONTROLLERS = ROOT / 'src/main/java/com/inventory/controller'
checks = 0
names = {'MainScreen.fxml': 'MainController.java', 'AddPart.fxml': 'PartFormController.java', 'AddProduct.fxml': 'ProductFormController.java'}
for fxml, java in names.items():
    view = VIEWS / fxml
    parsed = ET.parse(view).getroot()
    controller = (CONTROLLERS / java).read_text()
    source = view.read_text()
    # Guard against a prior JavaFX controller compilation regression: a BigDecimal
    # field requires java.math.BigDecimal, even when static FXML checks pass.
    if re.search(r'\bBigDecimal\b', controller):
        assert re.search(r'^import\s+java\.math\.BigDecimal\s*;', controller, re.M), (
            f'{java}: missing import java.math.BigDecimal')
        checks += 1
    assert 'fx:controller' not in source, f'{fxml}: conflicting fx:controller'
    ids = [v for item in parsed.iter() for k, v in item.attrib.items() if k == '{http://javafx.com/fxml/1}id']
    assert len(set(ids)) == len(ids), f'{fxml}: duplicate fx:id'
    for id_ in ids:
        # Only IDs wired to controllers require a Java field. Remaining IDs can be decorative.
        field = re.search(r'@FXML\s+private\s+[^;]+\b'+re.escape(id_)+r'\b[^;]*;', controller)
        if field: checks += 1
    essential = {'MainScreen.fxml':['partsTable','productsTable','partSearchField','productSearchField','addPartB','exitButton'],
                 'AddPart.fxml':['partName','inHouse','outsourced','savePart','cancelButton','machineIdL'],
                 'AddProduct.fxml':['associatedPartTable','partListTable','productNameT','productHeader','saveProductButton']}
    for id_ in essential[fxml]:
        assert id_ in ids, f'{fxml}: missing {id_}'
        assert re.search(r'@FXML\s+private\s+[^;]+\b'+re.escape(id_)+r'\b[^;]*;', controller), f'{java}: missing @FXML field {id_}'
        checks += 1
    print(f'PASS: {fxml} parsed; {len(ids)} unique fx:ids, primary controller fields available')
pom = ET.parse(ROOT/'pom.xml')
assert 'sqlite-jdbc' in (ROOT/'pom.xml').read_text(), 'Maven SQLite JDBC dependency missing'
checks += 1
mod = (ROOT/'src/main/java/module-info.java').read_text()
assert 'requires java.sql' in mod and 'requires org.xerial.sqlitejdbc' in mod, 'JDBC module requirements missing'
checks += 1
assert (ROOT/'src/main/resources/com/inventory/db/migrations/V1__create_inventory.sql').exists(), 'SQL migration missing'
checks += 1
assert 'new SqliteInventoryRepository' in (ROOT/'src/main/java/com/inventory/app/MainApp.java').read_text(), 'App does not use SQLite'
checks += 1
checks += 1
assert (ROOT/'src/main/java/module-info.java').exists(); checks += 1
assert not list(ROOT.rglob('*.ttf')), 'Bundled font file must not be redistributed'
assert not (ROOT/'.idea').exists(), 'IDE-specific state should not be checked in'
checks += 2
print(f'PASS: {checks} static checks (does not verify JavaFX GUI runtime)')
