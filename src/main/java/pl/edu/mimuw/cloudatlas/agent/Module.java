package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.message.AgentMessage;

/*
 * A Module is a (potentially stateful) event handler.
 */
public abstract class Module {
    public abstract void handle(AgentMessage event);
}
