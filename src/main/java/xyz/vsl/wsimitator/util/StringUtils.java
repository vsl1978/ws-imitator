package xyz.vsl.wsimitator.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
public class StringUtils {
    public static String trimToNull(String s) {
        if (s == null)
            return null;
        s = s.trim();
        return s.length() == 0 ? null : s;
    }

    public static String nullSafeLowerCase(String s) {
        return s == null ? null : s.toLowerCase();
    }

    public static String nullSafeSubstringAfter(String s, char c) {
        if (s == null)
            return null;
        int pos = s.indexOf(c);
        if (pos < 0)
            return null;
        return s.substring(pos + 1);
    }

    public static boolean isEmpty(String s) {
        return trimToNull(s) == null;
    }

    public static String replace(String string, Pattern pattern, Function<String, String> getter) {
        if (StringUtils.isEmpty(string))
            return null;
        if (pattern == null)
            return string;
        StringBuilder sb = new StringBuilder();
        int start = 0;
        Matcher m = pattern.matcher(string);
        while (m.find()) {
            sb.append(string, start, m.start());
            String name = m.group(1);
            String value = getter != null ? getter.apply(name) : null;
            if (!StringUtils.isEmpty(value))
                sb.append(value);
            start = m.end();
        }
        sb.append(string, start, string.length());
        return sb.toString();
    }

    public static String removeSpecialCharsFromPathElement(String s) {
        s = StringUtils.trimToNull(s);
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < ' ' || c == ':' || c == '?' || c == '*' || c == '"' || c == '\'' || c == '\\' || c == '/' || c == '<' || c == '>')
                continue;
            sb.append(c);
        }
        s = sb.toString().trim();
        return ".".equals(s) || "..".equals(s) ? "" : s;
    }

}
