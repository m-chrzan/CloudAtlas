package pl.edu.mimuw.cloudatlas.agent;

import java.util.concurrent.LinkedBlockingQueue;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;

public class MockEventBus extends EventBus {
    public LinkedBlockingQueue<AgentMessage> events;

    public MockEventBus() {
        events = new LinkedBlockingQueue<AgentMessage>();
    }

    public void addMessage(AgentMessage msg) throws InterruptedException {
        events.put(msg);
    }
}
