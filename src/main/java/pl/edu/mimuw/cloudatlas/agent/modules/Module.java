package pl.edu.mimuw.cloudatlas.agent.modules;

import pl.edu.mimuw.cloudatlas.agent.Executor;
import pl.edu.mimuw.cloudatlas.agent.messages.AgentMessage;

/*
 * A Module is a (potentially stateful) event handler.
 */
public abstract class Module {
    protected AgentMessage.AgentModule moduleType;
    protected Executor executor;

    public Module(AgentMessage.AgentModule moduleType) {
        this.moduleType = moduleType;
    }

    public abstract void handle(AgentMessage event) throws InterruptedException ;

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
