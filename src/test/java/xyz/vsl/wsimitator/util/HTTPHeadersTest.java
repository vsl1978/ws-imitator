package xyz.vsl.wsimitator.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vsl on 23.05.2016.
 */
public class HTTPHeadersTest {
    private HTTPHeaders headers;

    @Before
    public void setup() {
        headers = new HTTPHeaders();

        headers.add("Content-Type", "application/octet-stream");
        headers.add("Test", "1");
        headers.add("Test", "2");
        headers.add("X-OMG", "42");
        headers.add("x-doctor", "10");
    }

    @Test
    public void testRemoveFromEmpty() throws Exception {
        headers = new HTTPHeaders();
        headers.remove(null);
        assertNotNull(headers.headers());
        assertEquals(0, headers.headers().size());
        headers.remove("content-type");
        assertNotNull(headers.headers());
        assertEquals(0, headers.headers().size());
    }

    @Test
    public void testRemove() throws Exception {
        int size = headers.headers().size();

        headers.remove(null);
        assertNotNull(headers.headers());
        assertEquals("remove null", size, headers.headers().size());

        headers.remove("content-length");
        assertNotNull(headers.headers());
        assertEquals("remove non-existing header", size, headers.headers().size());

        headers.remove("test");
        assertNotNull(headers.headers());
        assertEquals("remove multi-value header", size - 2, headers.headers().size());

        size = headers.headers().size();
        headers.remove("content-type");
        assertNotNull(headers.headers());
        assertEquals("remove head", size - 1, headers.headers().size());

        size = headers.headers().size();
        headers.remove("x-doctor");
        assertNotNull(headers.headers());
        assertEquals("remove tail", size - 1, headers.headers().size());

        size = headers.headers().size();
        headers.remove("x-omg");
        assertNotNull(headers.headers());
        assertEquals("remove last", size - 1, headers.headers().size());
    }

    @Test
    public void testHeaders() throws Exception {
        assertEquals(0, headers.headers("HOST").size());
        assertEquals(1, headers.headers("x-omg").size());
        assertEquals("42", headers.headers("x-omg").get(0));
        assertArrayEquals(new String[] {"1", "2"}, headers.headers("TEST").toArray());
    }

    @Test
    public void testHeaders1() throws Exception {
        assertNotNull(new HTTPHeaders().headers());
    }

    @Test
    public void testHeader() throws Exception {
        assertNull(headers.header("abracadabra"));
        assertEquals("10", headers.header("X-doctor"));
        assertEquals("1", headers.header("tEsT"));
    }
}