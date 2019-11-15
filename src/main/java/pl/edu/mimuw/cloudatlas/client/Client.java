package pl.edu.mimuw.cloudatlas.client;

import pl.edu.mimuw.cloudatlas.api.Api;

import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for a client.
 *
 * Set the following properties on the command line:
 *
 * <br>
 * -Djava.rmi.server.useCodebaseOnly=false
 * <br>
 * -Djava.rmi.server.codebase=file:/path/to/compiled/classes/
 * <br>
 * -Djava.security.policy=client.policy
 * <br>
 *
 * <b>NOTE: MAKE SURE YOU HAVE THE TRAILING / ON THE CODEBASE PATH</b>
 */

// https://github.com/rm5248/Java-RMI-Example/

@SpringBootApplication
public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            Api api = (Api) registry.lookup("Api");

            SpringApplication.run(Client.class, args);
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
    }
}
