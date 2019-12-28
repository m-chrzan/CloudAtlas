package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.List;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetHierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
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
        assertEquals(1, countAttributes(zmi.getAttributes()));
    }

    @Test
    public void hierarchyIsDeepCopy() throws Exception {
        GetHierarchyMessage message = new GetHierarchyMessage("test_msg", 0, ModuleType.TEST, 42);
        stanik.handleTyped(message);
        HierarchyMessage receivedMessage = (HierarchyMessage) executor.messagesToPass.poll();
        assertNotNull(receivedMessage);
        AttributesMap attributes = receivedMessage.getZMI().getAttributes();
        assertNull(attributes.getOrNull("foo"));
        attributes.add("foo", new ValueInt(1337l));

        GetHierarchyMessage newMessage = new GetHierarchyMessage("test_msg2", 123, ModuleType.TEST, 43);
        stanik.handleTyped(newMessage);
        HierarchyMessage newReceivedMessage = (HierarchyMessage) executor.messagesToPass.poll();
        AttributesMap newAttributes = newReceivedMessage.getZMI().getAttributes();
        assertNull(newAttributes.getOrNull("foo"));
    }

    @Test
    public void updateRootAttributes() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        attributes.add("timestamp", new ValueTime("2012/12/21 04:20:00.000"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/", attributes);
        stanik.handleTyped(message);
        AttributesMap actualAttributes = stanik.getHierarchy().getAttributes();
        assertEquals(3, countAttributes(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.get("foo"));
        assertEquals(new ValueString("baz"), actualAttributes.get("bar"));
        assertEquals(new ValueTime("2012/12/21 04:20:00.000"), actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void updateWithNewZone() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", new ValueTime("2012/12/21 04:20:00.000"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);
        AttributesMap actualAttributes = stanik.getHierarchy().findDescendant("/new").getAttributes();
        assertEquals(4, countAttributes(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.getOrNull("foo"));
        assertEquals(new ValueString("baz"), actualAttributes.getOrNull("bar"));
        assertEquals(new ValueString("new"), actualAttributes.getOrNull("name"));
        assertEquals(new ValueTime("2012/12/21 04:20:00.000"), actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void updateWithRemovedAttributes() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/", attributes);
        attributes.add("timestamp", new ValueTime("2012/12/21 04:20:00.000"));
        stanik.handleTyped(message);

        AttributesMap newAttributes = new AttributesMap();
        newAttributes.add("timestamp", new ValueTime("2012/12/21 04:20:42.000"));
        newAttributes.add("foo", new ValueInt(1338l));
        UpdateAttributesMessage newMessage = new UpdateAttributesMessage("test_msg2", 0, "/", newAttributes);
        stanik.handleTyped(newMessage);

        AttributesMap actualAttributes = stanik.getHierarchy().getAttributes();
        assertEquals(2, countAttributes(actualAttributes));
        assertEquals(new ValueInt(1338l), actualAttributes.getOrNull("foo"));
        assertEquals(new ValueTime("2012/12/21 04:20:42.000"), actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void dontApplyUpdateWithOlderTimestamp() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("timestamp", new ValueTime("2012/12/21 04:20:00.000"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/", attributes);
        stanik.handleTyped(message);

        AttributesMap oldAttributes = new AttributesMap();
        oldAttributes.add("foo", new ValueInt(1336l));
        oldAttributes.add("timestamp", new ValueTime("2012/12/21 04:19:00.000"));
        UpdateAttributesMessage newMessage = new UpdateAttributesMessage("test_msg2", 0, "/", oldAttributes);
        stanik.handleTyped(newMessage);

        AttributesMap actualAttributes = stanik.getHierarchy().getAttributes();
        assertEquals(2, countAttributes(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.getOrNull("foo"));
        assertEquals(new ValueTime("2012/12/21 04:20:00.000"), actualAttributes.getOrNull("timestamp"));
    }

    public int countAttributes(AttributesMap attributes) {
        int count = 0;
        for (Entry<Attribute, Value> attribute : attributes) {
            count++;
        }

        return count;
    }
}
