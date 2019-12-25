package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import static org.junit.Assert.*;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage.AgentModule;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;

// TODO make agent messages specific subclass

public class ExecutorTest {
    public class MessageCounterModule extends Module {
        public int counter = 0;

        MessageCounterModule(AgentModule moduleType) {
            super(moduleType);
        }

        public void handle(AgentMessage m) {
            counter++;
        }
    }

    @Test
    public void testDoesntExecuteWhenNoMessages() throws Exception {
        MessageCounterModule module = new MessageCounterModule(AgentModule.UDP);
        Executor executor = new Executor(module);
        Thread thread = new Thread(executor);
        thread.start();
        Thread.sleep(100);
        thread.interrupt();
        assertEquals(0, module.counter);
    }

    @Test
    public void testExecutesHandlerOnce() throws Exception {
        MessageCounterModule module = new MessageCounterModule(AgentModule.UDP);
        Executor executor = new Executor(module);
        executor.addMessage(new AgentMessage("", AgentModule.UDP, 0) {
            @Override
            public AgentModule getCorrectMessageType() {
                return AgentModule.UDP;
            }
        });
        Thread thread = new Thread(executor);
        thread.start();
        Thread.sleep(100);
        thread.interrupt();
        assertEquals(1, module.counter);
    }

    @Test
    public void testExecutesHandlerMultipleTimes() throws Exception {
        MessageCounterModule module = new MessageCounterModule(AgentModule.UDP);
        Executor executor = new Executor(module);
        executor.addMessage(new AgentMessage("", AgentModule.UDP, 0) {
            @Override
            public AgentModule getCorrectMessageType() {
                return AgentModule.UDP;
            }
        });
        executor.addMessage(new AgentMessage("", AgentModule.UDP, 0) {
            @Override
            public AgentModule getCorrectMessageType() {
                return AgentModule.UDP;
            }
        });
        Thread thread = new Thread(executor);
        thread.start();
        Thread.sleep(100);
        executor.addMessage(new AgentMessage("", AgentModule.UDP, 0) {
            @Override
            public AgentModule getCorrectMessageType() {
                return AgentModule.UDP;
            }
        });
        Thread.sleep(100);
        thread.interrupt();
        assertEquals(3, module.counter);
    }
}