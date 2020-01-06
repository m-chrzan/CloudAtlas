package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;

public class UpdateAttributesMessage extends StanikMessage {
    private String pathName;
    private AttributesMap attributes;

    public UpdateAttributesMessage(String messageId, long timestamp, String pathName, AttributesMap attributes) {
        super(messageId, timestamp, Type.UPDATE_ATTRIBUTES);
        this.pathName = pathName;
        this.attributes = attributes;
    }

    public UpdateAttributesMessage() {}

    public String getPathName() {
        return pathName;
    }

    public AttributesMap getAttributes() {
        return attributes;
    }
}
