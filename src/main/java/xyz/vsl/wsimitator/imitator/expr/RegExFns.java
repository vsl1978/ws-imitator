package xyz.vsl.wsimitator.imitator.expr;

import java.util.regex.Matcher;

/**
 * Created by vsl on 21.05.2016.
 */
public class RegExFns {

    public String replace(String source, String pattern, String replacement) {
        return source.replaceAll(pattern, replacement);
    }

    public String replaceAll(String source, String pattern, String replacement) {
        return source.replaceAll(pattern, replacement);
    }

    public String mreplaceAll(Matcher matcher, String replacement) {
        return matcher.replaceAll(replacement);
    }

    public boolean matches(String text, String regex) {
        return text.matches(regex);
    }
}
