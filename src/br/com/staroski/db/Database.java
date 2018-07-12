package br.com.staroski.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
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
 * This class represents a database
 * 
 * @author Ricardo Artur Staroski
 */
public final class Database {

    public static Database connect(String driver, String protocol, String host, int port, String databaseName, String user, String pass) {
        return new Database(driver, protocol, host, port, databaseName, user, pass);
    }

    public static Database readFrom(InputStream in) {
        DataInputStream dataInput = in instanceof DataInputStream
                ? (DataInputStream) in
                : new DataInputStream(in);
        return readFrom(dataInput);
    }

    static Database readFrom(DataInputStream in) {
        String driver = IO.readString(in);
        String protocol = IO.readString(in);
        String host = IO.readString(in);
        int port = IO.readInt(in);
        String name = IO.readString(in);
        String user = IO.readString(in);
        String alias = IO.readString(in);
        List<Catalog> catalogs = new LinkedList<Catalog>();
        Map<String, Catalog> catalogMap = new HashMap<String, Catalog>();
        Database database = new Database(driver, protocol, host, port, name, user, alias, catalogs, catalogMap);
        int catalogCount = IO.readInt(in);
        for (int i = 0; i < catalogCount; i++) {
            Catalog catalog = Catalog.readFrom(in).setDatabase(database);
            catalogs.add(catalog);
            catalogMap.put(catalog.getName(), catalog);
        }
        return database;
    }

    private final String driver;

    private final String protocol;

    private final String host;
    private final int port;
    private final String name;
    private final String url;
    private final String user;

    private Connection connection;
    private String alias;
    private List<Catalog> catalogs;
    private Map<String, Catalog> catalogMap;

    private Database(String driver, String protocol, String host, int port, String name, String user, String pass) {
        try {
            Class.forName(driver).newInstance();
            this.driver = driver;
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.name = name;
            this.user = user;
            this.alias = name;
            this.url = protocol + "://" + host + ":" + port + "/" + name;
            connection = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            throw UncheckedException.wrap(e);
        }
    }

    private Database(String driver, String protocol, String host, int port, String name, String user, String alias, List<Catalog> catalogs,
                     Map<String, Catalog> catalogMap) {
        this.driver = driver;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.name = name;
        this.user = user;
        this.alias = alias;
        this.url = protocol + "://" + host + ":" + port + "/" + name;
        this.catalogs = Collections.unmodifiableList(catalogs);
        this.catalogMap = catalogMap;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw UncheckedException.wrap(e);
            }
        }
    }

    public String getAlias() {
        return alias;
    }

    public Catalog getCatalog(String name) {
        for (Catalog catalog : getCatalogs()) {
            if (Utils.areEqualsIgnoreCase(name, catalog.getName())) {
                return catalog;
            }
        }
        return null;
    }

    public List<Catalog> getCatalogs() {
        if (catalogs != null) {
            return catalogs;
        }
        catalogMap = new HashMap<String, Catalog>();
        List<Catalog> list = new LinkedList<Catalog>();
        if (connection != null) {
            try {
                ResultSet result = connection.getMetaData().getCatalogs();
                while (result.next()) {
                    String catalogName = result.getString("TABLE_CAT");
                    Catalog catalog = new Catalog(connection, this, catalogName);
                    list.add(catalog);
                    catalogMap.put(catalogName, catalog);
                }
            } catch (SQLException e) {
                throw UncheckedException.wrap(e);
            }
        }
        catalogs = Collections.unmodifiableList(list);
        return catalogs;
    }

    public String getDriver() {
        return driver;
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUser() {
        return user;
    }

    public Database setAlias(String alias) {
        this.alias = alias == null ? name : alias;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s[user=%s, url=%s, driver=%s]", Database.class.getSimpleName(), user, url, driver);
    }

    public void writeTo(OutputStream out) {
        DataOutputStream dataOutput = out instanceof DataOutputStream
                ? (DataOutputStream) out
                : new DataOutputStream(out);
        writeTo(dataOutput);
    }

    void writeTo(DataOutputStream out) {
        IO.writeString(out, driver);
        IO.writeString(out, protocol);
        IO.writeString(out, host);
        IO.writeInt(out, port);
        IO.writeString(out, name);
        IO.writeString(out, user);
        IO.writeString(out, alias);
        List<Catalog> catalogs = getCatalogs();
        IO.writeInt(out, catalogs.size());
        for (Catalog catalog : catalogs) {
            catalog.writeTo(out);
        }
    }
}
