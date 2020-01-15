package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.modules.*;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AgentConfig {
    private HashMap<ModuleType, Executor> executors;
    HashMap<ModuleType, Module> modules;

    public HashMap<ModuleType, Executor> getExecutors() {
        return executors;
    }

    public void runRegistry(EventBus eventBus) {
        try {
            NewApiImplementation api = new NewApiImplementation(eventBus);
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

    private HashMap<ModuleType, Module> initializeModules() throws UnknownHostException, SocketException, NullPointerException {
        Long freshnessPeriod = Long.getLong("freshness_period");
        Integer port = Integer.getInteger("UDUPServer.port");
        Integer timeout = Integer.getInteger("UDUPServer.timeout");
        Integer bufsize = Integer.getInteger("UDUPServer.bufsize");
        InetAddress serverAddr = InetAddress.getByName(System.getProperty("UDUPServer.hostname"));
        String ourPath = System.getProperty("zone_path");

        HashMap<ModuleType, Module> modules = new HashMap<ModuleType, Module>();
        modules.put(ModuleType.TIMER_SCHEDULER, new TimerScheduler(ModuleType.TIMER_SCHEDULER));
        modules.put(ModuleType.RMI, new Remik());
        modules.put(ModuleType.STATE, new Stanik(new PathName(ourPath), freshnessPeriod));
        modules.put(ModuleType.QUERY, new Qurnik());
        modules.put(ModuleType.GOSSIP, new GossipGirl());

        UDUPServer server = new UDUPServer(serverAddr, port, bufsize, freshnessPeriod);
        modules.put(ModuleType.UDP, new UDUP(port, timeout, bufsize, server));
        return modules;
    }

    public static HashMap<ModuleType, Executor> initializeExecutors(
            HashMap<ModuleType, Module> modules) {
        HashMap<ModuleType, Executor> executors = new HashMap<ModuleType, Executor>();

        for (Map.Entry<ModuleType, Module> moduleEntry : modules.entrySet()) {
            Module module = moduleEntry.getValue();
            Executor executor = new Executor(module);
            executors.put(moduleEntry.getKey(), executor);
        }

        return executors;
    }

    public static ArrayList<Thread> initializeExecutorThreads(HashMap<ModuleType, Executor> executors) {
        ArrayList<Thread> executorThreads = new ArrayList<Thread>();

        for (Map.Entry<ModuleType, Executor> executorEntry : executors.entrySet()) {
            Thread thread = new Thread(executorEntry.getValue());
            thread.setDaemon(true);
            System.out.println("Initializing executor " + executorEntry.getKey());
            thread.start();
            executorThreads.add(thread);
        }

        return executorThreads;
    }

    public void closeExecutors(ArrayList<Thread> executorThreads) {
        for (Thread executorThread : executorThreads) {
            executorThread.interrupt();
        }
    }

    public void runModulesAsThreads() {
        try {
            modules = initializeModules();
        } catch (UnknownHostException | SocketException e) {
            System.out.println("Module initialization failed");
            e.printStackTrace();
            return;
        }

        executors = initializeExecutors(modules);
        ArrayList<Thread> executorThreads = initializeExecutorThreads(executors);
    }

    void startNonModuleThreads(EventBus eventBus) {
        Thread UDUPServerThread = new Thread(((UDUP) modules.get(ModuleType.UDP)).getServer());
        Thread eventBusThread = new Thread(eventBus);
        System.out.println("Initializing event bus");
        eventBusThread.start();
        UDUPServerThread.start();
    }
}
