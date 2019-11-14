package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pl.edu.mimuw.cloudatlas.model.ValueSet;

public interface Api extends Remote {
	public int ping(int n) throws RemoteException;
    public ValueSet getZones() throws RemoteException;
}
