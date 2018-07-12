package br.com.staroski.db;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class keeps the differences between some tables
 * 
 * @author Ricardo Artur Staroski
 */
public final class TableDiff {

    public final boolean hasDifferences;
    public final List<String> columnNames;
    public final List<Table> tables;

    private final DiffFilter filter;

    protected TableDiff(DiffFilter filter, List<Table> tables) {
        this.filter = filter != null ? filter : new DiffFilter();
        this.tables = tables;
        this.columnNames = getColumnNames(tables, this.filter);
        this.hasDifferences = checkDifferences(columnNames);
    }

    public boolean allTablesContains(String columnName) {
        if (!filter.acceptColumn(columnName)) {
            return false;
        }
        for (Table table : tables) {
            if (!table.contains(columnName)) {
                return false;
            }
        }
        return true;
    }

    public List<Table> getTablesWithColumn(String columnName) {
        List<Table> containing = new LinkedList<Table>();
        if (!filter.acceptColumn(columnName)) {
            for (Table table : tables) {
                if (table.contains(columnName)) {
                    containing.add(table);
                }
            }
        }
        return containing;
    }

    private boolean checkDifferences(List<String> columnNames) {
        for (String columnName : columnNames) {
            if (!allTablesContains(columnName)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getColumnNames(List<Table> tables, DiffFilter filter) {
        final List<String> names = new LinkedList<String>();
        for (Table table : tables) {
            for (Column column : table.getColumns()) {
                String name = column.getName();
                if (filter.acceptColumn(name) && !names.contains(name)) {
                    names.add(name);
                }
            }
        }
        Collections.sort(names);
        return Collections.unmodifiableList(names);
    }
}
