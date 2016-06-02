package xyz.vsl.wsimitator.util;

import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DOMTest {
    private final static String DOM1 = "<root>\n<item id=\"1\"><item id=\"1.1\">asd</item><item id=\"1.2\">qwerty</item></item><meta>data</meta></root>";
    private final static String DOM2 = "<root xmlns='urn:test' xmlns:a='urn:test.a' xmlns:e='urn:test.e'>\n<item a:id=\"1\"><e:item id=\"1.1\">asd</e:item><e:item id=\"1.2\">qwerty</e:item></item><meta>data</meta></root>";
    private static Document doc;
    private static Document nsdoc;

    private static Element root;
    private static Element nsroot;

    private static Element i1, i11, i12, m;

    @BeforeClass
    @SneakyThrows(UnsupportedEncodingException.class)
    public static void init() {
        doc = DOM.parse(new ByteArrayInputStream(DOM1.getBytes("UTF-8")));
        nsdoc = DOM.parse(new ByteArrayInputStream(DOM2.getBytes("UTF-8")));
        root = doc.getDocumentElement();
        nsroot = nsdoc.getDocumentElement();
    }

    @Test public void testDOM1Step01() { assertEquals("root", DOM.name(root)); }
    @Test public void testDOM1Step02() { assertNull(DOM.next(root)); }
    @Test public void testDOM1Step04() { i1 = DOM.first(root, null); assertNotNull(i1); }
    @Test public void testDOM1Step05() { assertEquals("item", DOM.name(i1)); }
    @Test public void testDOM1Step06() { assertEquals("1", i1.getAttribute("id")); }
    @Test public void testDOM1Step07() { i11 = DOM.first(i1, "item"); assertNotNull(i11); }
    @Test public void testDOM1Step08() { assertEquals("item", DOM.name(i11)); }
    @Test public void testDOM1Step09() { assertEquals("1.1", i11.getAttribute("id")); }
    @Test public void testDOM1Step10() { assertEquals("text()", DOM.name(i11.getFirstChild())); }
    @Test public void testDOM1Step13() { i12 = DOM.next(i11); assertNotNull(i12); }
    @Test public void testDOM1Step14() { assertEquals("item", DOM.name(i12)); }
    @Test public void testDOM1Step15() { assertEquals("1.2", i12.getAttribute("id")); }
    @Test public void testDOM1Step16() { i12 = DOM.next(i11, "item"); assertNotNull(i12); }
    @Test public void testDOM1Step17() { assertEquals("item", DOM.name(i12)); }
    @Test public void testDOM1Step18() { assertEquals("1.2", i12.getAttribute("id")); }
    @Test public void testDOM1Step19() { assertNull(DOM.next(i12)); }
    @Test public void testDOM1Step20() { assertNull(DOM.next(i11, "abracadabra")); }
    @Test public void testDOM1Step21() { m = DOM.next(i1, null); assertNotNull(m); }
    @Test public void testDOM1Step22() { assertEquals("meta", DOM.name(m)); }



    @Test public void testDOM2Step01() { assertEquals("root", DOM.name(nsroot)); }
    @Test public void testDOM2Step02() { assertNull(DOM.next(nsroot)); }
    @Test public void testDOM2Step04() { i1 = DOM.first(nsroot, null); assertNotNull(i1); }
    @Test public void testDOM2Step05() { assertEquals("item", DOM.name(i1)); }
    @Test public void testDOM2Step06() { assertEquals("1", i1.getAttributeNS("urn:test.a", "id")); }
    @Test public void testDOM2Step07() { i11 = DOM.first(i1, "item"); assertNotNull(i11); }
    @Test public void testDOM2Step08() { assertEquals("item", DOM.name(i11)); }
    @Test public void testDOM2Step09() { assertEquals("1.1", i11.getAttribute("id")); }
    @Test public void testDOM2Step10() { assertEquals("text()", DOM.name(i11.getFirstChild())); }
    @Test public void testDOM2Step13() { i12 = DOM.next(i11); assertNotNull(i12); }
    @Test public void testDOM2Step14() { assertEquals("item", DOM.name(i12)); }
    @Test public void testDOM2Step15() { assertEquals("1.2", i12.getAttribute("id")); }
    @Test public void testDOM2Step16() { i12 = DOM.next(i11, "item"); assertNotNull(i12); }
    @Test public void testDOM2Step17() { assertEquals("item", DOM.name(i12)); }
    @Test public void testDOM2Step18() { assertEquals("1.2", i12.getAttribute("id")); }
    @Test public void testDOM2Step19() { assertNull(DOM.next(i12)); }
    @Test public void testDOM2Step20() { assertNull(DOM.next(i11, "abracadabra")); }
    @Test public void testDOM2Step21() { m = DOM.next(i1, null); assertNotNull(m); }
    @Test public void testDOM2Step22() { assertEquals("meta", DOM.name(m)); }

}
