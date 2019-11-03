package pl.edu.mimuw.cloudatlas.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValueDurationTest {
    @Test
    public void testGetValue() {
        ValueDuration v = new ValueDuration(42l);
        assertEquals(new Long(42l), v.getValue());
    }

    @Test
    public void testParseDurationMillisecond() {
        ValueDuration v = new ValueDuration("+0 00:00:00.001");
        assertEquals(new Long(1l), v.getValue());
    }

    @Test
    public void testParseDurationSecond() {
        ValueDuration v = new ValueDuration("+0 00:00:01.000");
        assertEquals(new Long(1000l), v.getValue());
    }

    @Test
    public void testParseDurationMinute() {
        ValueDuration v = new ValueDuration("+0 00:01:00.000");
        assertEquals(new Long(60000l), v.getValue());
    }

    @Test
    public void testParseDurationHour() {
        ValueDuration v = new ValueDuration("+0 01:00:00.000");
        assertEquals(new Long(3600000l), v.getValue());
    }

    @Test
    public void testParseDurationDay() {
        ValueDuration v = new ValueDuration("+1 00:00:00.000");
        assertEquals(new Long(86400000l), v.getValue());
    }

    @Test
    public void testParseDurationComplex() {
        ValueDuration v = new ValueDuration("+0 01:59:40.000");
        assertEquals(new Long(7180000l), v.getValue());
    }

    @Test
    public void testParseDurationNegative() {
        ValueDuration v = new ValueDuration("-1 01:01:01.001");
        assertEquals(new Long(-90061001l), v.getValue());
    }
}
