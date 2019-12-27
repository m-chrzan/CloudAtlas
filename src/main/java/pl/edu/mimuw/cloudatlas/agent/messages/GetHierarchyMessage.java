package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public class GetHierarchyMessage extends StanikMessage {
    private ModuleType requestingModule;
    private long requestId;

    public GetHierarchyMessage(String messageId, long timestamp, ModuleType requestingModule, long requestId) {
        super(messageId, timestamp, Type.GET_HIERARCHY);
        this.requestingModule = requestingModule;
        this.requestId = requestId;
    }

    public ModuleType getRequestingModule() {
        return requestingModule;
    }

    public long getRequestId() {
        return requestId;
    }
}
