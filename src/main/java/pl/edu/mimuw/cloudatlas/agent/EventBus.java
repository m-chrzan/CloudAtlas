package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * The EventBus routes messages sent between Modules.
 */
public class EventBus implements Runnable {
    private LinkedBlockingQueue<AgentMessage> events;
    private HashMap<ModuleType, Executor> executors;

    void setEventBusReference() {
        for (Map.Entry<ModuleType, Executor> executorEntry : this.executors.entrySet()) {
            executorEntry.getValue().setEventBus(this);
        }
    }

    // Allows for testing with a mock EventBus
    protected EventBus() {
        this.events = new LinkedBlockingQueue<AgentMessage>();
    }

    EventBus(HashMap<ModuleType, Executor> executors) {
        this.executors = executors;
        setEventBusReference();
        this.events = new LinkedBlockingQueue<AgentMessage>();
    }

    public void setEvents(LinkedBlockingQueue<AgentMessage> events) {
        this.events = events;
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
        System.out.println("Event bus routing message");
        executors.get(msg.getDestinationModule()).addMessage(msg);
    }

    public void addMessage(AgentMessage msg) throws InterruptedException {
        this.events.put(msg);
    }
}
