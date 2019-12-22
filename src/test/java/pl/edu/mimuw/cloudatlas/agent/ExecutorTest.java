package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExecutorTest {
    public class EventCounterModule extends Module {
        public int counter = 0;
        public void handle(Event e) {
            counter++;
        }
    }

    @Test
    public void testDoesntExecuteWhenNoEvents() throws Exception {
        EventCounterModule module = new EventCounterModule();
        Executor executor = new Executor(module);
        Thread thread = new Thread(executor);
        thread.start();
        Thread.sleep(100);
        thread.interrupt();
        assertEquals(0, module.counter);
    }

    @Test
    public void testExecutesHandlerOnce() throws Exception {
        EventCounterModule module = new EventCounterModule();
        Executor executor = new Executor(module);
        executor.addEvent(new Event() {});
        Thread thread = new Thread(executor);
        thread.start();
        Thread.sleep(100);
        thread.interrupt();
        assertEquals(1, module.counter);
    }

    @Test
    public void testExecutesHandlerMultipleTimes() throws Exception {
        EventCounterModule module = new EventCounterModule();
        Executor executor = new Executor(module);
        executor.addEvent(new Event() {});
        executor.addEvent(new Event() {});
        Thread thread = new Thread(executor);
        thread.start();
        Thread.sleep(100);
        executor.addEvent(new Event() {});
        Thread.sleep(100);
        thread.interrupt();
        assertEquals(3, module.counter);
    }
}
