package com.program.training.priorityeventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Priority-based implementation of {@link PriorityEventBus}.
 *
 * <h2>Concurrency</h2>
 * <p>The subscription registry is a {@link ConcurrentHashMap} keyed by event type; each
 * subscriber list is a {@link CopyOnWriteArrayList}, making reads lock-free and concurrent
 * registrations safe.  Delivery order is decided by sorting a per-dispatch snapshot of the
 * list, so late-arriving subscriptions do not interfere with an ongoing publish.
 *
 * <h2>Error isolation</h2>
 * <p>If a subscriber throws, the exception is caught and logged; remaining subscribers
 * continue to execute normally.
 *
 * @author naletov
 * @see PriorityEventBus
 */
public class PriorityEventBusImpl implements PriorityEventBus
{
    private final Map<Class<?>, List<Subscriber<?>>> subscriptions = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PriorityEventBusImpl.class);

    /** {@inheritDoc} */
    @Override
    public <T> void subscribe(Class<T> eventType, int priority, Consumer<T> action)
    {
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("The priority must be in the range from 1 to 10");
        }

        subscriptions.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new Subscriber<>(priority, action));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates a sorted snapshot of the subscriber list at dispatch time to avoid
     * holding a lock during subscriber execution.
     */
    @Override
    public <T> void publish(T event)
    {
        if (event == null) return;

        List<Subscriber<?>> subscribers = subscriptions.get(event.getClass());
        if (subscribers == null || subscribers.isEmpty()) return;

        // Sort by priority (high to low)
        List<Subscriber<?>> sortedSubscribers = new ArrayList<>(subscribers);
        sortedSubscribers.sort(Comparator.comparingInt((Subscriber<?> s) -> s.priority()).reversed());

        for (Subscriber<?> subscriber : sortedSubscribers) {
            try {
                // Safe type casting
                Consumer<T> action = (Consumer<T>) subscriber.action();
                action.accept(event);
            } catch (Exception e) {
                // According to the condition: an error by one subscriber should not break the call of the others
                LOGGER.error("Error processing event: ", e);
            }
        }
    }

    // A record for storing subscriber metadata
    private record Subscriber<T>(int priority, Consumer<T> action) {}
}
