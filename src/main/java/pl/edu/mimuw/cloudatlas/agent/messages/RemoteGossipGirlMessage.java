package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class RemoteGossipGirlMessage extends GossipGirlMessage {
    private ValueTime sentTimestamp;
    private ValueTime receivedTimestamp;

    public RemoteGossipGirlMessage(String messageId, long timestamp, Type type) {
        super(messageId, timestamp, type);
    }

    public RemoteGossipGirlMessage() {};

    public void setSentTimestamp(ValueTime sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public void setReceivedTimestamp(ValueTime receivedTimestamp) {
        this.receivedTimestamp = receivedTimestamp;
    }

    public ValueTime getSentTimestamp() {
        return sentTimestamp;
    }

    public ValueTime getReceivedTimestamp() {
        return receivedTimestamp;
    }
}
