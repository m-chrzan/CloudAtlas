package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public abstract class ResponseMessage extends AgentMessage {
    public enum Type {
        STATE
    }

    Type type;
    long requestId;

    public ResponseMessage(String messageId, ModuleType destinationModule, long timestamp, Type type, long requestId) {
        super(messageId, destinationModule, timestamp);
        this.type = type;
        this.requestId = requestId;
    }

    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }

    public long getRequestId() {
        return requestId;
    }

    public Type getType() {
        return type;
    }
}
