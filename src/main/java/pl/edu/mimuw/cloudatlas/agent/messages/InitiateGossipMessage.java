package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class InitiateGossipMessage extends GossipGirlMessage {
    private PathName ourPath;
    private ValueContact theirContact;

    public InitiateGossipMessage(String messageId, long timestamp, PathName ourPath, ValueContact theirContact) {
        super(messageId, timestamp, Type.INITIATE);
        this.ourPath = ourPath;
        this.theirContact = theirContact;
    }

    public PathName getOurPath() {
        return ourPath;
    }

    public ValueContact getTheirContact() {
        return theirContact;
    }
}
