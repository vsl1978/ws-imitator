package xyz.vsl.wsimitator.imitator;

/**
 * Created by vsl on 13.05.2016.
 */
public interface RequestProcessor {
    ResultType process(WSIContext context);
}
