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

import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.model.*;
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

    public void installQuery(String name, String queryCode, byte[] querySignature) throws RemoteException {
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

    public void uninstallQuery(String queryName, byte[] querySignature) throws RemoteException {
        try {
            Attribute attributeName = new Attribute(queryName);
            ValueTime timestamp = new ValueTime(System.currentTimeMillis());
            Map<Attribute, Entry<ValueQuery, ValueTime>> queries = new HashMap();
            queries.put(attributeName, new SimpleImmutableEntry(null, timestamp));
            UpdateQueriesMessage message = new UpdateQueriesMessage("", 0, queries);
            eventBus.addMessage(message);
        } catch (Exception e) {
            System.out.println("ERROR: failed to remove query");
            throw new RemoteException("Failed to uninstall query", e);
        }
    }

    public void setAttributeValue(String zoneName, String attributeName, Value value) throws RemoteException {
        try {
            SetAttributeMessage message = new SetAttributeMessage("", 0, zoneName, new Attribute(attributeName), value, new ValueTime(System.currentTimeMillis()));
            eventBus.addMessage(message);
        } catch (Exception e) {
            System.out.println("ERROR: failed to set attribute");
            throw new RemoteException("Failed to set attribute", e);
        }
    }

    public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
        try {
            UpdateContactsMessage message = new UpdateContactsMessage("", System.currentTimeMillis(), contacts);
            eventBus.addMessage(message);
        } catch (Exception e) {
            System.out.println("ERROR: failed to set contacts");
            throw new RemoteException("Failed to set contacts", e);
        }
    }
}
