package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.List;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetHierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StanikTest {
    private Stanik stanik;
    private MockExecutor executor;

    @Before
    public void setupEventBus() {
        stanik = new Stanik();
        executor = new MockExecutor(stanik);
    }

    @Test
    public void getEmptyHierarchy() throws Exception {
        GetHierarchyMessage message = new GetHierarchyMessage("test_msg", 0, ModuleType.TEST, 42);
        stanik.handleTyped(message);
        assertEquals(1, executor.messagesToPass.size());
        ResponseMessage receivedMessage = (ResponseMessage) executor.messagesToPass.take();
        assertEquals(ModuleType.TEST, receivedMessage.getDestinationModule());
        assertEquals(ResponseMessage.Type.HIERARCHY, receivedMessage.getType());
        assertEquals(42, receivedMessage.getRequestId());
        HierarchyMessage hierarchyMessage = (HierarchyMessage) receivedMessage;
        ZMI zmi = hierarchyMessage.getZMI();
        assertNull(zmi.getFather());
        assertTrue(zmi.getSons().isEmpty());
        boolean empty = true;
        for (Entry<Attribute, Value> entry : zmi.getAttributes()) {
            empty = false;
            break;
        }
        assertTrue(empty);
    }
}
