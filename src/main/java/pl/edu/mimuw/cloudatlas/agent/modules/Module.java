package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.Executor;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.GossipGirlMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerSchedulerMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.QurnikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.RemikMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ResponseMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.StanikMessage;

/*
 * A Module is a (potentially stateful) event handler.
 */
public abstract class Module {
    public class InvalidMessageType extends Exception {
        public InvalidMessageType(String message) {
            super(message);
        }
    }

    protected ModuleType moduleType;
    protected Executor executor;

    public Module(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public void handle(AgentMessage event) throws InterruptedException, InvalidMessageType {
        event.callMe(this);
    }

    public void handleTyped(TimerSchedulerMessage message) throws InterruptedException, InvalidMessageType {
        throw new InvalidMessageType("Got a TimerSchedulerMessage in module " + moduleType.toString());
    }

    public void handleTyped(GossipGirlMessage message) throws InterruptedException, InvalidMessageType {
        throw new InvalidMessageType("Got a GossipGirlMessage in module " + moduleType.toString());
    }

    public void handleTyped(QurnikMessage message) throws InterruptedException, InvalidMessageType {
        throw new InvalidMessageType("Got a QurnikMessage in module " + moduleType.toString());
    }

    public void handleTyped(RemikMessage message) throws InterruptedException, InvalidMessageType {
        throw new InvalidMessageType("Got a RemikMessage in module " + moduleType.toString());
    }

    public void handleTyped(StanikMessage message) throws InterruptedException, InvalidMessageType {
        throw new InvalidMessageType("Got a StanikMessage in module " + moduleType.toString());
    }

    public void handleTyped(ResponseMessage message) throws InterruptedException, InvalidMessageType {
        throw new InvalidMessageType("Got a ResponseMessage in module " + moduleType.toString());
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void sendMessage(AgentMessage event) throws InterruptedException {
        this.executor.passMessage(event);
    }

    @Override
    public String toString() {
        return moduleType.toString();
    }
}
