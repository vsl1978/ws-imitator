package xyz.vsl.wsimitator.imitator.expr;

import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.imitator.expr.Attribute;

/**
 * Created by vsl on 13.05.2016.
 */
public interface ContextAction {
    public void apply(WSIContext context, Attribute attribute);
}
