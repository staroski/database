package br.com.staroski.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Table implements Comparable<Table> {

    static boolean areColumnsEquals(String columnName, Table leftTable, Table rightTable) {
        Column leftColumn = leftTable.getColumn(columnName);
        Column rightColumn = rightTable.getColumn(columnName);
        if (leftColumn == rightColumn) {
            return true;
        }
        if (leftColumn == null) {
            return false;
        }
        if (rightColumn == null) {
            return false;
        }
        if (!leftColumn.getType().equals(rightColumn.getType())) {
            return false;
        }
        if (leftColumn.getSize() != rightColumn.getSize()) {
            return false;
        }
        if (leftColumn.getScale() != rightColumn.getScale()) {
            return false;
        }
        return true;
    }
    private final Connection connection;
    private final Schema schema;
    private final String name;
    private final String type;

    private List<Column> columns;

    Table(Connection connection, Schema schema, String name, String type) {
        this.connection = connection;
        this.schema = schema;
        this.name = name;
        this.type = type;
    }

    @Override
    public int compareTo(Table other) {
        if (this == other) {
            return 0;
        }
        if (other == null) {
            return 1;
        }
        int diff = this.name.compareTo(other.name);
        if (diff == 0) {
            diff = this.type.compareTo(other.type);
        }
        return diff;
    }

    public TableDiff compareWith(Table other) {
        final Table leftTable = this;
        final Table rightTable = other;
        final List<String> columnNames = new LinkedList<String>();
        final List<Column> leftColumns = new LinkedList<Column>(leftTable.getColumns());
        final List<Column> rightColumns = new LinkedList<Column>(rightTable.getColumns());
        while (!leftColumns.isEmpty()) {
            Column leftColumn = leftColumns.remove(0);
            columnNames.add(leftColumn.getName());
            Column rightColumn = rightTable.getColumn(leftColumn.getName());
            if (rightColumn != null) {
                rightColumns.remove(rightColumn);
            }
        }
        while (!rightColumns.isEmpty()) {
            Column rightColumn = rightColumns.remove(0);
            columnNames.add(rightColumn.getName());
        }
        return new TableDiff(leftTable, rightTable, columnNames);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Table)) {
            return false;
        }
        Table other = (Table) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    public Column getColumn(String name) {
        for (Column column : getColumns()) {
            if (Strings.areEqualsIgnoreCase(name, column.getName())) {
                return column;
            }
        }
        return null;
    }
    
    public List<Column> getColumns() {
        if (columns != null) {
            return columns;
        }
        List<Column> list = new LinkedList<Column>();
        Schema thisSchema = getSchema();
        try {
            String thisCatalogName = thisSchema.getCatalog().getName();
            String thisSchemaName = thisSchema.getName();
            String thisTableName = getName();
            ResultSet result = connection.getMetaData().getColumns(thisCatalogName, thisSchemaName, thisTableName, null);
            while (result.next()) {
                String catalogName = result.getString("TABLE_CAT");
                String schemaName = result.getString("TABLE_SCHEM");
                String tableName = result.getString("TABLE_NAME");
                if (!Strings.areEqualsIgnoreCase(thisCatalogName, catalogName)
                        || !Strings.areEqualsIgnoreCase(thisSchemaName, schemaName)
                        || !Strings.areEqualsIgnoreCase(thisTableName, tableName)) {
                    continue;
                }
                String columnName = result.getString("COLUMN_NAME");
                String columnType = result.getString("TYPE_NAME");
                int size = result.getInt("COLUMN_SIZE");
                int decimalDigits = result.getInt("DECIMAL_DIGITS");
                int javaSqlType = result.getInt("DATA_TYPE");
                list.add(new Column(columnName, columnType, size, decimalDigits, javaSqlType));
            }
        } catch (SQLException e) {
            throw UncheckedException.wrap(e);
        }
        columns = Collections.unmodifiableList(list);
        return columns;
    }

    public String getName() {
        return name;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public String toString() {
        String tableName = getName();
        return String.format("%s[%s]", Table.class.getSimpleName(), tableName);
    }
}
