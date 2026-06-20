package com.program.training.priorityeventbus;

import java.util.function.Consumer;

/**
 * Lightweight in-process event bus with priority-ordered delivery.
 *
 * <p>Components subscribe to a specific event type and supply a priority in the range
 * [1, 10].  When an event is published, all matching subscribers are invoked strictly in
 * descending priority order (10 is highest, 1 is lowest).
 *
 * @author naletov
 */
public interface PriorityEventBus
{
    /**
     * Registers a subscriber for the given event type.
     *
     * @param <T>       the event type
     * @param eventType the class token of the event type to listen for; must not be {@code null}
     * @param priority  delivery priority in the range [1, 10]; higher values are invoked first
     * @param action    the callback invoked with each published event; must not be {@code null}
     * @throws IllegalArgumentException if {@code priority} is outside [1, 10]
     */
    <T> void subscribe(Class<T> eventType, int priority, Consumer<T> action);

    /**
     * Dispatches an event to all subscribers registered for its runtime type.
     *
     * <p>Subscribers are called in descending priority order.  An exception thrown by one
     * subscriber must not prevent the remaining subscribers from being invoked.
     *
     * @param <T>   the event type
     * @param event the event to dispatch; a {@code null} value is silently ignored
     */
    <T> void publish(T event);
}
