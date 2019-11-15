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
import pl.edu.mimuw.cloudatlas.api.Api;

public class ApiImplementation implements Api {
    public Set<String> getZoneSet() throws RemoteException {
        return null;
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
