package xyz.vsl.wsimitator.imitator.actions;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.imitator.expr.CSVFns;
import xyz.vsl.wsimitator.imitator.expr.Expression;
import xyz.vsl.wsimitator.util.Pair;
import xyz.vsl.wsimitator.util.StringUtils;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by vsl on 22.05.2016.
 */
public class CSVLookup implements RequestProcessor {
    private final static Pattern PARAM = Pattern.compile("\\{([^}]+)\\}");
    private String newVariablesPrefix = "";
    private String separator = ";";
    private String charsets = "Cp1251";
    private String fileNameTemplate;
    private Expression expression;
    private Logger logger;

    public CSVLookup(Element element) {
        fileNameTemplate = element.getAttribute("file");
        expression = new Expression(element.getAttribute("lookup"), element);
        String charset = StringUtils.trimToNull(element.getAttribute("charset"));
        String separator = StringUtils.trimToNull(element.getAttribute("separator"));
        if (charset != null)
            this.charsets = charset;
        if (separator != null)
            this.separator = separator;
        this.newVariablesPrefix = element.getAttribute("prefix");
    }

    @Override
    public ResultType process(WSIContext context) {
        try (CSV data = getData(context)) {
            context.setParameter(CSVFns.CSV_HEADERS_MAP, data.columns);
            for (CSVRecord record : data.records) {
                context.setParameter(CSVFns.CSV_ROW, record);
                Object o = expression.eval(context);
                boolean found = (o instanceof Boolean) && ((Boolean)o).booleanValue() || "true".equals(o);
                if (found) {
                    context.enter();
                    for (Map.Entry<String, Integer> entry : data.columns.entrySet())
                        context.set(newVariablesPrefix + entry.getKey(), record.get(entry.getValue()));
                    return ResultType.enter;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ResultType.tryNext;
    }

    protected static class CSV implements AutoCloseable {
        BufferedReader reader;
        Map<String, Integer> columns = new HashMap<>();
        Iterable<CSVRecord> records;

        @Override
        public void close() throws Exception {
            if (reader != null)
                reader.close();
        }
    }

    protected CSV getData(WSIContext context) {
        File file = new File(context.getBaseDirectory(), context.substitute(fileNameTemplate, PARAM));
        CSV csv = new CSV();
        try {
            csv.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsets));
            String headerString = csv.reader.readLine();
            CSVFormat csvFormat = CSVFormat.EXCEL.withDelimiter(separator.charAt(0)).withSkipHeaderRecord(false).withHeader((headerString.replace("\"", "")).split(separator.substring(0, 1)));
            csv.records = csvFormat.parse(csv.reader);
            String[] header = csvFormat.getHeader();
            for (int i = 0; i < header.length; i++)
                csv.columns.put(header[i], i);
        } catch (IOException e) {
            if (csv.reader != null) {
                try {
                    csv.reader.close();
                } catch (IOException e1) {
                    logger.error(file.toString(), e1);
                }
                csv.reader = null;
                csv.records = Collections.emptyList();
            }
        }
        return csv;
    }
}
