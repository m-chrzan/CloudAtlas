package pl.edu.mimuw.cloudatlas.agent;

import java.io.PrintStream;

import java.rmi.RemoteException;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import pl.edu.mimuw.cloudatlas.agent.messages.RequestStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateQueriesMessage;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.api.Api;

public class NewApiImplementation implements Api {
    private EventBus eventBus;

    public NewApiImplementation(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Set<String> getZoneSet() throws RemoteException {
        CompletableFuture<ResponseMessage> responseFuture = new CompletableFuture();
        RequestStateMessage message = new RequestStateMessage("", 0, responseFuture);
        try {
            eventBus.addMessage(message);
            ResponseMessage response = responseFuture.get();

            if (response.getType() == ResponseMessage.Type.STATE) {
                StateMessage stateMessage = (StateMessage) response;
                Set<String> zones = new HashSet<String>();
                collectZoneNames(stateMessage.getZMI(), zones);
                return zones;
            } else {
                System.out.println("ERROR: getZoneSet didn't receive a StateMessage");
                throw new Exception("Failed to retrieve zone set");
            }
        } catch (Exception e) {
            System.out.println("ERROR: exception caught in getZoneSet");
            throw new RemoteException(e.getMessage());
        }
    }

    private void collectZoneNames(ZMI zone, Set<String> names) {
        names.add(zone.getPathName().toString());
        for (ZMI son : zone.getSons()) {
            collectZoneNames(son, names);
        }
    }

    public AttributesMap getZoneAttributeValues(String zoneName) throws RemoteException {
        CompletableFuture<ResponseMessage> responseFuture = new CompletableFuture();
        RequestStateMessage message = new RequestStateMessage("", 0, responseFuture);
        try {
            eventBus.addMessage(message);
            ResponseMessage response = responseFuture.get();

            if (response.getType() == ResponseMessage.Type.STATE) {
                StateMessage stateMessage = (StateMessage) response;
                return stateMessage.getZMI().findDescendant(zoneName).getAttributes();
            } else {
                System.out.println("ERROR: getZoneSet didn't receive a StateMessage");
                throw new Exception("Failed to retrieve zone set");
            }
        } catch (Exception e) {
            System.out.println("ERROR: exception caught in getZoneSet");
            throw new RemoteException(e.getMessage());
        }
    }

    public void installQuery(String name, String queryCode) throws RemoteException {
        Pattern queryNamePattern = Pattern.compile("&[a-zA-Z][\\w_]*");
        Matcher matcher = queryNamePattern.matcher(name);
        if (!matcher.matches()) {
            throw new RemoteException("Invalid query identifier");
        }
        try {
            ValueQuery query = new ValueQuery(queryCode);
            Attribute attributeName = new Attribute(name);
            ValueTime timestamp = new ValueTime(System.currentTimeMillis());
            Map<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap();
            queries.put(attributeName, new SimpleImmutableEntry(query, timestamp));
            UpdateQueriesMessage message = new UpdateQueriesMessage("", 0, queries);
            eventBus.addMessage(message);
        } catch (Exception e) {
            throw new RemoteException("Failed to install query", e);
        }
    }

    public void uninstallQuery(String queryName) throws RemoteException {
        // uninstallQueryInHierarchy(root, new Attribute(queryName));
    }

    private void uninstallQueryInHierarchy(ZMI zmi, Attribute queryName) {
        /*
        if (!zmi.getSons().isEmpty()) {
            zmi.getAttributes().remove(queryName);
            for (ZMI son : zmi.getSons()) {
                uninstallQueryInHierarchy(son, queryName);
            }
        }
        */
    }

    public void setAttributeValue(String zoneName, String attributeName, Value value) throws RemoteException {
        /*
        try {
            ZMI zmi = root.findDescendant(new PathName(zoneName));
            zmi.getAttributes().addOrChange(new Attribute(attributeName), value);
            executeAllQueries(root);
        } catch (ZMI.NoSuchZoneException e) {
            throw new RemoteException("Zone not found", e);
        }
        */
    }

    private void executeAllQueries(ZMI zmi) {
        /*
        if(!zmi.getSons().isEmpty()) {
            for(ZMI son : zmi.getSons()) {
                executeAllQueries(son);
            }

            Interpreter interpreter = new Interpreter(zmi);
            for (ValueQuery query : getQueries(zmi)) {
                try {
                    List<QueryResult> result = interpreter.interpretProgram(query.getQuery());
                    for(QueryResult r : result) {
                        zmi.getAttributes().addOrChange(r.getName(), r.getValue());
                    }
                } catch(InterpreterException exception) {}
            }
        }
        */
    }

    private Set<ValueQuery> getQueries(ZMI zmi) {
        Set<ValueQuery> querySet = new HashSet<ValueQuery>();
        /*
        for (Map.Entry<Attribute, Value> attribute : zmi.getAttributes()) {
            if (attribute.getValue().getType().getPrimaryType() == Type.PrimaryType.QUERY) {
                querySet.add((ValueQuery) attribute.getValue());
            }
        }
        */

        return querySet;
    }

    public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
        // this.contacts = contacts;
    }
}
