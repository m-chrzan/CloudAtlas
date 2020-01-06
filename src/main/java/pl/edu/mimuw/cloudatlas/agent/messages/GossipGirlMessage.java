package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public abstract class GossipGirlMessage extends AgentMessage {
    public enum Type {
        INITIATE
    }

    private Type type;

    public GossipGirlMessage(String messageId, long timestamp, Type type) {
        super(messageId, ModuleType.GOSSIP, timestamp);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }
}
