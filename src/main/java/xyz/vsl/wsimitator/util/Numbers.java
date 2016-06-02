package xyz.vsl.wsimitator.util;

/**
 * Created by vsl on 13.05.2016.
 */
public class Numbers {

    public static long val(String s, long defaultValue, long min, long max) {
        if (s == null || s.length() == 0)
            return defaultValue;
        try {
            long value = Long.parseLong(s);
            return (value < min || value > max) ? defaultValue : value;
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static long val(String s, long defaultValue) {
        return val(s, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }


    public static int val(String s, int defaultValue, int min, int max) {
        if (s == null || s.length() == 0)
            return defaultValue;
        try {
            int value = Integer.parseInt(s);
            return (value < min || value > max) ? defaultValue : value;
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static int val(String s, int defaultValue) {
        return val(s, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int val(String s, int defaultValue, int minValue) {
        return val(s, defaultValue, minValue, Integer.MAX_VALUE);
    }

    public static Integer integerVal(String s, Integer defaultValue, Integer min, Integer max) {
        if (s == null || s.length() == 0)
            return defaultValue;
        try {
            int value = Integer.parseInt(s);
            if (value < min || value > max)
                return defaultValue;
            return value;
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static Integer integerVal(String s, Integer defaultValue) {
        return integerVal(s, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static Long longVal(String s, Long defaultValue, long min, long max) {
        if (s == null || s.length() == 0)
            return defaultValue;
        try {
            long value = Long.parseLong(s);
            if (value < min || value > max)
                return defaultValue;
            return value;
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static Long longVal(String s, Long defaultValue) {
        return longVal(s, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static int intValue(Object o, int defaultValue) {
        if (o instanceof Number)
            return ((Number)o).intValue();
        return defaultValue;
    }

}
