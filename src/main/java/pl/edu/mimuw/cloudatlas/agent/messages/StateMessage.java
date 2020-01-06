package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class StateMessage extends ResponseMessage {
    private ZMI zmi;
    private Map<Attribute, Entry<ValueQuery, ValueTime>> queries;

    public StateMessage(String messageId, ModuleType destinationModule, long timestamp, long requestId, ZMI zmi, Map<Attribute, Entry<ValueQuery, ValueTime>> queries) {
        super(messageId, destinationModule, timestamp, Type.STATE, requestId);
        this.zmi = zmi;
        this.queries = queries;
    }

    public StateMessage() {}

    public ZMI getZMI() {
        return zmi;
    }

    public Map<Attribute, Entry<ValueQuery, ValueTime>> getQueries() {
        return queries;
    }
}
