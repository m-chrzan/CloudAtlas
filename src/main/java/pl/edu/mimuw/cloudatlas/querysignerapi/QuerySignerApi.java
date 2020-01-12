package pl.edu.mimuw.cloudatlas.querysignerapi;

import pl.edu.mimuw.cloudatlas.model.ValueQuery;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface QuerySignerApi extends Remote {
    public ValueQuery signInstallQuery(String queryName, String queryCode) throws RemoteException;

    public ValueQuery signUninstallQuery(String queryName) throws RemoteException;

    public void validateInstallQuery(String queryName, ValueQuery query) throws RemoteException;

    public void validateUninstallQuery(String queryName, ValueQuery query) throws RemoteException;

    public PublicKey getPublicKey() throws RemoteException;

    public void setPublicKey(PublicKey publicKey) throws RemoteException;

    public byte[] getQuerySignature(String queryName) throws RemoteException;
}
