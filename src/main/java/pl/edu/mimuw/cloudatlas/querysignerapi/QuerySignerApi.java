package pl.edu.mimuw.cloudatlas.querysignerapi;

import pl.edu.mimuw.cloudatlas.querysigner.QueryData;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QuerySignerApi extends Remote {
    public QueryData signInstallQuery(String queryName, String queryCode) throws RemoteException;

    public QueryData signUninstallQuery(String queryName) throws RemoteException;

    public void validateInstallQuery(String queryName, QueryData query) throws RemoteException;

    public void validateUninstallQuery(String queryName, QueryData query) throws RemoteException;
}
