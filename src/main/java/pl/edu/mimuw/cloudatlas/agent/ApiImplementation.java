package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.RemoteException;

import java.util.Set;
import java.util.HashSet;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.api.Api;

public class ApiImplementation implements Api {
    ZMI root;

    public ApiImplementation(ZMI root) {
        this.root = root;
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

    public void setAttributeValue(String attributeName, Value value) throws RemoteException {
    }

    public void setFallbackContacts(Set<ValueContact> serializedContacts) throws RemoteException {
    }
}
