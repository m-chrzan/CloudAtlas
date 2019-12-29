package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.Iterator;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateQueriesMessage;
import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
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
        assertEquals(1, iterableSize(zmi.getAttributes()));
        Map<Attribute, Entry<ValueQuery, ValueTime>> queries = stateMessage.getQueries();
        assertEquals(0, iterableSize(queries.keySet()));
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
        attributes.add("timestamp", new ValueTime("2012/12/21 04:20:00.000"));
        UpdateAttributesMessage message = new UpdateAttributesMessage("test_msg", 0, "/", attributes);
        stanik.handleTyped(message);
        AttributesMap actualAttributes = stanik.getHierarchy().getAttributes();
        assertEquals(3, iterableSize(actualAttributes));
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
        assertEquals(4, iterableSize(actualAttributes));
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
        assertEquals(2, iterableSize(actualAttributes));
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
        assertEquals(2, iterableSize(actualAttributes));
        assertEquals(new ValueInt(1337l), actualAttributes.getOrNull("foo"));
        assertEquals(new ValueTime("2012/12/21 04:20:00.000"), actualAttributes.getOrNull("timestamp"));
    }

    @Test
    public void addQuery() throws Exception {
        HashMap<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap<Attribute, Entry<ValueQuery, ValueTime>>();
        queries.put(new Attribute("&query"), new SimpleImmutableEntry(new ValueQuery("SELECT 1 AS one"), new ValueTime(42l)));
        UpdateQueriesMessage message = new UpdateQueriesMessage("test_msg", 0, queries);
        stanik.handleTyped(message);

        HashMap<Attribute, Entry<ValueQuery, ValueTime>> actualQueries = stanik.getQueries();
        assertEquals(1, iterableSize(actualQueries.keySet()));
        assertTrue(actualQueries.containsKey(new Attribute("&query")));
        Entry<ValueQuery, ValueTime> timestampedQuery = actualQueries.get(new Attribute("&query"));
        assertEquals(new ValueTime(42l), timestampedQuery.getValue());
        assertEquals(new ValueQuery("SELECT 1 AS one"), timestampedQuery.getKey());
    }

    @Test
    public void updateQueries() throws Exception {
        HashMap<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap<Attribute, Entry<ValueQuery, ValueTime>>();
        queries.put(new Attribute("&query1"), new SimpleImmutableEntry(new ValueQuery("SELECT 1 AS one"), new ValueTime(42l)));
        queries.put(new Attribute("&query3"), new SimpleImmutableEntry(new ValueQuery("SELECT 23 AS x"), new ValueTime(43l)));
        queries.put(new Attribute("&query4"), new SimpleImmutableEntry(new ValueQuery("SELECT 1000 AS foo"), new ValueTime(43l)));
        UpdateQueriesMessage message = new UpdateQueriesMessage("test_msg", 0, queries);
        stanik.handleTyped(message);

        HashMap<Attribute, Entry<ValueQuery, ValueTime>> otherQueries = new HashMap<Attribute, Entry<ValueQuery, ValueTime>>();
        otherQueries.put(new Attribute("&query1"), new SimpleImmutableEntry(new ValueQuery("SELECT 2 AS one"), new ValueTime(41l)));
        otherQueries.put(new Attribute("&query2"), new SimpleImmutableEntry(new ValueQuery("SELECT 42 AS answer"), new ValueTime(39l)));
        otherQueries.put(new Attribute("&query3"), new SimpleImmutableEntry(new ValueQuery("SELECT 17 AS y"), new ValueTime(44l)));
        UpdateQueriesMessage otherMessage = new UpdateQueriesMessage("test_msg", 0, otherQueries);
        stanik.handleTyped(otherMessage);

        HashMap<Attribute, Entry<ValueQuery, ValueTime>> actualQueries = stanik.getQueries();
        assertEquals(4, iterableSize(actualQueries.keySet()));
        assertTrue(actualQueries.containsKey(new Attribute("&query1")));
        assertTrue(actualQueries.containsKey(new Attribute("&query2")));
        assertTrue(actualQueries.containsKey(new Attribute("&query3")));
        assertTrue(actualQueries.containsKey(new Attribute("&query4")));

        Entry<ValueQuery, ValueTime> timestampedQuery1 = actualQueries.get(new Attribute("&query1"));
        assertEquals(new ValueTime(42l), timestampedQuery1.getValue());
        assertEquals(new ValueQuery("SELECT 1 AS one"), timestampedQuery1.getKey());

        Entry<ValueQuery, ValueTime> timestampedQuery2 = actualQueries.get(new Attribute("&query2"));
        assertEquals(new ValueTime(39l), timestampedQuery2.getValue());
        assertEquals(new ValueQuery("SELECT 42 AS answer"), timestampedQuery2.getKey());

        Entry<ValueQuery, ValueTime> timestampedQuery3 = actualQueries.get(new Attribute("&query3"));
        assertEquals(new ValueTime(44l), timestampedQuery3.getValue());
        assertEquals(new ValueQuery("SELECT 17 AS y"), timestampedQuery3.getKey());

        Entry<ValueQuery, ValueTime> timestampedQuery4 = actualQueries.get(new Attribute("&query4"));
        assertEquals(new ValueTime(43l), timestampedQuery4.getValue());
        assertEquals(new ValueQuery("SELECT 1000 AS foo"), timestampedQuery4.getKey());
    }

    public <T> int iterableSize(Iterable<T> iterable) {
        int count = 0;
        for (T attribute : iterable) {
            count++;
        }

        return count;
    }
}
