package net.handler;

import net.IncomingAction;

public abstract class ActionHandler {
    public abstract void handle(IncomingAction action);
}