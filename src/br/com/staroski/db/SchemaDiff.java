package br.com.staroski.db;

import java.util.Collections;
import java.util.List;

public final class SchemaDiff {

    private final boolean hasDifferences;
    private final Schema leftSchema;
    private final Schema rightSchema;
    private final List<String> tablesNames;

    SchemaDiff(Schema leftSchema, Schema rightSchema, List<String> tableNames) {
        Collections.sort(tableNames);
        boolean hasDifferences = false;
        for (String name : tableNames) {
            boolean hasLeft = leftSchema.getTable(name) != null;
            boolean hasRight = rightSchema.getTable(name) != null;
            if (hasLeft != hasRight) {
                hasDifferences = true;
                break;
            }
        }
        this.hasDifferences = hasDifferences;
        this.leftSchema = leftSchema;
        this.rightSchema = rightSchema;
        this.tablesNames = Collections.unmodifiableList(tableNames);
    }

    public Schema getLeftSchema() {
        return leftSchema;
    }

    public Schema getRightSchema() {
        return rightSchema;
    }

    public List<String> getTableNames() {
        return tablesNames;
    }

    public boolean hasDifferences() {
        return hasDifferences;
    }

    public boolean isMissingOnLeft(String tableName) {
        return leftSchema.getTable(tableName) == null;
    }

    public boolean isMissingOnRight(String tableName) {
        return rightSchema.getTable(tableName) == null;
    }
}
