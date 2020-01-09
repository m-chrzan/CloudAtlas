package pl.edu.mimuw.cloudatlas.agent.modules;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.hasItems;

import java.net.InetAddress;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.AttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HejkaMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GossipGirlMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.InitiateGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.QueryMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.NoCoTamMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UDUPMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TestUtil;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ValueUtils;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class GossipGirlTest {
    private GossipGirl gossipGirl;
    private MockExecutor executor;
    private final PathName ourPath = new PathName("/son/grand");
    private final PathName theirPath = new PathName("/son/bro");
    private ValueContact theirContact;
    private InitiateGossipMessage initiateGossipMessage;
    private ValueTime testTime;
    private ZMI initiatorHierarchy;
    private Map<Attribute, Entry<ValueQuery, ValueTime>> initiatorQueries;
    private StateMessage initiatorStateMessage;
    private NoCoTamMessage noCoTamMessage;

    @Before
    public void setupLocals() throws Exception {
        gossipGirl = new GossipGirl();
        executor = new MockExecutor(gossipGirl);

        theirContact = new ValueContact(
            theirPath,
            InetAddress.getByAddress("localhost", new byte[] { 127, 0, 0, 1 })
        );
        initiateGossipMessage = new InitiateGossipMessage(
            "test_msg",
            0,
            ourPath,
            theirContact
        );

        testTime = ValueUtils.currentTime();
        setupHierarchy();
        setupQueries();
        initiatorStateMessage = new StateMessage("", ModuleType.GOSSIP, 0, 0, initiatorHierarchy, initiatorQueries);

        Map<PathName, ValueTime> otherZoneTimestamps = makeOtherZoneTimestamps();
        Map<Attribute, ValueTime> otherQueryTimestamps = makeOtherQueryTimestamps();

        noCoTamMessage = new NoCoTamMessage("", 0, 0, 42, otherZoneTimestamps, otherQueryTimestamps, TestUtil.addToTime(testTime, 10), TestUtil.addToTime(testTime, 22));
    }

    public Map<PathName, ValueTime> makeOtherZoneTimestamps() {
        Map<PathName, ValueTime> zoneTimestamps = new HashMap();
        addOtherZoneTimestamp(zoneTimestamps, "/son/sis", -100);
        addOtherZoneTimestamp(zoneTimestamps, "/son/bro", 0);
        addOtherZoneTimestamp(zoneTimestamps, "/son/whodis", -300);

        return zoneTimestamps;
    }

    public Map<Attribute, ValueTime> makeOtherQueryTimestamps() {
        Map<Attribute, ValueTime> queryTimestamps = new HashMap();
        addOtherQueryTimestamp(queryTimestamps, "&one", 10);
        addOtherQueryTimestamp(queryTimestamps, "&query", -400);
        addOtherQueryTimestamp(queryTimestamps, "&three", 0);
        return queryTimestamps;
    }

    public void addOtherQueryTimestamp(Map<Attribute, ValueTime> timestamps, String name, long offset) {
        timestamps.put(new Attribute(name), TestUtil.addToTime(testTime, offset));
    }

    public void addOtherZoneTimestamp(Map<PathName, ValueTime> timestamps, String path, long offset) {
        timestamps.put(new PathName(path), TestUtil.addToTime(testTime, offset));
    }

    public void setupHierarchy() {
        initiatorHierarchy = makeZMI(null, null, 13l, "hello", testTime);
        ZMI son = makeZMI(initiatorHierarchy, "son", 42l, "world", testTime);
        ZMI daughter = makeZMI(initiatorHierarchy, "daughter", 24l, "kebab", testTime);
        ZMI grand = makeZMI(son, "grand", 1337l, "ok", testTime);
        ZMI bro = makeZMI(son, "bro", 3451434l, "whazzup", testTime);
        ZMI sis = makeZMI(son, "sis", 420l, "hey", testTime);
    }

    public void setupQueries() throws Exception {
        initiatorQueries = new HashMap();
        addQuery(initiatorQueries, "&one", "SELECT 1 AS one", testTime);
        addQuery(initiatorQueries, "&two", "SELECT 2 AS two", testTime);
        addQuery(initiatorQueries, "&query", "SELECT sum(foo) AS foo", testTime);
    }

    public void addQuery(Map<Attribute, Entry<ValueQuery, ValueTime>> queries, String name, String query, ValueTime timestamp) throws Exception {
        queries.put(
                new Attribute(name),
                new SimpleImmutableEntry(new ValueQuery(query), timestamp)
        );
    }

    private ZMI makeZMI(ZMI parent, String name, Long foo, String bar, ValueTime timestamp) {
        ZMI zmi = new ZMI(parent);
        if (parent != null) {
            parent.addSon(zmi);
        }
        AttributesMap attributes = zmi.getAttributes();
        attributes.add("name", new ValueString(name));
        attributes.add("foo", new ValueInt(foo));
        attributes.add("bar", new ValueString(bar));
        attributes.add("timestamp", timestamp);
        return zmi;
    }

    @Test
    public void initiateGossipRequestsState() throws Exception {
        gossipGirl.handleTyped(initiateGossipMessage);

        AgentMessage receivedMessage = executor.messagesToPass.poll();
        assertNotNull(receivedMessage);
        assertEquals(ModuleType.STATE, receivedMessage.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) receivedMessage;
        assertEquals(StanikMessage.Type.GET_STATE, stanikMessage.getType());
        GetStateMessage getStateMessage = (GetStateMessage) stanikMessage;
        assertEquals(ModuleType.GOSSIP, getStateMessage.getRequestingModule());
    }

    @Test
    public void initiatorSendsHejkaOnState() throws Exception {
        gossipGirl.handleTyped(initiateGossipMessage);
        executor.messagesToPass.take();
        gossipGirl.handleTyped(initiatorStateMessage);

        AgentMessage receivedMessage = executor.messagesToPass.poll();
        assertUDUPMessage(
                receivedMessage,
                new PathName("/son/bro"),
                GossipGirlMessage.Type.HEJKA
        );
        HejkaMessage hejkaMessage = (HejkaMessage) ((UDUPMessage) receivedMessage).getContent();
        assertEquals(0, hejkaMessage.getSenderGossipId());
        System.out.println(hejkaMessage.getZoneTimestamps().keySet());
        assertEquals(3, TestUtil.iterableSize(hejkaMessage.getZoneTimestamps().keySet()));
        Set<PathName> zoneSet = hejkaMessage.getZoneTimestamps().keySet();
        assertThat(zoneSet, hasItems(new PathName("/daughter")));
        assertThat(zoneSet, hasItems(new PathName("/son/sis")));
        assertThat(zoneSet, hasItems(new PathName("/son/grand")));

        assertEquals(3, TestUtil.iterableSize(hejkaMessage.getQueryTimestamps().keySet()));
        Set<Attribute> querySet = hejkaMessage.getQueryTimestamps().keySet();
        assertThat(querySet, hasItems(new Attribute("&one")));
        assertThat(querySet, hasItems(new Attribute("&two")));
        assertThat(querySet, hasItems(new Attribute("&query")));
    }

    @Test
    public void initiatorSendsZonesAndQueriesOnNoCoTam() throws Exception {
        gossipGirl.handleTyped(initiateGossipMessage);
        executor.messagesToPass.take();
        gossipGirl.handleTyped(initiatorStateMessage);
        executor.messagesToPass.take();
        gossipGirl.handleTyped(noCoTamMessage);

        // 3 ZMIs, 2 queries
        assertEquals(5, executor.messagesToPass.size());

        AgentMessage receivedMessage1 = executor.messagesToPass.poll();
        assertAttributeMessage(receivedMessage1, "/son/bro", "/daughter");
        AgentMessage receivedMessage2 = executor.messagesToPass.poll();
        assertAttributeMessage(receivedMessage2, "/son/bro", "/son/sis");
        AgentMessage receivedMessage3 = executor.messagesToPass.poll();
        assertAttributeMessage(receivedMessage3, "/son/bro", "/son/grand");

        AgentMessage receivedMessage4 = executor.messagesToPass.poll();
        assertQueryMessage(receivedMessage4, "/son/bro", "&two", "SELECT 2 AS two");
        AgentMessage receivedMessage5 = executor.messagesToPass.poll();
        assertQueryMessage(receivedMessage5, "/son/bro", "&query", "SELECT sum(foo) AS foo");
    }

    private void assertQueryMessage(AgentMessage message, String recipientPath, String name, String query) throws Exception {
        assertUDUPMessage(
                message,
                new PathName(recipientPath),
                GossipGirlMessage.Type.QUERY
        );
        QueryMessage queryMessage = (QueryMessage) ((UDUPMessage) message).getContent();
        assertEquals(new Attribute(name), queryMessage.getName());
        assertEquals(new ValueQuery(query), queryMessage.getQuery());
    }

    private void assertAttributeMessage(AgentMessage message, String recipientPath, String zonePath) throws Exception {
        assertUDUPMessage(
                message,
                new PathName(recipientPath),
                GossipGirlMessage.Type.ATTRIBUTES
        );
        AttributesMessage attributesMessage = (AttributesMessage) ((UDUPMessage) message).getContent();
        assertEquals(new PathName(zonePath), attributesMessage.getPath());
    }

    private void assertUDUPMessage(AgentMessage message, PathName destinationName, GossipGirlMessage.Type type) throws Exception {
        assertNotNull(message);
        assertEquals(ModuleType.UDP, message.getDestinationModule());
        UDUPMessage udupMessage = (UDUPMessage) message;
        assertEquals(destinationName, udupMessage.getContact().getName());
        assertEquals(ModuleType.GOSSIP, udupMessage.getContent().getDestinationModule());
        GossipGirlMessage gossipMessage = (GossipGirlMessage) udupMessage.getContent();
        assertEquals(type, gossipMessage.getType());
    }
}
