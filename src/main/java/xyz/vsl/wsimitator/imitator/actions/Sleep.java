package xyz.vsl.wsimitator.imitator.actions;

import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.Numbers;

/**
 * Created by vsl on 13.05.2016.
 */
public class Sleep implements RequestProcessor {
    private Long fixed;
    private Long min;
    private Long max;
    private Logger logger = Logger.getLogger(getClass());

    public Sleep(Element element) {
        min = Numbers.longVal(element.getAttribute("min"), null, 1L, 1 * 60 * 60 * 1000L);
        max = Numbers.longVal(element.getAttribute("max"), null, 1L, 1 * 60 * 60 * 1000L);
        fixed = Numbers.longVal(element.getAttribute("fixed"), null, 1L, 1 * 60 * 60 * 1000L);

        if (min != null && max != null) {
            if (min.longValue() > max.longValue()) {
                Long t = min;
                min = max;
                max = t;
            }
        }
        else if (min != null && max == null) {
            max = Math.min(2 * min, 1 * 60 * 60 * 1000L);
        }
        else if (max != null && min == null) {
            min = 1L;
        }
    }

    @Override
    @SneakyThrows
    public ResultType process(WSIContext context) {
        if (!context.isFeatureEnabled("xyz.vsl.WSImitator.sleep"))
            return ResultType.tryNext;

        context.enter();
        long period = 0L;
        if (fixed != null) {
            period = fixed.longValue();
        }
        else if (min != null && max != null) {
            period = (long) (min + Math.random() * (max - min));
        }
        long now = System.currentTimeMillis();
        long start = context.getRequestStart();
        if (now > start)
            period = period - (now - start);
        if (period > 0) {
            if (logger.isDebugEnabled())
                logger.debug("Sleep for "+period+"msec");
            Thread.sleep(period);
        }
        return ResultType.enter;
    }

}
