package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class StateMessage extends ResponseMessage {
    private ZMI zmi;
    private Map<Attribute, Entry<ValueQuery, ValueTime>> queries;
    private Set<ValueContact> contacts;

    public StateMessage(String messageId, ModuleType destinationModule, long timestamp, long requestId, ZMI zmi, Map<Attribute, Entry<ValueQuery, ValueTime>> queries, Set<ValueContact> contacts) {
        super(messageId, destinationModule, timestamp, Type.STATE, requestId);
        this.zmi = zmi;
        this.queries = queries;
        this.contacts = contacts;
    }

    public StateMessage() {}

    public ZMI getZMI() {
        return zmi;
    }

    public Map<Attribute, Entry<ValueQuery, ValueTime>> getQueries() {
        return queries;
    }

    public Set<ValueContact> getContacts() {
        return contacts;
    }
}
