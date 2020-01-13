package pl.edu.mimuw.cloudatlas.querysigner;

import pl.edu.mimuw.cloudatlas.KeyGenerator;
import pl.edu.mimuw.cloudatlas.agent.NewApiImplementation;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.querysignerapi.QuerySignerApi;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PrivateKey;
import java.security.PublicKey;

public class QuerySigner {
    public static class InvalidQueryException extends Exception {
        InvalidQueryException() {
            super("Query invalid");
        }
    }

    private static QuerySignerApiImplementation initApi() throws IOException {
        String publicKeyFile = System.getProperty("public_key_file");
        String privateKeyFile = System.getProperty("private_key_file");
        PublicKey publicKey = null; //KeyGenerator.getPublicKey(KeyGenerator.readKeyFromFile(publicKeyFile));
        PrivateKey privateKey = null; // KeyGenerator.getPrivateKey(KeyGenerator.readKeyFromFile(privateKeyFile));
        return new QuerySignerApiImplementation(publicKey, privateKey);
    }

    public static void runRegistry() {
        try {
            QuerySignerApiImplementation api = initApi();
//            NewApiImplementation apii = new NewApiImplementation(null);
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
