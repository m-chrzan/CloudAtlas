package pl.edu.mimuw.cloudatlas.agent.timer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Initializes a timer within a constructor during its attachment to the executor
 * Runs in a thread separate from executor - maybe refactor so that it's attached to executor's thread
 *
 * Handle used to attach tasks to schedule
 * Tasks declared as inherited from TimerTask
 *
 * TODO: add request id and custom time
 */
public class TimerScheduler {
    private Timer timer;

    TimerScheduler() {
        this.timer = new Timer();
    }

    public void handle(TimerTask task, long delay, long period) {
        this.timer.scheduleAtFixedRate(task, delay, period);
    }

    public void handle(TimerTask task, long delay) {
        this.timer.schedule(task, delay);
    }
}
