package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RemoveZMIMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.SetAttributeMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateQueriesMessage;
import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TestUtil;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ValueUtils;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StanikTest {
    private Stanik stanik;
    private MockExecutor executor;
    private ValueTime testTime;
    private static final long freshnessPeriod = 1000;

    @Before
    public void setupLocals() {
        stanik = new Stanik(new PathName("/new"), freshnessPeriod);
        executor = new MockExecutor(stanik);
        testTime = ValueUtils.currentTime();
    }

    @Test
    public void getEmptyState() throws Exception {
        GetStateMessage message = new GetStateMessage("test_msg", 0, ModuleType.TEST, 42);
        stanik.handleTyped(message);
        assertEquals(1, executor.messagesToPass.size());
        ResponseMessage receivedMessage = (ResponseMessage) executor.messagesToPass.take();
        assertEquals(ModuleType.TEST, receivedMessage.getDestinationModule());
        assertEquals(ResponseMessage.Type.STATE, receivedMessage.getType());
        assertEquals(42, receivedMessage.getRequestId());
        StateMessage stateMessage = (StateMessage) receivedMessage;
        ZMI zmi = stateMessage.getZMI();
        assertNull(zmi.getFather());
        assertTrue(zmi.getSons().isEmpty());
        assertEquals(3, TestUtil.iterableSize(zmi.getAttributes()));
        assertEquals(new ValueInt(0l), zmi.getAttributes().getOrNull("level"));
        assertEquals(new ValueString("/new"), zmi.getAttributes().getOrNull("owner"));
       Map<Attribute, ValueQuery>  queries = stateMessage.getQueries();
        assertEquals(2, TestUtil.iterableSize(queries.keySet()));
    }

    @Test
    public void hierarchyIsDeepCopy() throws Exception {
        GetStateMessage message = new GetStateMessage("test_msg", 0, ModuleType.TEST, 42);
        stanik.handleTyped(message);
        StateMessage receivedMessage = (StateMessage) executor.messagesToPass.poll();
        assertNotNull(receivedMessage);
        AttributesMap attributes = receivedMessage.getZMI().getAttributes();
        assertNull(attributes.getOrNull("foo"));
        attributes.add("foo", new ValueInt(1337l));

        GetStateMessage newMessage = new GetStateMessage("test_msg2", 123, ModuleType.TEST, 43);
        stanik.handleTyped(newMessage);
        StateMessage newReceivedMessage = (StateMessage) executor.messagesToPass.poll();
        AttributesMap newAttributes = newReceivedMessage.getZMI().getAttributes();
        assertNull(newAttributes.getOrNull("foo"));
    }

    @Test
    public void updateRootAttributes() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/", attributes);
        stanik.handleTyped(message);
        AttributesMap actualAttributes = stanik.getHierarchy().getAttributes();
        assertEquals(3, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.get("foo"));
        assertEquals(new ValueString("baz"), actualAttributes.get("bar"));
        assertEquals(testTime, actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void updateWithNewZone() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);
        AttributesMap actualAttributes = stanik.getHierarchy().findDescendant("/new").getAttributes();
        assertEquals(4, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.getOrNull("foo"));
        assertEquals(new ValueString("baz"), actualAttributes.getOrNull("bar"));
        assertEquals(new ValueString("new"), actualAttributes.getOrNull("name"));
        assertEquals(testTime, actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void newZoneHasNewLevel() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);
        GetStateMessage newMessage = new GetStateMessage("test_msg2", 123, ModuleType.TEST, 43);
        stanik.handleTyped(newMessage);

        StateMessage newReceivedMessage = (StateMessage) executor.messagesToPass.poll();
        AttributesMap actualAttributes = newReceivedMessage.getZMI().findDescendant("/new").getAttributes();
        assertEquals(7, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.getOrNull("foo"));
        assertEquals(new ValueString("baz"), actualAttributes.getOrNull("bar"));
        assertEquals(new ValueString("new"), actualAttributes.getOrNull("name"));
        assertEquals(new ValueString("/new"), actualAttributes.getOrNull("owner"));
        assertEquals(new ValueInt(1l), actualAttributes.getOrNull("cardinality"));
        assertEquals(testTime, actualAttributes.getOrNull("timestamp"));
        assertEquals(new ValueInt(1l), actualAttributes.getOrNull("level"));
    }

    @Test
    public void updateWithRemovedAttributes() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/", attributes);
        attributes.add("timestamp", testTime);
        stanik.handleTyped(message);

        AttributesMap newAttributes = new AttributesMap();
        ValueTime newTime = (ValueTime) testTime.addValue(new ValueDuration(1l));
        newAttributes.add("timestamp", newTime);
        newAttributes.add("foo", new ValueInt(1338l));
        UpdateAttributesMessage newMessage = new UpdateAttributesMessage("test_msg2", 0, "/", newAttributes);
        stanik.handleTyped(newMessage);

        AttributesMap actualAttributes = stanik.getHierarchy().getAttributes();
        assertEquals(2, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(1338l), actualAttributes.getOrNull("foo"));
        assertEquals(newTime, actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void dontApplyUpdateWithOlderTimestamp() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/", attributes);
        stanik.handleTyped(message);

        AttributesMap oldAttributes = new AttributesMap();
        oldAttributes.add("foo", new ValueInt(1336l));
        ValueTime olderTime = (ValueTime) testTime.subtract(new ValueDuration(1l));
        oldAttributes.add("timestamp", olderTime);
        UpdateAttributesMessage newMessage = new UpdateAttributesMessage("test_msg2", 0, "/", oldAttributes);
        stanik.handleTyped(newMessage);

        AttributesMap actualAttributes = stanik.getHierarchy().getAttributes();
        assertEquals(2, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.getOrNull("foo"));
        assertEquals(testTime, actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void dontApplyWithStaleTimestamp() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("timestamp", (ValueTime) testTime.subtract(new ValueDuration(freshnessPeriod + 100)));
        attributes.add("name", new ValueString("new"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);

        assertFalse(stanik.getHierarchy().descendantExists(new PathName("/new")));
    }

    @Test
    public void zoneRemovedAfterFreshnessPeriod() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("timestamp", testTime);
        attributes.add("name", new ValueString("new"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);
        Thread.sleep(freshnessPeriod + 100);

        AttributesMap attributes2 = new AttributesMap();
        attributes2.add("timestamp", ValueUtils.currentTime());
        UpdateAttributesMessage message2 = new UpdateAttributesMessage("test_msg", 0, "/", attributes2);
        stanik.handleTyped(message2);

        GetStateMessage getStateMessage = new GetStateMessage("", 0, ModuleType.TEST, 0);
        stanik.handleTyped(getStateMessage);

        StateMessage newReceivedMessage = (StateMessage) executor.messagesToPass.poll();
        assertNotNull(newReceivedMessage);
        assertFalse(newReceivedMessage.getZMI().descendantExists(new PathName("/new")));
        assertFalse(stanik.getHierarchy().descendantExists(new PathName("/new")));
    }

    @Test
    @Ignore
    public void addQuery() throws Exception {
        HashMap<Attribute, ValueQuery> queries = new HashMap<Attribute, ValueQuery>();
        queries.put(new Attribute("&query"), new ValueQuery("SELECT 1 AS one", 42l));
        UpdateQueriesMessage message = new UpdateQueriesMessage("test_msg", 0, queries);
        System.out.println(message);
        stanik.handleTyped(message);

        HashMap<Attribute, ValueQuery> actualQueries = stanik.getQueries();
        assertEquals(3, TestUtil.iterableSize(actualQueries.keySet()));
        assertTrue(actualQueries.containsKey(new Attribute("&query")));
        ValueQuery timestampedQuery = actualQueries.get(new Attribute("&query"));
        assertEquals(42l, timestampedQuery.getTimestamp());
        assertEquals("SELECT 1 AS one", timestampedQuery.getCode());
    }

    @Test
    @Ignore
    public void updateQueries() throws Exception {
        HashMap<Attribute, ValueQuery> queries = new HashMap<Attribute, ValueQuery>();
        queries.put(new Attribute("&query1"), new ValueQuery("SELECT 1 AS one", 42l));
        queries.put(new Attribute("&query3"), new ValueQuery("SELECT 23 AS x", 43l));
        queries.put(new Attribute("&query4"), new ValueQuery("SELECT 1000 AS foo", 43l));
        UpdateQueriesMessage message = new UpdateQueriesMessage("test_msg", 0, queries);
        stanik.handleTyped(message);

        HashMap<Attribute, ValueQuery> otherQueries = new HashMap<Attribute, ValueQuery>();
        otherQueries.put(new Attribute("&query1"), new ValueQuery("SELECT 2 AS one", 41l));
        otherQueries.put(new Attribute("&query2"), new ValueQuery("SELECT 42 AS answer", 39l));
        otherQueries.put(new Attribute("&query3"), new ValueQuery("SELECT 17 AS y", 44l));
        UpdateQueriesMessage otherMessage = new UpdateQueriesMessage("test_msg", 0, otherQueries);
        stanik.handleTyped(otherMessage);

        HashMap<Attribute, ValueQuery> actualQueries = stanik.getQueries();
        assertEquals(6, TestUtil.iterableSize(actualQueries.keySet()));
        assertTrue(actualQueries.containsKey(new Attribute("&query1")));
        assertTrue(actualQueries.containsKey(new Attribute("&query2")));
        assertTrue(actualQueries.containsKey(new Attribute("&query3")));
        assertTrue(actualQueries.containsKey(new Attribute("&query4")));

        ValueQuery timestampedQuery1 = actualQueries.get(new Attribute("&query1"));
        assertEquals(42l, timestampedQuery1.getTimestamp());
        assertEquals("SELECT 1 AS one", timestampedQuery1.getCode());

        ValueQuery timestampedQuery2 = actualQueries.get(new Attribute("&query2"));
        assertEquals(39l, timestampedQuery2.getTimestamp());
        assertEquals("SELECT 42 AS answer", timestampedQuery2.getCode());

        ValueQuery timestampedQuery3 = actualQueries.get(new Attribute("&query3"));
        assertEquals(44l, timestampedQuery3.getTimestamp());
        assertEquals("SELECT 17 AS y", timestampedQuery3.getCode());

        ValueQuery timestampedQuery4 = actualQueries.get(new Attribute("&query4"));
        assertEquals(43l, timestampedQuery4.getTimestamp());
        assertEquals("SELECT 1000 AS foo", timestampedQuery4.getCode());
    }

    @Test
    public void removeZMI() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);

        RemoveZMIMessage removeMessage = new RemoveZMIMessage("test_msg2", 0, "/new", (ValueTime) testTime.addValue(new ValueDuration(1l)));
        stanik.handleTyped(removeMessage);

        assertFalse(stanik.getHierarchy().descendantExists(new PathName("/new")));
    }

    @Test
    public void dontRemoveZMIIfTimestampOlder() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("bar", new ValueString("baz"));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);

        RemoveZMIMessage removeMessage = new RemoveZMIMessage("test_msg2", 0, "/new", (ValueTime) testTime.subtract(new ValueDuration(1l)));
        stanik.handleTyped(removeMessage);

        stanik.getHierarchy().findDescendant("/new");
    }

    @Test
    public void setOldAttribute() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);

        SetAttributeMessage setMessage = new SetAttributeMessage("test_msg2", 0, "/new", new Attribute("foo"), new ValueInt(43l), (ValueTime) testTime.subtract(new ValueDuration(1l)));
        stanik.handleTyped(setMessage);

        AttributesMap actualAttributes = stanik.getHierarchy().findDescendant("/new").getAttributes();
        assertEquals(3, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(43l), actualAttributes.getOrNull("foo"));
        assertEquals(testTime, actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void setOldAttribute2() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);

        ValueTime newTime = (ValueTime) testTime.addValue(new ValueDuration(1l));
        SetAttributeMessage setMessage = new SetAttributeMessage("test_msg2", 0, "/new", new Attribute("foo"), new ValueInt(43l), newTime);
        stanik.handleTyped(setMessage);

        AttributesMap actualAttributes = stanik.getHierarchy().findDescendant("/new").getAttributes();
        assertEquals(3, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(43l), actualAttributes.getOrNull("foo"));
        assertEquals(newTime, actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void setNewAttribute() throws Exception {
        AttributesMap attributes = new AttributesMap();
        attributes.add("foo", new ValueInt(1337l));
        attributes.add("name", new ValueString("new"));
        attributes.add("timestamp", testTime);
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/new", attributes);
        stanik.handleTyped(message);

        ValueTime newTime = (ValueTime) testTime.addValue(new ValueDuration(1l));
        SetAttributeMessage setMessage = new SetAttributeMessage("test_msg2", 0, "/new", new Attribute("bar"), new ValueInt(43l), newTime);
        stanik.handleTyped(setMessage);

        AttributesMap actualAttributes = stanik.getHierarchy().findDescendant("/new").getAttributes();
        assertEquals(4, TestUtil.iterableSize(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.getOrNull("foo"));
        assertEquals(new ValueInt(43l), actualAttributes.getOrNull("bar"));
        assertEquals(newTime, actualAttributes.getOrNull("timestamp"));
    }
}
