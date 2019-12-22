package pl.edu.mimuw.cloudatlas.agent.message;

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

    private String requestId;
    private AgentModule destinationModule;
    private long timestamp;

    public AgentMessage(String requestId, AgentModule destinationModule, long timestamp) {
        this.requestId = requestId;
        this.destinationModule = destinationModule;
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
