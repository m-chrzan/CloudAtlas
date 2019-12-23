package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduler;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class SchedulerTest {

    public HashMap<AgentMessage.AgentModule, Module> initializeModule() {
        HashMap<AgentMessage.AgentModule, Module> modules = new HashMap<AgentMessage.AgentModule, Module>();

        modules.put(AgentMessage.AgentModule.TIMER_SCHEDULER, new TimerScheduler(AgentMessage.AgentModule.RMI));

        return modules;
    }

    @Test
    void scheduleTask() {

    }

    @Test
    void scheduleTwoTasks() {
        HashMap<AgentMessage.AgentModule, Module> modules = ini();
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
        assertEquals(1, ((EventBusTest.MessageCounterModule) modules.get(AgentMessage.AgentModule.RMI)).counter);
    }
}
