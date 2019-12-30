package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class RemoveZMIMessage extends StanikMessage {
    private String pathName;
    private ValueTime removalTimestamp;

    public RemoveZMIMessage(String messageId, long timestamp, String pathName, ValueTime removalTimestamp) {
        super(messageId, timestamp, Type.REMOVE_ZMI);
        this.pathName = pathName;
        this.removalTimestamp = removalTimestamp;
    }

    public String getPathName() {
        return pathName;
    }

    public ValueTime getRemovalTimestamp() {
        return removalTimestamp;
    }
}
