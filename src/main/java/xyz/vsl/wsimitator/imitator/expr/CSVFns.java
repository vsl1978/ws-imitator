package xyz.vsl.wsimitator.imitator.expr;

import org.apache.commons.csv.CSVRecord;
import xyz.vsl.wsimitator.imitator.WSIContext;

import java.util.Map;

/**
 * Created by vsl on 22.05.2016.
 */
public class CSVFns {
    public static final String CSV_HEADERS_MAP = "urn:csv:headers:map";
    public static final String CSV_ROW = "urn:csv:row:current";

    public String column(String name) {
        WSIContext context = WSIContext.current();
        Map<String, Integer> map = context.getParameter(CSV_HEADERS_MAP);
        CSVRecord row = context.getParameter(CSV_ROW);
        if (map == null || row == null)
            return "";
        Integer idx = map.get(name);
        if (idx == null || idx < 0 || idx >= row.size())
            return "";
        String s = row.get(idx);
        return s == null ? "" : s;
    }
}
