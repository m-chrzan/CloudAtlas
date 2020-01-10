package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.concurrent.CompletableFuture;

public class RequestStateMessage extends RemikMessage {
    CompletableFuture<ResponseMessage> responseFuture;

    public RequestStateMessage(String messageId, long timestamp, CompletableFuture<ResponseMessage> responseFuture) {
        super(messageId, timestamp, Type.REQUEST_STATE);
        this.responseFuture = responseFuture;
    }

    public RequestStateMessage() {}

    public CompletableFuture<ResponseMessage> getFuture() {
        return responseFuture;
    }
}
