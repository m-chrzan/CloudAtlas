package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class CleanOldGossipsMessage extends GossipGirlMessage {
    private ValueTime ageThreshold;

    public CleanOldGossipsMessage(String messageId, long timestamp, ValueTime ageThreshold) {
        super(messageId, timestamp, Type.CLEAN);
        this.ageThreshold = ageThreshold;
    }

    public ValueTime getAgeThreshold() {
        return ageThreshold;
    }
}
