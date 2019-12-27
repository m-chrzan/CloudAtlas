package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public abstract class StanikMessage extends AgentMessage {
    public enum Type {
        GET_HIERARCHY
    }

    private Type type;

    public StanikMessage(String messageId, long timestamp, Type type) {
        super(messageId, ModuleType.STATE, timestamp);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }
}
