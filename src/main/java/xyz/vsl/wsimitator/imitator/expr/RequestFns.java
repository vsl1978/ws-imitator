package xyz.vsl.wsimitator.imitator.expr;

import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.Pair;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.http.Cookie;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vsl on 21.05.2016.
 */
public class RequestFns {

    public String uri() {
        return WSIContext.current().getRequest().getRequestURI();
    }
    public String host() {
        return WSIContext.current().getRequest().getServerName();
    }
    public String port() {
        return String.valueOf(WSIContext.current().getRequest().getServerPort());
    }

    public String method() {
        return WSIContext.current().getRequest().getMethod();
    }

    public String header(String regex) {
        WSIContext context = WSIContext.current();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        for (Enumeration<String> names = context.getRequest().getHeaderNames(); names.hasMoreElements(); ) {
            String name = names.nextElement();
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches())
                return context.getRequest().getHeader(name);
        }
        return "";
    }

    public boolean hasHeader(String regex) {
        WSIContext context = WSIContext.current();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        for (Enumeration<String> names = context.getRequest().getHeaderNames(); names.hasMoreElements(); ) {
            String name = names.nextElement();
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches())
                return true;
        }
        return false;
    }

    public String headerName(String regex) {
        WSIContext context = WSIContext.current();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        for (Enumeration<String> names = context.getRequest().getHeaderNames(); names.hasMoreElements(); ) {
            String name = names.nextElement();
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches())
                return name;
        }
        return "";
    }

    public Matcher headerNameMatcher(String regex) {
        WSIContext context = WSIContext.current();
        Pattern pattern = Pattern.compile(regex);
        for (Enumeration<String> names = context.getRequest().getHeaderNames(); names.hasMoreElements(); ) {
            String name = names.nextElement();
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches())
                return matcher;
        }
        return null;
    }

    private Pair<Matcher, Map.Entry<String, String[]>> findParameter(String regex) {
        WSIContext context = WSIContext.current();
        Pattern pattern = Pattern.compile(regex);

        Map<String, String[]> map = context.getRequest().getParameterMap();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            Matcher m = pattern.matcher(entry.getKey());
            if (m.matches())
                return Pair.of(m, entry);
        }
        return Pair.of(null, null);
    }

    public String parameter(String regex) {
        Map.Entry<String, String[]> parameter = findParameter(regex).second;
        if (parameter == null)
            return "";
        String[] values = parameter.getValue();
        if (values != null)
            for (String v : values)
                if (!StringUtils.isEmpty(v))
                    return v;
        return "";
    }

    public boolean hasParameter(String regex) {
        return findParameter(regex).second != null;
    }

    public String parameterName(String regex) {
        Map.Entry<String, String[]> parameter = findParameter(regex).second;
        return parameter == null ? "" : parameter.getKey();
    }

    public Matcher parameterNameMatcher(String regex) {
        return findParameter(regex).first;
    }

    public String cookieValue(String name) {
        Cookie[] cookies = WSIContext.current().getRequest().getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                if (Objects.equals(cookie.getName(), name))
                    return cookie.getValue();
            }
        return "";
    }

    public boolean hasCookie(String name) {
        Cookie[] cookies = WSIContext.current().getRequest().getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                if (Objects.equals(cookie.getName(), name))
                    return true;
            }
        return false;
    }
}
