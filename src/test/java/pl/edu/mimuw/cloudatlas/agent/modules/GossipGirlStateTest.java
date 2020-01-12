package pl.edu.mimuw.cloudatlas.agent.modules;

import org.junit.Test;
import static org.junit.Assert.*;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class GossipGirlStateTest {
    @Test
    public void gtpTest1() throws Exception {
        GossipGirlState state = new GossipGirlState(0, new PathName("/"), new ValueContact(null, null), true);
        state.hejkaSendTimestamp = new ValueTime(100l);
        state.hejkaReceiveTimestamp = new ValueTime(110l);
        state.noCoTamSendTimestamp = new ValueTime(120l);
        state.noCoTamReceiveTimestamp = new ValueTime(130l);
        state.computeOffset();
        assertEquals(new ValueDuration(0l), state.offset);
    }

    @Test
    public void gtpTest2() throws Exception {
        GossipGirlState state = new GossipGirlState(0, new PathName("/"), new ValueContact(null, null), true);
        state.hejkaSendTimestamp = new ValueTime(100l);
        state.hejkaReceiveTimestamp = new ValueTime(60l);
        state.noCoTamSendTimestamp = new ValueTime(70l);
        state.noCoTamReceiveTimestamp = new ValueTime(130l);
        state.computeOffset();
        assertEquals(new ValueDuration(-50l), state.offset);
    }

    @Test
    public void gtpTest3() throws Exception {
        GossipGirlState state = new GossipGirlState(0, new PathName("/"), new ValueContact(null, null), true);
        state.hejkaSendTimestamp = new ValueTime(100l);
        state.hejkaReceiveTimestamp = new ValueTime(160l);
        state.noCoTamSendTimestamp = new ValueTime(170l);
        state.noCoTamReceiveTimestamp = new ValueTime(130l);
        state.computeOffset();
        assertEquals(new ValueDuration(50l), state.offset);
    }
}
