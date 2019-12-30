package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public abstract class RemikMessage extends AgentMessage {
    public enum Type {
        REQUEST_STATE
    }

    private Type type;

    public RemikMessage(String messageId, long timestamp, Type type) {
        super(messageId, ModuleType.RMI, timestamp);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }
}
