package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;


import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RemikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RequestStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;

/*
 * Remik is a cute little module that allows RMI functions to interface with
 * agent's asynchronous modules.
 */
public class Remik extends Module {
    private Map<Long, CompletableFuture<ResponseMessage>> awaitingRequests;
    private long nextRequestId = 0;

    public Remik() {
        super(ModuleType.RMI);
        awaitingRequests = new HashMap();
    }

    public void handleTyped(RemikMessage message) throws InvalidMessageType, InterruptedException {
        switch (message.getType()) {
            case REQUEST_STATE:
                handleRequestState((RequestStateMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Remik");
        }
    }

    public void handleTyped(ResponseMessage message) {
        CompletableFuture<ResponseMessage> responseFuture = awaitingRequests.get(message.getRequestId());

        if (responseFuture == null) {
            System.out.println("ERROR: Remik got response for nonexistent/finished request");
        } else {
            responseFuture.complete(message);
        }
    }

    private void handleRequestState(RequestStateMessage message) throws InterruptedException {
        awaitingRequests.put(nextRequestId, message.getFuture());

        GetStateMessage getStateMessage = new GetStateMessage("", 0, ModuleType.RMI, nextRequestId);
        nextRequestId++;

        sendMessage(getStateMessage);
    }
}
