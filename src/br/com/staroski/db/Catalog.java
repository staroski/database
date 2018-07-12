package br.com.staroski.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.com.staroski.IO;
import br.com.staroski.UncheckedException;
import br.com.staroski.Utils;

/**
 * This class represents a database catalog
 * 
 * @author Ricardo Artur Staroski
 */
public final class Catalog {

    static Catalog readFrom(DataInputStream in) {
        String name = IO.readString(in);
        List<Schema> schemas = new LinkedList<Schema>();
        Map<String, Schema> schemaMap = new HashMap<String, Schema>();
        Catalog catalog = new Catalog(name, schemas, schemaMap);
        int schemaCount = IO.readInt(in);
        for (int i = 0; i < schemaCount; i++) {
            Schema schema = Schema.readFrom(in).setCatalog(catalog);
            schemas.add(schema);
            schemaMap.put(schema.getName(), schema);
        }
        return catalog;
    }

    private final String name;

    private Connection connection;
    private Database database;
    private List<Schema> schemas;
    private Map<String, Schema> schemaMap;

    private Catalog(String name, List<Schema> schemas, Map<String, Schema> schemaMap) {
        this.name = name;
        this.schemas = Collections.unmodifiableList(schemas);
        this.schemaMap = new HashMap<String, Schema>();
    }

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
        schemaMap = new HashMap<String, Schema>();
        List<Schema> list = new LinkedList<Schema>();
        if (connection != null) {
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
                    Schema schema = new Schema(connection, this, schemaName);
                    list.add(schema);
                    schemaMap.put(schemaName, schema);
                }
            } catch (SQLException e) {
                throw UncheckedException.wrap(e);
            }
        }
        schemas = Collections.unmodifiableList(list);
        return schemas;
    }

    @Override
    public String toString() {
        String catalogName = getName();
        return String.format("%s[%s]", Catalog.class.getSimpleName(), catalogName == null ? "<unnamed>" : catalogName);
    }

    Catalog setDatabase(Database database) {
        this.database = database;
        return this;
    }

    void writeTo(DataOutputStream out) {
        IO.writeString(out, name);
        List<Schema> schemas = getSchemas();
        IO.writeInt(out, schemas.size());
        for (Schema schema : schemas) {
            schema.writeTo(out);
        }
    }
}
