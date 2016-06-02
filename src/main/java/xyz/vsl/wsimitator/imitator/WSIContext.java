package xyz.vsl.wsimitator.imitator;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import xyz.vsl.wsimitator.util.DOM;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
public class WSIContext {
    @Getter private HttpServletRequest request;
    @Getter private HttpServletResponse response;
    @Getter private File baseDirectory;
    @Getter private String baseDirectoryPath;
    @Getter private long requestStart = System.currentTimeMillis();
    private Map<String, Object> parameters = new HashMap<>();
    private static AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    private LinkedList<Map<String, String>> variables = new LinkedList<>();
    private Document cachedDOM;
    private byte[] cachedBody;

    private static ThreadLocal<WSIContext> threadLocal = new ThreadLocal<>();

    public static WSIContext current() {
        return threadLocal.get();
    }

    public WSIContext(HttpServletRequest request, HttpServletResponse response, File baseDirectory) {
        this.request = request;
        this.response = response;
        this.baseDirectory = baseDirectory;

        baseDirectoryPath = baseDirectory == null ? "/" : baseDirectory.getAbsolutePath();
        if (!baseDirectoryPath.endsWith("/") && !baseDirectoryPath.endsWith("\\"))
            baseDirectoryPath = baseDirectoryPath + File.separator;

        variables.add(new HashMap<>());
        initContext();
    }

    private void initContext() {
        Calendar c = Calendar.getInstance();
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month  = String.valueOf(101 + c.get(Calendar.MONTH)).substring(1);
        String day  = String.valueOf(100 + c.get(Calendar.DATE)).substring(1);
        String dayOfYear  = String.valueOf(1000 + c.get(Calendar.DAY_OF_YEAR)).substring(1);
        String week = String.valueOf(100 + c.get(Calendar.WEEK_OF_YEAR)).substring(1);
        String hour = String.valueOf(100 + c.get(Calendar.HOUR_OF_DAY)).substring(1);
        String minute = String.valueOf(100 + c.get(Calendar.MINUTE)).substring(1);
        String second = String.valueOf(100 + c.get(Calendar.SECOND)).substring(1);
        String msec = String.valueOf(1000 + c.get(Calendar.MILLISECOND)).substring(1);
        set("YEAR", year);
        set("MONTH", month);
        set("MM", month);
        set("DAY", day);
        set("DAY_OF_YEAR", dayOfYear);
        set("WEEK", week);
        set("HOUR", hour);
        set("HH", hour);
        set("HH24", hour);
        set("MINUTE", minute);
        set("MI", minute);
        set("SECOND", second);
        set("SS", second);
        set("MILLISECOND", msec);
        set("MILLIS", msec);
        set("FFF", msec);
        set("SSS", msec);
        set("DATE", year + "-" + month + "-"+day);
        set("TIME", hour + "-" + minute + "-"+second+"_"+msec);
        set("REQ_NO", String.valueOf(counter.incrementAndGet()));
        set("METHOD", request.getMethod());
    }

    public WSIContext set(String name, String value) {
        variables.getFirst().put(name, value);
        return this;
    }

    public String get(String name) {
        return variables.getFirst().get(name);
    }

    public Map<String, String> context() { return variables.getFirst(); }

    public WSIContext enter() {
        return enter(true);
    }

    public WSIContext enter(boolean newContext) {
        Map<String, String> map = newContext ? new HashMap<>(variables.getFirst()) : variables.getFirst();
        variables.add(0, map);
        return this;
    }

    public WSIContext leave() {
        if (variables.size() > 1)
            variables.remove(0);
        return this;
    }

    public Document getRequestDOM() {
        if (cachedDOM != null)
            return cachedDOM;
        byte[] data = getRequestBody();
        cachedDOM = DOM.parse(new ByteArrayInputStream(data));
        return cachedDOM;
    }

    public byte[] getRequestBody() {
        if (cachedBody != null)
            return cachedBody;
        try {
            ServletInputStream inputStream = request.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[128 * 1024];
            int r;
            while ((r = inputStream.read(buffer)) >= 0)
                baos.write(buffer, 0, r);
            cachedBody = baos.toByteArray();
            inputStream.close();
        } catch (IOException e) {
            Logger.getLogger(getClass()).error("Failed to read request body", e);
        }
        return cachedBody;
    }

    public boolean isFeatureEnabled(String feature) {
        String a = StringUtils.nullSafeLowerCase(System.getProperty(feature));
        return "true".equals(a) || "enable".equals(a) || "enabled".equals(a) || "on".equals(a);
    }

    public WSIContext setThreadLocal(boolean set) {
        if (set) {
            threadLocal.set(this);
        } else {
            threadLocal.remove();
        }
        return this;
    }

    public WSIContext setParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public <T> T getParameter(String key) {
        return (T)parameters.get(key);
    }

    public WSIContext removeParameter(String key) {
        parameters.remove(key);
        return this;
    }

    public String substitute(String string, Pattern pattern) {
        return StringUtils.replace(string, pattern, this::get);
    }
    public String fileSystemSafeSubstitute(String string, Pattern pattern) {
        return StringUtils.replace(string, pattern, s -> "basedir".equals(StringUtils.nullSafeLowerCase(s)) ? getBaseDirectoryPath() : StringUtils.removeSpecialCharsFromPathElement(get(s)));
    }
}
