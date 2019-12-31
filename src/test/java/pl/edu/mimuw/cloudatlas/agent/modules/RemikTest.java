package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RequestStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.MockExecutor;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RemikTest {
    private Remik remik;
    private MockExecutor executor;

    @Before
    public void setupLocals() {
        remik = new Remik();
        executor = new MockExecutor(remik);
    }

    @Test
    public void asksForStateOnStateRequest() throws Exception {
        CompletableFuture<ResponseMessage> future = new CompletableFuture();
        RequestStateMessage message = new RequestStateMessage("", 0, future);
        remik.handleTyped(message);
        AgentMessage receivedMessage = (AgentMessage) executor.messagesToPass.take();
        assertEquals(ModuleType.STATE, receivedMessage.getDestinationModule());
        StanikMessage stanikMessage = (StanikMessage) receivedMessage;
        assertEquals(StanikMessage.Type.GET_STATE, stanikMessage.getType());
        GetStateMessage getStateMessage = (GetStateMessage) stanikMessage;
        assertEquals(ModuleType.RMI, getStateMessage.getRequestingModule());
    }

    @Test
    public void completesFutureOnReceivedState() throws Exception {
        CompletableFuture<ResponseMessage> future = new CompletableFuture();
        RequestStateMessage message = new RequestStateMessage("", 0, future);
        remik.handleTyped(message);

        ZMI zmi = new ZMI();
        zmi.getAttributes().add("timestamp", new ValueTime(42l));
        StateMessage response = new StateMessage("", ModuleType.RMI, 0, 0, zmi, new HashMap());
        remik.handleTyped(response);

        ResponseMessage passedResponse = future.get(100, TimeUnit.MILLISECONDS);
        assertNotNull(passedResponse);
        assertEquals(ResponseMessage.Type.STATE, passedResponse.getType());
        StateMessage stateMessage = (StateMessage) passedResponse;
        assertEquals(new ValueTime(42l), stateMessage.getZMI().getAttributes().get("timestamp"));
    }
}
