package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;

import java.util.TimerTask;

public abstract class TimerScheduledTask extends TimerTask {
    private TimerScheduler scheduler;

    public abstract void run();

    public void setScheduler(TimerScheduler scheduler) {
        this.scheduler = scheduler;
    }

    protected void sendMessage(AgentMessage msg) throws InterruptedException {
        this.scheduler.passMessageFromTask(msg);
    }
}
