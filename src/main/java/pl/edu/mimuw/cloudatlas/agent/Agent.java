package pl.edu.mimuw.cloudatlas.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.RMI;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduler;

public class Agent {

    public static HashMap<AgentMessage.AgentModule, Module> initializeModules() {
        HashMap<AgentMessage.AgentModule, Module> modules = new HashMap<AgentMessage.AgentModule, Module>();
        modules.put(AgentMessage.AgentModule.TIMER_SCHEDULER, new TimerScheduler(AgentMessage.AgentModule.TIMER_SCHEDULER));
        modules.put(AgentMessage.AgentModule.RMI, new RMI(AgentMessage.AgentModule.RMI));
        // TODO add modules as we implement them
        return modules;
    }

    public static HashMap<AgentMessage.AgentModule, Executor> initializeExecutors(
            HashMap<AgentMessage.AgentModule, Module> modules) {
        HashMap<AgentMessage.AgentModule, Executor> executors = new HashMap<AgentMessage.AgentModule, Executor>();
        Iterator it = modules.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<AgentMessage.AgentModule, Module> moduleEntry =
                    (Map.Entry<AgentMessage.AgentModule, Module>) it.next();
            Module module = moduleEntry.getValue();
            Executor executor = new Executor(module);
            executors.put(moduleEntry.getKey(), executor);
        }

        return executors;
    }

    public static ArrayList<Thread>  initializeExecutorThreads(HashMap<AgentMessage.AgentModule, Executor> executors) {
        ArrayList<Thread> executorThreads = new ArrayList<Thread>();
        Iterator it = executors.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<AgentMessage.AgentModule, Executor> executorEntry =
                    (Map.Entry<AgentMessage.AgentModule, Executor>) it.next();
            Thread thread = new Thread(executorEntry.getValue());
            thread.setDaemon(true);
            System.out.println("Initializing executor " + executorEntry.getKey());
            thread.start();
            executorThreads.add(thread);
        }

        return executorThreads;
    }

    public static void closeExecutors(ArrayList<Thread> executorThreads) {
        for (Thread executorThread : executorThreads) {
            executorThread.interrupt();
        }
    }

    public static void runModulesAsThreads() {
        HashMap<AgentMessage.AgentModule, Module> modules = initializeModules();
        HashMap<AgentMessage.AgentModule, Executor> executors = initializeExecutors(modules);
        ArrayList<Thread> executorThreads = initializeExecutorThreads(executors);

        Thread eventBusThread = new Thread(new EventBus(executors));
        System.out.println("Initializing event bus");
        eventBusThread.start();

        System.out.println("Closing executors");
        closeExecutors(executorThreads);
    }

    public static void main(String[] args) {
        runModulesAsThreads();
    }
}
