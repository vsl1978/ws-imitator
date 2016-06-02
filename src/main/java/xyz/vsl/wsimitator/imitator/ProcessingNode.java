package xyz.vsl.wsimitator.imitator;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by vsl on 13.05.2016.
 */
@Accessors(chain = true)
@Data
public class ProcessingNode {
    private RequestProcessor test;
    private ProcessingNode parent;
    private ProcessingNode next;
    private ProcessingNode firstChild;
    private ProcessingNode lastChild;

    public ProcessingNode() {
    }

    public ProcessingNode(RequestProcessor test) {
        this.test = test;
    }

    public ProcessingNode addChild(RequestProcessor test) {
        return addChild(new ProcessingNode(test));
    }

    public ProcessingNode addChild(ProcessingNode node) {
        node.setParent(this);
        if (firstChild == null) {
            firstChild = lastChild = node;
        } else {
            lastChild.setNext(node);
            lastChild = node;
        }
        return this;
    }
}
