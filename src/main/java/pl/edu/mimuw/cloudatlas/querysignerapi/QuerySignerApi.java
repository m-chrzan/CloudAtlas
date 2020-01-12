package pl.edu.mimuw.cloudatlas.querysignerapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface QuerySignerApi extends Remote {
    public byte[] signQuery(String queryName, String queryCode) throws RemoteException;

    public String checkQuery(byte[] encryptedQuery, String queryName, String queryCode) throws RemoteException;

    public PublicKey getPublicKey() throws RemoteException;

    public void setPublicKey(PublicKey publicKey) throws RemoteException;

    public byte[] getQuerySignature(String queryName) throws RemoteException;
}
