package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class HierarchyMessage extends ResponseMessage {
    private ZMI zmi;

    public HierarchyMessage(String messageId, ModuleType destinationModule, long timestamp, long requestId, ZMI zmi) {
        super(messageId, destinationModule, timestamp, Type.HIERARCHY, requestId);
        this.zmi = zmi;
    }

    public ZMI getZMI() {
        return zmi;
    }
}
