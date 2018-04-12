package br.com.staroski.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SchemaDiff {

    public final boolean hasDifferences;
    public final List<Schema> schemas;
    public final List<String> tableNames;

    private final Map<String, TableDiff> tableDiffMap;

    SchemaDiff(List<Schema> schemas) {
        this.schemas = schemas;
        this.tableNames = getTableNames(schemas);
        this.hasDifferences = checkDifferences(tableNames);
        this.tableDiffMap = new HashMap<String, TableDiff>();
    }

    public boolean allSchemasContains(String tableName) {
        for (Schema schema : schemas) {
            if (!schema.contains(tableName)) {
                return false;
            }
        }
        return true;
    }

    public TableDiff getTableDiffBetweenAllSchemas(String tableName) {
        if (tableDiffMap.containsKey(tableName)) {
            return tableDiffMap.get(tableName);
        }
        List<Schema> schemasWithTable = getSchemasWithTable(tableName);
        if (schemasWithTable.size() > 1) {
            Table table = schemasWithTable.get(0).getTable(tableName);
            List<Table> otherTables = new LinkedList<Table>();
            for (int i = 1; i < schemasWithTable.size(); i++) {
                otherTables.add(schemasWithTable.get(i).getTable(tableName));
            }
            TableDiff tableDiff = table.compareWith(otherTables);
            tableDiffMap.put(tableName, tableDiff);
            return tableDiff;
        }
        tableDiffMap.put(tableName, null);
        return null;
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

    private List<Schema> getSchemasWithTable(String tableName) {
        List<Schema> containing = new LinkedList<Schema>();
        for (Schema schema : schemas) {
            if (schema.contains(tableName)) {
                containing.add(schema);
            }
        }
        return containing;
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
