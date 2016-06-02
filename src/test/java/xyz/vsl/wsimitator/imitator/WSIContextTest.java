package xyz.vsl.wsimitator.imitator;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by vsl on 24.05.2016.
 */
public class WSIContextTest {
    private WSIContext ctx;

    @Before
    public void setup() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        ctx = new WSIContext(request, null, new File("."));
    }

    @Test
    public void test1() {
        assertNull(WSIContext.current());
        ctx.setThreadLocal(true);
        assertTrue(ctx == WSIContext.current());
        ctx.setThreadLocal(false);
        assertNull(WSIContext.current());
    }

    @Test
    public void test2() {
        ctx.set("foo", "bar");
        assertEquals("bar", ctx.get("foo"));

        ctx.enter();
        ctx.set("foo", "zoom-zoom");
        ctx.set("one", "two");
        assertEquals("zoom-zoom", ctx.get("foo"));
        assertEquals("two", ctx.get("one"));
        ctx.leave();
        assertEquals("bar", ctx.get("foo"));
        assertNull(ctx.get("one"));

        ctx.enter(false);
        ctx.set("foo", "zoom-zoom");
        ctx.set("one", "two");
        assertEquals("zoom-zoom", ctx.get("foo"));
        assertEquals("two", ctx.get("one"));
        ctx.leave();
        assertEquals("zoom-zoom", ctx.get("foo"));
        assertEquals("two", ctx.get("one"));
    }

}