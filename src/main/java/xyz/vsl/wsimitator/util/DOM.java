package xyz.vsl.wsimitator.util;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * Created by vsl on 13.05.2016.
 */
public class DOM {
    public static String name(Node n) {
        if (n == null) return null;
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            String s = n.getLocalName();
            if (s != null) return s;
            s = n.getNodeName();
            if (s != null) return s;
            return null;
        }
        if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
            String s = n.getLocalName();
            if (s != null) return "@"+s;
            s = n.getNodeName();
            if (s != null) return "@"+s;
            return null;
        }
        if (n.getNodeType() == Node.TEXT_NODE) return "text()";
        if (n.getNodeType() == Node.CDATA_SECTION_NODE) return "#cdata";
        if (n.getNodeType() == Node.DOCUMENT_NODE) return "#document";
        if (n.getNodeType() == Node.COMMENT_NODE) return "#comment";
        if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) return "?"+((ProcessingInstruction)n).getTarget();
        return null;
    }

    public static Element first(Node parent, String name) {
        boolean any = name == null || name.length() == 0;
        if (parent != null)
            for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                if (any || name.equals(name(n))) return (Element)n;
            }
        return null;
    }

    public static Element next(Node current, String name) {
        boolean any = name == null || name.length() == 0;
        if (current != null)
            for (Node n = current.getNextSibling(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                if (any || name.equals(name(n))) return (Element)n;
            }
        return null;
    }

    public static Element next(Element current) {
        if (current == null) return null;
        if (current.getNodeType() != Node.ELEMENT_NODE) return null;
        String name = ObjectUtils.firstNotNull(current.getLocalName(), current.getNodeName());
        return next(current, name);
    }

    @SneakyThrows
    public static Document parse(InputStream is) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(is);
    }

}
