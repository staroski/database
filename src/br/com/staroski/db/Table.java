package br.com.staroski.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Table {

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

    public TableDiff compareWith(Table other) {
        final Table leftTable = this;
        final Table rightTable = other;
        final List<Column> allColumns = new LinkedList<Column>();
        final List<Column> leftColumns = new LinkedList<Column>(leftTable.getColumns());
        final List<Column> rightColumns = new LinkedList<Column>(rightTable.getColumns());
        final List<Column> rightMissingColumns = new LinkedList<Column>();
        final List<Column> leftMissingColumns = new LinkedList<Column>();
        while (!leftColumns.isEmpty()) {
            Column leftColumn = leftColumns.remove(0);
            allColumns.add(leftColumn);
            Column rightColumn = rightTable.getColumn(leftColumn.getName());
            if (rightColumn == null) {
                rightMissingColumns.add(leftColumn);
            } else {
                rightColumns.remove(rightColumn);
            }
        }
        while (!rightColumns.isEmpty()) {
            Column rightColumn = rightColumns.remove(0);
            allColumns.add(rightColumn);
            leftMissingColumns.add(rightColumn);
        }
        return new TableDiff(leftTable, rightTable, allColumns, leftMissingColumns, rightMissingColumns);
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
                int javaSqlType = result.getInt("DATA_TYPE");
                list.add(new Column(columnName, columnType, javaSqlType));
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
    public String toString() {
        String tableName = getName();
        return String.format("%s[%s]", Table.class.getSimpleName(), tableName);
    }
}
