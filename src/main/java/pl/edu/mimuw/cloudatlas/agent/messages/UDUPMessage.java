package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class UDUPMessage extends AgentMessage {
    private ValueContact contact;
    private AgentMessage content;

    public UDUPMessage(String messageId, long timestamp, ValueContact contact, AgentMessage content) {
        super(messageId, ModuleType.UDP, timestamp);
        this.contact = contact;
        this.content = content;
    }

    public UDUPMessage(String messageId, ValueContact contact, AgentMessage content) {
        super(messageId, ModuleType.UDP);
        this.contact = contact;
        this.content = content;
    }

    public UDUPMessage() {
        super("", ModuleType.UDP);
    }

    @Override
    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }

    public AgentMessage getContent() {
        return content;
    }

    public void setContent(AgentMessage content) {
        this.content = content;
    }

    public ValueContact getContact() { return contact; }

    public void setContact(ValueContact contact) {
        this.contact = contact;
    }
}
