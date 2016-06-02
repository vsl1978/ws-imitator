package xyz.vsl.wsimitator.imitator;

import lombok.Getter;
import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by vsl on 13.05.2016.
 */
class Root implements RequestProcessor {
    @Getter private ProcessingNode rootNode = new ProcessingNode().setTest(this);
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public ResultType process(WSIContext context) {
        return process(rootNode, context);
    }

    private ResultType process(ProcessingNode node, WSIContext context) {
        Queue<ProcessingNode> queue = new ArrayDeque<>();
        for (ProcessingNode child = node.getFirstChild(); child != null; child = child.getNext())
            queue.add(child);
        while (!queue.isEmpty()) {
            ProcessingNode n = queue.poll();

            if (logger.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (ProcessingNode p = n; p != null; p = p.getParent()) sb.append("    ");
                sb.append("running ").append(n.getTest().getClass().getName());
                logger.trace(sb.toString());
            }

            ResultType result = n.getTest().process(context);
            if (result == ResultType.enter) {
                try {
                    result = n.getFirstChild() != null ? process(n, context) : ResultType.tryNext;
                } finally {
                    context.leave();
                }
            }
            if (result == ResultType.stop)
                return result;
            if (result == ResultType.error)
                return ResultType.tryNext;
        }
        return ResultType.tryNext;
    }
}
