package xyz.vsl.wsimitator.imitator.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by vsl on 22.05.2016.
 */
public class Include implements RequestProcessor {
    private final static Pattern PARAM = Pattern.compile("\\{([^}]+)\\}");
    private String uri;

    public Include(Element element) {
        uri = StringUtils.trimToNull(element.getAttribute("uri"));
        if (StringUtils.isEmpty(uri))
            uri = StringUtils.trimToNull(element.getAttribute("path"));
    }

    @Override
    public ResultType process(WSIContext context) {
        String uri = context.substitute(this.uri, PARAM);
        RequestDispatcher requestDispatcher = context.getRequest().getRequestDispatcher(uri);
        try {
            requestDispatcher.include(context.getRequest(), context.getResponse());
            return ResultType.stop;
        } catch (Exception e) {
            Logger.getLogger(getClass()).error("Include response from  "+ this.uri +" into response for "+context.getRequest().getMethod()+" to "+context.getRequest().getRequestURI(), e);
            context.enter();
            return ResultType.enter;
        }
    }
}
