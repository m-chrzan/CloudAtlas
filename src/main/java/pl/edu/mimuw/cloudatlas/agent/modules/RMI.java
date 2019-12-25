package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.ApiImplementation;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMI extends Module {
    public RMI(AgentMessage.AgentModule moduleType) {
        super(moduleType);
        runRegistry();
    }

    public void runRegistry() {
        try {
            ZMI root = Main.createTestHierarchy2();
            ApiImplementation api = new ApiImplementation(root);
            Api apiStub =
                    (Api) UnicastRemoteObject.exportObject(api, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Api", apiStub);
            System.out.println("Agent: api bound");
        } catch (Exception e) {
            System.err.println("Agent registry initialization exception:");
            e.printStackTrace();
        }
    }

    @Override
    public void handle(AgentMessage event) throws InterruptedException {
        assert event.getDestinationModule() == event.getCorrectMessageType();
    }
}
