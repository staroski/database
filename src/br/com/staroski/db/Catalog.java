package br.com.staroski.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Catalog {

    private final Connection connection;
    private final Database database;
    private final String name;
    private List<Schema> schemas;

    Catalog(Connection connection, Database database, String name) {
        this.connection = connection;
        this.database = database;
        this.name = name;
    }

    public Database getDatabase() {
        return database;
    }

    public String getName() {
        return name;
    }

    public Schema getSchema(String name) {
        for (Schema schema : getSchemas()) {
            if (Utils.areEqualsIgnoreCase(name, schema.getName())) {
                return schema;
            }
        }
        return null;
    }

    public List<Schema> getSchemas() {
        if (schemas != null) {
            return schemas;
        }
        List<Schema> list = new LinkedList<Schema>();
        try {
            String thisCatalogName = this.getName();
            ResultSet result = connection.getMetaData().getSchemas();
            while (result.next()) {
                String schemaName = result.getString("TABLE_SCHEM");
                String catalogName;
                try {
                    catalogName = result.getString("TABLE_CAT"); // documentation says it's TABLE_CAT
                } catch (Exception e) {
                    catalogName = result.getString("TABLE_CATALOG"); // but many return TABLE_CATALOG
                }
                if (!Utils.areEqualsIgnoreCase(thisCatalogName, catalogName)) {
                    continue;
                }
                list.add(new Schema(connection, this, schemaName));
            }
        } catch (SQLException e) {
            throw UncheckedException.wrap(e);
        }
        schemas = Collections.unmodifiableList(list);
        return schemas;
    }

    @Override
    public String toString() {
        String catalogName = getName();
        return String.format("%s[%s]", Catalog.class.getSimpleName(), catalogName == null ? "<unnamed>" : catalogName);
    }
}
