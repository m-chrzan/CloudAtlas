package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.RemoteException;

import java.util.Set;
import java.util.HashSet;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.Value;
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

    public AttributesMap getZoneAttributeValue(String zoneName) throws RemoteException {
        return null;
    }

    public void installQuery(String queryName, String query) throws RemoteException {
    }

    public void uninstallQuery(String queryName) throws RemoteException {
    }

    public void setAttributeValue(String attributeName, Value value) throws RemoteException {
    }

    public void setFallbackContacts(Set<ValueContact> serializedContacts) throws RemoteException {
    }
}
