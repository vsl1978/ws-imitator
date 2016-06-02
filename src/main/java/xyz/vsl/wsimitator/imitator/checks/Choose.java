package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.DOM;

import java.util.UUID;

/**
 * Created by vsl on 22.05.2016.
 */
public class Choose implements RequestProcessor {

    public Choose(Element element) {
        element.setAttribute("uuid", UUID.randomUUID().toString());
        for (Element child = DOM.first(element, null), next = null; child != null; child = next) {
            next = DOM.next(child, null);
            String name = DOM.name(child);
            if (!"when".equals(name) && !"otherwise".equals(name))
                element.removeChild(child);
        }
    }

    @Override
    public ResultType process(WSIContext context) {
        context.enter(false);
        return ResultType.enter;
    }
}
