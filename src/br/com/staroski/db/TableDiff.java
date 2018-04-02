package br.com.staroski.db;

import java.util.Collections;
import java.util.List;

public final class TableDiff {

    private final Table leftTable;
    private final Table rightTable;
    private final List<Column> allColumns;
    private final List<Column> leftMissingColumns;
    private final List<Column> rightMissingColumns;

    TableDiff(Table leftTable,
              Table rightTable,
              List<Column> allColumns,
              List<Column> leftMissingColumns,
              List<Column> rightMissingColumns) {
        this.leftTable = leftTable;
        this.rightTable = rightTable;
        Collections.sort(allColumns);
        Collections.sort(leftMissingColumns);
        Collections.sort(rightMissingColumns);
        this.allColumns = Collections.unmodifiableList(allColumns);
        this.leftMissingColumns = Collections.unmodifiableList(leftMissingColumns);
        this.rightMissingColumns = Collections.unmodifiableList(rightMissingColumns);
    }

    public List<Column> getAllColumns() {
        return allColumns;
    }

    public List<Column> getLeftMissingColumns() {
        return leftMissingColumns;
    }

    public Table getLeftTable() {
        return leftTable;
    }

    public List<Column> getRightMissingColumns() {
        return rightMissingColumns;
    }

    public Table getRightTable() {
        return rightTable;
    }

    public boolean hasDifferences() {
        return !(leftMissingColumns.isEmpty() && rightMissingColumns.isEmpty());
    }
}
