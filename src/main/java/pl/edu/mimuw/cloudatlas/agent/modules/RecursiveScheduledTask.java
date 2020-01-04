package pl.edu.mimuw.cloudatlas.agent.modules;

import java.util.function.Supplier;

import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;

public class RecursiveScheduledTask extends TimerScheduledTask {
    private long delay;
    private Supplier<TimerScheduledTask> taskSupplier;

    public RecursiveScheduledTask(long delay, Supplier<TimerScheduledTask> taskSupplier) {
        this.delay = delay;
        this.taskSupplier = taskSupplier;
    }

    public void run() {
        try {
            TimerScheduledTask task = taskSupplier.get();
            task.setScheduler(scheduler);
            task.run();
            sendMessage(new TimerSchedulerMessage("", 0, "", delay, 0, new RecursiveScheduledTask(delay, taskSupplier)));
        } catch (InterruptedException e) {
            System.out.println("Caught interrupted exception while running RecursiveScheduledTask");
        }
    }
}
