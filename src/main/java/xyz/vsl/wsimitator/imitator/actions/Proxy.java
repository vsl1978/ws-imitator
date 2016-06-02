package xyz.vsl.wsimitator.imitator.actions;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.HTTPHeaders;
import xyz.vsl.wsimitator.util.Numbers;
import xyz.vsl.wsimitator.util.Pair;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vsl on 19.05.2016.
 */
public class Proxy implements RequestProcessor {
    private Logger logger = Logger.getLogger(getClass());
    private String host;
    private int port;
    private PersistenceMode persistenceMode;

    public Proxy(Element element) {
        this.host = StringUtils.trimToNull(element.getAttribute("host"));
        this.port = Numbers.val(element.getAttribute("port"), 80, 1, 65000);
        this.persistenceMode = PersistenceMode.valueOf(element.getAttribute("persistence"), PersistenceMode.cli);
    }

    @Override
    public ResultType process(WSIContext context) {
        Pair<ResponseInfo, byte[]> response = sendRequest(context);
        if (response.first == null)
            return ResultType.tryNext;
        store(response, context);
        returnResponse(response, context);
        return ResultType.stop;
    }

    private void returnResponse(Pair<ResponseInfo, byte[]> response, WSIContext context) {
        setStatus(response.first, context);
        copyHeaders(response.first, context);
        writeBody(response.second, context);
    }

    private void writeBody(byte[] body, WSIContext context) {
        if (body == null || body.length == 0)
            return;
        try {
            context.getResponse().setIntHeader("Content-Length", body.length);
            ServletOutputStream outputStream = context.getResponse().getOutputStream();
            outputStream.write(body);
            outputStream.flush();
            outputStream.close();
            logger.trace("response sent");
        } catch (IOException e) {
            logger.error("response body length="+body.length, e);
        }

    }

    private void copyHeaders(ResponseInfo responseInfo, WSIContext context) {
        Set<String> processed = new HashSet<>();
        for (Pair<String, String> header : responseInfo.headers()) {
            String h = header.first.toLowerCase();
            if ("content-length".equals(h) || "content-encoding".equals(h) || "transfer-encoding".equals(h))
                continue;
            if (processed.add(h)) {
                context.getResponse().setHeader(header.first, header.second);
            } else {
                context.getResponse().addHeader(header.first, header.second);
            }
        }
    }

    private void setStatus(ResponseInfo responseInfo, WSIContext context) {
        String message = StringUtils.trimToNull(responseInfo.reason);
        if (logger.isTraceEnabled())
            logger.trace("Status is "+responseInfo.status+", message is '"+message+"'");
        if (message != null && !"ok".equals(message.toLowerCase()))
            try {
                context.getResponse().sendError(responseInfo.status, message);
            } catch (IOException e) {
                logger.error("Status is "+responseInfo.status+", message is '"+message+"'", e);
                context.getResponse().setStatus(responseInfo.status);
            }
        else {
            context.getResponse().setStatus(responseInfo.status);
        }
    }

    private void store(Pair<ResponseInfo, byte[]> response, WSIContext context) {
        if (response.second == null || response.second.length == 0)
            return;
        if (persistenceMode == PersistenceMode.disabled || persistenceMode == PersistenceMode.cli && !context.isFeatureEnabled("xyz.vsl.WSImitator.autoLearning"))
            return;
        String fileName = context.get(StaticFile.TARGET_FILE);
        if (fileName == null)
            return;
        File file = new File(fileName);
        if (file.exists()) // concurrent request
            return;
        try {
            File parent = file.getAbsoluteFile().getParentFile();
            if (!parent.exists())
                parent.mkdirs();
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(response.second);
            }
            logger.info("Created file "+fileName+", "+response.second.length+" byte(s)");
        } catch (IOException e) {
            logger.error(fileName, e);
        }

    }

    private Pair<ResponseInfo, byte[]> sendRequest(WSIContext context) {
        try {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpHost target = new HttpHost(host, port);
                HttpRequest request = buildRequest(context);
                logger.debug("Send request to "+target.toString()+context.getRequest().getRequestURI());
                try (CloseableHttpResponse response = httpclient.execute(target, request)) {
                    return processResponse(response, target, request, context);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Pair.of(null, null);
    }

    private Pair<ResponseInfo, byte[]> processResponse(CloseableHttpResponse response, HttpHost target, HttpRequest request, WSIContext context) {
        if (logger.isDebugEnabled())
            logger.debug("Received response - "+response.getStatusLine());

        Header[] allHeaders = response.getAllHeaders();
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.status = response.getStatusLine().getStatusCode();
        responseInfo.reason = response.getStatusLine().getReasonPhrase();
        if (allHeaders != null) {
            for (Header header : allHeaders) {
                responseInfo.add(header.getName(), header.getValue());
                logger.trace(header.getName() + ":" + header.getValue());
            }
        }

        HttpEntity entity = response.getEntity();
        byte[] data = null;
        if (entity != null) {
            logger.debug("Content-Length: " + entity.getContentLength() + ", " + entity.getContentEncoding() + ", " + entity.getContentType());
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InputStream is = entity.getContent()) {
                    int r;
                    byte[] buf = new byte[1024];
                    while ((r = is.read(buf)) >= 0) baos.write(buf, 0, r);
                    data = baos.toByteArray();
                }
            } catch (NullPointerException e) {
                // ignore
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return Pair.of(null, null);
            }
        }

        return Pair.of(responseInfo, data);
    }

    private HttpRequest buildRequest(WSIContext context) {
        String uri = context.getRequest().getRequestURI();
        String query = context.getRequest().getQueryString();
        if (query != null)
                uri = uri + "?" + query;
        RequestBuilder builder = RequestBuilder.create(context.getRequest().getMethod()).setUri(uri);
        for (Enumeration<String> names = context.getRequest().getHeaderNames(); names.hasMoreElements(); ) {
            String name = names.nextElement();
            String lowerCase = name.toLowerCase();
            if ("content-length".equals(lowerCase) || "host".equals(lowerCase))
                continue;
            boolean firstValue = true;
            for (Enumeration<String> values = context.getRequest().getHeaders(name); values.hasMoreElements(); ) {
                String value = values.nextElement();
                if (firstValue) {
                    firstValue = false;
                    builder.setHeader(name, value);
                } else {
                    builder.addHeader(name, value);
                }
            }
        }
        byte[] body = context.getRequestBody();
        if (body != null && body.length > 0) {
            InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream(body), body.length);
            builder.setEntity(entity);
        }
        HttpUriRequest request = builder.build();
        return request;
    }

    private static class ResponseInfo extends HTTPHeaders {
        int status;
        public String reason;
    }
}
