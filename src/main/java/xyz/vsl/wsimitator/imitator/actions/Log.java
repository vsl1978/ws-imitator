package xyz.vsl.wsimitator.imitator.actions;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;

import java.util.regex.Pattern;

/**
 * Created by vsl on 22.05.2016.
 */
public class Log implements RequestProcessor {
    private Level logLevel;
    private String message;
    private Logger logger = Logger.getLogger(getClass());
    private Pattern pattern = Pattern.compile("\\{([^}]+)\\}");

    public Log(Element element) {
        logLevel = element.hasAttribute("level") ? Level.toLevel(element.getAttribute("level"), Level.INFO) : Level.INFO;
        message = element.getTextContent().trim();
    }

    @Override
    public ResultType process(WSIContext context) {
        logger.log(logLevel, context.substitute(message, pattern));
        return ResultType.tryNext;
    }
}
