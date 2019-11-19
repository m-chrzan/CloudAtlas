package pl.edu.mimuw.cloudatlas.agent;

import java.io.PrintStream;

import java.rmi.RemoteException;

import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.api.Api;

public class ApiImplementation implements Api {
    ZMI root;
    Set<ValueContact> contacts;

    public ApiImplementation(ZMI root) {
        this.root = root;
        this.contacts = new HashSet<ValueContact>();
    }

    public Set<String> getZoneSet() throws RemoteException {
        Set<String> zones = new HashSet<String>();
        collectZoneNames(root, zones);
        return zones;
    }

    private void collectZoneNames(ZMI zone, Set<String> names) {
        names.add(zone.getPathName().toString());
        for (ZMI son : zone.getSons()) {
            collectZoneNames(son, names);
        }
    }

    public AttributesMap getZoneAttributeValues(String zoneName) throws RemoteException {
        try {
            ZMI zmi = root.findDescendant(new PathName(zoneName));
            return zmi.getAttributes();
        } catch (ZMI.NoSuchZoneException e) {
            throw new RemoteException("Zone not found", e);
        }
    }

    public void installQuery(String name, String queryCode) throws RemoteException {
        try {
            ValueQuery query = new ValueQuery(queryCode);
            Attribute attributeName = new Attribute(name);
            installQueryInHierarchy(root, attributeName, query);
            executeAllQueries(root);
        } catch (Exception e) {
            throw new RemoteException("Failed to install query", e);
        }
    }

    private void installQueryInHierarchy(ZMI zmi, Attribute queryName, ValueQuery query) {
        if (!zmi.getSons().isEmpty()) {
            zmi.getAttributes().addOrChange(queryName, query);
            for (ZMI son : zmi.getSons()) {
                installQueryInHierarchy(son, queryName, query);
            }
        }
    }

    public void uninstallQuery(String queryName) throws RemoteException {
        uninstallQueryInHierarchy(root, new Attribute(queryName));
    }

    private void uninstallQueryInHierarchy(ZMI zmi, Attribute queryName) {
        if (!zmi.getSons().isEmpty()) {
            zmi.getAttributes().remove(queryName);
            for (ZMI son : zmi.getSons()) {
                uninstallQueryInHierarchy(son, queryName);
            }
        }
    }

    public void setAttributeValue(String zoneName, String attributeName, Value value) throws RemoteException {
        try {
            ZMI zmi = root.findDescendant(new PathName(zoneName));
            zmi.getAttributes().addOrChange(new Attribute(attributeName), value);
            executeAllQueries(root);
        } catch (ZMI.NoSuchZoneException e) {
            throw new RemoteException("Zone not found", e);
        }
    }

    private void executeAllQueries(ZMI zmi) {
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
    }

    private Set<ValueQuery> getQueries(ZMI zmi) {
        Set<ValueQuery> querySet = new HashSet<ValueQuery>();
        for (Map.Entry<Attribute, Value> attribute : zmi.getAttributes()) {
            if (attribute.getValue().getType().getPrimaryType() == Type.PrimaryType.QUERY) {
                querySet.add((ValueQuery) attribute.getValue());
            }
        }

        return querySet;
    }

    public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
        this.contacts = contacts;
    }
}
