package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduler;

import java.util.ArrayList;
import java.util.HashMap;

public class SchedulerTest {
    private HashMap<ModuleType, Module> modules;
    private HashMap<ModuleType, Executor> executors;
    private ArrayList<Thread> executorThreads;
    private EventBus eventBus;
    private Thread eventBusThread;

    public SchedulerTest() {
        this.modules = initializeModule();
        this.executors = AgentConfig.initializeExecutors(modules);
        this.executorThreads = AgentConfig.initializeExecutorThreads(executors);
        this.eventBus = new EventBus(executors);
        this.eventBusThread = new Thread(eventBus);
        eventBusThread.start();
    }

    public HashMap<ModuleType, Module> initializeModule() {
        HashMap<ModuleType, Module> modules = new HashMap<ModuleType, Module>();
        modules.put(ModuleType.TIMER_SCHEDULER, new TimerScheduler(ModuleType.TIMER_SCHEDULER));
        return modules;
    }

    @Test
    public void initializeWrongModuleType() {
        try {
            Module timer = new TimerScheduler(ModuleType.RMI);
        } catch (AssertionError e) {
            System.out.println("Wrong timer type during init error caught");
        }
    }

    @Test
    public void scheduleTask() throws InterruptedException {
        this.eventBus.addMessage(new TimerSchedulerMessage(
                "0",
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

        Thread.sleep(300);
    }

    @Test
    public void scheduleTwoTasks() throws InterruptedException {
        this.eventBus.addMessage(new TimerSchedulerMessage(
                "0",
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


        Thread.sleep(300);
    }

    @Test
    public void scheduleTwoMessagingTasks() throws InterruptedException {
        TimerSchedulerMessage messageToSend = new TimerSchedulerMessage(
                "0",
                System.currentTimeMillis() / 1000L,
                "1",
                20,
                System.currentTimeMillis() / 1000L,
                new TimerScheduledTask() {
                    @Override
                    public void run() {
                        System.out.println("Task 2 executed");
                    }
                });

        this.eventBus.addMessage(new TimerSchedulerMessage(
                "0",
                System.currentTimeMillis() / 1000L,
                "1",
                10,
                System.currentTimeMillis() / 1000L,
                new TimerScheduledTask() {
                    @Override
                    public void run() {
                        try {
                            this.sendMessage(messageToSend);
                        } catch (InterruptedException e) {
                            System.out.println("Task 1 message interrupted");
                            e.printStackTrace();
                        }
                        System.out.println("Task 1 executed");
                    }
                }));

        Thread.sleep(300);
    }
}
