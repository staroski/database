package br.com.staroski.db;

public final class Column {

    private final String name;
    private final String type;
    private final int javaSqlType;

    public Column(String name, String type, int javaSqlType) {
        this.name = name;
        this.type = type;
        this.javaSqlType = javaSqlType;
    }

    public int getJavaSqlType() {
        return javaSqlType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", Column.class.getSimpleName(), getName());
    }
}
