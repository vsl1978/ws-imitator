package xyz.vsl.wsimitator.imitator;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xyz.vsl.wsimitator.util.DOM;

import java.io.*;

/**
 * Created by vsl on 13.05.2016.
 */
@Accessors(chain = true)
@Data
public class Config {
    @Getter private static volatile Config[] configs = new Config[0];

    private File configFile;
    private File baseDirectory;
    private RequestProcessorsFactory requestProcessorsFactory = new RequestProcessorsFactory();

    private volatile RequestProcessor requestProcessor;

    public Config() {
    }

    public Config(ProcessingNode node) {
        requestProcessor = new Root();
        ProcessingNode rootNode = ((Root) requestProcessor).getRootNode();
        rootNode.setFirstChild(node);
        for (ProcessingNode n = node; n != null; n = n.getNext())
            n.setParent(rootNode.setLastChild(n));
        registerFirst();
    }

    public void parse() {
        try {
            RequestProcessor newRequestProcessor = null;
            try (InputStream is = new FileInputStream(configFile)) {
                Document document = DOM.parse(is);
                newRequestProcessor = parse(document.getDocumentElement(), null);
            }
            boolean newConfig = requestProcessor == null;
            requestProcessor = newRequestProcessor;
            if (newConfig)
                register();
        } catch (IOException ioe) {
            if (requestProcessor == null)
                throw new RuntimeException(ioe);
        }
    }

    private void register() {
        synchronized (Config.class) {
            Config[] temp = new Config[configs.length + 1];
            System.arraycopy(configs, 0, temp, 0, configs.length);
            temp[configs.length] = this;
            configs = temp;
        }
    }

    private void registerFirst() {
        synchronized (Config.class) {
            Config[] temp = new Config[configs.length + 1];
            temp[0] = this;
            System.arraycopy(configs, 0, temp, 1, configs.length);
            configs = temp;
        }
    }

    private RequestProcessor parse(Element element, ProcessingNode parent) {
        String name = DOM.name(element);
        RequestProcessor rp = null;
        ProcessingNode node = null;
        if ("ws-imitator-config".equals(name)) {
            if (element.getParentNode() instanceof Element)
                throw new ConfigurationParseException();
            rp = new Root();
            node = ((Root)rp).getRootNode();
        } else {
            rp = requestProcessorsFactory.build(name, element);
            if (rp == null)
                throw new ConfigurationParseException("Unknown configuration element '"+name+"'");
            parent.addChild(node = new ProcessingNode().setTest(rp));
        }
        for (Element child = DOM.first(element, null); child != null; child = DOM.next(child, null))
            parse(child, node);

        return rp;
    }
}
