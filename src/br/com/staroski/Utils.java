package br.com.staroski;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public static String formatInterval(final long elapsed) {
        final long h = TimeUnit.MILLISECONDS.toHours(elapsed);
        final long min = TimeUnit.MILLISECONDS.toMinutes(elapsed - TimeUnit.HOURS.toMillis(h));
        final long s = TimeUnit.MILLISECONDS.toSeconds(elapsed - TimeUnit.HOURS.toMillis(h) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(elapsed - TimeUnit.HOURS.toMillis(h) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(s));
        return String.format("%02d:%02d:%02d.%03d", h, min, s, ms);
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
