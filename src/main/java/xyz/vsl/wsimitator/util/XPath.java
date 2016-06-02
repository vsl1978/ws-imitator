package xyz.vsl.wsimitator.util;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPath {
    private Document doc;
    private javax.xml.xpath.XPath xp;
    private NSPrefixResolver resolver;
    private Node contextNode;

    public XPath(Document doc, Element context) {
        if (doc == null)
            throw new NullPointerException("XPath(null)");
        this.doc = doc;
        xp = XPathFactory.newInstance().newXPath();
        resolver = new NSPrefixResolver(xp.getNamespaceContext(), doc, context);
        xp.setNamespaceContext(resolver);
        contextNode = context;
    }

    public XPath(Document doc) {
        this(doc, null);
    }

    public XPath ns(String prefix, String ns) {
        assert prefix != null;
        resolver.addNamespace(prefix, ns != null ? ns : resolver.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
        return this;
    }

    public XPath ns(String defaultPrefix) {
        assert defaultPrefix != null;
        Element root = doc.getDocumentElement();
        return ns(defaultPrefix, root != null ? root.getNamespaceURI() : null);
    }

    private static class NSPrefixResolver implements NamespaceContext {
        private NamespaceContext defaultContext;
        private Map<String, Set<String>> ns2p;
        private Map<String, String> p2ns;

        private static List<String> XML_NS_PREFIX   = new ArrayList<String>(1);
        private static List<String> XMLNS_ATTRIBUTE = new ArrayList<String>(1);

        static {
            XML_NS_PREFIX.add(XMLConstants.XML_NS_PREFIX);
            XMLNS_ATTRIBUTE.add(XMLConstants.XMLNS_ATTRIBUTE);
        }

        public NSPrefixResolver(NamespaceContext defaultContext, NSPrefixResolver base) {
            this.ns2p = base.ns2p;
            this.p2ns = base.p2ns;
            this.defaultContext = defaultContext;
        }

        public NSPrefixResolver(NamespaceContext defaultContext, Document target, Element current) {
            this.defaultContext = defaultContext;
            ns2p = new HashMap<String, Set<String>>();
            p2ns = new HashMap<String, String>();
            while (current != null) {
                NamedNodeMap attrs = current.getAttributes();
                if (attrs != null)
                    for (int i = 0; i < attrs.getLength(); i++) {
                        Node a = attrs.item(i);
                        if (a == null) continue;
                        if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) continue;
                        String p = a.getLocalName();
                        if (p == null || p.length() == 0) /* hmm... */ continue;
                        String ns = a.getTextContent();
                        if (ns.length() == 0) continue;
                        if (p2ns.containsKey(p)) continue;
                        p2ns.put(p, ns);
                        Set<String> set = ns2p.get(ns);
                        if (set == null) ns2p.put(ns, set = new HashSet<String>());
                        set.add(p);
                    }
                Node n = current.getParentNode();
                current = n != null && n.getNodeType() == Node.ELEMENT_NODE ? (Element)n : null;
            }
            if (target != null && target.getDocumentElement() != null) {
                Element root = target.getDocumentElement();
                String rootPrefix = root.getPrefix();
                if (rootPrefix != null && rootPrefix.length() == 0)
                    rootPrefix = null;
                String defaultNamespace = root.getNamespaceURI();
                NamedNodeMap attrs = root.getAttributes();
                if (attrs != null)
                    for (int i = 0; i < attrs.getLength(); i++) {
                        Node a = attrs.item(i);
                        if (a == null) continue;
                        if (rootPrefix != null && XMLConstants.XMLNS_ATTRIBUTE.equals(a.getNodeName())) {
                            rootPrefix = null;
                            defaultNamespace = a.getTextContent();
                        }
                        if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) continue;
                        String p = a.getLocalName();
                        if (p == null || p.length() == 0) /* hmm... */ continue;
                        String ns = a.getTextContent();
                        if (ns.length() == 0) continue;
                        if (p2ns.containsKey(p)) continue;
                        p2ns.put(p, ns);
                        Set<String> set = ns2p.get(ns);
                        if (set == null) ns2p.put(ns, set = new HashSet<String>());
                        set.add(p);
                    }
                if (rootPrefix == null) {
                    p2ns.put(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespace);
                    Set<String> set = ns2p.get(defaultNamespace);
                    if (set == null) ns2p.put(defaultNamespace, set = new HashSet<String>());
                    set.add(XMLConstants.DEFAULT_NS_PREFIX);
                }
            }
        }

        public void addNamespace(String prefix, String nsuri) {
            if (nsuri != null && nsuri.equals(p2ns.get(prefix)))
                return;
            p2ns.put(prefix, nsuri);
            Set<String> set = ns2p.get(nsuri);
            if (set == null) ns2p.put(nsuri, set = new HashSet<String>());
            set.add(prefix);
        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null)
                throw new IllegalArgumentException();
            if (XMLConstants.XML_NS_PREFIX.equals(prefix))
                return XMLConstants.XML_NS_URI;
            if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            if (p2ns.containsKey(prefix))
                return p2ns.get(prefix);
            return defaultContext != null ? defaultContext.getNamespaceURI(prefix) : null;
        }

        public String getPrefix(String namespaceURI) {
            if (namespaceURI == null)
                throw new IllegalArgumentException();
            if (XMLConstants.XML_NS_URI.equals(namespaceURI))
                return XMLConstants.XML_NS_PREFIX;
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI))
                return XMLConstants.XMLNS_ATTRIBUTE;

            Set<String> set = ns2p.get(namespaceURI);
            if (set != null && !set.isEmpty())
                return set.iterator().next();
            return defaultContext != null ? defaultContext.getPrefix(namespaceURI) : null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            if (namespaceURI == null)
                throw new IllegalArgumentException();
            if (XMLConstants.XML_NS_URI.equals(namespaceURI))
                return XML_NS_PREFIX.iterator();
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI))
                return XMLNS_ATTRIBUTE.iterator();

            if (ns2p.containsKey(namespaceURI))
                return ns2p.get(namespaceURI).iterator();
            return defaultContext != null ? defaultContext.getPrefixes(namespaceURI) : Collections.EMPTY_LIST.iterator();
        }

    }

    public Node get(String expression) throws XPathExpressionException {
        NodeList nodes = find(expression);
        final int len = nodes != null ? nodes.getLength() : 0;
        return len > 0 ? nodes.item(0) : null;
    }

    public NodeList find(String expression) throws XPathExpressionException {
        return (NodeList) xp.evaluate(expression, contextNode, XPathConstants.NODESET);
    }

    public String evaluateString(String expression) throws XPathExpressionException {
        Object o = xp.evaluate(expression, contextNode, XPathConstants.STRING);
        return o instanceof String ? (String)o : "";
    }

    public boolean evaluateBoolean(String expression) throws XPathExpressionException {
        Object o = xp.evaluate(expression, contextNode, XPathConstants.BOOLEAN);
        return (o instanceof Boolean) && ((Boolean)o).booleanValue();
    }

    public Number evaluateNumber(String expression) throws XPathExpressionException {
        Object o = xp.evaluate(expression, contextNode, XPathConstants.NUMBER);
        return o instanceof Number ? (Number)o : null;
    }

    public XPath setContextNode(Node e) {
        contextNode = e;
        return this;
    }

}
