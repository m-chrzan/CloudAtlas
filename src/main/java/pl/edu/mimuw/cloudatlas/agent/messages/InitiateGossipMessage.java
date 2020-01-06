package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public class InitiateGossipMessage extends GossipGirlMessage {
    private long nextGossipId = 0;

    public InitiateGossipMessage(String messageId, long timestamp) {
        super(messageId, timestamp, Type.INITIATE);
    }
}
