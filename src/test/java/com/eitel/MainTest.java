package com.eitel;
import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;

public class MainTest {

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateWithWrongFormat() {
        Main.parseDate("3/14/1519");
    }

    @Test
    public void testParseDateWithCorrectFormat() {
        assertEquals(new DateTime(1519, 3, 14, 0, 0), Main.parseDate("3-14-1519"));
    }
}
