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
import pl.edu.mimuw.cloudatlas.agent.messages.HejkaMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GossipGirlMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.InitiateGossipMessage;
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
    private ZMI initiatorHierarchy;
    private ValueTime testTime;
    private Map<Attribute, Entry<ValueQuery, ValueTime>> initiatorQueries;
    private StateMessage initiatorStateMessage;

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
        assertNotNull(receivedMessage);
        assertEquals(ModuleType.UDP, receivedMessage.getDestinationModule());
        UDUPMessage udupMessage = (UDUPMessage) receivedMessage;
        assertEquals(new PathName("/son/bro"), udupMessage.getContact().getName());
        assertEquals(ModuleType.GOSSIP, udupMessage.getContent().getDestinationModule());
        GossipGirlMessage gossipMessage = (GossipGirlMessage) udupMessage.getContent();

        assertEquals(GossipGirlMessage.Type.HEJKA, gossipMessage.getType());
        HejkaMessage hejkaMessage = (HejkaMessage) gossipMessage;
        assertEquals(0, hejkaMessage.getSenderGossipId());
        System.out.println(hejkaMessage.getZoneTimestamps().keySet());
        assertEquals(3, TestUtil.iterableSize(hejkaMessage.getZoneTimestamps().keySet()));
        Set<PathName> zoneSet = hejkaMessage.getZoneTimestamps().keySet();
        assertThat(zoneSet, hasItems(new PathName("/daughter")));
        assertThat(zoneSet, hasItems(new PathName("/son/sis")));
        assertThat(zoneSet, hasItems(new PathName("/son/grand")));

        assertEquals(2, TestUtil.iterableSize(hejkaMessage.getQueryTimestamps().keySet()));
    }
}
