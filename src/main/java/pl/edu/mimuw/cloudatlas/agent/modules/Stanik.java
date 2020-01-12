package pl.edu.mimuw.cloudatlas.agent.modules;

import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

public class Stanik extends Module {
    private class InvalidUpdateAttributesMessage extends Exception {
        public InvalidUpdateAttributesMessage(String message) {
            super(message);
        }
    }

    private ZMI hierarchy;
    private HashMap<Attribute, Entry<ValueQuery, ValueTime>> queries;
    private long freshnessPeriod;
    private Set<ValueContact> contacts;
    private ValueTime contactsTimestamp;

    public Stanik(long freshnessPeriod) {
        super(ModuleType.STATE);
        hierarchy = new ZMI();
        queries = new HashMap<Attribute, Entry<ValueQuery, ValueTime>>();
        hierarchy.getAttributes().add("timestamp", new ValueTime(0l));
        this.freshnessPeriod = freshnessPeriod;
        this.contactsTimestamp = ValueUtils.currentTime();
        this.contacts = new HashSet<>();
    }

    public Stanik() {
        this(60 * 1000);
    }

    public void handleTyped(StanikMessage message) throws InterruptedException, InvalidMessageType {
        switch(message.getType()) {
            case GET_STATE:
                handleGetState((GetStateMessage) message);
                break;
            case REMOVE_ZMI:
                handleRemoveZMI((RemoveZMIMessage) message);
                break;
            case SET_ATTRIBUTE:
                handleSetAttribte((SetAttributeMessage) message);
                break;
            case UPDATE_ATTRIBUTES:
                handleUpdateAttributes((UpdateAttributesMessage) message);
                break;
            case UPDATE_QUERIES:
                handleUpdateQueries((UpdateQueriesMessage) message);
                break;
            case UPDATE_CONTACTS:
                handleUpdateContacts((UpdateContactsMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Stanik" + message.getType().toString());
        }
    }

    public void handleGetState(GetStateMessage message) throws InterruptedException {
        pruneHierarchy();
        addLevels();
        StateMessage response = new StateMessage(
            "",
            message.getRequestingModule(),
            0,
            message.getRequestId(),
            hierarchy.clone(),
            (HashMap<Attribute, Entry<ValueQuery, ValueTime>>) queries.clone(),
            contacts
        );
        sendMessage(response);
    }

    private void pruneHierarchy() {
        ValueTime now = ValueUtils.currentTime();
        pruneZMI(hierarchy, now);
    }

    private void addLevels() {
        addLevelsRecursive(hierarchy, 0);
    }

    private void addLevelsRecursive(ZMI zmi, long level) {
        zmi.getAttributes().addOrChange("level", new ValueInt(level));
        for (ZMI son : zmi.getSons()) {
            addLevelsRecursive(son, level + 1);
        }
    }

    private boolean pruneZMI(ZMI zmi, ValueTime time) {
        Value timestamp = zmi.getAttributes().get("timestamp");

        boolean isLeaf = zmi.getSons().isEmpty();

        List<ZMI> sonsToRemove = new LinkedList();
        if (ValueUtils.valueLower(timestamp, time.subtract(new ValueDuration(freshnessPeriod)))) {
            if (zmi.getFather() != null) {
                return true;
            }
        } else {
            for (ZMI son : zmi.getSons()) {
                if (pruneZMI(son, time)) {
                    sonsToRemove.add(son);
                }
            }
        }

        for (ZMI son : sonsToRemove) {
            zmi.removeSon(son);
        }

        if (!isLeaf && zmi.getSons().isEmpty()) {
            return true;
        }

        return false;
    }

    public void handleRemoveZMI(RemoveZMIMessage message) {
        try {
            ZMI zmi = hierarchy.findDescendant(new PathName(message.getPathName()));
            if (ValueUtils.valueLower(zmi.getAttributes().getOrNull("timestamp"), message.getRemovalTimestamp())) {
                zmi.getFather().removeSon(zmi);
            } else {
                System.out.println("DEBUG: not removing zone with fresher timestamp than removal");
            }
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("DEBUG: trying to remove zone that doesn't exist");
        }
    }

    /*
     * Always adds the new attribute.
     * The zone must already exist.
     * The zone's timestamp will be the maximum of its current timestamp or the
     * timestamp provided with the new value.
     */
    public void handleSetAttribte(SetAttributeMessage message) {
        try {
            PathName descendantPath = new PathName(message.getPathName());
            if (!hierarchy.descendantExists(descendantPath)) {
                addMissingZones(descendantPath);
            }
            ZMI zmi = hierarchy.findDescendant(descendantPath);
            ValueTime updateTimestamp = message.getUpdateTimestamp();
            ValueTime currentTimestamp = (ValueTime) zmi.getAttributes().getOrNull("timestamp");
            if (ValueUtils.valueLower(currentTimestamp, updateTimestamp)) {
                zmi.getAttributes().addOrChange("timestamp", updateTimestamp);
            }

            zmi.getAttributes().addOrChange(message.getAttribute(), message.getValue());
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("DEBUG: trying to set attribute in zone that doesn't exist");
        }
    }

    public void handleUpdateAttributes(UpdateAttributesMessage message) {
        try {
            validateUpdateAttributesMessage(message);
            if (!ValueUtils.valueLower(
                        message.getAttributes().get("timestamp"),
                        new ValueTime(System.currentTimeMillis() - freshnessPeriod)
            )) {
                addMissingZones(new PathName(message.getPathName()));
                ZMI zone = hierarchy.findDescendant(message.getPathName());
                AttributesMap attributes = zone.getAttributes();
                if (ValueUtils.valueLower(attributes.get("timestamp"), message.getAttributes().get("timestamp"))) {
                    AttributesUtil.transferAttributes(message.getAttributes(), attributes);
                } else {
                    System.out.println("DEBUG: not applying update with older attributes");
                }
            } else {
                System.out.println("DEBUG: not applying update with stale attributes");
            }
        } catch (InvalidUpdateAttributesMessage e) {
            System.out.println("ERROR: invalid UpdateAttributesMessage " + e.getMessage());
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("ERROR: zone should exist after being added");
        }
    }

    public void handleUpdateQueries(UpdateQueriesMessage message) {
        for (Entry<Attribute, Entry<ValueQuery, ValueTime>> entry : message.getQueries().entrySet()) {
            Attribute attribute = entry.getKey();
            ValueTime timestamp = entry.getValue().getValue();
            Entry<ValueQuery, ValueTime> currentTimestampedQuery = queries.get(attribute);
            if (currentTimestampedQuery == null || ValueUtils.valueLower(currentTimestampedQuery.getValue(), timestamp)) {
                queries.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void validateUpdateAttributesMessage(UpdateAttributesMessage message) throws InvalidUpdateAttributesMessage {
        validateZoneName(message);
        validateHasTimeStamp(message);
    }

    private void validateZoneName(UpdateAttributesMessage message) throws InvalidUpdateAttributesMessage {
        Value name = message.getAttributes().getOrNull("name");
        if (message.getPathName().equals("/")) {
            if (name != null && !name.isNull()) {
                throw new InvalidUpdateAttributesMessage("The root zone should have a null name");
            }
        } else {
            if (valueNonNullOfType(name, TypePrimitive.STRING)) {
                ValueString nameString = (ValueString) name;
                String expectedName = (new PathName(message.getPathName())).getSingletonName();
                if (!nameString.getValue().equals(expectedName)) {
                    throw new InvalidUpdateAttributesMessage("The zone's name attribute should match its path name");
                }
            } else {
                throw new InvalidUpdateAttributesMessage("Zone attributes should have a name attribute of type String");
            }
        }
    }

    private void validateHasTimeStamp(UpdateAttributesMessage message) throws InvalidUpdateAttributesMessage {
        if (!valueNonNullOfType(message.getAttributes().getOrNull("timestamp"), TypePrimitive.TIME)) {
            throw new InvalidUpdateAttributesMessage("Zone attriutes should have a timestamp attribute of type Time");
        }
    }

    private boolean valueNonNullOfType(Value value, Type type) {
        return value != null && !value.isNull() && value.getType().isCompatible(type);
    }

    private void addMissingZones(PathName path) {
        try {
            if (!hierarchy.descendantExists(path)) {
                addMissingZones(path.levelUp());
                ZMI parent = hierarchy.findDescendant(path.levelUp());
                ZMI newSon = new ZMI(parent);
                newSon.getAttributes().add("name", new ValueString(path.getSingletonName()));
                newSon.getAttributes().add("timestamp", new ValueTime(0l));
                parent.addSon(newSon);
            }
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("ERROR: zone should exist after being added");
        }
    }

    public ZMI getHierarchy() {
        return hierarchy;
    }

    public HashMap<Attribute, Entry<ValueQuery, ValueTime>> getQueries() {
        return queries;
    }

    private void handleUpdateContacts(UpdateContactsMessage message) {
        if (message.getContacts() != null && !message.getContacts().isEmpty() &&
                ValueUtils.valueLower(contactsTimestamp, new ValueTime(message.getTimestamp()))) {
            this.contacts = message.getContacts();
        }
    }
}
