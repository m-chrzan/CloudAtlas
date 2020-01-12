package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;

public class QueryMessage extends RemoteGossipGirlMessage {
    private Attribute name;
    private ValueQuery query;
    private long receiverGossipId;
    private ValueDuration offset;

    public QueryMessage(String messageId, long timestamp, Attribute name, ValueQuery query, long receiverGossipId, ValueDuration offset) {
        super(messageId, timestamp, Type.QUERY);
        this.name = name;
        this.query = query;
        this.receiverGossipId = receiverGossipId;
        this.offset = offset;
    }

    public QueryMessage() {}

    public Attribute getName() {
        return name;
    }

    public ValueQuery getQuery() {
        return query;
    }

    public long getReceiverGossipId() {
        return receiverGossipId;
    }

    public ValueDuration getOffset() {
        return offset;
    }
}
