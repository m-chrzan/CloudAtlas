package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RunQueriesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.TestUtil;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class QurnikTest {
    private Qurnik qurnik;
    private MockExecutor executor;

    @Before
    public void setupLocals() {
        qurnik = new Qurnik();
        executor = new MockExecutor(qurnik);
    }

    @Test
    public void runQueriesRequestsState() throws Exception {
        RunQueriesMessage message = new RunQueriesMessage("", 0);
        qurnik.handleTyped(message);
        AgentMessage receivedMessage = (AgentMessage) executor.messagesToPass.take();
        assertEquals(ModuleType.STATE, receivedMessage.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) receivedMessage;
        assertEquals(StanikMessage.Type.GET_STATE, stanikMessage.getType());
        GetStateMessage getStateMessage = (GetStateMessage) stanikMessage;
        assertEquals(ModuleType.QUERY, getStateMessage.getRequestingModule());
    }

    @Test
    public void simpleQuery() throws Exception {
        ZMI root = new ZMI();
        ZMI son = new ZMI(root);
        root.addSon(son);
        AttributesMap sonAttributes = new AttributesMap();
        sonAttributes.add("name", new ValueString("son"));
        Map<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap();
        queries.put(
                new Attribute("&query"),
                new SimpleImmutableEntry(
                    new ValueQuery("SELECT 1 AS one"),
                    new ValueTime(0l)
                )
        );
        StateMessage message = new StateMessage("", ModuleType.QUERY, 0, 0, root, queries);
        long timeBefore = System.currentTimeMillis();
        qurnik.handleTyped(message);
        long timeAfter = System.currentTimeMillis();

        assertEquals(1, executor.messagesToPass.size());
        AgentMessage receivedMessage = (AgentMessage) executor.messagesToPass.take();
        assertEquals(ModuleType.STATE, receivedMessage.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) receivedMessage;
        assertEquals(StanikMessage.Type.UPDATE_ATTRIBUTES, stanikMessage.getType());
        UpdateAttributesMessage updateAttributesMessage = (UpdateAttributesMessage) stanikMessage;
        assertEquals("/", updateAttributesMessage.getPathName());
        AttributesMap updatedAttributes = updateAttributesMessage.getAttributes();
        assertEquals(3, TestUtil.iterableSize(updatedAttributes));
        assertEquals(new ValueString(null), updatedAttributes.getOrNull("name"));
        assertEquals(new ValueInt(1l), updatedAttributes.getOrNull("one"));
        long timestamp = ((ValueTime) updatedAttributes.getOrNull("timestamp")).getValue();
        assertTrue(timeBefore <= timestamp);
        assertTrue(timestamp <= timeAfter);
    }

    private ZMI setupSampleHierarchy() {
        ZMI root = new ZMI();

        ZMI uw = new ZMI(root);
        root.addSon(uw);
        ZMI pw = new ZMI(root);
        root.addSon(pw);

        ZMI uw1 = new ZMI(uw);
        uw.addSon(uw1);
        ZMI uw2 = new ZMI(uw);
        uw.addSon(uw2);

        AttributesMap uwAttributes = uw.getAttributes();
        uwAttributes.add("name", new ValueString("uw"));

        AttributesMap pwAttributes = pw.getAttributes();
        pwAttributes.add("name", new ValueString("pw"));
        pwAttributes.add("x", new ValueInt(42l));
        pwAttributes.add("y", new ValueInt(250l));
        pwAttributes.add("z", new ValueInt(5l));

        AttributesMap uw1Attributes = uw1.getAttributes();
        uw1Attributes.add("name", new ValueString("uw1"));
        uw1Attributes.add("x", new ValueInt(12l));
        uw1Attributes.add("y", new ValueInt(100l));
        uw1Attributes.add("a", new ValueInt(123l));

        AttributesMap uw2Attributes = uw2.getAttributes();
        uw2Attributes.add("name", new ValueString("uw2"));
        uw2Attributes.add("x", new ValueInt(13l));
        uw2Attributes.add("a", new ValueInt(134l));
        uw2Attributes.add("b", new ValueInt(777l));

        return root;
    }

    public Map<Attribute, Entry<ValueQuery, ValueTime>> setupSampleQueries() throws Exception {
        Map<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap();

        queries.put(
                new Attribute("&query1"),
                new SimpleImmutableEntry(
                    new ValueQuery("SELECT sum(x) AS x"),
                    new ValueTime(0l)
                )
        );
        queries.put(
                new Attribute("&query2"),
                new SimpleImmutableEntry(
                    new ValueQuery("SELECT min(y) AS y"),
                    new ValueTime(0l)
                )
        );
        queries.put(
                new Attribute("&query3"),
                new SimpleImmutableEntry(
                    new ValueQuery("SELECT max(z) AS z"),
                    new ValueTime(0l)
                )
        );
        queries.put(
                new Attribute("&query4"),
                new SimpleImmutableEntry(
                    new ValueQuery("SELECT sum(a + 1) AS a"),
                    new ValueTime(0l)
                )
        );
        queries.put(
                new Attribute("&query5"),
                new SimpleImmutableEntry(
                    new ValueQuery("SELECT sum(2 * b) AS b"),
                    new ValueTime(0l)
                )
        );

        return queries;
    }

    @Test
    public void multipleQueries() throws Exception {
        ZMI root = setupSampleHierarchy();

        Map<Attribute, Entry<ValueQuery, ValueTime>> queries = setupSampleQueries();
        StateMessage message = new StateMessage("", ModuleType.QUERY, 0, 0, root, queries);
        long timeBefore = System.currentTimeMillis();
        qurnik.handleTyped(message);
        long timeAfter = System.currentTimeMillis();

        assertEquals(2, executor.messagesToPass.size());

        UpdateAttributesMessage message1 = (UpdateAttributesMessage) executor.messagesToPass.take();
        assertEquals("/uw", message1.getPathName());
        AttributesMap attributes1 = message1.getAttributes();
        assertEquals(6, TestUtil.iterableSize(attributes1));
        assertEquals(new ValueString("uw"), attributes1.getOrNull("name"));
        assertEquals(new ValueInt(25l), attributes1.getOrNull("x"));
        assertEquals(new ValueInt(100l), attributes1.getOrNull("y"));
        assertEquals(new ValueInt(259l), attributes1.getOrNull("a"));
        assertEquals(new ValueInt(1554l), attributes1.getOrNull("b"));
        long timestamp1 = ((ValueTime) attributes1.getOrNull("timestamp")).getValue();
        assertTrue(timeBefore <= timestamp1);
        assertTrue(timestamp1 <= timeAfter);

        UpdateAttributesMessage message2 = (UpdateAttributesMessage) executor.messagesToPass.take();
        assertEquals("/", message2.getPathName());
        AttributesMap attributes2 = message2.getAttributes();
        System.out.println("got attributes " + attributes2.toString());
        assertEquals(7, TestUtil.iterableSize(attributes2));
        assertEquals(new ValueString(null), attributes2.getOrNull("name"));
        assertEquals(new ValueInt(67l), attributes2.getOrNull("x"));
        assertEquals(new ValueInt(100l), attributes2.getOrNull("y"));
        assertEquals(new ValueInt(5l), attributes2.getOrNull("z"));
        assertEquals(new ValueInt(260l), attributes2.getOrNull("a"));
        assertEquals(new ValueInt(3108l), attributes2.getOrNull("b"));
        long timestamp2 = ((ValueTime) attributes2.getOrNull("timestamp")).getValue();
        assertTrue(timeBefore <= timestamp2);
        assertTrue(timestamp2 <= timeAfter);
    }

    @Test
    public void ignoresNullQueries() throws Exception {
        ZMI root = setupSampleHierarchy();

        Map<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap();
        queries.put(new Attribute("&query1"), new SimpleImmutableEntry(
                    new ValueQuery("SELECT 1 AS one"),
                    new ValueTime(42l)
                )
        );
        queries.put(new Attribute("&query2"), new SimpleImmutableEntry(
                    null,
                    new ValueTime(43l)
                )
        );
        queries.put(new Attribute("&query3"), new SimpleImmutableEntry(
                    new ValueQuery("SELECT 2 AS two"),
                    new ValueTime(44l)
                )
        );
        StateMessage message = new StateMessage("", ModuleType.QUERY, 0, 0, root, queries);
        long timeBefore = System.currentTimeMillis();
        qurnik.handleTyped(message);
        long timeAfter = System.currentTimeMillis();

        UpdateAttributesMessage message1 = (UpdateAttributesMessage) executor.messagesToPass.take();
        assertEquals("/uw", message1.getPathName());
        AttributesMap attributes1 = message1.getAttributes();
        assertEquals(4, TestUtil.iterableSize(attributes1));
        assertEquals(new ValueInt(1l), attributes1.getOrNull("one"));
        assertEquals(new ValueInt(2l), attributes1.getOrNull("two"));
        long timestamp1 = ((ValueTime) attributes1.getOrNull("timestamp")).getValue();
        assertTrue(timeBefore <= timestamp1);
        assertTrue(timestamp1 <= timeAfter);

        UpdateAttributesMessage message2 = (UpdateAttributesMessage) executor.messagesToPass.take();
        assertEquals("/", message2.getPathName());
        AttributesMap attributes2 = message2.getAttributes();
        System.out.println("got attributes " + attributes2.toString());
        assertEquals(4, TestUtil.iterableSize(attributes2));
        assertEquals(new ValueString(null), attributes2.getOrNull("name"));
        assertEquals(new ValueInt(1l), attributes2.getOrNull("one"));
        assertEquals(new ValueInt(2l), attributes2.getOrNull("two"));
        long timestamp2 = ((ValueTime) attributes2.getOrNull("timestamp")).getValue();
        assertTrue(timeBefore <= timestamp2);
        assertTrue(timestamp2 <= timeAfter);
    }
}
