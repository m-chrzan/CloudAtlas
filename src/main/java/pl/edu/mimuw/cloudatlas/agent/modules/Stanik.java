package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.HashMap;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RemoveZMIMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateQueriesMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.AttributesUtil;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ValueUtils;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Stanik extends Module {
    private class InvalidUpdateAttributesMessage extends Exception {
        public InvalidUpdateAttributesMessage(String message) {
            super(message);
        }
    }

    private ZMI hierarchy;
    private HashMap<Attribute, Entry<ValueQuery, ValueTime>> queries;

    public Stanik() {
        super(ModuleType.STATE);
        hierarchy = new ZMI();
        queries = new HashMap<Attribute, Entry<ValueQuery, ValueTime>>();
        hierarchy.getAttributes().add("timestamp", new ValueTime(0l));
    }

    public void handleTyped(StanikMessage message) throws InterruptedException, InvalidMessageType {
        switch(message.getType()) {
            case GET_STATE:
                handleGetState((GetStateMessage) message);
                break;
            case REMOVE_ZMI:
                handleRemoveZMI((RemoveZMIMessage) message);
                break;
            case UPDATE_ATTRIBUTES:
                handleUpdateAttributes((UpdateAttributesMessage) message);
                break;
            case UPDATE_QUERIES:
                handleUpdateQueries((UpdateQueriesMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Stanik");
        }
    }

    public void handleGetState(GetStateMessage message) throws InterruptedException {
        StateMessage response = new StateMessage(
            "",
            message.getRequestingModule(),
            0,
            message.getRequestId(),
            hierarchy.clone(),
            (HashMap<Attribute, Entry<ValueQuery, ValueTime>>) queries.clone()
        );
        sendMessage(response);
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

    public void handleUpdateAttributes(UpdateAttributesMessage message) {
        try {
            validateUpdateAttributesMessage(message);
            addMissingZones(new PathName(message.getPathName()));
            ZMI zone = hierarchy.findDescendant(message.getPathName());
            AttributesMap attributes = zone.getAttributes();
            if (ValueUtils.valueLower(attributes.get("timestamp"), message.getAttributes().get("timestamp"))) {
                AttributesUtil.transferAttributes(message.getAttributes(), attributes);
            } else {
                System.out.println("DEBUG: not applying update with older attributes");
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
}
