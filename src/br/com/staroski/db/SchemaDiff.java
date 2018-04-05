package br.com.staroski.db;

import java.util.Collections;
import java.util.List;

public final class SchemaDiff {

    private final Schema left;
    private final Schema right;
    private final List<String> tablesNames;
    private final boolean hasDifferences;

    SchemaDiff(Schema leftSchema, Schema rightSchema, List<String> tableNames) {
        Collections.sort(tableNames);
        boolean hasDifferences = false;
        for (String table : tableNames) {
            boolean hasLeft = leftSchema.getTable(table) != null;
            boolean hasRight = leftSchema.getTable(table) != null;
            if (hasLeft != hasRight) {
                hasDifferences = true;
                break;
            }
        }
        this.left = leftSchema;
        this.right = rightSchema;
        this.tablesNames = Collections.unmodifiableList(tableNames);
        this.hasDifferences = hasDifferences;
    }

    public Schema getLeftSchema() {
        return left;
    }

    public Schema getRightSchema() {
        return right;
    }

    public List<String> getTableNames() {
        return tablesNames;
    }

    public boolean hasDifferences() {
        return hasDifferences;
    }

    public boolean isMissingOnLeft(String table) {
        return left.getTable(table) == null;
    }

    public boolean isMissingOnRight(String table) {
        return right.getTable(table) == null;
    }
}
