package pl.edu.mimuw.cloudatlas.querysigner;

import pl.edu.mimuw.cloudatlas.querysignerapi.QuerySignerApi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class QuerySigner {
    public static class InvalidQueryException extends Exception {
        InvalidQueryException() {
            super("Query invalid");
        }
    }

    public static void runRegistry() {
        try {
            // TODO reading from files
            String publicKey = System.getProperty("public_key");
            String privateKey = System.getProperty("private_key");
            QuerySignerApiImplementation api = new QuerySignerApiImplementation(publicKey.getBytes(), privateKey.getBytes());
            QuerySignerApi apiStub =
                    (QuerySignerApi) UnicastRemoteObject.exportObject(api, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("QuerySignerApi", apiStub);
            System.out.println("QuerySigner: api bound");
        } catch (Exception e) {
            System.err.println("QuerySigner registry initialization exception:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        runRegistry();
    }
}
