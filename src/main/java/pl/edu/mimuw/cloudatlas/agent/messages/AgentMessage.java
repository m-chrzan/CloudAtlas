package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.Agent;

public class AgentMessage {

    public enum AgentModule {
        TIMER_SCHEDULER,
        TIMER_GTP,
        RMI,
        UDP,
        GOSSIP_IN,
        GOSSIP_OUT,
        STATE,
        QUERY
    }

    private String messageId;
    private AgentModule destinationModule;
    private long timestamp;

    public AgentMessage(String messageId, AgentModule destinationModule, long timestamp) {
        this.messageId = messageId;
        this.destinationModule = destinationModule;
        this.timestamp = timestamp;
    }

    public AgentMessage(String messageId, AgentModule destinationModule) {
        this.messageId = messageId;
        this.destinationModule = destinationModule;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public AgentModule getDestinationModule() {
        return destinationModule;
    }

    public void setDestinationModule(AgentModule destinationModule) {
        this.destinationModule = destinationModule;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
