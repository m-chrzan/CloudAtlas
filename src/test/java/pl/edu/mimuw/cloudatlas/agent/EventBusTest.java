package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.JUnit4;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;

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
                // TODO correct message subclass
                sendMessage(new AgentMessage("1", AgentMessage.AgentModule.UDP) {
                    @Override
                    public AgentModule getCorrectMessageType() {
                        return AgentModule.UDP;
                    }
                });
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
    @Ignore
    // TODO correct message subclass
    public void messageModule() throws InterruptedException {
        HashMap<AgentMessage.AgentModule, Module> modules = initializeModule();
        HashMap<AgentMessage.AgentModule, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);

        eventBusThread.start();
        eventBus.addMessage(new AgentMessage("0", AgentMessage.AgentModule.RMI) {
            @Override
            public AgentModule getCorrectMessageType() {
                return AgentModule.RMI;
            }
        });
        Thread.sleep(1000);
        eventBusThread.interrupt();
        Agent.closeExecutors(executorThreads);
        assertEquals(1, ((MessageCounterModule) modules.get(AgentMessage.AgentModule.RMI)).counter);
    }

    @Test
    @Ignore
    // TODO correct message subclass
    public void messagingBetweenModules() throws InterruptedException {
        HashMap<AgentMessage.AgentModule, Module> modules = initializeTwoModules();
        HashMap<AgentMessage.AgentModule, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);
        eventBusThread.start();

        eventBus.addMessage(new AgentMessage("0", AgentMessage.AgentModule.RMI) {
            @Override
            public AgentModule getCorrectMessageType() {
                return AgentModule.RMI;
            }
        });

        Thread.sleep(1000);

        eventBusThread.interrupt();
        Agent.closeExecutors(executorThreads);
        assertEquals(1, ((MessageCounterModule) modules.get(AgentMessage.AgentModule.RMI)).counter);
        assertEquals(1, ((MessageCounterModule) modules.get(AgentMessage.AgentModule.UDP)).counter);
    }

    @Test
    public void sendWrongMessageType1() throws InterruptedException {
        HashMap<AgentMessage.AgentModule, Module> modules = initializeModule();
        HashMap<AgentMessage.AgentModule, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);
        Boolean routingErrorCaught = false;
        eventBusThread.start();

        try {
            eventBus.addMessage(new TimerSchedulerMessage(
                    "0",
                    AgentMessage.AgentModule.RMI,
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
        HashMap<AgentMessage.AgentModule, Module> modules = initializeModule();
        HashMap<AgentMessage.AgentModule, Executor> executors = Agent.initializeExecutors(modules);
        ArrayList<Thread> executorThreads = Agent.initializeExecutorThreads(executors);
        EventBus eventBus = new EventBus(executors);
        Thread eventBusThread = new Thread(eventBus);
        Boolean routingErrorCaught = false;
        eventBusThread.start();

        try {
            eventBus.addMessage(new AgentMessage("0", AgentMessage.AgentModule.RMI) {
                @Override
                public AgentModule getCorrectMessageType() {
                    return AgentModule.QUERY;
                }
            });
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
}
