package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.agent.modules.GossipGirlStrategies;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;
import pl.edu.mimuw.cloudatlas.model.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class HierarchyConfig {
    private GossipGirlStrategies gossipGirlStrategies;
    private GossipGirlStrategies.ZoneSelectionStrategy zoneSelectionStrategy;
    private Random random = new Random();
    private EventBus eventBus;

    HierarchyConfig(EventBus eventBus, String zonePath, String zoneStrategy) {
        zoneSelectionStrategy = parseStrategy(zoneStrategy);
        gossipGirlStrategies = new GossipGirlStrategies(new PathName(zonePath));
        this.eventBus = eventBus;
    }

    private GossipGirlStrategies.ZoneSelectionStrategy parseStrategy(String selectionStrategy) {
        switch (selectionStrategy) {
            case "RoundRobinExp":
                return GossipGirlStrategies.ZoneSelectionStrategy.ROUND_ROBIN_EXP_FREQ;
            case "RoundRobinUniform":
                return GossipGirlStrategies.ZoneSelectionStrategy.ROUND_ROBIN_SAME_FREQ;
            case "RandomExp":
                return GossipGirlStrategies.ZoneSelectionStrategy.RANDOM_DECR_EXP;
            case "RandomUniform":
                return GossipGirlStrategies.ZoneSelectionStrategy.RANDOM_UNFIORM;
            default:
                throw new UnsupportedOperationException("Selection strategy doesnt exist");
        }
    }

    public void startGossip(long gossipPeriod, String zonePath) {
        Supplier<TimerScheduledTask> taskSupplier = () ->
                new TimerScheduledTask() {
                    public void run() {
                        try {
                            System.out.println("INFO: initiating gossip");
                            PathName gossipLevel = gossipGirlStrategies.selectStrategy(zoneSelectionStrategy);
                            ValueContact contact = selectContactFromLevel(gossipLevel);
                            if (contact != null) {
                                System.out.println("INFO: found a contact " + contact.toString());
                                InitiateGossipMessage message = new InitiateGossipMessage("", 0, new PathName(zonePath), contact);
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

        AgentUtils.startRecursiveTask(taskSupplier, gossipPeriod, eventBus);
    }

    private ValueContact selectContactFromLevel(PathName path) throws Exception {
        CompletableFuture<ResponseMessage> responseFuture = new CompletableFuture();
        this.eventBus.addMessage(new RequestStateMessage("", 0, responseFuture));
        StateMessage response = (StateMessage) responseFuture.get();
        ZMI root = response.getZMI();
        List<ZMI> siblings = getSiblings(root, path);
        filterEmptyContacts(siblings);
        if (siblings.isEmpty()) {
            return selectFallbackContact(response.getContacts());
        }
        ZMI zmi = selectZMI(siblings);
        ValueSet contactsValue = (ValueSet) zmi.getAttributes().getOrNull("contacts");
        Set<Value> valueSet = contactsValue.getValue();
        return selectContactFromSet(valueSet);
    }

    // TODO
    private ValueContact selectFallbackContact(Set<ValueContact> contacts) throws Exception {
        if (contacts.isEmpty()) {
            return null;
        } else {
            return selectContactFromSet(contacts);
        }
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

    private <T> ValueContact selectContactFromSet(Set<T> contacts) throws Exception {
        int i = random.nextInt(contacts.size());
        for (T contact : contacts) {
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

        AgentUtils.startRecursiveTask(taskSupplier, queriesPeriod, eventBus);
    }
}
