package xyz.vsl.wsimitator.imitator;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by vsl on 25.05.2016.
 */
public class ProcessingTreeTest {
    private WSIContext ctx;

    @Before
    public void setup() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        ctx = new WSIContext(request, null, new File("."));
    }

    @Test
    public void testEmptyTree() {
        Root root = new Root();
        assertEquals(ResultType.tryNext, root.process(ctx));
    }

    @Test
    public void test1() {
        Root root = new Root();
        root.getRootNode().addChild(c -> ResultType.enter);
        assertEquals(ResultType.tryNext, root.process(ctx));
    }

    @Test
    public void test2() {
        Root root = new Root();
        root.getRootNode().addChild(c -> ResultType.stop);
        assertEquals(ResultType.stop, root.process(ctx));
    }

}