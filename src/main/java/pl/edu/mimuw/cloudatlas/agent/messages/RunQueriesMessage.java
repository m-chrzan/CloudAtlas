package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public class RunQueriesMessage extends QurnikMessage {
    public RunQueriesMessage(String messageId, long timestamp) {
        super(messageId, timestamp, Type.RUN_QUERIES);
    }
}
