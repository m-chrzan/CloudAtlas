package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.TimerScheduledTask;
import pl.edu.mimuw.cloudatlas.agent.modules.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.ModuleType;

public class TimerSchedulerMessage extends AgentMessage {
    private String requestId;
    private long delay;
    private long baseTime;
    private TimerScheduledTask task;

    public TimerSchedulerMessage(String messageId,
                                 long timestamp,
                                 String requestId,
                                 long delay,
                                 long baseTime,
                                 TimerScheduledTask task) {
        super(messageId, ModuleType.TIMER_SCHEDULER, timestamp);
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

    public TimerScheduledTask getTask() {
        return task;
    }

    public void setTask(TimerScheduledTask task) {
        this.task = task;
    }

    public String getRequestId() { return requestId; }

    public void callMe(Module module) throws InterruptedException, Module.InvalidMessageType {
        module.handleTyped(this);
    }

    public void setRequestId(String requestId) { this.requestId = requestId; }
}
