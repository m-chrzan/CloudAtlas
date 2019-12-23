package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;

import java.util.Timer;

/**
 * Initializes a timer within a constructor during its attachment to the executor
 * Runs in a thread separate from executor - maybe refactor so that it's attached to executor's thread
 *
 * Handle used to attach tasks to schedule
 * Tasks declared as inherited from TimerTask
 *
 * TODO: add request id and custom time
 * TODO: enable messaging from scheduled tasks
 */
public class TimerScheduler extends Module {
    private Timer timer;

    public TimerScheduler(AgentMessage.AgentModule moduleType) {
        super(moduleType);
        assert moduleType == AgentMessage.AgentModule.TIMER_SCHEDULER;
        this.timer = new Timer();
        System.out.println("TimerScheduler instance initialized");
    }

    @Override
    public void handle(AgentMessage event) throws InterruptedException {
        assert event.getDestinationModule() == event.getCorrectMessageType();
        TimerSchedulerMessage timerEvent = (TimerSchedulerMessage) event;
        this.timer.schedule(timerEvent.getTask(), timerEvent.getDelay());
        System.out.println("Task with delay " + timerEvent.getDelay() + " scheduled");
    }
}
