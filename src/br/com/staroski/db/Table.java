package br.com.staroski.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.com.staroski.UncheckedException;
import br.com.staroski.Utils;

public final class Table implements Comparable<Table> {

    private final Connection connection;
    private final Schema schema;
    private final String name;
    private final String type;

    private List<Column> columns;
    private Map<String, Column> columnMap;

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

    public TableDiff compareWith(Collection<Table> otherTables) {
        List<Table> tables = new LinkedList<Table>();
        tables.add(this);
        tables.addAll(otherTables);
        return new TableDiff(tables);
    }

    public TableDiff compareWith(Table other, Table... moreTables) {
        return compareWith(Utils.asList(other, moreTables));
    }

    public boolean contains(String columnName) {
        return getColumn(columnName) != null;
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
        if (getColumns().isEmpty()) {
            return null;
        }
        return columnMap.get(name);
    }

    public List<Column> getColumns() {
        if (columns != null) {
            return columns;
        }
        List<Column> list = new LinkedList<Column>();
        columnMap = new HashMap<String, Column>();
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
                if (!Utils.areEqualsIgnoreCase(thisCatalogName, catalogName)
                        || !Utils.areEqualsIgnoreCase(thisSchemaName, schemaName)
                        || !Utils.areEqualsIgnoreCase(thisTableName, tableName)) {
                    continue;
                }
                String columnName = result.getString("COLUMN_NAME");
                String columnType = result.getString("TYPE_NAME");
                int size = result.getInt("COLUMN_SIZE");
                int decimalDigits = result.getInt("DECIMAL_DIGITS");
                int javaSqlType = result.getInt("DATA_TYPE");
                Column column = new Column(columnName, columnType, size, decimalDigits, javaSqlType);
                list.add(column);
                columnMap.put(columnName, column);
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
