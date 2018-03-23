package br.com.staroski.db;

public final class Column implements Comparable<Column> {

    private final String name;
    private final String type;
    private final int javaSqlType;

    public Column(String name, String type, int javaSqlType) {
        this.name = name;
        this.type = type;
        this.javaSqlType = javaSqlType;
    }

    @Override
    public int compareTo(Column other) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Column)) {
            return false;
        }
        Column other = (Column) obj;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", Column.class.getSimpleName(), getName());
    }
}
