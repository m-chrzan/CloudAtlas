package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.Container;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RequestStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.SetAttributeMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateQueriesMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.TestUtil;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class NewApiImplementationTests {
    private NewApiImplementation api;
    private MockEventBus eventBus;

    @Before
    public void initializeApi() throws Exception {
        eventBus = new MockEventBus();
        api = new NewApiImplementation(eventBus);
    }

    @Test
    public void testGetZoneSet() throws Exception {
        final Set<String> zoneSet = new HashSet();
        final Container<Exception> exceptionContainer = new Container();
        Thread apiThread = new Thread(() -> {
            try {
                zoneSet.addAll(api.getZoneSet());
            } catch (Exception e) {
                exceptionContainer.thing = e;
            }
        });
        apiThread.start();

        AgentMessage message = eventBus.events.poll(100, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertEquals(ModuleType.RMI, message.getDestinationModule());
        RequestStateMessage requestMessage = (RequestStateMessage) message;

        ZMI root = new ZMI();
        StateMessage responseMessage = new StateMessage("", ModuleType.RMI, 0, 0, root, null);
        requestMessage.getFuture().complete(responseMessage);

        apiThread.join(100);
        assertFalse(apiThread.isAlive());
        assertNull(exceptionContainer.thing);

        assertThat(zoneSet, hasItems("/"));
    }

    @Test
    public void testRootGetZoneAttributeValues() throws Exception {
        final Container<AttributesMap> attributes = new Container();
        final Container<Exception> exceptionContainer = new Container();
        Thread apiThread = new Thread(() -> {
            try {
                attributes.thing = api.getZoneAttributeValues("/");
            } catch (Exception e) {
                exceptionContainer.thing = e;
            }
        });
        apiThread.start();

        AgentMessage message = eventBus.events.poll(100, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertEquals(ModuleType.RMI, message.getDestinationModule());
        RequestStateMessage requestMessage = (RequestStateMessage) message;

        ZMI zmi = new ZMI();
        zmi.getAttributes().add("timestamp", new ValueTime(42l));
        StateMessage response = new StateMessage("", ModuleType.RMI, 0, 0, zmi, new HashMap());
        requestMessage.getFuture().complete(response);

        apiThread.join(100);
        assertFalse(apiThread.isAlive());
        assertNull(exceptionContainer.thing);

        assertEquals(new ValueTime(42l), attributes.thing.getOrNull("timestamp"));
    }

    @Test
    public void testGetZoneAttributeValues() throws Exception {
        final Container<AttributesMap> attributes = new Container();
        final Container<Exception> exceptionContainer = new Container();
        Thread apiThread = new Thread(() -> {
            try {
                attributes.thing = api.getZoneAttributeValues("/son");
            } catch (Exception e) {
                exceptionContainer.thing = e;
            }
        });
        apiThread.start();

        AgentMessage message = eventBus.events.poll(100, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertEquals(ModuleType.RMI, message.getDestinationModule());
        RequestStateMessage requestMessage = (RequestStateMessage) message;

        ZMI zmi = new ZMI();
        zmi.getAttributes().add("timestamp", new ValueTime(42l));
        ZMI son = new ZMI(zmi);
        zmi.addSon(son);
        son.getAttributes().add("name", new ValueString("son"));
        son.getAttributes().add("timestamp", new ValueTime(43l));
        StateMessage response = new StateMessage("", ModuleType.RMI, 0, 0, zmi, new HashMap());
        requestMessage.getFuture().complete(response);

        apiThread.join(100);
        assertFalse(apiThread.isAlive());
        assertNull(exceptionContainer.thing);

        assertEquals(new ValueTime(43l), attributes.thing.getOrNull("timestamp"));
        assertEquals(new ValueString("son"), attributes.thing.getOrNull("name"));
    }

    @Test
    public void testInstallQuery() throws Exception {
        String name = "&query";
        String queryCode = "SELECT 1 AS one";
        long timeBefore = System.currentTimeMillis();
        api.installQuery(name, queryCode);
        long timeAfter = System.currentTimeMillis();

        assertEquals(1, eventBus.events.size());
        AgentMessage message = eventBus.events.take();
        assertEquals(ModuleType.STATE, message.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) message;
        assertEquals(StanikMessage.Type.UPDATE_QUERIES, stanikMessage.getType());
        UpdateQueriesMessage updateMessage = (UpdateQueriesMessage) stanikMessage;
        Map<Attribute, Entry<ValueQuery, ValueTime>> queries = updateMessage.getQueries();
        assertEquals(1, TestUtil.iterableSize(queries.keySet()));
        assertEquals(new ValueQuery("SELECT 1 AS one"), queries.get(new Attribute("&query")).getKey());
        long timestamp = queries.get(new Attribute("&query")).getValue().getValue();
        assertTrue(timeBefore <= timestamp);
        assertTrue(timestamp <= timeAfter);
    }

    @Test
    public void testUninstallQuery() throws Exception {
        String name = "&query";
        long timeBefore = System.currentTimeMillis();
        api.uninstallQuery(name);
        long timeAfter = System.currentTimeMillis();

        assertEquals(1, eventBus.events.size());
        AgentMessage message = eventBus.events.take();
        assertEquals(ModuleType.STATE, message.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) message;
        assertEquals(StanikMessage.Type.UPDATE_QUERIES, stanikMessage.getType());
        UpdateQueriesMessage updateMessage = (UpdateQueriesMessage) stanikMessage;
        Map<Attribute, Entry<ValueQuery, ValueTime>> queries = updateMessage.getQueries();
        assertEquals(1, TestUtil.iterableSize(queries.keySet()));
        assertNull(queries.get(new Attribute("&query")).getKey());
        long timestamp = queries.get(new Attribute("&query")).getValue().getValue();
        assertTrue(timeBefore <= timestamp);
        assertTrue(timestamp <= timeAfter);
    }

    @Test
    public void testSetAttributeValueChange() throws Exception {
        ValueInt numProcesses = new ValueInt(42l);
        long timeBefore = System.currentTimeMillis();
        api.setAttributeValue("/uw/khaki13", "num_processes", numProcesses);
        long timeAfter = System.currentTimeMillis();

        assertEquals(1, eventBus.events.size());
        AgentMessage message = eventBus.events.take();
        assertEquals(ModuleType.STATE, message.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) message;
        assertEquals(StanikMessage.Type.SET_ATTRIBUTE, stanikMessage.getType());
        SetAttributeMessage setMessage = (SetAttributeMessage) stanikMessage;
        assertEquals(new Attribute("num_processes"), setMessage.getAttribute());
        assertEquals(new ValueInt(42l), setMessage.getValue());
        long timestamp = setMessage.getUpdateTimestamp().getValue();
        assertTrue(timeBefore <= timestamp);
        assertTrue(timestamp <= timeAfter);
    }
}
