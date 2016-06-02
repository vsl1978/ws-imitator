package xyz.vsl.wsimitator.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vsl on 23.05.2016.
 */
public class ObjectUtilsTest {

    @Test public void testFirstNotNull1() throws Exception { assertNull(ObjectUtils.firstNotNull(null, null)); }
    @Test public void testFirstNotNull2() throws Exception { assertTrue(ObjectUtils.firstNotNull(null, "foo") == "foo"); }
    @Test public void testFirstNotNull3() throws Exception { assertTrue(ObjectUtils.firstNotNull("foo", null) == "foo"); }
    @Test public void testFirstNotNull4() throws Exception { assertTrue(ObjectUtils.firstNotNull("bar", "foo") == "bar"); }
    @Test public void testFirstNotNull5() throws Exception { assertNull(ObjectUtils.firstNotNull(null, null, null)); }
    @Test public void testFirstNotNull6() throws Exception { assertTrue(ObjectUtils.firstNotNull(null, "bar", "foo") == "bar"); }
    @Test public void testFirstNotNull7() throws Exception { assertTrue(ObjectUtils.firstNotNull("bar", null, "foo") == "bar"); }
    @Test public void testFirstNotNull8() throws Exception { assertTrue(ObjectUtils.firstNotNull("foo", "bar", null) == "foo"); }
}