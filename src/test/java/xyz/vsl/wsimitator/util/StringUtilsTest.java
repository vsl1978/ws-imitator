package xyz.vsl.wsimitator.util;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by vsl on 23.05.2016.
 */
public class StringUtilsTest {

    @Test public void testNullStringIsEmpty() { assertTrue(StringUtils.isEmpty(null)); }
    @Test public void testEmptyStringIsEmpty() { assertTrue(StringUtils.isEmpty("")); }
    @Test public void testWhitespaceStringIsEmpty() { assertTrue(StringUtils.isEmpty(" \n \t ")); }
    @Test public void testWhitespacedStringIsNotEmpty() { assertFalse(StringUtils.isEmpty(" \n42\t ")); }
    @Test public void test42IsNotEmpty() { assertFalse(StringUtils.isEmpty("42")); }
    @Test public void testNonLatinStringIsNotEmpty() { assertFalse(StringUtils.isEmpty(" бокрёнок ")); }


    @Test public void testLowercasedNullIsNull() { assertNull(StringUtils.nullSafeLowerCase(null)); }
    @Test public void testLowercasedEmptyString() { assertEquals("", StringUtils.nullSafeLowerCase("")); }
    @Test public void testLowercasedString() {
        assertEquals(
                "  beware the jabberwock, my son!\nthe jaws that bite, the claws that catch!\nа в глуще рымит исполин —\nзлопастный брандашмыг  ",
                StringUtils.nullSafeLowerCase("  Beware the Jabberwock, my son!\nThe jaws that bite, the claws that catch!\nА в глуще рымит исполин —\nЗлопастный Брандашмыг  ")
        );
    }

    @Test public void trimNull() { assertNull(StringUtils.trimToNull(null)); }
    @Test public void trimEmptyString() { assertNull(StringUtils.trimToNull("")); }
    @Test public void trimWhitespaceString() { assertNull(StringUtils.trimToNull("  \n \t  ")); }
    @Test public void trimWhitespacesString() { assertEquals("42   42", StringUtils.trimToNull("  \n 42   42\t  ")); }

    @Test public void substringAfterNull() { assertNull(StringUtils.nullSafeSubstringAfter(null, ':')); }
    @Test public void substringAfter404() { assertEquals(null, StringUtils.nullSafeSubstringAfter("test", ':')); }
    @Test public void substringAfterLast() { assertEquals("", StringUtils.nullSafeSubstringAfter("test:", ':')); }
    @Test public void substringAfterFirst() { assertEquals("test", StringUtils.nullSafeSubstringAfter(":test", ':')); }
    @Test public void substringAfterMiddle() { assertEquals("something", StringUtils.nullSafeSubstringAfter("test:something", ':')); }
    @Test public void substringAfterFirstOf() { assertEquals("something:boring", StringUtils.nullSafeSubstringAfter("test:something:boring", ':')); }

    @Test
    public void testReplace() throws Exception {
        assertNull(StringUtils.replace(null, null, null));
        assertNull(StringUtils.replace("", null, null));
        assertNull(StringUtils.replace("    ", null, null));
        assertNull(StringUtils.replace(null, Pattern.compile("\\{(.+?)\\}"), null));
        assertNull(StringUtils.replace("", Pattern.compile("\\{(.+?)\\}"), null));
        assertNull(StringUtils.replace("    ", Pattern.compile("\\{(.+?)\\}"), null));
        assertEquals("", StringUtils.replace("{foo}{bar}", Pattern.compile("\\{(.+?)\\}"), null));
        assertEquals("42", StringUtils.replace("4{foo}2{bar}", Pattern.compile("\\{(.+?)\\}"), null));
        assertEquals("4foo2bar", StringUtils.replace("4{foo}2{bar}", Pattern.compile("\\{(.+?)\\}"), s -> s));
        assertEquals("4FOO2BAR", StringUtils.replace("4{foo}2{bar}", Pattern.compile("\\{(.+?)\\}"), s -> s.toUpperCase()));
    }

    @Test
    public void testRemoveSpecialCharsFromPathElement() throws Exception {
        assertEquals("", StringUtils.removeSpecialCharsFromPathElement(null));
        assertEquals("", StringUtils.removeSpecialCharsFromPathElement(""));
        assertEquals("", StringUtils.removeSpecialCharsFromPathElement("\u0020\t\u0020"));
        assertEquals(",  ,", StringUtils.removeSpecialCharsFromPathElement(",\u0020\t\u0020,"));
        assertEquals("test", StringUtils.removeSpecialCharsFromPathElement("test*/\\?"));
        assertEquals("", StringUtils.removeSpecialCharsFromPathElement("*.?.:"));
        assertEquals("a..b", StringUtils.removeSpecialCharsFromPathElement("a..b"));
    }
}