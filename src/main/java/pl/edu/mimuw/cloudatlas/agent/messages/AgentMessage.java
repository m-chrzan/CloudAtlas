package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public abstract class AgentMessage {
    private String messageId;
    private ModuleType destinationModule;
    private long timestamp;

    public AgentMessage(String messageId, ModuleType destinationModule, long timestamp) {
        this.messageId = messageId;
        this.destinationModule = destinationModule;
        this.timestamp = timestamp;
    }

    public AgentMessage(String messageId, ModuleType destinationModule) {
        this.messageId = messageId;
        this.destinationModule = destinationModule;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public AgentMessage() {}

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public ModuleType getDestinationModule() {
        return destinationModule;
    }

    public void setDestinationModule(ModuleType destinationModule) {
        this.destinationModule = destinationModule;
    }

    public abstract void callMe(Module module) throws InterruptedException, Module.InvalidMessageType;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
