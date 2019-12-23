package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.message.AgentMessage;

/*
 * A Module is a (potentially stateful) event handler.
 */
public abstract class Module {
    private AgentMessage.AgentModule moduleType;
    private Executor executor;

    Module(AgentMessage.AgentModule moduleType) {
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
