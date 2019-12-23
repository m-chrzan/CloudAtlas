package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import pl.edu.mimuw.cloudatlas.agent.message.AgentMessage;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class EventBusTest {
    public abstract class MessageCounterModule extends Module {
        public int counter = 0;

        MessageCounterModule(AgentMessage.AgentModule moduleType) {
            super(moduleType);
        }
    }

    public HashMap<AgentMessage.AgentModule, Module> initializeTwoModules() {
        HashMap<AgentMessage.AgentModule, Module> modules = new HashMap<AgentMessage.AgentModule, Module>();
        modules.put(AgentMessage.AgentModule.RMI, new MessageCounterModule(AgentMessage.AgentModule.RMI) {
            @Override
            public void handle(AgentMessage event) throws InterruptedException {
                System.out.println("Module 1 handle called");
                sendMessage(new AgentMessage("1", AgentMessage.AgentModule.UDP));
                counter ++;
            }
        });

        modules.put(AgentMessage.AgentModule.UDP, new MessageCounterModule(AgentMessage.AgentModule.UDP) {
            @Override
            public void handle(AgentMessage event) {
                System.out.println("Module 2 handle called");
                counter++;
            }
        });

        return modules;
    }

    public HashMap<AgentMessage.AgentModule, Module> initializeModule() {
        HashMap<AgentMessage.AgentModule, Module> modules = new HashMap<AgentMessage.AgentModule, Module>();

        modules.put(AgentMessage.AgentModule.RMI, new MessageCounterModule(AgentMessage.AgentModule.RMI) {
            @Override
            public void handle(AgentMessage event) {
                System.out.println("Module 1 handle called");
                counter++;
            }
        });

        return modules;
    }

    @Test
    public void messageModule() throws InterruptedException {
        HashMap<AgentMessage.AgentModule, Module> modules = initializeModule();
        HashMap<AgentMessage.AgentModule, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);

        eventBusThread.start();
        eventBus.addMessage(new AgentMessage("0", AgentMessage.AgentModule.RMI));
        Thread.sleep(1000);
        eventBusThread.interrupt();
        Agent.closeExecutors(executorThreads);
        assertEquals(1, ((MessageCounterModule) modules.get(AgentMessage.AgentModule.RMI)).counter);
    }

    @Test
    public void messagingBetweenModules() throws InterruptedException {
        HashMap<AgentMessage.AgentModule, Module> modules = initializeTwoModules();
        HashMap<AgentMessage.AgentModule, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);
        eventBusThread.start();

        eventBus.addMessage(new AgentMessage(
                "0",
                AgentMessage.AgentModule.RMI,
                System.currentTimeMillis() / 1000L));

        Thread.sleep(1000);

        eventBusThread.interrupt();
        Agent.closeExecutors(executorThreads);
        assertEquals(1, ((MessageCounterModule) modules.get(AgentMessage.AgentModule.RMI)).counter);
        assertEquals(1, ((MessageCounterModule) modules.get(AgentMessage.AgentModule.UDP)).counter);
    }
}
