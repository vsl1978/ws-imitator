package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import xyz.vsl.wsimitator.imitator.NS;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.imitator.expr.Expression;
import xyz.vsl.wsimitator.util.StringUtils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by vsl on 22.05.2016.
 */
public class If implements RequestProcessor {
    private Predicate<WSIContext> test;

    public If(Element element) {
        if (element.hasAttribute("test")) {
            test = expression(element.getAttribute("test"), element);
        } else {
            NamedNodeMap attributes = element.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength() && test == null; i++) {
                    Attr attr = (Attr)attributes.item(i);
                    if (attr == null) continue;
                    String ns = attr.getNamespaceURI();
                    String prefix = attr.getPrefix();
                    String name = attr.getLocalName();
                    String value = attr.getValue();
                    if (NS.IF.equals(ns) && "var".equals(prefix) || NS.IF.equals(ns) && "variable".equals(prefix) || NS.IF_VARIABLE.equals(ns))
                        test = variable(name, value);
                    else if (NS.IF.equals(ns) && "param".equals(prefix) || NS.IF.equals(ns) && "parameter".equals(prefix) || NS.IF_PARAMETER.equals(ns))
                        test = parameter(name, value);
                    else if (NS.IF.equals(ns) && "header".equals(prefix) || NS.IF_HEADER.equals(ns))
                        test = header(name, value);
                }
            }
        }
        if (test == null)
            test = c -> false;
    }

    @Override
    public ResultType process(WSIContext context) {
        if (test.test(context)) {
            context.enter(false);
            return ResultType.enter;
        } else {
            return ResultType.tryNext;
        }
    }

    private Predicate<WSIContext> header(String name, String value) {
        return c -> value.equals(c.getRequest().getHeader(name));
    }

    private Predicate<WSIContext> parameter(String name, String value) {
        return c -> value.equals(c.getRequest().getParameter(name));
    }

    private Predicate<WSIContext> variable(String name, String value) {
        return c -> value.equals(c.get(name));
    }

    private Predicate<WSIContext> expression(String test, Element element) {
        test = StringUtils.trimToNull(test);
        if (test == null)
            return null;
        final Expression expression = new Expression(test, element);
        return c -> {
            Object o = expression.eval(c);
            if (o instanceof Boolean)
                return ((Boolean)o).booleanValue();
            if (o instanceof String)
                return "true".equals(((String) o).trim().toLowerCase());
            if (o instanceof Number)
                return ((Number)o).longValue() != 0L;
            return false;
        };
    }
}
