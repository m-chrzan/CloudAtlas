package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.List;
import java.util.LinkedList;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.QurnikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RunQueriesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.AttributesUtil;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ValueUtils;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Qurnik extends Module {
    public Qurnik() {
        super(ModuleType.QUERY);
    }

    @Override
    public void handleTyped(QurnikMessage message) throws InterruptedException, InvalidMessageType {
        switch (message.getType()) {
            case RUN_QUERIES:
                handleRunQueries((RunQueriesMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Qurnik");
        }
    }

    @Override
    public void handleTyped(ResponseMessage message) throws InterruptedException, InvalidMessageType {
        switch (message.getType()) {
            case STATE:
                runQueriesOnState((StateMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Qurnik");
        }
    }

    private void handleRunQueries(RunQueriesMessage message) throws InterruptedException {
        GetStateMessage getStateMessage = new GetStateMessage("", 0, ModuleType.QUERY, 0);
        sendMessage(getStateMessage);
    }

    private void runQueriesOnState(StateMessage message) throws InterruptedException {
        List<ValueQuery> queries = new LinkedList();
        for (Entry<ValueQuery, ValueTime> timestampedQuery : message.getQueries().values()) {
            queries.add(timestampedQuery.getKey());
        }
        executeAllQueries(message.getZMI(), queries, PathName.ROOT);
    }

    private void executeAllQueries(ZMI zmi, List<ValueQuery> queries, PathName currentPath) throws InterruptedException {
        if(!zmi.getSons().isEmpty()) {
            for(ZMI son : zmi.getSons()) {
                Value sonName = son.getAttributes().getOrNull("name");
                if (ValueUtils.valueNonNullOfType(sonName, TypePrimitive.STRING)) {
                    String sonNameString = ((ValueString) sonName).getValue();
                    executeAllQueries(son, queries, currentPath.levelDown(sonNameString));
                } else {
                    System.out.println("ERROR: zone without a name attribute found while executing queries");
                }
            }

            Interpreter interpreter = new Interpreter(zmi);
            AttributesMap newAttributes = new AttributesMap();
            for (ValueQuery query : queries) {
                try {
                    List<QueryResult> result = interpreter.interpretProgram(query.getQuery());
                    for(QueryResult r : result) {
                        newAttributes.addOrChange(r.getName(), r.getValue());
                    }
                } catch(InterpreterException exception) {
                    System.out.println("ERROR: thrown while running interpreter: " + exception.getMessage());
                }
            }

            if (!currentPath.toString().equals("/")) {
                newAttributes.add("name", new ValueString(currentPath.getSingletonName()));
            }
            long currentTime = System.currentTimeMillis();
            newAttributes.add("timestamp", new ValueTime(currentTime));

            AttributesUtil.transferAttributes(newAttributes, zmi.getAttributes());

            UpdateAttributesMessage message = new UpdateAttributesMessage("", currentTime, currentPath.toString(), newAttributes);
            sendMessage(message);
        }
    }
}
