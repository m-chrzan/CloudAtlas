package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;

public class QueryMessage extends RemoteGossipGirlMessage {
    private Attribute name;
    private ValueQuery query;
    private long receiverGossipId;

    public QueryMessage(String messageId, long timestamp, Attribute name, ValueQuery query, long receiverGossipId) {
        super(messageId, timestamp, Type.QUERY);
        this.name = name;
        this.query = query;
        this.receiverGossipId = receiverGossipId;
    }

    public Attribute getName() {
        return name;
    }

    public ValueQuery getQuery() {
        return query;
    }

    public long getReceiverGossipId() {
        return receiverGossipId;
    }
}
