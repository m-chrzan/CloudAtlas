package pl.edu.mimuw.cloudatlas.agent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import pl.edu.mimuw.cloudatlas.agent.NewApiImplementation;
import pl.edu.mimuw.cloudatlas.agent.messages.RunQueriesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.*;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Agent {
    private static EventBus eventBus;

    public static void runRegistry() {
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

    public static HashMap<ModuleType, Module> initializeModules() throws UnknownHostException, SocketException, NullPointerException {
        HashMap<ModuleType, Module> modules = new HashMap<ModuleType, Module>();
        modules.put(ModuleType.TIMER_SCHEDULER, new TimerScheduler(ModuleType.TIMER_SCHEDULER));
        modules.put(ModuleType.RMI, new Remik());
        Long freshnessPeriod = Long.getLong("freshness_period");
        modules.put(ModuleType.STATE, new Stanik(freshnessPeriod));
        modules.put(ModuleType.QUERY, new Qurnik());

        Integer port = Integer.getInteger("UDUPServer.port");
        Integer timeout = Integer.getInteger("UDUPServer.timeout");
        Integer bufsize = Integer.getInteger("UDUPServer.bufsize");
        UDUPServer server = new UDUPServer(InetAddress.getByName("127.0.0.1"), port, bufsize);
        modules.put(ModuleType.UDP, new UDUP(port, timeout, bufsize, server));
        // TODO add modules as we implement them
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

    public static ArrayList<Thread>  initializeExecutorThreads(HashMap<ModuleType, Executor> executors) {
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

    public static void closeExecutors(ArrayList<Thread> executorThreads) {
        for (Thread executorThread : executorThreads) {
            executorThread.interrupt();
        }
    }

    public static void runModulesAsThreads() {
        HashMap<ModuleType, Module> modules = null;

        try {
            modules = initializeModules();
        } catch (UnknownHostException | SocketException e) {
            System.out.println("Module initialization failed");
            e.printStackTrace();
            return;
        }

        HashMap<ModuleType, Executor> executors = initializeExecutors(modules);
        ArrayList<Thread> executorThreads = initializeExecutorThreads(executors);
        eventBus = new EventBus(executors);
        Thread UDUPServerThread = new Thread(((UDUP) modules.get(ModuleType.UDP)).getServer());
        Thread eventBusThread = new Thread(eventBus);
        System.out.println("Initializing event bus");
        eventBusThread.start();
        UDUPServerThread.start();
    }

    private static void initZones() {
        try {
            ZMI root = Main.createTestHierarchy2();
            addZoneAndChildren(root, new PathName(""));
            System.out.println("Initialized with test hierarchy");
        } catch (Exception e) {
            System.out.println("ERROR: failed to create test hierarchy");
        }
    }

    private static void startQueries(long queriesPeriod) {
        Supplier<TimerScheduledTask> taskSupplier = () ->
            new TimerScheduledTask() {
                public void run() {
                    try {
                        sendMessage(new RunQueriesMessage("", 0));
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while triggering queries");
                    }
                }
            };

        TimerScheduledTask timerTask = new RecursiveScheduledTask(queriesPeriod, taskSupplier);

        try {
            eventBus.addMessage(new TimerSchedulerMessage("", 0, "", queriesPeriod, 0, timerTask));
        } catch (InterruptedException e) {
            System.out.println("Interrupted while starting queries");
        }
    }

    private static void addZoneAndChildren(ZMI zmi, PathName pathName) {
        try {
            UpdateAttributesMessage message = new UpdateAttributesMessage("", 0, pathName.toString(), zmi.getAttributes());
            eventBus.addMessage(message);
            for (ZMI son : zmi.getSons()) {
                addZoneAndChildren(son, pathName.levelDown(son.getAttributes().getOrNull("name").toString()));
            }
        } catch (Exception e) {
            System.out.println("ERROR: failed to add zone");
        }
    }

    public static void main(String[] args) {
        runModulesAsThreads();
        runRegistry();
        initZones();
        // TODO: make query period confiurable with config file and from tests
        startQueries(100l);
    }
}
