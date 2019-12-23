package pl.edu.mimuw.cloudatlas.agent.messages;

import java.util.TimerTask;

public class TimerSchedulerMessage extends AgentMessage {
    private String requestId;
    private long delay;
    private long baseTime;
    private TimerTask task;

    public TimerSchedulerMessage(String messageId, AgentModule destinationModule, long timestamp, String requestId, long delay, long baseTime, TimerTask task) {
        super(messageId, destinationModule, timestamp);
        this.requestId = requestId;
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

    public String getRequestId() { return requestId; }

    public void setRequestId(String requestId) { this.requestId = requestId; }
}
