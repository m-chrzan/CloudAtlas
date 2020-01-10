package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class UDUPMessage extends AgentMessage {
    private ValueContact contact;
    private RemoteGossipGirlMessage content;

    public UDUPMessage(String messageId, long timestamp, ValueContact contact, RemoteGossipGirlMessage content) {
        super(messageId, ModuleType.UDP, timestamp);
        this.contact = contact;
        this.content = content;
    }

    public UDUPMessage(String messageId, ValueContact contact, RemoteGossipGirlMessage content) {
        super(messageId, ModuleType.UDP);
        this.contact = contact;
        this.content = content;
    }

    public UDUPMessage() {}

    @Override
    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }

    public RemoteGossipGirlMessage getContent() {
        return content;
    }

    public void setContent(RemoteGossipGirlMessage content) {
        this.content = content;
    }

    public ValueContact getContact() { return contact; }

    public void setContact(ValueContact contact) {
        this.contact = contact;
    }
}
