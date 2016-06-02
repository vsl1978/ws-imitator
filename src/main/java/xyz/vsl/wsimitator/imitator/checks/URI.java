package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.*;
import xyz.vsl.wsimitator.imitator.expr.Attribute;
import xyz.vsl.wsimitator.imitator.expr.ContextAction;

import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
public class URI extends AbstractCheck {
    public URI(Element element) {
        super(element);
    }

    @Override
    public ResultType process(WSIContext context) {
        Attribute attribute = getAttribute(context);
        Pattern p = Pattern.compile(getMatches());
        attribute.setValueMatcher(p.matcher((String)attribute.getValue()));

        if (attribute.getValueMatcher().matches() ^ isInverseCondition()) {
            context.enter();
            for (ContextAction action : getActions())
                action.apply(context, attribute);
            return ResultType.enter;
        } else {
            return ResultType.tryNext;
        }
    }

    private Attribute getAttribute(WSIContext context) {
        return new Attribute().setObject(context.getRequest()).setValue(context.getRequest().getRequestURI());
    }
}
