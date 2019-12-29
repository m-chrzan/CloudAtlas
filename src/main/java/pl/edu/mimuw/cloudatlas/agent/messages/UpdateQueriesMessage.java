package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class UpdateQueriesMessage extends StanikMessage {
    private Map<Attribute, Entry<ValueQuery, ValueTime>> queries;

    public UpdateQueriesMessage(String messageId, long timestamp, Map<Attribute, Entry<ValueQuery, ValueTime>> queries) {
        super(messageId, timestamp, Type.UPDATE_QUERIES);
        this.queries = queries;
    }

    public Map<Attribute, Entry<ValueQuery, ValueTime>> getQueries() {
        return queries;
    }
}
