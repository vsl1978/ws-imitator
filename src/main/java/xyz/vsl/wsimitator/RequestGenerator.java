package xyz.vsl.wsimitator;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import xyz.vsl.wsimitator.util.HTTPHeaders;
import xyz.vsl.wsimitator.util.Numbers;
import xyz.vsl.wsimitator.util.Pair;
import xyz.vsl.wsimitator.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vsl on 17.05.2016.
 */
class RequestGenerator implements Runnable {
    private boolean invalidState;
    private int port;
    private HTTPHeaders headers = new HTTPHeaders();
    private String postTemplate;
    private byte[] postBody;
    private Logger logger = Logger.getLogger(getClass());
    private String uri;
    private List<Map<String, String>> dataSet = new ArrayList<>();
    @Getter @Setter @Accessors(chain = true) private String prefix = "#{{";
    @Getter @Setter @Accessors(chain = true) private String suffix = "}}";
    private Long pause;

    public RequestGenerator(int port) {
        this.port = port;
    }

    public RequestGenerator setRequestURI(String uri) {
        if (uri.startsWith("/"))
            uri = uri.substring(1);
        this.uri = uri;
        return this;
    }

    public RequestGenerator loadRequestHeaders(String fileName) {
        if (StringUtils.isEmpty(fileName))
            return this;
        File file = new File(fileName);
        if (!file.exists()) {
            invalidState = true;
            logger.error("File "+file+" not found");
        }
        else if (!file.isFile()) {
            invalidState = true;
            logger.error(file+" is not a file");
        }
        else {
            try {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                    for (String s; (s = r.readLine()) != null; ) {
                        int colon = s.indexOf(':');
                        String name = s.substring(0, colon).trim();
                        String value = s.substring(colon + 1).trim();
                        headers.add(name, value);
                    }
                }
            } catch (IOException ioe) {
                invalidState = true;
                logger.error(file.getAbsolutePath(), ioe);
            }
        }
        return this;
    }

    public RequestGenerator loadRequestBody(String fileName) {
        if (StringUtils.isEmpty(fileName))
            return this;
        File file = new File(fileName);
        if (!file.exists()) {
            invalidState = true;
            logger.error("File "+file+" not found");
        }
        else if (!file.isFile()) {
            invalidState = true;
            logger.error(file+" is not a file");
        }
        else {
            try {
                try (InputStream is = new FileInputStream(file)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream((int)file.length());
                    byte[] buf = new byte[128 * 1024];
                    int count;
                    while ((count = is.read(buf)) >= 0)
                        baos.write(buf, 0, count);
                    postBody = baos.toByteArray();
                }
                fileName = file.getName().toLowerCase();
                boolean binary = isPOSTBodyBinary(fileName, postBody);
                if (!binary)
                    postTemplate = new String(postBody, getPOSTBodyCharset());
            } catch (IOException ioe) {
                invalidState = true;
                logger.error(file.getAbsolutePath(), ioe);
            }
        }
        return this;
    }

    public String getPOSTBodyCharset() {
        return "UTF-8";
    }

    public boolean isPOSTBodyBinary(String fileName, byte[] content) {
        return fileName.endsWith(".bin"); // || fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".")
    }

    public RequestGenerator loadDataSet(String fileName) {
        if (StringUtils.isEmpty(fileName))
            return this;
        File file = new File(fileName);
        if (!file.exists()) {
            invalidState = true;
            logger.error("File "+file+" not found");
            return this;
        }
        else if (!file.isFile()) {
            invalidState = true;
            logger.error(file+" is not a file");
            return this;
        }
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), getDataSetCharset()))) {
                String headerString = reader.readLine();
                CSVFormat csvFormat = CSVFormat.EXCEL.withDelimiter(',').withSkipHeaderRecord(false).withHeader((headerString.replace("\"", "")).split(","));
                Iterable<CSVRecord> records = csvFormat.parse(reader);
                String[] header = csvFormat.getHeader();
                Object[] buf = null;

                for (CSVRecord record : records) {
                    Map<String, String> map = new HashMap<>();
                    for (int i = 0; i < header.length; i++)
                        map.put(header[i], record.get(i));
                    dataSet.add(map);
                }
            }
        } catch (IOException ioe) {
            logger.error(file.getAbsolutePath(), ioe);
        }

        return this;
    }

    public String getDataSetCharset() {
        return "Cp1251";
    }

    @Override
    @SneakyThrows({InterruptedException.class})
    public void run() {
        if (invalidState) return;
        if (dataSet.isEmpty()) {
            logger.error("Dataset is empty, nothing to generate");
            return;
        }
        for (Map<String, String> map : dataSet) {
            sendRequest(map);
            if (pause != null)
                Thread.sleep(pause);
        }
    }

    @SneakyThrows({UnsupportedEncodingException.class})
    protected void sendRequest(Map<String, String> map) {
        String uri = replace(this.uri, map);
        byte[] data = postTemplate != null ? replace(postTemplate, map).getBytes("UTF-8") : postBody;
        String url = "http://127.0.0.1:"+port+"/"+uri;
        HttpRequestBase req;
        if (data == null || data.length == 0) {
            req = new HttpGet(url);
            data = null;
        } else {
            req = new HttpPost(url);
        }
        try {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                Set<String> headers = new HashSet<>();
                for (Pair<String, String> header : this.headers.headers()) {
                    if (headers.add(header.first.toLowerCase()))
                        req.setHeader(header.first, header.second);
                    else
                        req.addHeader(header.first, header.second);
                }
                if (data != null) {
                    InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream(data), data.length);
                    ((HttpPost) req).setEntity(entity);
                }
                if (logger.isDebugEnabled())
                    logger.debug("Send request to " + url + ", " + map);
                else
                    logger.info("Send request to "+url);
                try (CloseableHttpResponse response = httpclient.execute(req)) {
                    if (logger.isDebugEnabled())
                        logger.debug("Received response - "+response.getStatusLine());

                    Header[] allHeaders = response.getAllHeaders();
                    if (allHeaders != null && logger.isTraceEnabled()) {
                        for (Header header : allHeaders)
                            logger.trace(header.getName() + ":" + header.getValue());
                    }

                    HttpEntity entity = response.getEntity();
                    if (entity != null && logger.isDebugEnabled())
                        logger.debug("Content-Length: " + entity.getContentLength() + ", " + entity.getContentEncoding() + ", " + entity.getContentType());

                    try {
                        try (InputStream is = entity.getContent()) {
                            while (is.read() >= 0) ;
                        }
                    } catch (NullPointerException | IOException e) {
                        // ignore
                    }
                }
            }
        } catch (IOException e) {
            logger.error(url+", "+map, e);
        }
    }

    public String replace(String text, Map<String, String> map) {
        Pattern pattern = Pattern.compile(Pattern.quote(this.prefix)+"(.+?)"+Pattern.quote(this.suffix), Pattern.MULTILINE);;
        StringBuilder sb = new StringBuilder();
        int start = 0;
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            sb.append(text, start, m.start());
            String name = m.group(1);
            String value = map.get(name);
            if (!StringUtils.isEmpty(value))
                sb.append(value);
            start = m.end();
        }
        sb.append(text, start, text.length());
        return sb.toString();
    }

    public RequestGenerator setPause(String generatorPause) {
        pause = Numbers.longVal(generatorPause, null);
        return this;
    }
}
