package xyz.vsl.wsimitator.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by vsl on 17.05.2016.
 */
public class HTTPHeaders {
    protected List<Pair<String, String>> headers = new ArrayList<>();

    public void remove(String name) {
        name = StringUtils.nullSafeLowerCase(name);
        for (Iterator<Pair<String, String>> it = headers.iterator(); it.hasNext(); ) {
            Pair<String, String> header = it.next();
            if (Objects.equals(name, StringUtils.nullSafeLowerCase(header.first)))
                it.remove();
        }
    }

    public List<String> headers(String name) {
        List<String> list = new ArrayList<>();
        name = StringUtils.nullSafeLowerCase(name);
        for (Pair<String, String> header : headers) {
            if (Objects.equals(name, StringUtils.nullSafeLowerCase(header.first)) && !StringUtils.isEmpty(header.second))
                list.add(header.second);
        }
        return list;
    }

    public String header(String name) {
        List<String> values = headers(name);
        return values.isEmpty() ? null : values.get(0);
    }

    public List<Pair<String, String>> headers() {
        return headers;
    }

    public void add(String name, String value) {
        headers.add(Pair.of(name, value));
    }
}
