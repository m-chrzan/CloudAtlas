package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.RecursiveScheduledTask;
import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;

import java.util.function.Supplier;

public class AgentUtils {

    public static void startRecursiveTask(Supplier<TimerScheduledTask> taskSupplier, long period, EventBus eventBus) {
        TimerScheduledTask timerTask = new RecursiveScheduledTask(period, taskSupplier);
        try {
            eventBus.addMessage(new TimerSchedulerMessage("", 0, "", period, 0, timerTask));
        } catch (InterruptedException e) {
            System.out.println("Interrupted while starting queries");
        }
    }
}
