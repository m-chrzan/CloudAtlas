package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class UDUPMessage extends AgentMessage {
    private ValueContact contact;
    private AgentMessage content;
    private int retry;
    private String conversationId;

    public UDUPMessage(String messageId, long timestamp, ValueContact contact, AgentMessage content, int retry, String conversationId) {
        super(messageId, ModuleType.UDP, timestamp);
        this.contact = contact;
        this.content = content;
        this.retry = retry;
        this.conversationId = conversationId;
    }

    public UDUPMessage(String messageId, ValueContact contact, AgentMessage content, int retry, String conversationId) {
        super(messageId, ModuleType.UDP);
        this.contact = contact;
        this.content = content;
        this.retry = retry;
        this.conversationId = conversationId;
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

    public int getRetry() {
        return retry;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setRetry(int retry) { this.retry = retry; }

    public ValueContact getContact() { return contact; }

    public void setContact(ValueContact contact) {
        this.contact = contact;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
