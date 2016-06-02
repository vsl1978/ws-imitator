package xyz.vsl.wsimitator.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vsl on 23.05.2016.
 */
public class PairTest {

    @Test
    public void test1() {
        assertNotNull(Pair.of(1, 2));
        assertNotNull(Pair.of(null, 2));
        assertNotNull(Pair.of(1, null));
        assertNotNull(Pair.of(null, null));
        assertNull(Pair.of(null, null).first);
        assertNull(Pair.of(null, null).second);
        assertTrue("foo" == Pair.of("foo", "bar").first);
        assertTrue("bar" == Pair.of("foo", "bar").second);
    }

}