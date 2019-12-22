package pl.edu.mimuw.cloudatlas.agent;

import java.util.concurrent.LinkedBlockingQueue;

import pl.edu.mimuw.cloudatlas.agent.message.AgentMessage;

/*
 * Queues messages sent to a particular module and ensures they are eventually
 * handled.
 */
public class Executor implements Runnable {
    private Module module;
    private LinkedBlockingQueue<AgentMessage> events;

    public Executor(Module module) {
        this.module = module;
        this.events = new LinkedBlockingQueue<AgentMessage>();
    }

    public void run() {
        while (!Thread.currentThread().interrupted()) {
            try {
                AgentMessage event = events.take();
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
}
