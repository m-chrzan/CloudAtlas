package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Set;

import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class UpdateContactsMessage extends StanikMessage {
    private Set<ValueContact> contacts;

    public UpdateContactsMessage(String messageId, long timestamp, Set<ValueContact> contacts) {
        super(messageId, timestamp, Type.UPDATE_CONTACTS);
        this.contacts = contacts;
    }

    public UpdateContactsMessage() {}

    public Set<ValueContact> getContacts() {
        return contacts;
    }
}
