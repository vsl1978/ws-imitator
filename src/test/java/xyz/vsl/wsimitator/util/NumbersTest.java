package xyz.vsl.wsimitator.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vsl on 23.05.2016.
 */
public class NumbersTest {

    @Test
    public void testVal1() throws Exception {
        assertEquals(42, Numbers.val(null, 42));
        assertEquals(42, Numbers.val("", 42));
        assertEquals(42, Numbers.val("hello world", 42));
        assertEquals(42, Numbers.val(" 33 ", 42));
        assertEquals(42, Numbers.val("3333333333333333333333333333333333333333333333333333", 42));
        assertEquals(42, Numbers.val("33", 42, 40, 50));
        assertEquals(42, Numbers.val("133", 42, 40, 50));
        assertEquals(33, Numbers.val("33", 42));
        assertEquals(-1, Numbers.val("-1", 42));
    }

    @Test
    public void testVal2() throws Exception {
        assertEquals(424_242_424_242L, Numbers.val(null, 424_242_424_242L));
        assertEquals(424_242_424_242L, Numbers.val("", 424_242_424_242L));
        assertEquals(424_242_424_242L, Numbers.val("hello world", 424_242_424_242L));
        assertEquals(424_242_424_242L, Numbers.val(" 33 ", 424_242_424_242L));
        assertEquals(424_242_424_242L, Numbers.val("3333333333333333333333333333333333333333333333333333", 424_242_424_242L));
        assertEquals(424_242_424_242L, Numbers.val("33", 424_242_424_242L, 40L, 50L));
        assertEquals(424_242_424_242L, Numbers.val("133", 424_242_424_242L, 40L, 50L));
        assertEquals(33, Numbers.val("33", 424_242_424_242L));
        assertEquals(-1, Numbers.val("-1", 424_242_424_242L));
        assertEquals(10123456789L, Numbers.val("10123456789", 424_242_424_242L));
    }

    @Test
    public void testIntegerVal() throws Exception {
        assertNull(Numbers.integerVal(null, null));
        assertNull(Numbers.integerVal("", null));
        assertNull(Numbers.integerVal("qwerty", null));
        assertNull(Numbers.integerVal(" 42 ", null));
        assertEquals(new Integer(42), Numbers.integerVal("42", null));
    }

    @Test
    public void testIntegerVal1() throws Exception {
        assertNull(Numbers.integerVal("10", null, 20, 30));
        assertEquals(new Integer(42), Numbers.integerVal("10", 42, 20, 30));
        assertEquals(new Integer(10), Numbers.integerVal("10", 42, 0, 30));
    }

    @Test
    public void testLongVal() throws Exception {
        assertNull(Numbers.longVal(null, null));
        assertNull(Numbers.longVal("", null));
        assertNull(Numbers.longVal("qwerty", null));
        assertNull(Numbers.longVal(" 42 ", null));
        assertEquals(new Long(42), Numbers.longVal("42", null));
    }

    @Test
    public void testLongVal1() throws Exception {
        assertNull(Numbers.longVal("10", null, 20, 30));
        assertEquals(new Long(42), Numbers.longVal("10", 42L, 20, 30));
        assertEquals(new Long(10), Numbers.longVal("10", 42L, 0, 30));
    }

    @Test
    public void testIntValue() throws Exception {
        assertEquals(-1, Numbers.intValue(null, -1));
        assertEquals(-1, Numbers.intValue("42", -1));
        assertEquals(-1, Numbers.intValue(Boolean.TRUE, -1));
        assertEquals(42, Numbers.intValue(new Integer(42), -1));
        assertEquals(42, Numbers.intValue(new Long(42L), -1));
    }

}