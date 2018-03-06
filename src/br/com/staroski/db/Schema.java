package br.com.staroski.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Schema {

    private final Connection connection;
    private final Catalog catalog;
    private final String name;
    private List<Table> tables;

    Schema(Connection connection, Catalog catalog, String name) {
        this.connection = connection;
        this.catalog = catalog;
        this.name = name;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public String getName() {
        return name;
    }

    public Table getTable(String name) {
        for (Table table : getTables()) {
            if (Strings.areEqualsIgnoreCase(name, table.getName())) {
                return table;
            }
        }
        return null;
    }

    public List<Table> getTables() {
        if (tables != null) {
            return tables;
        }
        List<Table> list = new LinkedList<Table>();
        try {
            String thisCatalogName = getCatalog().getName();
            String thisSchemaName = getName();
            ResultSet result = connection.getMetaData().getTables(thisCatalogName, thisSchemaName, null, null);
            while (result.next()) {
                String catalogName = result.getString("TABLE_CAT");
                String schemaName = result.getString("TABLE_SCHEM");
                if (!Strings.areEqualsIgnoreCase(thisCatalogName, catalogName)
                        || !Strings.areEqualsIgnoreCase(thisSchemaName, schemaName)) {
                    continue;
                }
                String tableName = result.getString("TABLE_NAME");
                String tableType = result.getString("TABLE_TYPE");
                list.add(new Table(connection, this, tableName, tableType));
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
