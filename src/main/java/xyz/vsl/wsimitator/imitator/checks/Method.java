package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.DOM;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by vsl on 15.05.2016.
 */
public class Method implements RequestProcessor {
    private Set<String> methods = new HashSet<>();
    private boolean inverse;

    public Method(String name) {
        methods.add(name);
    }

    public Method(Element element) {
        if (element != null && element.hasAttribute("code"))
            methods.addAll(Arrays.asList(element.getAttribute("code").split("[ ,;/|]+")));
        else
            methods.add(DOM.name(element));
        if (element != null)
            inverse = "true".equals(element.getAttribute("inverse"));
    }

    @Override
    public ResultType process(WSIContext context) {
        if (methods.contains(context.getRequest().getMethod()) ^ inverse) {
            context.enter();
            return ResultType.enter;
        } else {
            return ResultType.tryNext;
        }
    }
}
