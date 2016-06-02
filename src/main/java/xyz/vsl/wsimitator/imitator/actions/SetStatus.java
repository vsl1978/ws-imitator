package xyz.vsl.wsimitator.imitator.actions;

import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.DOM;
import xyz.vsl.wsimitator.util.Numbers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vsl on 14.05.2016.
 */
public class SetStatus implements RequestProcessor {
    private int status = 200;

    public SetStatus(Element config) {
        Matcher matcher = Pattern.compile("^[a-zA-Z_.-]+\\.([1-9][0-9][0-9])$").matcher(DOM.name(config));
        if (matcher.matches()) {
            status = Numbers.val(matcher.group(1), -1, 100, 999);
        } else {
            status = Numbers.val(config.getAttribute("code"), -1, 100, 999);
        }
    }

    @Override
    public ResultType process(WSIContext context) {
        if (status > 0) {
            context.getResponse().setStatus(status);
            return ResultType.tryNext;
        } else {
            return ResultType.error;
        }
    }
}
