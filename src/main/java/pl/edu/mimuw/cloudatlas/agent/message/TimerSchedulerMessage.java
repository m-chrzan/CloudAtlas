package pl.edu.mimuw.cloudatlas.agent.message;

import java.util.TimerTask;

public class TimerSchedulerMessage extends AgentMessage {
    long delay;
    long baseTime;
    TimerTask task;

    public TimerSchedulerMessage(String requestId, AgentModule destinationModule, long timestamp, long delay, long baseTime, TimerTask task) {
        super(requestId, destinationModule, timestamp);
        this.delay = delay;
        this.baseTime = baseTime;
        this.task = task;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }

    public TimerTask getTask() {
        return task;
    }

    public void setTask(TimerTask task) {
        this.task = task;
    }
}
