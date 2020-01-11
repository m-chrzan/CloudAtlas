package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.agent.modules.GossipGirl;
import pl.edu.mimuw.cloudatlas.agent.modules.GossipGirlStrategies;
import pl.edu.mimuw.cloudatlas.agent.modules.RecursiveScheduledTask;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class HierarchyConfig {
    private GossipGirlStrategies gossipGirlStrategies;
    private GossipGirlStrategies.ZoneSelectionStrategy zoneSelectionStrategy;
    private Random random = new Random();
    private EventBus eventBus;

    HierarchyConfig(EventBus eventBus) {
        zoneSelectionStrategy = GossipGirlStrategies.ZoneSelectionStrategy.ROUND_ROBIN_SAME_FREQ;
        gossipGirlStrategies = new GossipGirlStrategies(new PathName("/uw/violet07"));
        this.eventBus = eventBus;
    }

    public void startGossip(long gossipPeriod) {
        Supplier<TimerScheduledTask> taskSupplier = () ->
                new TimerScheduledTask() {
                    public void run() {
                        try {
                            System.out.println("INFO: initiating gossip");
                            PathName gossipLevel = gossipGirlStrategies.selectStrategy(zoneSelectionStrategy);
                            ValueContact contact = selectContactFromLevel(gossipLevel);
                            if (contact != null) {
                                System.out.println("INFO: found a contact " + contact.toString());
                                InitiateGossipMessage message = new InitiateGossipMessage("", 0, new PathName("/uw/violet07"), contact);
                                sendMessage(message);
                            } else {
                                System.out.println("DEBUG: couldn't find contact for gossip");
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Interrupted while initiating gossip");
                        } catch (Exception e) {
                            System.out.println("ERROR: something happened " + e.toString());
                        }
                    }
                };

        startRecursiveTask(taskSupplier, gossipPeriod);
    }

    private ValueContact selectContactFromLevel(PathName path) throws Exception {
        CompletableFuture<ResponseMessage> responseFuture = new CompletableFuture();
        this.eventBus.addMessage(new RequestStateMessage("", 0, responseFuture));
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

    // TODO
    private ValueContact selectFallbackContact() throws Exception {
        return null;
    }

    private ZMI selectZMI(List<ZMI> zmis) throws Exception {
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

    private ValueContact selectContactFromSet(Set<Value> contacts) throws Exception {
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

    private List<ZMI> getSiblings(ZMI root, PathName path) {
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

    private void filterEmptyContacts(List<ZMI> zmis) {
        Iterator<ZMI> iterator = zmis.iterator();
        while (iterator.hasNext()) {
            ZMI zmi = iterator.next();
            ValueSet contacts = (ValueSet) zmi.getAttributes().getOrNull("contacts");
            if (contacts == null || contacts.isNull() || contacts.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void startRecursiveTask(Supplier<TimerScheduledTask> taskSupplier, long period) {
        TimerScheduledTask timerTask = new RecursiveScheduledTask(period, taskSupplier);

        try {
            this.eventBus.addMessage(new TimerSchedulerMessage("", 0, "", period, 0, timerTask));
        } catch (InterruptedException e) {
            System.out.println("Interrupted while starting queries");
        }
    }

    private void addZoneAndChildren(ZMI zmi, PathName pathName) {
        try {
            UpdateAttributesMessage message = new UpdateAttributesMessage("", 0, pathName.toString(), zmi.getAttributes());
            this.eventBus.addMessage(message);
            for (ZMI son : zmi.getSons()) {
                addZoneAndChildren(son, pathName.levelDown(son.getAttributes().getOrNull("name").toString()));
            }
        } catch (Exception e) {
            System.out.println("ERROR: failed to add zone");
        }
    }

    public void initZones() {
        try {
            ZMI root = Main.createTestHierarchy2();
            addZoneAndChildren(root, new PathName(""));
            System.out.println("Initialized with test hierarchy");
        } catch (Exception e) {
            System.out.println("ERROR: failed to create test hierarchy");
        }
    }

    public void startQueries(long queriesPeriod) {
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
}
