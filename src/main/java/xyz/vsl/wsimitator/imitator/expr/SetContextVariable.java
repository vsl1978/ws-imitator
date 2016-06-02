package xyz.vsl.wsimitator.imitator.expr;

import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.StringUtils;

/**
 * Created by vsl on 13.05.2016.
 */
public class SetContextVariable implements ContextAction {
    private final String name;
    private final String value;
    private final String defaultValue;

    public SetContextVariable(String name, String value) {
        this.name = name;
        int pos = value.lastIndexOf('|');
        if (pos > 0) {
            defaultValue = value.substring(pos + 1);
            this.value = value.substring(0, pos);
        }
        else {
            this.value = value;
            defaultValue = "";
        }
    }

    @Override
    public void apply(WSIContext context, Attribute attribute) {
        String s = attribute.getValueMatcher().replaceAll(value);
        if (StringUtils.isEmpty(s))
            s = defaultValue;
        context.set(name, s);
    }
}
