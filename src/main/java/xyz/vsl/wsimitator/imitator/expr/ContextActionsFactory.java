package xyz.vsl.wsimitator.imitator.expr;

import xyz.vsl.wsimitator.imitator.NS;
import xyz.vsl.wsimitator.imitator.expr.ContextAction;
import xyz.vsl.wsimitator.imitator.expr.SetContextVariable;

/**
 * Created by vsl on 13.05.2016.
 */
public class ContextActionsFactory {

    public static ContextAction build(String ns, String prefix, String name, String value) {
        if (NS.SET_VARIABLE_USING_VALUE_REGEX.equals(ns)) {
            return new SetContextVariable(name, value);
        }
        return null;
    }
}
