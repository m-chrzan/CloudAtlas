package pl.edu.mimuw.cloudatlas.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValueDurationTest {
    @Test
    public void testGetValue() {
        ValueDuration v = new ValueDuration(42l);
        assertEquals(v.getValue(), new Long(42l));
    }
}
