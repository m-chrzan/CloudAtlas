package pl.edu.mimuw.cloudatlas.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;

/**
 *
 * from: https://www.mimuw.edu.pl/~iwanicki/courses/ds/2019/labs/04/

 Returning the set of zones on which the agent stores information.
 Returning the values of attributes of a given zone.
 Installing a query on the agent. We assume that the query is installed in all zones of the agent.
 Uninstalling a query on the agent. Again, the query is uninstalled from all zones of the agent.
 Setting the values of attributes of a given zone (this operation should be allowed only for the singleton zones).
 Setting the fallback contacts. These contacts are stored aside from the ZMIs, in a dedicated set. Each invocation of the function overrides this set.

 */
import java.util.Set;

public interface Api extends Remote {

    public Set<String> getZoneSet() throws RemoteException;

    public AttributesMap getZoneAttributeValue(String zoneName) throws RemoteException;

    public void installQuery(String queryName, String query) throws RemoteException;

    public void uninstallQuery(String queryName) throws RemoteException;

    public void setAttributeValue(String attributeName, Value value) throws RemoteException;

    public void setFallbackContacts(Set<ValueContact> serializedContacts) throws RemoteException;

}
