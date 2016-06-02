package xyz.vsl.wsimitator.imitator.checks;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.*;
import xyz.vsl.wsimitator.imitator.expr.Attribute;
import xyz.vsl.wsimitator.util.StringUtils;

import java.util.Enumeration;
import java.util.regex.Matcher;

/**
 * Created by vsl on 13.05.2016.
 */
public class Header extends NameValueCheck {

    public Header(Element element) {
        super(element);
    }

    protected Attribute getAttribute(WSIContext context) {
        if (getNamePattern() != null) {
            for (Enumeration<String> headerNames = context.getRequest().getHeaderNames(); headerNames.hasMoreElements(); ) {
                String name = headerNames.nextElement();
                Matcher m = getNamePattern().matcher(name);
                if (m.matches())
                    return new Attribute().setName(name).setNameMatcher(m).setObject(context.getRequest()).setValue(context.getRequest().getHeader(name));
            }
        }
        else if (!StringUtils.isEmpty(getName())) {
            String name = getName().trim().toLowerCase();
            for (Enumeration<String> headerNames = context.getRequest().getHeaderNames(); headerNames.hasMoreElements(); ) {
                String hname = headerNames.nextElement();
                if (name.equals(hname.toLowerCase()))
                    return new Attribute().setName(hname).setNameMatcher(dummy.matcher(hname)).setObject(context.getRequest()).setValue(context.getRequest().getHeader(hname));
            }
        }
        return null;
    }
}
