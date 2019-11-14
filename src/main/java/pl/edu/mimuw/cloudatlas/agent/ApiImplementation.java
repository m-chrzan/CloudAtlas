package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.RemoteException;

import java.util.Set;
import java.util.HashSet;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;

public class ApiImplementation implements Api {
    public int ping(int n) throws RemoteException {
        return n + 1;
    }

    public ValueSet getZones() throws RemoteException {
        Set<Value> set = new HashSet();
        set.add(ValueNull.getInstance());
        return new ValueSet(set, TypePrimitive.STRING);
    }
}
