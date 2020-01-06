package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public class GetStateMessage extends StanikMessage {
    private ModuleType requestingModule;
    private long requestId;

    public GetStateMessage(String messageId, long timestamp, ModuleType requestingModule, long requestId) {
        super(messageId, timestamp, Type.GET_STATE);
        this.requestingModule = requestingModule;
        this.requestId = requestId;
    }

    public GetStateMessage() {}

    public ModuleType getRequestingModule() {
        return requestingModule;
    }

    public long getRequestId() {
        return requestId;
    }
}
