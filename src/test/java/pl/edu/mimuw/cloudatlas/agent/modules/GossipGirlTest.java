package pl.edu.mimuw.cloudatlas.agent.modules;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GossipGirlMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.InitiateGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;

public class GossipGirlTest {
    private GossipGirl gossipGirl;
    private MockExecutor executor;

    @Before
    public void setupLocals() {
        gossipGirl = new GossipGirl();
        executor = new MockExecutor(gossipGirl);
    }

    @Test
    public void initiateGossipRequestsState() throws Exception {
        InitiateGossipMessage message = new InitiateGossipMessage("test_msg", 0);
        gossipGirl.handleTyped(message);

        AgentMessage receivedMessage = executor.messagesToPass.poll();
        assertNotNull(receivedMessage);
        assertEquals(ModuleType.STATE, receivedMessage.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) receivedMessage;
        assertEquals(StanikMessage.Type.GET_STATE, stanikMessage.getType());
        GetStateMessage getStateMessage = (GetStateMessage) stanikMessage;
        assertEquals(ModuleType.GOSSIP, getStateMessage.getRequestingModule());
    }
}
