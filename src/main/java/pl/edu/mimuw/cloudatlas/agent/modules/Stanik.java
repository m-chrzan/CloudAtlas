package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetHierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Stanik extends Module {
    private ZMI hierarchy;

    public Stanik() {
        super(ModuleType.STATE);
        hierarchy = new ZMI();
    }

    public void handleTyped(StanikMessage message) throws InterruptedException, InvalidMessageType {
        switch(message.getType()) {
            case GET_HIERARCHY:
                handleGetHierarchy((GetHierarchyMessage) message);
                break;
            case UPDATE_ATTRIBUTES:
                handleUpdateAttributes((UpdateAttributesMessage) message);
                break;
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Stanik");
        }
    }

    public void handleGetHierarchy(GetHierarchyMessage message) throws InterruptedException {
        HierarchyMessage response = new HierarchyMessage("", message.getRequestingModule(), 0, message.getRequestId(), hierarchy.clone());
        sendMessage(response);
    }

    public void handleUpdateAttributes(UpdateAttributesMessage message) {
        try {
            addMissingZones(new PathName(message.getPathName()));
            ZMI zone = hierarchy.findDescendant(message.getPathName());
            for (Entry<Attribute, Value> entry : zone.getAttributes()) {
                Attribute attribute = entry.getKey();
                Value newValue = message.getAttributes().getOrNull(attribute);
                if (newValue == null) {
                    zone.getAttributes().remove(attribute);
                }
            }
            for (Entry<Attribute, Value> entry : message.getAttributes()) {
                zone.getAttributes().addOrChange(entry.getKey(), entry.getValue());
            }
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("ERROR: zone should exist after being added");
        }
    }

    private void addMissingZones(PathName path) {
        try {
            if (!hierarchy.descendantExists(path)) {
                addMissingZones(path.levelUp());
                ZMI parent = hierarchy.findDescendant(path.levelUp());
                ZMI newSon = new ZMI(parent);
                newSon.getAttributes().add("name", new ValueString(path.getSingletonName()));
                parent.addSon(newSon);
            }
        } catch (ZMI.NoSuchZoneException e) {
            System.out.println("ERROR: zone should exist after being added");
        }
    }

    public ZMI getHierarchy() {
        return hierarchy;
    }
}
