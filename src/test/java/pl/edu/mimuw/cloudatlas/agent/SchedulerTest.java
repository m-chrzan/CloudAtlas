package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduler;

import java.util.ArrayList;
import java.util.HashMap;

// TODO better task tests after enabling messaging from tasks
// TODO add wrong message test with switched types

public class SchedulerTest {
    private HashMap<AgentMessage.AgentModule, Module> modules;
    private HashMap<AgentMessage.AgentModule, Executor> executors;
    private ArrayList<Thread> executorThreads;
    private EventBus eventBus;
    private Thread eventBusThread;

    public SchedulerTest() {
        this.modules = initializeModule();
        this.executors = Agent.initializeExecutors(modules);
        this.executorThreads = Agent.initializeExecutorThreads(executors);
        this.eventBus = new EventBus(executors);
        this.eventBusThread = new Thread(eventBus);
        eventBusThread.start();
    }

    public HashMap<AgentMessage.AgentModule, Module> initializeModule() {
        HashMap<AgentMessage.AgentModule, Module> modules = new HashMap<AgentMessage.AgentModule, Module>();
        modules.put(AgentMessage.AgentModule.TIMER_SCHEDULER, new TimerScheduler(AgentMessage.AgentModule.TIMER_SCHEDULER));
        return modules;
    }

    @Test
    public void initializeWrongModuleType() {
        try {
            Module timer = new TimerScheduler(AgentMessage.AgentModule.RMI);
        } catch (AssertionError e) {
            System.out.println("Wrong timer type during init error caught");
        }
    }

    @Test
    public void sendWrongMessageType() throws InterruptedException {
        try {
            this.eventBus.addMessage(new TimerSchedulerMessage(
                    "0",
                    AgentMessage.AgentModule.UDP,
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
        }
    }

    @Test
    public void scheduleTask() throws InterruptedException {
        this.eventBus.addMessage(new TimerSchedulerMessage(
                "0",
                AgentMessage.AgentModule.TIMER_SCHEDULER,
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
    }

    @Test
    public void scheduleTwoTasks() throws InterruptedException {
        this.eventBus.addMessage(new TimerSchedulerMessage(
                "0",
                AgentMessage.AgentModule.TIMER_SCHEDULER,
                System.currentTimeMillis() / 1000L,
                "1",
                10,
                System.currentTimeMillis() / 1000L,
                new TimerScheduledTask() {
                    @Override
                    public void run() {
                        System.out.println("Task 1 executed");
                    }
                }));

        this.eventBus.addMessage(new TimerSchedulerMessage(
                "0",
                AgentMessage.AgentModule.TIMER_SCHEDULER,
                System.currentTimeMillis() / 1000L,
                "1",
                20,
                System.currentTimeMillis() / 1000L,
                new TimerScheduledTask() {
                    @Override
                    public void run() {
                        System.out.println("Task 2 executed");
                    }
                }));


        Thread.sleep(1000);
    }
}
