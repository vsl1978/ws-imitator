package xyz.vsl.wsimitator.imitator.expr;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.StringUtils;

/**
 * Created by vsl on 21.05.2016.
 */
public class Variable implements RequestProcessor {
    private String name;
    private Expression expression;
    private String ifEmpty;

    public Variable(Element element) {
        name = StringUtils.trimToNull(element.getAttribute("name"));
        ifEmpty = StringUtils.trimToNull(element.getAttribute(element.hasAttribute("if-empty") ? "if-empty" : element.hasAttribute("ifempty") ? "ifempty" : "ifEmpty"));
        expression = new Expression(element.getTextContent().trim(), element);
    }

    @Override
    public ResultType process(WSIContext context) {
        Object o = expression.eval(context);
        if (o != null) {
            String s = o.toString();
            if (StringUtils.isEmpty(s))
                s = ifEmpty;
            if (!StringUtils.isEmpty(s))
                context.set(name, s);
        }
        return ResultType.tryNext;
    }
}
