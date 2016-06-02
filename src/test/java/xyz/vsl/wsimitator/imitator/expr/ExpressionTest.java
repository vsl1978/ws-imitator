package xyz.vsl.wsimitator.imitator.expr;

import org.junit.Before;
import org.junit.Test;
import xyz.vsl.wsimitator.imitator.WSIContext;
import xyz.vsl.wsimitator.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by vsl on 25.05.2016.
 */
public class ExpressionTest {
    private WSIContext ctx;

    @Before
    public void setup() {
        Hashtable<String, String> headers = new Hashtable<>();
        headers.put("content-type", "text/xml");
        headers.put("soapaction", "\"getAnswer\"");
        Map<String, String[]> params = new HashMap<>();
        params.put("id", new String[] {"42"});
        params.put("flag", new String[0]);
        params.put("foo", new String[] {"bar", "qux"});
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeaderNames()).then(invocationOnMock -> headers.keys());
        when(request.getHeader(anyString())).then(invocationOnMock -> headers.get(StringUtils.nullSafeLowerCase((String) invocationOnMock.getArguments()[0])));
        when(request.getHeaders(anyString())).then(invocationOnMock -> {
            String key = StringUtils.nullSafeLowerCase((String) invocationOnMock.getArguments()[0]);
            if (!headers.containsKey(key))
                return Collections.emptyEnumeration();
            return new Vector<String>(Collections.singletonList(headers.get(key))).elements();
        });
        when(request.getParameter(anyString())).then(invocationOnMock -> {
            String[] values = params.get((String)invocationOnMock.getArguments()[0]);
            return values != null && values.length > 0 ? values[0] : null;
        });
        when(request.getParameterValues(anyString())).then(invocationOnMock -> params.get((String)invocationOnMock.getArguments()[0]));
        when(request.getParameterNames()).then(invocationOnMock -> new Vector<>(params.keySet()).elements());
        when(request.getParameterMap()).thenReturn(params);

        ctx = new WSIContext(request, null, new File("."));
    }

    @Test
    public void testHttpHeaders() {
        assertEquals("text/xml", new Expression("http:header('Content-Type')", null).eval(ctx));
        assertEquals("\"getAnswer\"", new Expression("http:header('SOAPAction')", null).eval(ctx));
        assertTrue(StringUtils.isEmpty((String) new Expression("http:header('ThereAreNoSuchHeader')", null).eval(ctx)));
        assertTrue((Boolean)new Expression("http:hasHeader('soapAction')", null).eval(ctx));
        assertTrue((Boolean) new Expression("http:hasHeader('content-type')", null).eval(ctx));
        assertFalse((Boolean) new Expression("http:hasHeader('content-type-')", null).eval(ctx));
        assertEquals("content-type", new Expression("http:headerName('content-.*')", null).eval(ctx));
    }

    @Test
    public void testHttpParameters() {
        assertEquals("42", new Expression("http:parameter('id')", null).eval(ctx));
        assertEquals("bar", new Expression("http:parameter('foo')", null).eval(ctx));
        assertTrue((Boolean)new Expression("http:hasParameter('id')", null).eval(ctx));
        assertTrue((Boolean)new Expression("http:hasParameter('foo')", null).eval(ctx));
        assertTrue((Boolean)new Expression("http:hasParameter('flag')", null).eval(ctx));
        assertFalse((Boolean)new Expression("http:hasParameter('ID')", null).eval(ctx));
        assertFalse((Boolean)new Expression("http:hasParameter('XXX')", null).eval(ctx));
        assertTrue(StringUtils.isEmpty((String)new Expression("http:parameter('flag')", null).eval(ctx)));
        assertTrue(StringUtils.isEmpty((String)new Expression("http:parameter('XXX')", null).eval(ctx)));
    }

    @Test
    public void testRE() {
        assertEquals("bar<-foo", new Expression("re:replace('when foo then bar', '.*(foo).*(bar).*', '$2<-$1')", null).eval(ctx));
        assertTrue((Boolean)new Expression("re:matches('http://some/namespace/getFoo', '.*/getFoo')", null).eval(ctx));
        assertFalse((Boolean)new Expression("re:matches('http://some/namespace/getFoo', '.*/getFooBar')", null).eval(ctx));
        assertFalse((Boolean)new Expression("re:matches('http://some/namespace/getFoo', '.*/getBar')", null).eval(ctx));
    }

}