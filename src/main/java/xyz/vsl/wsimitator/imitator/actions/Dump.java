package xyz.vsl.wsimitator.imitator.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.Pair;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Created by vsl on 22.05.2016.
 */
public class Dump implements RequestProcessor {
    private final static Pattern PARAM = Pattern.compile("\\{([^}]+)\\}");
    private Logger logger = Logger.getLogger(getClass());

    private static ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private String requestHeaders;
    private String requestAttributes;
    private String requestBody;

    private PersistenceMode dumpHeadersMode;
    private PersistenceMode dumpAttributesMode;
    private PersistenceMode dumpBodyMode;

    public Dump(Element element) {
        requestHeaders = StringUtils.trimToNull(element.getAttribute("requestHeadersFile"));
        if ("false".equals(StringUtils.nullSafeLowerCase(requestHeaders)))
            requestHeaders = null;

        requestAttributes = StringUtils.trimToNull(element.getAttribute("requestAttributesFile"));
        if ("true".equals(StringUtils.nullSafeLowerCase(requestAttributes)))
            requestAttributes = requestHeaders;
        if ("false".equals(StringUtils.nullSafeLowerCase(requestAttributes)))
            requestAttributes = null;

        requestBody = StringUtils.trimToNull(element.getAttribute("requestBodyFile"));
        if ("false".equals(StringUtils.nullSafeLowerCase(requestBody)))
            requestBody = null;

        dumpHeadersMode = PersistenceMode.valueOf(element.getAttribute("dumpHeadersMode"), PersistenceMode.valueOf(element.getAttribute("dumpMode"), PersistenceMode.cli));
        dumpAttributesMode = PersistenceMode.valueOf(element.getAttribute("dumpAttributesMode"), PersistenceMode.valueOf(element.getAttribute("dumpMode"), PersistenceMode.cli));
        dumpBodyMode = PersistenceMode.valueOf(element.getAttribute("dumpBodyMode"), PersistenceMode.valueOf(element.getAttribute("dumpMode"), PersistenceMode.cli));
    }

    @Override
    public ResultType process(WSIContext context) {
        dumpHeaders(context);
        dumpAttributes(context);
        dumpBody(context);
        return ResultType.enter;
    }

    private void dumpHeaders(WSIContext context) {
        if (requestHeaders == null)
            return;
        if (dumpHeadersMode == PersistenceMode.disabled || dumpHeadersMode == PersistenceMode.cli && !context.isFeatureEnabled("xyz.vsl.WSImitator.dumpHeaders"))
            return;

        File file = new File(context.fileSystemSafeSubstitute(requestHeaders, PARAM));
        try {
            String req = "("+context.get("REQ_NO")+") ";
            Pair<String, ReentrantLock> lock = getLock(file);
            lock.second.lockInterruptibly();
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(reopen(file, context), "UTF-8"))) {
                HttpServletRequest hsr = context.getRequest();
                w.write(req + hsr.getMethod() + " " + hsr.getRequestURI() + (hsr.getQueryString() != null ? "?" + hsr.getQueryString() : "") + " " + hsr.getProtocol() + "\r\n");
                for (Enumeration<String> names = hsr.getHeaderNames(); names.hasMoreElements(); ) {
                    String name = names.nextElement();
                    for (Enumeration<String> values = hsr.getHeaders(name); values.hasMoreElements(); ) {
                        w.write(req + name + ":" + values.nextElement() + "\r\n");
                    }
                }
                w.flush();
            } finally {
                unlock(lock);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void unlock(Pair<String, ReentrantLock> lockPair) {
        boolean destroyed = false;
        if (!lockPair.second.hasQueuedThreads()) {
            destroyed = true;
            locks.remove(lockPair.first);
        }
        lockPair.second.unlock();
        if (destroyed)
            logger.trace("Destroy lock for " + lockPair.first);
    }


    private void dumpAttributes(WSIContext context) {
        if (requestAttributes == null)
            return;
        if (dumpAttributesMode == PersistenceMode.disabled || dumpAttributesMode == PersistenceMode.cli && !context.isFeatureEnabled("xyz.vsl.WSImitator.dumpAttributes"))
            return;

        File file = new File(context.fileSystemSafeSubstitute(requestAttributes, PARAM));
        try {
            String req = "("+context.get("REQ_NO")+") ";
            Pair<String, ReentrantLock> lock = getLock(file);
            lock.second.lockInterruptibly();
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(reopen(file, context), "UTF-8"))) {
                for (Enumeration<String> names = context.getRequest().getAttributeNames(); names.hasMoreElements(); ) {
                    String name = names.nextElement();
                    Object attribute = context.getRequest().getAttribute(name);
                    if (attribute != null)
                        w.write(req + "attribute '" + name + "': ("+attribute.getClass().getName()+") '" + attribute + "'\r\n");
                    else
                        w.write(req + "attribute '" + name + "': null\r\n");
                }
                w.flush();
            } finally {
                unlock(lock);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void dumpBody(WSIContext context) {
        if (requestBody == null)
            return;
        if (dumpBodyMode == PersistenceMode.disabled || dumpBodyMode == PersistenceMode.cli && !context.isFeatureEnabled("xyz.vsl.WSImitator.dumpBody"))
            return;

        File file = new File(context.fileSystemSafeSubstitute(requestBody, PARAM));
        try {
            String req = "("+context.get("REQ_NO")+") ";
            Pair<String, ReentrantLock> lock = getLock(file);
            lock.second.lockInterruptibly();
            try (OutputStream os = open(file, context)) {
                os.write(context.getRequestBody());
            } finally {
                unlock(lock);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Pair<String, ReentrantLock> getLock(File file) {
        String lockKey = file.getAbsolutePath();
        ReentrantLock lock = new ReentrantLock();
        ReentrantLock existing = locks.putIfAbsent(lockKey, lock);
        if (existing != null)
            lock = existing;
        return Pair.of(lockKey, lock);
    }

    private OutputStream reopen(File file, WSIContext context) throws IOException {
        file = getFile(file, context);
        return new FileOutputStream(file, true);
    }

    private OutputStream open(File file, WSIContext context) throws IOException {
        file = getFile(file, context);
        return new FileOutputStream(file);
    }

    private File getFile(File file, WSIContext context) throws IOException {
        file = file.getAbsoluteFile();
        if (file.isFile())
            return file;
        if (file.isDirectory())
            return File.createTempFile("dump", ".tmp", file);
        File parent = file.getParentFile();
        if (!parent.exists())
            parent.mkdirs();
        return file;
    }

}
