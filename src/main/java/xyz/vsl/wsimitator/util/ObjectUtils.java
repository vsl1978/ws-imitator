package xyz.vsl.wsimitator.util;

/**
 * Created by vsl on 13.05.2016.
 */
public class ObjectUtils {

    public static <T> T firstNotNull(T a, T b) {
        return a != null ? a : b;
    }

    public static <T> T firstNotNull(T a, T b, T c) {
        return a != null ? a : b != null ? b : c;
    }
}
