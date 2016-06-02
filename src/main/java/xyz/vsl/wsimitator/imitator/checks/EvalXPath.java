package xyz.vsl.wsimitator.imitator.checks;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.XPath;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vsl on 13.05.2016.
 */
public class EvalXPath implements RequestProcessor {
    private String xpath;
    private String variable;
    private Map<String, String> prefixToNSURI = new HashMap<>();

    public EvalXPath(Element element) {
        xpath = element.getAttribute("xpath");
        variable = element.getAttribute("into");
        collectNamespaces(element);
    }

    private void collectNamespaces(Element element) {
        NamedNodeMap attrs = element.getAttributes();
        if (attrs == null)
            return;
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            if (a == null) continue;
            if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) continue;
            String p = a.getLocalName();
            if (p == null || p.length() == 0) /* hmm... */ continue;
            String ns = a.getTextContent();
            prefixToNSURI.put(p, ns);
        }
    }

    @Override
    public ResultType process(WSIContext context) {
        Document document = context.getRequestDOM();
        XPath xpath = new XPath(document, document.getDocumentElement());
        for (Map.Entry<String, String> entry : prefixToNSURI.entrySet())
            xpath = xpath.ns(entry.getKey(), entry.getValue());

        try {
            String value = xpath.evaluateString(this.xpath);
            if (value != null)
                context.set(variable, value);
            return ResultType.tryNext;
        } catch (XPathExpressionException e) {
            Logger.getLogger(getClass()).error(xpath, e);
            return ResultType.error;
        }
    }
}
