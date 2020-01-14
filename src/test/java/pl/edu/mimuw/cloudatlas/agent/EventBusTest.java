package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.JUnit4;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class EventBusTest {
    public abstract class MessageCounterModule extends Module {
        public int counter = 0;

        MessageCounterModule(ModuleType moduleType) {
            super(moduleType);
        }
    }

    /*
    public HashMap<ModuleType, Module> initializeTwoModules() {
        HashMap<ModuleType, Module> modules = new HashMap<ModuleType, Module>();
        modules.put(ModuleType.RMI, new MessageCounterModule(ModuleType.RMI) {
            @Override
            public void handle(AgentMessage event) throws InterruptedException {
                System.out.println("Module 1 handle called");
                sendMessage(new AgentMessage("1", ModuleType.UDP) {});
                counter ++;
            }
        });

        modules.put(ModuleType.UDP, new MessageCounterModule(ModuleType.UDP) {
            @Override
            public void handle(AgentMessage event) {
                System.out.println("Module 2 handle called");
                counter++;
            }
        });

        return modules;
    }

    public HashMap<ModuleType, Module> initializeModule() {
        HashMap<ModuleType, Module> modules = new HashMap<ModuleType, Module>();

        modules.put(ModuleType.RMI, new MessageCounterModule(ModuleType.RMI) {
            @Override
            public void handle(AgentMessage event) {
                System.out.println("Module 1 handle called");
                counter++;
            }
        });

        return modules;
    }

    @Test
    @Ignore
    public void messageModule() throws InterruptedException {
        HashMap<ModuleType, Module> modules = initializeModule();
        HashMap<ModuleType, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);

        eventBusThread.start();
        eventBus.addMessage(new AgentMessage("0", ModuleType.RMI) {});
        Thread.sleep(1000);
        eventBusThread.interrupt();
        Agent.closeExecutors(executorThreads);
        assertEquals(1, ((MessageCounterModule) modules.get(ModuleType.RMI)).counter);
    }

    @Test
    @Ignore
    public void messagingBetweenModules() throws InterruptedException {
        HashMap<ModuleType, Module> modules = initializeTwoModules();
        HashMap<ModuleType, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);
        eventBusThread.start();

        eventBus.addMessage(new AgentMessage("0", ModuleType.RMI) {});

        Thread.sleep(1000);

        eventBusThread.interrupt();
        Agent.closeExecutors(executorThreads);
        assertEquals(1, ((MessageCounterModule) modules.get(ModuleType.RMI)).counter);
        assertEquals(1, ((MessageCounterModule) modules.get(ModuleType.UDP)).counter);
    }

    @Test
    public void sendWrongMessageType1() throws InterruptedException {
        HashMap<ModuleType, Module> modules = initializeModule();
        HashMap<ModuleType, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);
        Boolean routingErrorCaught = false;
        eventBusThread.start();

        try {
            eventBus.addMessage(new TimerSchedulerMessage(
                    "0",
                    ModuleType.RMI,
                    System.currentTimeMillis() / 1000L,
                    "1",
                    10,
                    System.currentTimeMillis() / 1000L,
                    new TimerScheduledTask() {
                        @Override
                        public void run() {
                            System.out.println("Task executed");
                        }
                    }));
            Thread.sleep(1000);
        } catch (AssertionError e) {
            System.out.println("Wrong timer-scheduler message type error caught");
            routingErrorCaught = true;
        } finally {
            eventBusThread.interrupt();
            Agent.closeExecutors(executorThreads);
        }

        if (!routingErrorCaught) {
            Assert.fail("Routing not detected as faulty");
        }
    }

    @Test
    public void sendWrongMessageType2() throws InterruptedException {
        HashMap<ModuleType, Module> modules = initializeModule();
        HashMap<ModuleType, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);
        Boolean routingErrorCaught = false;
        eventBusThread.start();

        try {
            eventBus.addMessage(new AgentMessage("0", ModuleType.RMI) {});
            Thread.sleep(1000);
        } catch (AssertionError e) {
            System.out.println("Wrong timer-scheduler message type error caught");
            routingErrorCaught = true;
        } finally {
            eventBusThread.interrupt();
            Agent.closeExecutors(executorThreads);
        }

        if (!routingErrorCaught) {
            Assert.fail("Routing not detected as faulty");
        }
    }
    */
}
