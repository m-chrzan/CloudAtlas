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
        long timeBefore = System.currentTimeMillis() / 1000;
        qurnik.handleTyped(message);
        long timeAfter = System.currentTimeMillis() / 1000;

        assertEquals(1, executor.messagesToPass.size());
        AgentMessage receivedMessage = (AgentMessage) executor.messagesToPass.take();
        assertEquals(ModuleType.STATE, receivedMessage.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) receivedMessage;
        assertEquals(StanikMessage.Type.UPDATE_ATTRIBUTES, stanikMessage.getType());
        UpdateAttributesMessage updateAttributesMessage = (UpdateAttributesMessage) stanikMessage;
        assertEquals("/", updateAttributesMessage.getPathName());
        AttributesMap updatedAttributes = updateAttributesMessage.getAttributes();
        assertEquals(2, TestUtil.iterableSize(updatedAttributes));
        assertEquals(new ValueInt(1l), updatedAttributes.getOrNull("one"));
        System.out.println(timeBefore);
        System.out.println(updatedAttributes.getOrNull("timestamp"));
        long timestamp = ((ValueTime) updatedAttributes.getOrNull("timestamp")).getValue();
        assertTrue(timeBefore <= timestamp);
        assertTrue(timestamp <= timeAfter);
    }
}
