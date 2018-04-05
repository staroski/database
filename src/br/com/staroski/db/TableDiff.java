package br.com.staroski.db;

import java.util.Collections;
import java.util.List;

public final class TableDiff {

    private final boolean hasDifferences;
    private final Table leftTable;
    private final Table rightTable;
    private final List<String> columnNames;

    TableDiff(Table leftTable, Table rightTable, List<String> columnNames) {
        Collections.sort(columnNames);
        boolean hasDifferences = false;
        for (String name : columnNames) {
            boolean hasLeft = leftTable.getColumn(name) != null;
            boolean hasRight = rightTable.getColumn(name) != null;
            if (hasLeft != hasRight) {
                hasDifferences = true;
                break;
            }
            if (hasLeft && hasRight && !Table.areColumnsEquals(name, leftTable, rightTable)) {
                hasDifferences = true;
                break;
            }
        }
        this.hasDifferences = hasDifferences;
        this.leftTable = leftTable;
        this.rightTable = rightTable;
        this.columnNames = Collections.unmodifiableList(columnNames);
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public Table getLeftTable() {
        return leftTable;
    }

    public Table getRightTable() {
        return rightTable;
    }

    public boolean hasDifferences() {
        return hasDifferences;
    }

    public boolean isMissingOnLeft(String columnName) {
        return leftTable.getColumn(columnName) == null;
    }

    public boolean isMissingOnRight(String columnName) {
        return rightTable.getColumn(columnName) == null;
    }
}
