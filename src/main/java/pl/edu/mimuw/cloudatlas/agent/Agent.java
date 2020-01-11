package pl.edu.mimuw.cloudatlas.agent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import pl.edu.mimuw.cloudatlas.agent.NewApiImplementation;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.agent.modules.*;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Agent {
    private static EventBus eventBus;
    private static GossipGirlStrategies.ZoneSelectionStrategy zoneSelectionStrategy;
    private static GossipGirlStrategies gossipGirlStrategies;
    private static Random random = new Random();

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
        modules.put(ModuleType.GOSSIP, new GossipGirl());

        Integer port = Integer.getInteger("UDUPServer.port");
        Integer timeout = Integer.getInteger("UDUPServer.timeout");
        Integer bufsize = Integer.getInteger("UDUPServer.bufsize");
        UDUPServer server = new UDUPServer(InetAddress.getByName("127.0.0.1"), port, bufsize);
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

        startRecursiveTask(taskSupplier, queriesPeriod);
    }

    private static void startGossip(long gossipPeriod) {
        Supplier<TimerScheduledTask> taskSupplier = () ->
            new TimerScheduledTask() {
                public void run() {
                    try {
                        System.out.println("INFO: initiating gossip");
                        PathName gossipLevel = gossipGirlStrategies.selectStrategy(zoneSelectionStrategy);
                        ValueContact contact = selectContactFromLevel(gossipLevel);
                        if (contact != null) {
                            InitiateGossipMessage message = new InitiateGossipMessage("", 0, new PathName("/uw/violet07"), contact);
                            sendMessage(message);
                        } else {
                            System.out.println("DEBUG: couldn't find contact for gossip");
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while initiating gossip");
                    } catch (Exception e) {
                        System.out.println("ERROR: something happened");
                    }
                }
            };

        startRecursiveTask(taskSupplier, gossipPeriod);
    }

    private static ValueContact selectContactFromLevel(PathName path) throws Exception {
        CompletableFuture<ResponseMessage> responseFuture = new CompletableFuture();
        eventBus.addMessage(new RequestStateMessage("", 0, responseFuture));
        StateMessage response = (StateMessage) responseFuture.get();
        ZMI root = response.getZMI();
        List<ZMI> siblings = getSiblings(root, path);
        filterEmptyContacts(siblings);
        if (siblings.isEmpty()) {
            return selectFallbackContact();
        }
        ZMI zmi = selectZMI(siblings);
        ValueSet contactsValue = (ValueSet) zmi.getAttributes().getOrNull("contacts");
        Set<Value> valueSet = contactsValue.getValue();
        return selectContactFromSet(valueSet);
    }

    private static ValueContact selectFallbackContact() throws Exception {
        return selectContactFromSet(new HashSet());
    }

    private static ZMI selectZMI(List<ZMI> zmis) throws Exception {
        int i = random.nextInt(zmis.size());
        for (ZMI zmi : zmis) {
            if (i == 0) {
                return zmi;
            }
            i--;
        }
        System.out.println("ERROR: empty list passed to selectZMI");
        throw new Exception("empty list passed to selectZMI");
    }

    private static ValueContact selectContactFromSet(Set<Value> contacts) throws Exception {
        int i = random.nextInt(contacts.size());
        for (Value contact : contacts) {
            if (i == 0) {
                return (ValueContact) contact;
            }
            i--;
        }
        System.out.println("ERROR: empty list passed to selectContactFromSet");
        throw new Exception("empty list passed to selectContactFromSet");
    }

    private static List<ZMI> getSiblings(ZMI root, PathName path) {
        try {
            List<ZMI> siblingsAndI = root.findDescendant(path).getFather().getSons();
            List<ZMI> siblings = new ArrayList();
            for (ZMI siblingOrI : siblingsAndI) {
                if (!siblingOrI.getPathName().equals(path)) {
                    siblings.add(siblingOrI);
                }
            }
            return siblings;
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("ERROR: didn't find path when looking for siblings");
            return new ArrayList();
        }
    }

    private static void filterEmptyContacts(List<ZMI> zmis) {
        Iterator<ZMI> iterator = zmis.iterator();
        while (iterator.hasNext()) {
            ZMI zmi = iterator.next();
            ValueSet contacts = (ValueSet) zmi.getAttributes().getOrNull("contacts");
            if (contacts == null || contacts.isNull() || contacts.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private static void startRecursiveTask(Supplier<TimerScheduledTask> taskSupplier, long period) {
        TimerScheduledTask timerTask = new RecursiveScheduledTask(period, taskSupplier);

        try {
            eventBus.addMessage(new TimerSchedulerMessage("", 0, "", period, 0, timerTask));
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
        zoneSelectionStrategy = GossipGirlStrategies.ZoneSelectionStrategy.ROUND_ROBIN_SAME_FREQ;
        gossipGirlStrategies = new GossipGirlStrategies(new PathName("/uw/violet07"));
        runModulesAsThreads();
        runRegistry();
        initZones();
        // TODO: make query period confiurable with config file and from tests
        startQueries(6000l);
        startGossip(5000l);
    }
}
