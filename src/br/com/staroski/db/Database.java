package br.com.staroski.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Database {

    public static Database connect(String driver, String url, String user, String pass) {
        return new Database(driver, url, user, pass);
    }

    private final String driver;
    private final String url;
    private final String user;
    private final Connection connection;
    private List<Catalog> catalogs;

    private Database(String driver, String url, String user, String pass) {
        try {
            Class.forName(driver).newInstance();
            this.driver = driver;
            this.url = url;
            this.user = user;
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

    public Catalog getCatalog(String name) {
        for (Catalog catalog : getCatalogs()) {
            if (Strings.areEqualsIgnoreCase(name, catalog.getName())) {
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
                list.add(new Catalog(connection, catalogName));
            }
        } catch (SQLException e) {
            throw UncheckedException.wrap(e);
        }
        catalogs = Collections.unmodifiableList(list);
        return catalogs;
    }

    @Override
    public String toString() {
        return String.format("%s[user=%s, url=%s, driver=%s]", Database.class.getSimpleName(), user, url, driver);
    }
}
