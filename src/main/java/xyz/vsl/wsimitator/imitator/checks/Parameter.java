package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.*;
import xyz.vsl.wsimitator.imitator.expr.Attribute;
import xyz.vsl.wsimitator.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by vsl on 13.05.2016.
 */
public class Parameter extends NameValueCheck {
    public Parameter(Element element) {
        super(element);
    }

    protected Attribute getAttribute(WSIContext context) {
        Map<String, String[]> map = context.getRequest().getParameterMap();
        if (getNamePattern() != null) {
            for (String name : map.keySet()) {
                Matcher m = getNamePattern().matcher(name);
                if (m.matches())
                    return new Attribute().setName(name).setNameMatcher(m).setObject(context.getRequest()).setValue(map.get(name));
            }
        }
        else if (!StringUtils.isEmpty(getName())) {
            if (map.containsKey(getName()))
                return new Attribute().setName(getName()).setNameMatcher(dummy.matcher(getName())).setObject(context.getRequest()).setValue(map.get(getName()));
        }
        return null;
    }
}
