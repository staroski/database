package br.com.staroski.db;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class SchemaDiff {

    public final boolean hasDifferences;
    public final List<String> tableNames;
    public final List<Schema> schemas;

    SchemaDiff(List<Schema> schemas) {
        this.schemas = schemas;
        this.tableNames = getTableNames(schemas);
        this.hasDifferences = checkDifferences(tableNames);
    }

    public boolean allSchemasContains(String tableName) {
        for (Schema schema : schemas) {
            if (!schema.contains(tableName)) {
                return false;
            }
        }
        return true;
    }

    public List<Schema> getSchemasWithTable(String tableName) {
        List<Schema> containing = new LinkedList<Schema>();
        for (Schema schema : schemas) {
            if (schema.contains(tableName)) {
                containing.add(schema);
            }
        }
        return containing;
    }

    private boolean checkDifferences(List<String> tableNames) {
        for (String tableName : tableNames) {
            boolean firstContains = schemas.get(0).contains(tableName);
            for (int i = 1; i < schemas.size(); i++) {
                boolean otherContains = schemas.get(i).contains(tableName);
                if (firstContains != otherContains) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getTableNames(List<Schema> schemas) {
        final List<String> names = new LinkedList<String>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                String name = table.getName();
                if (!names.contains(name)) {
                    names.add(name);
                }
            }
        }
        Collections.sort(names);
        return Collections.unmodifiableList(names);
    }
}
