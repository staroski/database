package br.com.staroski.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import br.com.staroski.UncheckedException;
import br.com.staroski.Utils;

public final class Database {

    public static Database connect(String driver, String protocol, String host, int port, String databaseName, String user, String pass) {
        return new Database(driver, protocol, host, port, databaseName, user, pass);
    }

    private final String driver;
    private final String protocol;
    private final String host;
    private final int port;
    private final String name;
    private final String url;
    private final String user;
    private final Connection connection;
    private List<Catalog> catalogs;
    private String alias;

    private Database(String driver, String protocol, String host, int port, String name, String user, String pass) {
        try {
            Class.forName(driver).newInstance();
            this.driver = driver;
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.name = name;
            this.url = protocol + "://" + host + ":" + port + "/" + name;
            this.user = user;
            this.alias = name;
            connection = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            throw UncheckedException.wrap(e);
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw UncheckedException.wrap(e);
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
        List<Catalog> list = new LinkedList<Catalog>();
        try {
            ResultSet result = connection.getMetaData().getCatalogs();
            while (result.next()) {
                String catalogName = result.getString("TABLE_CAT");
                list.add(new Catalog(connection, this, catalogName));
            }
        } catch (SQLException e) {
            throw UncheckedException.wrap(e);
        }
        catalogs = Collections.unmodifiableList(list);
        return catalogs;
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
}
