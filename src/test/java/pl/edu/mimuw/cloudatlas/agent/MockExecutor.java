package pl.edu.mimuw.cloudatlas.agent;

import java.util.concurrent.LinkedBlockingQueue;

import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;

/*
 * Instead of passing messages to an EventBus, this one just collects them
 * locally for inspection in unit tests.
 */
public class MockExecutor extends Executor {
    public LinkedBlockingQueue<AgentMessage> messagesToPass;

    public MockExecutor(Module module) {
        super(module);
        messagesToPass = new LinkedBlockingQueue<AgentMessage>();
    }

    @Override
    public void passMessage(AgentMessage message) throws InterruptedException {
        messagesToPass.put(message);
    }
}
