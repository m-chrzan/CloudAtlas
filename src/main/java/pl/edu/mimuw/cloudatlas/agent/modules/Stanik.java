package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetStateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StateMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
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
            case UPDATE_ATTRIBUTES:
                handleUpdateAttributes((UpdateAttributesMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Stanik");
        }
    }

    public void handleGetState(GetStateMessage message) throws InterruptedException {
        StateMessage response = new StateMessage("", message.getRequestingModule(), 0, message.getRequestId(), hierarchy.clone(), (HashMap<Attribute, Entry<ValueQuery, ValueTime>>) queries.clone());
        sendMessage(response);
    }

    public void handleUpdateAttributes(UpdateAttributesMessage message) {
        try {
            validateUpdateAttributesMessage(message);
            addMissingZones(new PathName(message.getPathName()));
            ZMI zone = hierarchy.findDescendant(message.getPathName());
            AttributesMap attributes = zone.getAttributes();
            if (valueLower(attributes.get("timestamp"), message.getAttributes().get("timestamp"))) {
                transferAttributes(message.getAttributes(), attributes);
            } else {
                System.out.println("DEBUG: not applying update with older attributes");
            }
        } catch (InvalidUpdateAttributesMessage e) {
            System.out.println("ERROR: invalid UpdateAttributesMessage " + e.getMessage());
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("ERROR: zone should exist after being added");
        }
    }

    private boolean valueLower(Value a, Value b) {
        return ((ValueBoolean) a.isLowerThan(b)).getValue();
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

    private void transferAttributes(AttributesMap fromAttributes, AttributesMap toAttributes) {
        Iterator<Entry<Attribute, Value>> iterator = toAttributes.iterator();
        while (iterator.hasNext()) {
            Entry<Attribute, Value> entry = iterator.next();
            Attribute attribute = entry.getKey();
            Value newValue = fromAttributes.getOrNull(attribute);
            if (newValue == null) {
                iterator.remove();
            }
        }
        for (Entry<Attribute, Value> entry : fromAttributes) {
            toAttributes.addOrChange(entry.getKey(), entry.getValue());
        }
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
