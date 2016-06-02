package xyz.vsl.wsimitator.imitator.checks;

import lombok.Getter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import xyz.vsl.wsimitator.imitator.expr.ContextAction;
import xyz.vsl.wsimitator.imitator.expr.ContextActionsFactory;
import xyz.vsl.wsimitator.imitator.NS;
import xyz.vsl.wsimitator.imitator.RequestProcessor;
import xyz.vsl.wsimitator.util.ObjectUtils;
import xyz.vsl.wsimitator.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by vsl on 13.05.2016.
 */
abstract class AbstractCheck implements RequestProcessor {
    protected Pattern dummy = Pattern.compile("^.*$");
    @Getter
    private String name;
    @Getter
    private Pattern namePattern;

    @Getter
    private boolean inverseCondition;

    @Getter
    private String matches;

    @Getter
    private List<ContextAction> actions = new ArrayList<>();

    protected AbstractCheck(Element element) {
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String ns = a.getNamespaceURI();
            String name = ObjectUtils.firstNotNull(a.getLocalName(), a.getName());
            String prefix = a.getPrefix();
            setup(ns, prefix, name, a.getValue());
        }
    }

    protected void setup(String ns, String prefix, String name, String value) {
        if (StringUtils.isEmpty(ns) && "name".equals(name)) {
            this.name = value;
        }
        else if (NS.REGEX.equals(ns) && "name".equals(name)) {
            this.namePattern = Pattern.compile(value);
        }
        else if ("matches".equals(name)) {
            this.matches = value;
        }
        else if ("not-matches".equals(name)) {
            this.matches = value;
            inverseCondition = !inverseCondition;
        }
        else if ("inverse".equals(name)) {
            if (!"false".equals(value))
                inverseCondition = !inverseCondition;
        }
        else if (!StringUtils.isEmpty(ns)) {
            ContextAction action = ContextActionsFactory.build(ns, prefix, name, value);
            if (action != null)
                actions.add(action);
        }
    }

}
