package pl.edu.mimuw.cloudatlas.agent;

/*
 * A Module is a (potentially stateful) event handler.
 */
public abstract class Module {
    public abstract void handle(Event event);
}
