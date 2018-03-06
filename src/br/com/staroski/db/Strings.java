package br.com.staroski.db;

public final class Strings {

    public static boolean areEquals(String a, String b) {
        return equals(a, b, false);
    }

    public static boolean areEqualsIgnoreCase(String a, String b) {
        return equals(a, b, true);
    }

    private static boolean equals(String a, String b, boolean ignoreCase) {
        if (a == b) {
            return true;
        }
        if (a == null) {
            return false;
        }
        if (b == null) {
            return false;
        }
        return ignoreCase ? a.equalsIgnoreCase(b) : a.equals(b);
    }

    private Strings() {}
}
