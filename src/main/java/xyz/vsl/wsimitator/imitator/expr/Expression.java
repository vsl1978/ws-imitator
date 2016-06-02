package xyz.vsl.wsimitator.imitator.expr;

import org.apache.commons.jexl3.*;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import xyz.vsl.wsimitator.imitator.WSIContext;

import javax.xml.XMLConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vsl on 22.05.2016.
 */
public class Expression {
    private JexlExpression expression;
    private Map<String, String> prefixToNSURI = new HashMap<>();
    private static JexlEngine jexl;

    static {
        Map<String, Object> functions = new HashMap<>();
        functions.put("xpath", new XPathFns());
        functions.put("http", new RequestFns());
        functions.put("re", new RegExFns());
        functions.put("csv", new CSVFns());
        jexl = new JexlBuilder().namespaces(functions).create();
    }

    public Expression(String expression, Element base) {
        this.expression = jexl.createExpression(expression);
        collectNamespaces(base);
    }

    private void collectNamespaces(Element element) {
        while (element != null) {
            NamedNodeMap attrs = element.getAttributes();
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node a = attrs.item(i);
                    if (a == null) continue;
                    if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) continue;
                    String p = a.getLocalName();
                    if (p == null || p.length() == 0) /* hmm... */ continue;
                    String ns = a.getTextContent();
                    prefixToNSURI.putIfAbsent(p, ns);
                }
            }
            Node parent = element.getParentNode();
            if (parent instanceof Element)
                element = (Element)parent;
            else
                break;
        }
    }

    public Object eval(WSIContext context) {
        JexlContext jc = new MapContext();
        for (Map.Entry<String, String> entry : context.context().entrySet())
            jc.set(entry.getKey(), entry.getValue());
        context.setThreadLocal(true).setParameter("NS", prefixToNSURI);
        try {
            return expression.evaluate(jc);
        } finally {
            context.setThreadLocal(false).removeParameter("NS");
        }
    }
}
