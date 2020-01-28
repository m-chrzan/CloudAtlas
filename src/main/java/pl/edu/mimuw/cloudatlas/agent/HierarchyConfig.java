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
    private PathName ourPath;
    private EventBus eventBus;

    HierarchyConfig(EventBus eventBus, String zonePath, String zoneStrategy) {
        zoneSelectionStrategy = parseStrategy(zoneStrategy);
        ourPath = new PathName(zonePath);
        gossipGirlStrategies = new GossipGirlStrategies(ourPath);
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

                            StateMessage state = getState();
                            PathName gossipLevel = gossipGirlStrategies.selectStrategy(zoneSelectionStrategy);
                            ValueContact contact;
                            if (random.nextDouble() < 0.2) {
				System.out.println("FORCING FALLBACK");
                                contact = selectFallbackContact(state.getContacts());
                            } else {
				System.out.println("LOOKING FOR CONTACT");
                                contact = selectContactFromLevel(gossipLevel, state);
                            }

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
			    e.printStackTrace();
                        }
                    }
                };

        AgentUtils.startRecursiveTask(taskSupplier, gossipPeriod, eventBus);
    }

    private ValueContact selectContactFromLevel(PathName path, StateMessage state) throws Exception {
        ZMI root = state.getZMI();
        List<ZMI> siblings = getSiblings(root, path);
        filterEmptyContacts(siblings);
	System.out.println("filtered siblings: " + siblings.toString());
        if (siblings.isEmpty()) {
	    System.out.println("SIBLINGS IS EMPTY!!!");
            return selectFallbackContact(state.getContacts());
        }
        ZMI zmi = selectZMI(siblings);
	System.out.println("SELECTED: " + zmi.toString());
        ValueSet contactsValue = (ValueSet) zmi.getAttributes().getOrNull("contacts");
	System.out.println("ITS CONTACTS: " + contactsValue.toString());
        Set<Value> valueSetOrig = contactsValue.getValue();
	System.out.println("valueSetOrig: " + valueSetOrig.toString());
	Set<Value> valueSet = new HashSet(valueSetOrig);
	System.out.println("valueSet: " + valueSet.toString());
        filterOurContact(valueSet);
	System.out.println("FILTERED VALUE SET: " + valueSet.toString());
        return selectContactFromSet(valueSet);
    }

    private void filterOurContact(Set<Value> contacts) {
        Iterator it = contacts.iterator();
        while (it.hasNext()) {
            Value value = (Value) it.next();
            if (value.getType().getPrimaryType() == Type.PrimaryType.CONTACT) {
                ValueContact contact = (ValueContact) value;
                if (contact.getName().equals(ourPath)) {
                    it.remove();
                }
            } else {
                System.out.println("WARN: non contact value passed to filterOurContact");
            }
        }
    }

    private StateMessage getState() throws Exception {
        CompletableFuture<ResponseMessage> responseFuture = new CompletableFuture();
        this.eventBus.addMessage(new RequestStateMessage("", 0, responseFuture));
        return (StateMessage) responseFuture.get();
    }

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
	if (contacts.size() == 0) {
		System.out.println("GOT EMPTY SET FOR SELECTION");
		return null;
	}
        int i = random.nextInt(contacts.size());
	System.out.println("ROLLED RANDOM: " + Integer.toString(i));
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
	    System.out.println("DEBUG: path in getSiblings: " + path.toString());
            List<ZMI> siblingsImm = root.findDescendant(path).getFather().getSons();
	    System.out.println("DEBUG: siblingsImm: " + siblingsImm.toString());
            List<ZMI> siblings = new ArrayList();
            for (ZMI siblingOrI : siblingsImm) {
	    	siblings.add(siblingOrI);
            }
	    System.out.println("found siblings: " + siblings.toString());
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
	    System.out.println("checking zmi " + zmi.toString());
	    System.out.println("its contacts: " + contacts.toString());
            if (contacts == null || contacts.isNull() || contacts.isEmpty() || onlyContactIsUs(contacts)) {
		System.out.println("Let's remove it!");
                iterator.remove();
            } else {
		System.out.println("Let's keep it!");
	    }
        }
    }

    private boolean onlyContactIsUs(ValueSet contacts) {
        for (Value value : contacts) {
            if (value.getType().getPrimaryType() == Type.PrimaryType.CONTACT) {
                ValueContact contact = (ValueContact) value;
                if (!contact.getName().equals(ourPath)) {
                    return false;
                }
            } else {
                System.out.println("WARN: non contact value passed to onlyContactIsUs");
            }
        }

        return true;
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

    public void startCleaningGossips(long gossipCleanPeriod) {
        Supplier<TimerScheduledTask> taskSupplier = () ->
                new TimerScheduledTask() {
                    public void run() {
                        try {
                            System.out.println("INFO: Scheduling old gossip cleanup");
                            sendMessage(new CleanOldGossipsMessage("", 0, ValueUtils.addToTime(ValueUtils.currentTime(), -gossipCleanPeriod)));
                        } catch (InterruptedException e) {
                            System.out.println("Interrupted while triggering queries");
                        }
                    }
                };

        AgentUtils.startRecursiveTask(taskSupplier, gossipCleanPeriod, eventBus);
    }
}
