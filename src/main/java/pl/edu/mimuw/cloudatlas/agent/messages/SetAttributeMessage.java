package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class SetAttributeMessage extends StanikMessage {
    private String pathName;
    private Attribute attribute;
    private Value value;
    private ValueTime updateTimestamp;

    public SetAttributeMessage(String messageId, long timestamp, String pathName, Attribute attribute, Value value, ValueTime updateTimestamp) {
        super(messageId, timestamp, Type.SET_ATTRIBUTE);
        this.pathName = pathName;
        this.attribute = attribute;
        this.value = value;
        this.updateTimestamp = updateTimestamp;
    }

    public String getPathName() {
        return pathName;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Value getValue() {
        return value;
    }

    public ValueTime getUpdateTimestamp() {
        return updateTimestamp;
    }
}
