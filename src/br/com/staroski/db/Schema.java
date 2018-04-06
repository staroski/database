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

public final class Schema {

    private final Connection connection;
    private final Catalog catalog;
    private final String name;

    private List<Table> tables;
    private Map<String, Table> tableMap;

    Schema(Connection connection, Catalog catalog, String name) {
        this.connection = connection;
        this.catalog = catalog;
        this.name = name;
    }

    public SchemaDiff compareWith(Collection<Schema> otherSchemas) {
        List<Schema> schemas = new LinkedList<Schema>();
        schemas.add(this);
        schemas.addAll(otherSchemas);
        return new SchemaDiff(schemas);
    }

    public SchemaDiff compareWith(Schema other, Schema... moreSchemas) {
        return compareWith(Utils.asList(other, moreSchemas));
    }

    public boolean contains(String tableName) {
        return getTable(tableName) != null;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public String getName() {
        return name;
    }

    public Table getTable(String name) {
        if (getTables().isEmpty()) {
            return null;
        }
        return tableMap.get(name);
    }

    public List<Table> getTables() {
        if (tables != null) {
            return tables;
        }
        List<Table> list = new LinkedList<Table>();
        tableMap = new HashMap<String, Table>();
        try {
            String thisCatalogName = getCatalog().getName();
            String thisSchemaName = getName();
            ResultSet result = connection.getMetaData().getTables(thisCatalogName, thisSchemaName, null, null);
            while (result.next()) {
                String catalogName = result.getString("TABLE_CAT");
                String schemaName = result.getString("TABLE_SCHEM");
                if (!Utils.areEqualsIgnoreCase(thisCatalogName, catalogName)
                        || !Utils.areEqualsIgnoreCase(thisSchemaName, schemaName)) {
                    continue;
                }
                String tableName = result.getString("TABLE_NAME");
                String tableType = result.getString("TABLE_TYPE");
                Table table = new Table(connection, this, tableName, tableType);
                list.add(table);
                tableMap.put(tableName, table);
            }
        } catch (SQLException e) {
            throw UncheckedException.wrap(e);
        }
        tables = Collections.unmodifiableList(list);
        return tables;
    }

    @Override
    public String toString() {
        String schemaName = getName();
        return String.format("%s[%s]", Schema.class.getSimpleName(), schemaName == null ? "<unnamed>" : schemaName);
    }
}
