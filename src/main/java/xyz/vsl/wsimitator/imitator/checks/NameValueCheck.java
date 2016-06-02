package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.*;
import xyz.vsl.wsimitator.imitator.expr.Attribute;
import xyz.vsl.wsimitator.imitator.expr.ContextAction;
import xyz.vsl.wsimitator.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
public abstract class NameValueCheck extends AbstractCheck {

    public NameValueCheck(Element element) {
        super(element);
    }

    @Override
    public ResultType process(WSIContext context) {
        Attribute attribute = getAttribute(context);
        if (attribute == null) {
            if (isInverseCondition()) {
                context.enter();
                return ResultType.enter;
            }
            else
                return ResultType.tryNext;
        }

        boolean testPassed;
        if (StringUtils.isEmpty(getMatches())) {
            testPassed = true;
        } else {
            Pattern p = Pattern.compile(getMatches());
            attribute.setValueMatcher(p.matcher((String) attribute.getValue()));
            testPassed = attribute.getValueMatcher().matches();
        }

        if (testPassed ^ isInverseCondition()) {
            context.enter();
            for (ContextAction action : getActions())
                action.apply(context, attribute);
            return ResultType.enter;
        } else {
            return ResultType.tryNext;
        }
    }

    protected abstract Attribute getAttribute(WSIContext context);

}
