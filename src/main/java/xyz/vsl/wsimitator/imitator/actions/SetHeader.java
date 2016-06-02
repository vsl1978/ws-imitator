package xyz.vsl.wsimitator.imitator.actions;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.DOM;
import xyz.vsl.wsimitator.util.ObjectUtils;
import xyz.vsl.wsimitator.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
public class SetHeader implements RequestProcessor {
    private final static Pattern PARAM = Pattern.compile("\\{([^}]+)\\}");
    private String name;
    private String value;

    public SetHeader(Element element) {
        name = ObjectUtils.firstNotNull(StringUtils.trimToNull(element.getAttribute("name")), DOM.name(element));
        value = element.hasAttribute("value") ? element.getAttribute("value") : element.getTextContent();
    }

    @Override
    public ResultType process(WSIContext context) {
        context.getResponse().setHeader(name, context.substitute(value, PARAM));
        return ResultType.tryNext;
    }
}
