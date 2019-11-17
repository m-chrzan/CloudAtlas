package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import pl.edu.mimuw.cloudatlas.api.Api;

public class Agent {
    public static void main(String[] args) {
        try {
            ApiImplementation api = new ApiImplementation();
            Api apiStub =
                (Api) UnicastRemoteObject.exportObject(api, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Api", apiStub);
            System.out.println("Api bound");
        } catch (Exception e) {
            System.err.println("Agent exception:");
            e.printStackTrace();
        }
    }
}
