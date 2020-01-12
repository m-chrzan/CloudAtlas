package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.PathName;

public class AttributesMessage extends RemoteGossipGirlMessage {
    private PathName path;
    private AttributesMap attributes;
    private long receiverGossipId;
    private ValueDuration offset;

    public AttributesMessage(String messageId, long timestamp, PathName path, AttributesMap attributes, long receiverGossipId, ValueDuration offset) {
        super(messageId, timestamp, Type.ATTRIBUTES);
        this.path = path;
        this.attributes = attributes;
        this.receiverGossipId = receiverGossipId;
        this.offset = offset;
    }

    private AttributesMessage() {}

    public PathName getPath() {
        return path;
    }

    public AttributesMap getAttributes() {
        return attributes;
    }

    public long getReceiverGossipId() {
        return receiverGossipId;
    }

    public ValueDuration getOffset() {
        return offset;
    }
}
