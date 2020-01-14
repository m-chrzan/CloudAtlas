package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.RemoteException;

import java.security.PublicKey;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.querysigner.*;

public class NewApiImplementation implements Api {
    private EventBus eventBus;
    private PublicKey publicKey;

    public NewApiImplementation(EventBus eventBus) {
        this.eventBus = eventBus;
        String publicKeyFile = System.getProperty("public_key_file");
        publicKey = KeyUtils.getPublicKey(publicKeyFile);
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

    public void installQuery(String name, QueryData query) throws RemoteException {
        try {
            QueryUtils.validateQueryName(name);
            QuerySignerApiImplementation.validateInstallQuery(name, query, this.publicKey);
            Attribute attributeName = new Attribute(name);
            ValueTime timestamp = new ValueTime(System.currentTimeMillis());
            Map<Attribute, ValueQuery> queries = new HashMap();
            queries.put(attributeName, new ValueQuery(query));
            UpdateQueriesMessage message = new UpdateQueriesMessage("", 0, queries);
            eventBus.addMessage(message);
        } catch (Exception e) {
            throw new RemoteException("Failed to install query", e);
        }
    }

    public void uninstallQuery(String queryName, QueryData query) throws RemoteException {
        try {
            QueryUtils.validateQueryName(queryName);
            QuerySignerApiImplementation.validateUninstallQuery(queryName, query, this.publicKey);
            Attribute attributeName = new Attribute(queryName);
            ValueTime timestamp = new ValueTime(System.currentTimeMillis());
            Map<Attribute, ValueQuery> queries = new HashMap();
            queries.put(attributeName, new ValueQuery(query));
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
