package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * The EventBus routes messages sent between Modules.
 */
public class EventBus implements Runnable {
    private LinkedBlockingQueue<AgentMessage> events;
    private HashMap<AgentMessage.AgentModule, Executor> executors;

    void setEventBusReference() {
        Iterator it = this.executors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<AgentMessage.AgentModule, Executor> executorEntry =
                    (Map.Entry<AgentMessage.AgentModule, Executor>) it.next();
            executorEntry.getValue().setEventBus(this);
        }
    }

    EventBus(HashMap<AgentMessage.AgentModule, Executor> executors) {
        this.executors = executors;
        setEventBusReference();
        this.events = new LinkedBlockingQueue<AgentMessage>();
    }

    public void run() {
        System.out.println("Event bus running");
        while (!Thread.currentThread().interrupted()) {
            try {
                AgentMessage event = events.take();
                routeMessage(event);
            } catch (InterruptedException e) {
                System.out.println("Event bus interrupted. Exiting loop.");
                break;
            }
        }
    }

    public void routeMessage(AgentMessage msg) throws InterruptedException {
        assert msg.getCorrectMessageType() == msg.getDestinationModule();
        System.out.println("Event bus routing message");
        executors.get(msg.getDestinationModule()).addMessage(msg);
    }

    public void addMessage(AgentMessage msg) throws InterruptedException {
        assert msg.getCorrectMessageType() == msg.getDestinationModule();
        this.events.put(msg);
    }
}
