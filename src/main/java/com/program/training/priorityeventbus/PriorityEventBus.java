package com.program.training.priorityeventbus;

import java.util.function.Consumer;

/**
 * Develop a lightweight event bus.
 * System components can subscribe to a specific event type, specifying their priority (from 1 to 10).
 * When publishing an event, subscribers must be called strictly in order of their priority.
 *
 * @author naletov
 */
public interface PriorityEventBus
{
    <T> void subscribe(Class<T> eventType, int priority, Consumer<T> action);
    <T> void publish(T event);
}
