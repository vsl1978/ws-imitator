package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.StringUtils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by vsl on 16.05.2016.
 */
public class When extends If {
    private String uuid;

    public When(Element element) {
        super(element);
        Element choose = (Element) element.getParentNode();
        uuid = choose.getAttribute("uuid");
    }

    @Override
    public ResultType process(WSIContext context) {
        Object o = context.getParameter(uuid);
        if (o != null)
            return ResultType.tryNext;
        ResultType result = super.process(context);
        if (result == ResultType.enter)
            context.setParameter(uuid, uuid);
        return result;
    }
}
