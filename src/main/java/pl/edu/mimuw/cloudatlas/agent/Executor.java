package pl.edu.mimuw.cloudatlas.agent;

import java.util.concurrent.LinkedBlockingQueue;

import pl.edu.mimuw.cloudatlas.agent.message.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;

/*
 * Queues messages sent to a particular module and ensures they are eventually
 * handled.
 */
public class Executor implements Runnable {
    private Module module;
    private LinkedBlockingQueue<AgentMessage> events;
    private EventBus eventBus;

    public Executor(Module module) {
        this.module = module;
        this.module.setExecutor(this);
        this.events = new LinkedBlockingQueue<AgentMessage>();
    }

    public void run() {
        System.out.println("Executor " + this.module.toString() + " running");
        while (!Thread.currentThread().interrupted()) {
            try {
                AgentMessage event = events.take();
                System.out.println("Executor " + this.module.toString() + " passed message to handle");
                module.handle(event);
            } catch (InterruptedException e) {
                System.out.println("Executor interrupted. Exiting loop.");
                break;
            }
        }
    }

    public void addMessage(AgentMessage event) throws InterruptedException {
        events.put(event);
    }

    public void passMessage(AgentMessage event) throws InterruptedException {
        eventBus.addMessage(event);
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
