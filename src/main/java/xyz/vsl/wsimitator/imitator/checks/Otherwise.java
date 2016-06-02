package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;

/**
 * Created by vsl on 22.05.2016.
 */
public class Otherwise implements RequestProcessor {
    private String uuid;

    public Otherwise(Element element) {
        Element choose = (Element) element.getParentNode();
        uuid = choose.getAttribute("uuid");
    }

    @Override
    public ResultType process(WSIContext context) {
        Object o = context.getParameter(uuid);
        if (o != null)
            return ResultType.tryNext;
        context.setParameter(uuid, uuid);
        context.enter(false);
        return ResultType.enter;
    }
}
