package xyz.vsl.wsimitator.imitator.expr;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.XPath;

import javax.xml.xpath.XPathExpressionException;
import java.util.Map;

/**
 * Created by vsl on 21.05.2016.
 */
public class XPathFns {

    public String string(String selector) throws XPathExpressionException {
        String value = xpath().evaluateString(selector);
        return value;
    }

    public boolean bool(String selector) throws XPathExpressionException {
        return xpath().evaluateBoolean(selector);
    }

    public Number number(String selector) throws XPathExpressionException {
        return xpath().evaluateNumber(selector);
    }

    private XPath xpath() {
        WSIContext context = WSIContext.current();
        Map<String, String> prefixToNSURI = context.getParameter("NS");
        Document document = context.getRequestDOM();

        XPath xpath = new XPath(document, document.getDocumentElement());
        for (Map.Entry<String, String> entry : prefixToNSURI.entrySet())
            xpath = xpath.ns(entry.getKey(), entry.getValue());

        return xpath;
    }
}
