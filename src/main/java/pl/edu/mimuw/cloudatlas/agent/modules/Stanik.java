package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GetHierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.HierarchyMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;
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
            default:
                throw new InvalidMessageType("This type of message cannot be handled by Stanik");
        }
    }

    public void handleGetHierarchy(GetHierarchyMessage message) throws InterruptedException {
        HierarchyMessage response = new HierarchyMessage("", message.getRequestingModule(), 0, message.getRequestId(), hierarchy.clone());
        sendMessage(response);
    }
}
