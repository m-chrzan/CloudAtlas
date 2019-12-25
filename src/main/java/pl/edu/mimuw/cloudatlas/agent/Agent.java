package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Agent {

    public static void runRegistry() {
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

    public static HashMap<ModuleType, Module> initializeModules() {
        HashMap<ModuleType, Module> modules = new HashMap<ModuleType, Module>();
        // TODO add modules as we implement them
        return modules;
    }

    public static HashMap<ModuleType, Executor> initializeExecutors(
            HashMap<ModuleType, Module> modules) {
        HashMap<ModuleType, Executor> executors = new HashMap<ModuleType, Executor>();
        Iterator it = modules.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<ModuleType, Module> moduleEntry =
                    (Map.Entry<ModuleType, Module>) it.next();
            Module module = moduleEntry.getValue();
            Executor executor = new Executor(module);
            executors.put(moduleEntry.getKey(), executor);
        }

        return executors;
    }

    public static ArrayList<Thread>  initializeExecutorThreads(HashMap<ModuleType, Executor> executors) {
        ArrayList<Thread> executorThreads = new ArrayList<Thread>();
        Iterator it = executors.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<ModuleType, Executor> executorEntry =
                    (Map.Entry<ModuleType, Executor>) it.next();
            Thread thread = new Thread(executorEntry.getValue());
            thread.setDaemon(true);
            System.out.println("Initializing executor " + executorEntry.getKey());
            thread.start();
            executorThreads.add(thread);
        }

        return executorThreads;
    }

    public static void closeExecutors(ArrayList<Thread> executorThreads) {
        for (Thread executorThread : executorThreads) {
            executorThread.interrupt();
        }
    }

    public static void runModulesAsThreads() {
        HashMap<ModuleType, Module> modules = initializeModules();
        HashMap<ModuleType, Executor> executors = initializeExecutors(modules);
        ArrayList<Thread> executorThreads = initializeExecutorThreads(executors);

        Thread eventBusThread = new Thread(new EventBus(executors));
        System.out.println("Initializing event bus");
        eventBusThread.start();

        System.out.println("Closing executors");
        closeExecutors(executorThreads);
    }

    public static void main(String[] args) {
        runRegistry();
        runModulesAsThreads();
    }
}
