package xyz.vsl.wsimitator.imitator.actions;

import lombok.Getter;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.util.DOM;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vsl on 13.05.2016.
 */
abstract class AbstractAction implements RequestProcessor {
    @Getter private Set<String> children = new HashSet<>();

    protected AbstractAction(Element element) {
        for (Element child = DOM.first(element, null); child != null; child = DOM.next(child, null))
            children.add(DOM.name(child));
    }

}
