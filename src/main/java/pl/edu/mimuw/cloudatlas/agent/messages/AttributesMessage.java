package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;

public class AttributesMessage extends RemoteGossipGirlMessage {
    private PathName path;
    private AttributesMap attributes;
    private long receiverGossipId;

    public AttributesMessage(String messageId, long timestamp, PathName path, AttributesMap attributes, long receiverGossipId) {
        super(messageId, timestamp, Type.ATTRIBUTES);
        this.path = path;
        this.attributes = attributes;
        this.receiverGossipId = receiverGossipId;
    }

    public PathName getPath() {
        return path;
    }

    public AttributesMap getAttributes() {
        return attributes;
    }

    public long getReceiverGossipId() {
        return receiverGossipId;
    }
}
