package br.com.staroski.db;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Utils {

    public static boolean areEquals(String a, String b) {
        return equals(a, b, false);
    }

    public static boolean areEqualsIgnoreCase(String a, String b) {
        return equals(a, b, true);
    }

    public static <T> List<T> asList(T object, T... moreObjects) {
        List<T> list = new LinkedList<T>();
        list.add(object);
        for (T objectN : moreObjects) {
            list.add(objectN);
        }
        return Collections.unmodifiableList(list);
    }

    public static <T> List<T> asList(T object1, T object2, T... moreObjects) {
        List<T> list = new LinkedList<T>();
        list.add(object1);
        list.add(object2);
        for (T objectN : moreObjects) {
            list.add(objectN);
        }
        return Collections.unmodifiableList(list);
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

    private Utils() {}
}
