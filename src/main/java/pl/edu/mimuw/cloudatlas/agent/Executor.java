package pl.edu.mimuw.cloudatlas.agent;

import java.util.concurrent.LinkedBlockingQueue;

/*
 * Queues messages sent to a particular module and ensures they are eventually
 * handled.
 */
public class Executor implements Runnable {
    private Module module;
    private LinkedBlockingQueue<Event> events;

    public Executor(Module module) {
        this.module = module;
        this.events = new LinkedBlockingQueue<Event>();
    }

    public void run() {
        while (!Thread.currentThread().interrupted()) {
            try {
                Event event = events.take();
                module.handle(event);
            } catch (InterruptedException e) {
                System.out.println("Executor interrupted. Exiting loop.");
                break;
            }
        }
    }

    public void addEvent(Event event) throws InterruptedException {
        events.put(event);
    }
}
