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

    public static String formatInterval(long elapsed) {
        final long h = TimeUnit.MILLISECONDS.toHours(elapsed);
        final long min = TimeUnit.MILLISECONDS.toMinutes(elapsed - TimeUnit.HOURS.toMillis(h));
        final long s = TimeUnit.MILLISECONDS.toSeconds(elapsed - TimeUnit.HOURS.toMillis(h) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(elapsed - TimeUnit.HOURS.toMillis(h) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(s));

        StringBuilder text = new StringBuilder("");
        String space = "";
        if (h > 0) {
            text.append(String.format("%d hour%s", h, h > 1 ? "s" : ""));
            space = " ";
        }
        if (min > 0) {
            text.append(space);
            text.append(String.format("%d minute%s", min, min > 1 ? "s" : ""));
            space = " ";
        }
        if (s > 0) {
            text.append(space);
            text.append(String.format("%d second%s", s, s > 1 ? "s" : ""));
            space = " ";
        }
        if (ms > 0) {
            text.append(space);
            text.append(String.format("%d millisecond%s", ms, ms > 1 ? "s" : ""));
        }
        return text.toString();
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
