package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public abstract class QurnikMessage extends AgentMessage {
    public enum Type {
        RUN_QUERIES
    }

    private Type type;

    public QurnikMessage(String messageId, long timestamp, Type type) {
        super(messageId, ModuleType.QUERY, timestamp);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }
}
